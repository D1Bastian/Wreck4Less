import base64
import hashlib
import hmac
import os
import secrets
import psycopg2
import psycopg2.extras
import uuid
import csv
import io
from datetime import datetime, timedelta, timezone
from typing import Dict, List, Optional

import jwt
from dotenv import load_dotenv
from fastapi import Depends, FastAPI, Header, HTTPException, Request, status
from pydantic import BaseModel, Field, validator
import re

load_dotenv()

app = FastAPI(
    title="Wreck4Less Private Dispatch Engine",
    description="High-saturation Red & Black Fleet Management API",
)

DB_DSN = os.getenv(
    "DATABASE_URL",
    "postgresql://postgres:postgres@localhost:5432/wreck4less",
)
JWT_SECRET = os.getenv("JWT_SECRET", "dev-change-me")
JWT_ISSUER = os.getenv("JWT_ISSUER", "wreck4less")
JWT_AUDIENCE = os.getenv("JWT_AUDIENCE", "wreck4less-mobile")
JWT_EXPIRES_MIN = int(os.getenv("JWT_EXPIRES_MIN", "120"))
ADMIN_BOOTSTRAP_SECRET = os.getenv("ADMIN_BOOTSTRAP_SECRET", "")
REQUIRE_HTTPS = os.getenv("REQUIRE_HTTPS", "false").lower() == "true"


def _get_conn() -> psycopg2.extensions.connection:
    return psycopg2.connect(DB_DSN)


def _init_db() -> None:
    schema = """
    CREATE TABLE IF NOT EXISTS users (
        id UUID PRIMARY KEY,
        email TEXT UNIQUE NOT NULL,
        password_hash TEXT NOT NULL,
        full_name TEXT NOT NULL,
        role TEXT NOT NULL CHECK (role IN ('customer', 'manager', 'driver', 'admin')),
        created_at TIMESTAMPTZ NOT NULL
    );

    CREATE TABLE IF NOT EXISTS dispatch_requests (
        id TEXT PRIMARY KEY,
        customer_id UUID NOT NULL REFERENCES users(id),
        vehicle_make TEXT NOT NULL,
        vehicle_model TEXT NOT NULL,
        vehicle_year TEXT NOT NULL,
        damage_report TEXT,
        location_label TEXT,
        location_lat DOUBLE PRECISION,
        location_lng DOUBLE PRECISION,
        status TEXT NOT NULL,
        created_at TIMESTAMPTZ NOT NULL,
        approved_rate NUMERIC(10, 2) DEFAULT 0.0,
        driver_id UUID REFERENCES users(id),
        driver_lat DOUBLE PRECISION,
        driver_lng DOUBLE PRECISION,
        payment_method TEXT,
        cancel_reason TEXT
    );

    CREATE INDEX IF NOT EXISTS idx_dispatch_status ON dispatch_requests(status);
    CREATE INDEX IF NOT EXISTS idx_dispatch_driver ON dispatch_requests(driver_id);
    ALTER TABLE dispatch_requests ADD COLUMN IF NOT EXISTS cancel_reason TEXT;
    """
    with _get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(schema)
        conn.commit()


@app.on_event("startup")
def _startup() -> None:
    _init_db()


def _hash_password(password: str) -> str:
    salt = secrets.token_bytes(16)
    iterations = 210_000
    digest = hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), salt, iterations)
    return "pbkdf2_sha256$%d$%s$%s" % (
        iterations,
        base64.b64encode(salt).decode("utf-8"),
        base64.b64encode(digest).decode("utf-8"),
    )


def _verify_password(password: str, stored_hash: str) -> bool:
    try:
        scheme, iterations_s, salt_b64, digest_b64 = stored_hash.split("$")
        if scheme != "pbkdf2_sha256":
            return False
        iterations = int(iterations_s)
        salt = base64.b64decode(salt_b64.encode("utf-8"))
        expected = base64.b64decode(digest_b64.encode("utf-8"))
        actual = hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), salt, iterations)
        return hmac.compare_digest(expected, actual)
    except Exception:
        return False


