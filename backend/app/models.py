from typing import Optional
from sqlmodel import Field, SQLModel
from datetime import datetime

class UserBase(SQLModel):
    username: str = Field(index=True, unique=True)
    email: str = Field(unique=True, index=True)
    name: str
    bio: Optional[str] = None
    avatar_url: Optional[str] = None

class User(UserBase, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    hashed_password: str

class UserCreate(UserBase):
    password: str

class UserRead(UserBase):
    id: int

class UserLogin(SQLModel):
    email: str
    password: str

class PostBase(SQLModel):
    description: str
    media_url: str
    media_type: str # "image" or "video"
    created_at: datetime = Field(default_factory=datetime.utcnow)

class Post(PostBase, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    user_id: int = Field(foreign_key="user.id")

class PostRead(PostBase):
    id: int
    user_id: int
    username: str  # Agregamos username para facilitar el display en Android
    user_avatar: Optional[str] = None

class Token(SQLModel):
    access_token: str
    token_type: str
    user_id: int
