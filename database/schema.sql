-- Wreck4Less Database Blueprint
-- Place in /database/schema.sql

-- 1. Create User Roles
CREATE TYPE user_role AS ENUM ('customer', 'manager', 'driver', 'admin');

-- 2. Core Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    full_name TEXT NOT NULL,
    role user_role NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Wreck Dispatch Requests
CREATE TABLE dispatch_requests (
    id VARCHAR(12) PRIMARY KEY, -- Format: WRK-XXXXXX
    customer_id UUID REFERENCES users(id),
    vehicle_make TEXT NOT NULL,
    vehicle_model TEXT NOT NULL,
    vehicle_year VARCHAR(4) NOT NULL,
    damage_report TEXT,
    location_label TEXT,
    status TEXT DEFAULT 'AWAITING_OPS',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Evidence Imagery
CREATE TABLE request_evidence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dispatch_id VARCHAR(12) REFERENCES dispatch_requests(id) ON DELETE CASCADE,
    image_url TEXT NOT NULL, -- S3 or Supabase Storage Link
    uploaded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Operational Logs (The Link between Manager & Driver)
CREATE TABLE operational_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dispatch_id VARCHAR(12) REFERENCES dispatch_requests(id),
    manager_id UUID REFERENCES users(id),
    driver_id UUID REFERENCES users(id),
    approved_rate DECIMAL(10, 2),
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. Payments
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dispatch_id VARCHAR(12) REFERENCES dispatch_requests(id),
    amount DECIMAL(10, 2) NOT NULL,
    protocol TEXT NOT NULL CHECK (protocol IN ('card', 'bank_transfer', 'cash')),
    status TEXT DEFAULT 'PENDING'
);