def _issue_token(user_id: str, email: str, role: str) -> str:
    now = datetime.now(timezone.utc)
    payload = {
        "iss": JWT_ISSUER,
        "aud": JWT_AUDIENCE,
        "sub": user_id,
        "email": email,
        "role": role,
        "iat": int(now.timestamp()),
        "exp": int((now + timedelta(minutes=JWT_EXPIRES_MIN)).timestamp()),
    }
    return jwt.encode(payload, JWT_SECRET, algorithm="HS256")


def _parse_token(token: str) -> Dict:
    return jwt.decode(
        token,
        JWT_SECRET,
        algorithms=["HS256"],
        issuer=JWT_ISSUER,
        audience=JWT_AUDIENCE,
    )


async def _enforce_https(request: Request) -> None:
    if not REQUIRE_HTTPS:
        return
    proto = request.headers.get("x-forwarded-proto", request.url.scheme)
    if proto != "https":
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="HTTPS required")


async def get_current_user(
    request: Request,
    authorization: str = Header(default=""),
    _: None = Depends(_enforce_https),
) -> Dict:
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing token")
    token = authorization.split(" ", 1)[1].strip()
    try:
        payload = _parse_token(token)
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Token expired")
    except jwt.InvalidTokenError:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token")

    user_id = payload.get("sub")
    with _get_conn() as conn:
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            cur.execute(
                "SELECT id, email, full_name, role FROM users WHERE id = %s",
                (user_id,),
            )
            row = cur.fetchone()
    if row is None:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="User not found")
    return dict(row)


def require_roles(*roles: str):
    async def _role_guard(user: Dict = Depends(get_current_user)) -> Dict:
        if user["role"] not in roles:
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Forbidden")
        return user

    return _role_guard


class RegisterRequest(BaseModel):
    email: str
    password: str = Field(min_length=8, max_length=128)
    full_name: str = Field(min_length=2, max_length=100)
    role: Optional[str] = None
    bootstrap_token: Optional[str] = None

    @validator("email")
    def validate_email(cls, value: str) -> str:
        value = value.strip()
        if not _is_valid_email(value):
            raise ValueError("Email must be valid")
        return value.lower()


class LoginRequest(BaseModel):
    email: str
    password: str = Field(min_length=8, max_length=128)

    @validator("email")
    def validate_email(cls, value: str) -> str:
        value = value.strip()
        if not _is_valid_email(value):
            raise ValueError("Email must be valid")
        return value.lower()


class AuthResponse(BaseModel):
    token: str
    role: str
    user_id: str
    full_name: str
    expires_in_minutes: int


class WreckIntel(BaseModel):
    make: str = Field(min_length=1, max_length=60)
    model: str = Field(min_length=1, max_length=60)
    year: str = Field(min_length=4, max_length=4)
    damage_description: str = Field(min_length=1, max_length=500)
    image_keys: List[str] = Field(default_factory=list, max_length=4)
    location_label: str = Field(min_length=1, max_length=120)
    location_lat: Optional[float] = None
    location_lng: Optional[float] = None

    @validator("year")
    def validate_year(cls, value: str) -> str:
        if not value.isdigit():
            raise ValueError("Year must be numeric")
        return value


def _is_valid_email(value: str) -> bool:
    return re.match(r"^[^@\s]+@[^@\s]+\.[^@\s]+$", value) is not None

    @validator("location_lat")
    def validate_lat(cls, value: Optional[float]) -> Optional[float]:
        if value is not None and not (-90.0 <= value <= 90.0):
            raise ValueError("Latitude out of range")
        return value

    @validator("location_lng")
    def validate_lng(cls, value: Optional[float]) -> Optional[float]:
        if value is not None and not (-180.0 <= value <= 180.0):
            raise ValueError("Longitude out of range")
        return value


class WreckSubmission(BaseModel):
    intel: WreckIntel


class WreckResponse(BaseModel):
    job_id: str
    status: str
    ops_timer: str
    support_line: str


class ManagerConfirm(BaseModel):
    job_id: str
    assigned_driver_id: str
    approved_rate: float

    @validator("approved_rate")
    def validate_rate(cls, value: float) -> float:
        if value < 0:
            raise ValueError("Rate must be non-negative")
        return value


