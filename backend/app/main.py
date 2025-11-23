from fastapi import FastAPI, Depends, HTTPException, status, UploadFile, File, Form
from fastapi.staticfiles import StaticFiles
from sqlmodel import Session, select
from typing import List
import shutil
import os
import uuid
from app.database import create_db_and_tables, get_session
from app.models import User, UserCreate, UserRead, UserLogin, Post, PostRead, Token
from app.auth import get_password_hash, verify_password, create_access_token, get_current_user
from datetime import timedelta

app = FastAPI()

# Crear carpeta uploads si no existe
os.makedirs("uploads", exist_ok=True)
app.mount("/uploads", StaticFiles(directory="uploads"), name="uploads")

@app.on_event("startup")
def on_startup():
    create_db_and_tables()

# --- AUTH ---

@app.post("/auth/register", response_model=Token)
def register(user: UserCreate, session: Session = Depends(get_session)):
    # Check email/username
    if session.exec(select(User).where(User.email == user.email)).first():
        raise HTTPException(status_code=400, detail="Email already registered")
    if session.exec(select(User).where(User.username == user.username)).first():
        raise HTTPException(status_code=400, detail="Username already taken")

    hashed_pw = get_password_hash(user.password)
    db_user = User.from_orm(user)
    db_user.hashed_password = hashed_pw
    session.add(db_user)
    session.commit()
    session.refresh(db_user)
    
    access_token = create_access_token(data={"sub": db_user.email})
    return {"access_token": access_token, "token_type": "bearer", "user_id": db_user.id}

@app.post("/auth/login", response_model=Token)
def login(user_data: UserLogin, session: Session = Depends(get_session)):
    user = session.exec(select(User).where(User.email == user_data.email)).first()
    if not user or not verify_password(user_data.password, user.hashed_password):
        raise HTTPException(status_code=400, detail="Incorrect email or password")
    
    access_token = create_access_token(data={"sub": user.email})
    return {"access_token": access_token, "token_type": "bearer", "user_id": user.id}

# --- USERS ---

@app.get("/users/me", response_model=UserRead)
def read_users_me(current_user: User = Depends(get_current_user)):
    return current_user

@app.put("/users/me/avatar", response_model=UserRead)
async def update_avatar(file: UploadFile = File(...), current_user: User = Depends(get_current_user), session: Session = Depends(get_session)):
    filename = f"{uuid.uuid4()}_{file.filename}"
    path = f"uploads/{filename}"
    with open(path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
    
    # URL accesible desde fuera (asumiendo localhost:8000 o IP)
    # En producción usar dominio real. Aquí devolvemos path relativo.
    current_user.avatar_url = f"/uploads/{filename}"
    session.add(current_user)
    session.commit()
    session.refresh(current_user)
    return current_user

@app.put("/users/me", response_model=UserRead)
def update_profile(bio: str = Form(...), session: Session = Depends(get_session), current_user: User = Depends(get_current_user)):
    current_user.bio = bio
    session.add(current_user)
    session.commit()
    session.refresh(current_user)
    return current_user

@app.get("/users/{user_id}", response_model=UserRead)
def read_user(user_id: int, session: Session = Depends(get_session)):
    user = session.get(User, user_id)
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user

@app.get("/users/{user_id}/posts", response_model=List[PostRead])
def read_user_posts(user_id: int, session: Session = Depends(get_session)):
    posts = session.exec(select(Post).where(Post.user_id == user_id).order_by(Post.created_at.desc())).all()
    # Enriquecer con datos del usuario (ineficiente pero simple para este ejemplo)
    user = session.get(User, user_id)
    result = []
    for p in posts:
        result.append(PostRead(
            **p.dict(), 
            username=user.username, 
            user_avatar=user.avatar_url
        ))
    return result


# --- POSTS ---

@app.post("/posts", response_model=PostRead)
async def create_post(
    description: str = Form(...),
    file: UploadFile = File(...),
    session: Session = Depends(get_session),
    current_user: User = Depends(get_current_user)
):
    filename = f"{uuid.uuid4()}_{file.filename}"
    path = f"uploads/{filename}"
    with open(path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
    
    media_type = "image" if "image" in file.content_type else "video"
    
    post = Post(
        description=description,
        media_url=f"/uploads/{filename}",
        media_type=media_type,
        user_id=current_user.id
    )
    session.add(post)
    session.commit()
    session.refresh(post)
    
    return PostRead(**post.dict(), username=current_user.username, user_avatar=current_user.avatar_url)

@app.get("/posts", response_model=List[PostRead])
def read_posts(session: Session = Depends(get_session)):
    posts = session.exec(select(Post).order_by(Post.created_at.desc())).all()
    # Join manual simple
    result = []
    for p in posts:
        user = session.get(User, p.user_id)
        result.append(PostRead(
            **p.dict(),
            username=user.username if user else "Unknown",
            user_avatar=user.avatar_url if user else None
        ))
    return result

@app.delete("/posts/{post_id}")
def delete_post(post_id: int, session: Session = Depends(get_session), current_user: User = Depends(get_current_user)):
    post = session.get(Post, post_id)
    if not post:
        raise HTTPException(status_code=404, detail="Post not found")
    if post.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")
    
    session.delete(post)
    session.commit()
    return {"ok": True}

@app.put("/posts/{post_id}", response_model=PostRead)
def update_post(post_id: int, description: str = Form(...), session: Session = Depends(get_session), current_user: User = Depends(get_current_user)):
    post = session.get(Post, post_id)
    if not post:
        raise HTTPException(status_code=404, detail="Post not found")
    if post.user_id != current_user.id:
        raise HTTPException(status_code=403, detail="Not authorized")
    
    post.description = description
    session.add(post)
    session.commit()
    session.refresh(post)
    return PostRead(**post.dict(), username=current_user.username, user_avatar=current_user.avatar_url)
