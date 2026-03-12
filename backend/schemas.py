from pydantic import BaseModel

class TowRequest(BaseModel):
    customer_name: str
    location: str

class TowResponse(BaseModel):
    id: int
    customer_name: str
    location: str
    status: str

    class Config:
        orm_mode = True
