import os
import uuid
from datetime import datetime, timedelta
from typing import List, Optional
from fastapi import FastAPI, HTTPException, Depends, status
from pydantic import BaseModel
from dotenv import load_dotenv

# Load variables from backend/.env
load_dotenv()

app = FastAPI(
    title="Wreck4Less Private Dispatch Engine",
    description="High-saturation Red & Black Fleet Management API"
)

# --- DATA MODELS ---

class UserProfile(BaseModel):
    username: str
    role: str # options: customer, manager, driver, admin

class WreckIntel(BaseModel):
    make: str
    model: str
    year: str
    damage_description: str
    image_keys: List[str] # References to storage (max 4)
    location_label: str

class WreckSubmission(BaseModel):
    user_id: str
    intel: WreckIntel

class ManagerConfirm(BaseModel):
    job_id: str
    assigned_driver_id: str
    approved_rate: float

class PaymentFinalize(BaseModel):
    job_id: str
    method: str # 'card', 'bank_transfer', 'cash'
    amount: float

# --- VOLATILE MEMORY STORAGE (Replace with PostgreSQL/SQLAlchemy) ---
DISPATCH_REGISTRY = {}

# --- ENDPOINTS ---

@app.post("/api/v1/auth/access", status_code=200)
async def request_access(user: UserProfile):
    """Initial portal login logic."""
    return {"message": f"Access granted for {user.username}", "role": user.role}

@app.post("/api/v1/wreck/submit")
async def submit_wreck_intel(submission: WreckSubmission):
    """
    CUSTOMER ROLE:
    Submit vehicle intel and damage imagery.
    Triggers the 5-minute countdown on the mobile client.
    """
    job_id = f"WRK-{str(uuid.uuid4())[:6].upper()}"
    DISPATCH_REGISTRY[job_id] = {
        "status": "AWAITING_OPS",
        "user_id": submission.user_id,
        "intel": submission.intel.dict(),
        "created_at": datetime.now().isoformat(),
        "confirmed_rate": 0.0,
        "driver_id": None
    }
    return {
        "job_id": job_id,
        "status": "PENDING_OPS",
        "ops_timer": "05:00",
        "support_line": "1-800-WRECK-HQ"
    }

@app.post("/api/v1/manager/dispatch")
async def manager_assign_driver(action: ManagerConfirm):
    """
    MANAGER ROLE:
    Assigns driver and approves the price after reviewing intel.
    """
    if action.job_id not in DISPATCH_REGISTRY:
        raise HTTPException(status_code=404, detail="Job record not found")

    job = DISPATCH_REGISTRY[action.job_id]
    job["status"] = "RATE_APPROVED"
    job["confirmed_rate"] = action.approved_rate
    job["driver_id"] = action.assigned_driver_id

    return {"status": "SUCCESS", "client_notified": True}

@app.post("/api/v1/payment/finalize")
async def finalize_payment(payment: PaymentFinalize):
    """
    CUSTOMER ROLE:
    Confirm the approved rate via Card, Bank Transfer, or Cash.
    """
    if payment.job_id not in DISPATCH_REGISTRY:
        raise HTTPException(status_code=404, detail="Invalid job ID")

    job = DISPATCH_REGISTRY[payment.job_id]
    job["status"] = "TRACKING_ACTIVE"
    job["payment_method"] = payment.method

    return {
        "status": "DISPATCHED",
        "tracking_active": True,
        "comm_options": ["Call Driver", "Message Driver"]
    }

@app.get("/api/v1/admin/fleet/status")
async def admin_audit():
    """
    ADMIN ROLE:
    Global system overview for financial and fleet audit.
    """
    return {
        "active_dispatches": len(DISPATCH_REGISTRY),
        "registry": DISPATCH_REGISTRY
    }

if __name__ == "__main__":
    import uvicorn
    # This runs the server on http://localhost:8000
    uvicorn.run(app, host=os.getenv("HOST", "0.0.0.0"), port=int(os.getenv("PORT", 8000)))