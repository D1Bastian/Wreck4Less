from sqlalchemy import Column, Integer, String
from database import Base

class TowJob(Base):
    __tablename__ = "tow_jobs"

    id = Column(Integer, primary_key=True, index=True)
    customer_name = Column(String, index=True)
    location = Column(String)
    status = Column(String, default="Pending")
