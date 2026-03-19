import uuid

import psycopg2
import psycopg2.extras

from main import _hash_password, _init_db, DB_DSN

USERS = [
    {
        "email": "admin@wreck4less.test",
        "password": "Admin#12345",
        "full_name": "Wreck4Less Admin",
        "role": "admin",
    },
    {
        "email": "customer@wreck4less.test",
        "password": "Customer#12345",
        "full_name": "Wreck4Less Customer",
        "role": "customer",
    },
    {
        "email": "driver@wreck4less.test",
        "password": "Driver#12345",
        "full_name": "Wreck4Less Driver",
        "role": "driver",
    },
]


def seed_users() -> None:
    _init_db()
    with psycopg2.connect(DB_DSN) as conn:
        with conn.cursor(cursor_factory=psycopg2.extras.RealDictCursor) as cur:
            for user in USERS:
                cur.execute(
                    "SELECT id FROM users WHERE email = %s",
                    (user["email"],),
                )
                existing = cur.fetchone()
                if existing is not None:
                    user["id"] = existing["id"]
                    continue
                user_id = str(uuid.uuid4())
                cur.execute(
                    "INSERT INTO users (id, email, password_hash, full_name, role, created_at) VALUES (%s, %s, %s, %s, %s, NOW())",
                    (
                        user_id,
                        user["email"],
                        _hash_password(user["password"]),
                        user["full_name"],
                        user["role"],
                    ),
                )
                user["id"] = user_id
        conn.commit()


if __name__ == "__main__":
    seed_users()
    print("Seeded users (if missing):")
    for user in USERS:
        print(f"- {user['role']}: {user['email']} / {user['password']} (id: {user.get('id', 'unknown')})")