class PaymentFinalize(BaseModel):
    job_id: str
    method: str
    amount: float

    @validator("method")
    def validate_method(cls, value: str) -> str:
        allowed = {"card", "bank_transfer", "cash"}
        if value not in allowed:
            raise ValueError("Unsupported payment method")
        return value

    @validator("amount")
    def validate_amount(cls, value: float) -> float:
        if value <= 0:
            raise ValueError("Amount must be positive")
        return value


class DriverUpdate(BaseModel):
    job_id: str
    status: str
    lat: Optional[float] = None
    lng: Optional[float] = None

    @validator("status")
    def validate_status(cls, value: str) -> str:
        allowed = {"EN_ROUTE", "ARRIVED", "COMPLETE"}
        if value not in allowed:
            raise ValueError("Invalid status")
        return value


class CustomerProfile(BaseModel):
    user_id: str
    email: str
    full_name: str
    role: str


class DriverRosterEntry(BaseModel):
    id: str
    email: str
    full_name: str
    online: bool
    active_job_id: Optional[str]


class ReassignDriverRequest(BaseModel):
    driver_id: str


class UpdateRateRequest(BaseModel):
    approved_rate: float

    @validator("approved_rate")
    def validate_rate(cls, value: float) -> float:
        if value < 0:
            raise ValueError("Rate must be non-negative")
        return value


class CancelJobRequest(BaseModel):
    reason: Optional[str] = None


def _job_payload(row: Dict) -> Dict:
    driver_location = None
    if row["driver_lat"] is not None and row["driver_lng"] is not None:
        driver_location = {"lat": row["driver_lat"], "lng": row["driver_lng"]}
    created_at = row.get("created_at")
    if hasattr(created_at, "isoformat"):
        created_at = created_at.isoformat()
    return {
        "job_id": row["id"],
        "status": row["status"],
        "user_id": str(row["customer_id"]),
        "intel": {
            "make": row["vehicle_make"],
            "model": row["vehicle_model"],
            "year": row["vehicle_year"],
            "damage_description": row["damage_report"],
            "image_keys": [],
            "location_label": row["location_label"],
            "location_lat": row["location_lat"],
            "location_lng": row["location_lng"],
        },
        "created_at": created_at,
        "confirmed_rate": row["approved_rate"] or 0.0,
        "driver_id": str(row["driver_id"]) if row["driver_id"] is not None else None,
        "driver_location": driver_location,
        "cancel_reason": row.get("cancel_reason"),
    }


def _fetch_job(job_id: str) -> Dict:
    with _get_conn() as conn:
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            cur.execute(
                "SELECT * FROM dispatch_requests WHERE id = %s",
                (job_id,),
            )
            row = cur.fetchone()
    if row is None:
        raise HTTPException(status_code=404, detail="Job record not found")
    return row


def _authorize_job_access(user: Dict, job_row: Dict) -> None:
    if user["role"] in {"admin", "manager"}:
        return
    if user["role"] == "driver" and job_row["driver_id"] == user["id"]:
        return
    if user["role"] == "customer" and job_row["customer_id"] == user["id"]:
        return
    raise HTTPException(status_code=403, detail="Forbidden")


@app.post("/api/v1/auth/register", response_model=AuthResponse, status_code=201)
async def register(payload: RegisterRequest):
    role = (payload.role or "customer").lower()
    if role not in {"customer", "manager", "driver", "admin"}:
        raise HTTPException(status_code=400, detail="Invalid role")
    if role != "customer":
        if not ADMIN_BOOTSTRAP_SECRET or payload.bootstrap_token != ADMIN_BOOTSTRAP_SECRET:
            raise HTTPException(status_code=403, detail="Admin bootstrap required for elevated roles")

    user_id = str(uuid.uuid4())
    password_hash = _hash_password(payload.password)
    created_at = datetime.now(timezone.utc).isoformat()

    try:
        with _get_conn() as conn:
            with conn.cursor() as cur:
                cur.execute(
                    "INSERT INTO users (id, email, password_hash, full_name, role, created_at) VALUES (%s, %s, %s, %s, %s, %s)",
                    (user_id, payload.email.lower(), password_hash, payload.full_name, role, created_at),
                )
            conn.commit()
    except psycopg2.IntegrityError:
        raise HTTPException(status_code=409, detail="Email already registered")

    token = _issue_token(user_id, payload.email.lower(), role)
    return AuthResponse(
        token=token,
        role=role,
        user_id=user_id,
        full_name=payload.full_name,
        expires_in_minutes=JWT_EXPIRES_MIN,
    )


