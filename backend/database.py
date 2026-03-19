from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, DeclarativeBase
import os

# Postgres database URL (pgAdmin-managed)
DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "postgresql://postgres:postgres@localhost:5432/wreck4less",
)

# Create the SQLAlchemy engine
# "check_same_thread": False is required for SQLite to work with FastAPI
engine = create_engine(DATABASE_URL)

# SessionLocal is the factory for database sessions
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)

# Modern SQLAlchemy 2.0 Declarative Base
class Base(DeclarativeBase):
    pass
