from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, DeclarativeBase

# SQLite database file URL
DATABASE_URL = "sqlite:///./wreck4less.db"

# Create the SQLAlchemy engine
# "check_same_thread": False is required for SQLite to work with FastAPI
engine = create_engine(
    DATABASE_URL, connect_args={"check_same_thread": False}
)

# SessionLocal is the factory for database sessions
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)

# Modern SQLAlchemy 2.0 Declarative Base
class Base(DeclarativeBase):
    pass