@app.post("/api/v1/auth/login", response_model=AuthResponse, status_code=200)
async def login(payload: LoginRequest):
    with _get_conn() as conn:
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            cur.execute(
                "SELECT id, email, password_hash, full_name, role FROM users WHERE email = %s",
                (payload.email.lower(),),
            )
            row = cur.fetchone()
    if row is None or not _verify_password(payload.password, row["password_hash"]):
        raise HTTPException(status_code=401, detail="Invalid credentials")

    user_id = str(row["id"])
    token = _issue_token(user_id, row["email"], row["role"])
    return AuthResponse(
        token=token,
        role=row["role"],
        user_id=user_id,
        full_name=row["full_name"],
        expires_in_minutes=JWT_EXPIRES_MIN,
    )


@app.post("/api/v1/wreck/submit", response_model=WreckResponse)
async def submit_wreck_intel(
    submission: WreckSubmission,
    user: Dict = Depends(require_roles("customer")),
):
    job_id = f"WRK-{str(uuid.uuid4())[:6].upper()}"
    created_at = datetime.now(timezone.utc).isoformat()
    intel = submission.intel

    with _get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(
                """
                INSERT INTO dispatch_requests (
                    id, customer_id, vehicle_make, vehicle_model, vehicle_year,
                    damage_report, location_label, location_lat, location_lng,
                    status, created_at
                ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                """,
                (
                    job_id,
                    user["id"],
                    intel.make,
                    intel.model,
                    intel.year,
                    intel.damage_description,
                    intel.location_label,
                    intel.location_lat,
                    intel.location_lng,
                    "AWAITING_OPS",
                    created_at,
                ),
            )
        conn.commit()

    return WreckResponse(
        job_id=job_id,
        status="PENDING_OPS",
        ops_timer="05:00",
        support_line="1-800-WRECK-HQ",
    )


@app.get("/api/v1/jobs/{job_id}")
async def job_status(
    job_id: str,
    user: Dict = Depends(get_current_user),
):
    row = _fetch_job(job_id)
    _authorize_job_access(user, row)
    return _job_payload(row)


@app.get("/api/v1/manager/queue")
async def manager_queue(user: Dict = Depends(require_roles("manager"))):
    with _get_conn() as conn:
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            cur.execute(
                "SELECT * FROM dispatch_requests WHERE status = %s ORDER BY created_at DESC",
                ("AWAITING_OPS",),
            )
            rows = cur.fetchall()
    return {"jobs": [_job_payload(row) for row in rows]}


@app.post("/api/v1/manager/dispatch")
async def manager_assign_driver(
    action: ManagerConfirm,
    user: Dict = Depends(require_roles("manager")),
):
    updated_rowcount = 0
    with _get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT id FROM users WHERE id = %s AND role = %s",
                (action.assigned_driver_id, "driver"),
            )
            driver = cur.fetchone()
            if driver is None:
                raise HTTPException(status_code=404, detail="Driver not found")
            cur.execute(
                """
                UPDATE dispatch_requests
                SET status = %s, approved_rate = %s, driver_id = %s, driver_lat = NULL, driver_lng = NULL
                WHERE id = %s
                """,
                ("RATE_APPROVED", action.approved_rate, action.assigned_driver_id, action.job_id),
            )
            updated_rowcount = cur.rowcount
        conn.commit()

    if updated_rowcount == 0:
        raise HTTPException(status_code=404, detail="Job record not found")
    return {"status": "SUCCESS", "client_notified": True}


@app.post("/api/v1/payment/finalize")
async def finalize_payment(
    payment: PaymentFinalize,
    user: Dict = Depends(require_roles("customer")),
):
    row = _fetch_job(payment.job_id)
    _authorize_job_access(user, row)

    with _get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(
                "UPDATE dispatch_requests SET status = %s, payment_method = %s WHERE id = %s",
                ("TRACKING_ACTIVE", payment.method, payment.job_id),
            )
        conn.commit()

    return {
        "status": "DISPATCHED",
        "tracking_active": True,
        "comm_options": ["Call Driver", "Message Driver"],
    }


@app.get("/api/v1/driver/assignment/{driver_id}")
async def driver_assignment(
    driver_id: str,
    user: Dict = Depends(require_roles("driver")),
):
    if driver_id != str(user["id"]):
        raise HTTPException(status_code=403, detail="Forbidden")
    with _get_conn() as conn:
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            cur.execute(
                """
                SELECT * FROM dispatch_requests
                WHERE driver_id = %s AND status IN ('RATE_APPROVED', 'TRACKING_ACTIVE', 'EN_ROUTE', 'ARRIVED')
                ORDER BY created_at DESC LIMIT 1
                """,
                (user["id"],),
            )
            row = cur.fetchone()
    if row is None:
        return {"job": None}
    return {"job": _job_payload(row)}


@app.post("/api/v1/driver/update")
async def driver_update(
    update: DriverUpdate,
    user: Dict = Depends(require_roles("driver")),
):
    with _get_conn() as conn:
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            cur.execute(
                "SELECT id, driver_id FROM dispatch_requests WHERE id = %s",
                (update.job_id,),
            )
            row = cur.fetchone()
            if row is None:
                raise HTTPException(status_code=404, detail="Job record not found")
            if row["driver_id"] != user["id"]:
                raise HTTPException(status_code=403, detail="Driver not assigned to job")

            cur.execute(
                """
                UPDATE dispatch_requests
                SET status = %s, driver_lat = %s, driver_lng = %s
                WHERE id = %s
                """,
                (update.status, update.lat, update.lng, update.job_id),
            )
        conn.commit()
    return {"status": "UPDATED"}


@app.get("/api/v1/admin/fleet/status")
async def admin_audit(user: Dict = Depends(require_roles("admin"))):
    with _get_conn() as conn:
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            cur.execute("SELECT * FROM dispatch_requests ORDER BY created_at DESC")
            rows = cur.fetchall()
    return {
        "active_dispatches": len(rows),
        "registry": {row["id"]: _job_payload(row) for row in rows},
    }


@app.get("/api/v1/admin/drivers")
async def admin_drivers(user: Dict = Depends(require_roles("admin"))):
    with _get_conn() as conn:
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            cur.execute(
                "SELECT id, email, full_name FROM users WHERE role = %s ORDER BY full_name",
                ("driver",),
            )
            drivers = cur.fetchall()
            cur.execute(
                "SELECT driver_id, id FROM dispatch_requests WHERE status IN ('RATE_APPROVED','TRACKING_ACTIVE','EN_ROUTE','ARRIVED')"
            )
            active_jobs = {row["driver_id"]: row["id"] for row in cur.fetchall() if row["driver_id"] is not None}
    roster = []
    for driver in drivers:
        driver_id = str(driver["id"])
        active_job_id = active_jobs.get(driver["id"])
        roster.append(
            {
                "id": driver_id,
                "email": driver["email"],
                "full_name": driver["full_name"],
                "online": active_job_id is not None,
                "active_job_id": active_job_id,
            }
        )
    return {"drivers": roster}


@app.post("/api/v1/admin/jobs/{job_id}/reassign")
async def admin_reassign_job(
    job_id: str,
    payload: ReassignDriverRequest,
    user: Dict = Depends(require_roles("admin")),
):
    with _get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(
                "SELECT id FROM users WHERE id = %s AND role = %s",
                (payload.driver_id, "driver"),
            )
            driver = cur.fetchone()
            if driver is None:
                raise HTTPException(status_code=404, detail="Driver not found")
            cur.execute(
                "UPDATE dispatch_requests SET driver_id = %s WHERE id = %s",
                (payload.driver_id, job_id),
            )
            if cur.rowcount == 0:
                raise HTTPException(status_code=404, detail="Job record not found")
        conn.commit()
    return {"status": "REASSIGNED"}


@app.post("/api/v1/admin/jobs/{job_id}/rate")
async def admin_update_rate(
    job_id: str,
    payload: UpdateRateRequest,
    user: Dict = Depends(require_roles("admin")),
):
    with _get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(
                "UPDATE dispatch_requests SET approved_rate = %s WHERE id = %s",
                (payload.approved_rate, job_id),
            )
            if cur.rowcount == 0:
                raise HTTPException(status_code=404, detail="Job record not found")
        conn.commit()
    return {"status": "RATE_UPDATED"}


@app.post("/api/v1/admin/jobs/{job_id}/cancel")
async def admin_cancel_job(
    job_id: str,
    payload: CancelJobRequest,
    user: Dict = Depends(require_roles("admin")),
):
    with _get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(
                "UPDATE dispatch_requests SET status = %s, cancel_reason = %s WHERE id = %s",
                ("CANCELLED", payload.reason, job_id),
            )
            if cur.rowcount == 0:
                raise HTTPException(status_code=404, detail="Job record not found")
        conn.commit()
    return {"status": "CANCELLED"}


@app.get("/api/v1/admin/export")
async def admin_export(user: Dict = Depends(require_roles("admin"))):
    with _get_conn() as conn:
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            cur.execute(
                """
                SELECT dr.id AS job_id,
                       dr.status,
                       dr.created_at,
                       dr.approved_rate,
                       dr.payment_method,
                       dr.location_label,
                       dr.vehicle_make,
                       dr.vehicle_model,
                       dr.vehicle_year,
                       dr.cancel_reason,
                       cu.email AS customer_email,
                       cu.id AS customer_id,
                       du.email AS driver_email,
                       du.id AS driver_id
                FROM dispatch_requests dr
                LEFT JOIN users cu ON cu.id = dr.customer_id
                LEFT JOIN users du ON du.id = dr.driver_id
                ORDER BY dr.created_at DESC
                """
            )
            rows = cur.fetchall()
    output = io.StringIO()
    writer = csv.writer(output)
    writer.writerow(
        [
            "job_id",
            "status",
            "created_at",
            "approved_rate",
            "payment_method",
            "location_label",
            "vehicle_make",
            "vehicle_model",
            "vehicle_year",
            "cancel_reason",
            "customer_id",
            "customer_email",
            "driver_id",
            "driver_email",
        ]
    )
    for row in rows:
        created_at = row["created_at"].isoformat() if row.get("created_at") else ""
        writer.writerow(
            [
                row.get("job_id"),
                row.get("status"),
                created_at,
                row.get("approved_rate"),
                row.get("payment_method"),
                row.get("location_label"),
                row.get("vehicle_make"),
                row.get("vehicle_model"),
                row.get("vehicle_year"),
                row.get("cancel_reason"),
                row.get("customer_id"),
                row.get("customer_email"),
                row.get("driver_id"),
                row.get("driver_email"),
            ]
        )
    return {"csv": output.getvalue()}


@app.get("/api/v1/customer/profile", response_model=CustomerProfile)
async def customer_profile(user: Dict = Depends(require_roles("customer"))):
    return CustomerProfile(
        user_id=str(user["id"]),
        email=user["email"],
        full_name=user["full_name"],
        role=user["role"],
    )


@app.get("/api/v1/customer/history")
async def customer_history(user: Dict = Depends(require_roles("customer"))):
    with _get_conn() as conn:
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            cur.execute(
                "SELECT * FROM dispatch_requests WHERE customer_id = %s ORDER BY created_at DESC",
                (user["id"],),
            )
            rows = cur.fetchall()
    return {"jobs": [_job_payload(row) for row in rows]}


if __name__ == "__main__":
    import uvicorn

    ssl_keyfile = os.getenv("SSL_KEYFILE")
    ssl_certfile = os.getenv("SSL_CERTFILE")
    uvicorn.run(
        app,
        host=os.getenv("HOST", "0.0.0.0"),
        port=int(os.getenv("PORT", 8000)),
        ssl_keyfile=ssl_keyfile if ssl_keyfile else None,
        ssl_certfile=ssl_certfile if ssl_certfile else None,
    )
