import sqlite3
import os
import uuid
from flask import Flask, jsonify, request, g, send_from_directory
from werkzeug.security import generate_password_hash, check_password_hash
from datetime import datetime

# --- CONFIGURACIÓN ---
DATABASE = 'social_app_db.sqlite'
UPLOAD_FOLDER = 'uploads'
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif', 'mp4'} 

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
app.config['SECRET_KEY'] = 'una-clave-secreta-fuerte-para-jwt'

if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

# --- UTILS DE BASE DE DATOS ---

def get_db():
    """Establece la conexión a la base de datos SQLite."""
    db = getattr(g, '_database', None)
    if db is None:
        db = g._database = sqlite3.connect(DATABASE)
        db.row_factory = sqlite3.Row 
    return db

@app.teardown_appcontext
def close_connection(exception):
    """Cierra la conexión DB al final de la petición."""
    db = getattr(g, '_database', None)
    if db is not None:
        db.close()

def init_db():
    """Crea las tablas de Users y Posts si no existen."""
    with app.app_context():
        db = get_db()
        cursor = db.cursor()
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                username TEXT NOT NULL UNIQUE,
                email TEXT NOT NULL UNIQUE,
                passwordHash TEXT NOT NULL, 
                bio TEXT,
                avatar_url TEXT
            )
        """)
        
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS posts (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                description TEXT NOT NULL,
                media_url TEXT NOT NULL, 
                media_type TEXT NOT NULL,
                created_at TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """)
        db.commit()

init_db()

# --- UTILS DE SEGURIDAD Y ARCHIVOS ---

def get_user_from_token(auth_header):
    """Simula la autenticación extrayendo el ID del usuario del header Bearer."""
    if not auth_header or not auth_header.startswith("Bearer "):
        return None
    try:
        user_id = int(auth_header.split(" ")[1])
        db = get_db()
        cursor = db.cursor()
        cursor.execute("SELECT * FROM users WHERE id = ?", (user_id,))
        user = cursor.fetchone()
        return dict(user) if user else None
    except Exception:
        return None

def allowed_file(filename):
    """Verifica la extensión del archivo."""
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def save_file(uploaded_file):
    """Guarda un archivo subido con un nombre único (UUID)."""
    extension = uploaded_file.filename.rsplit('.', 1)[1].lower()
    filename = str(uuid.uuid4()) + '.' + extension
    filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
    uploaded_file.save(filepath)
    return filename 

def user_row_to_json(user_row):
    """Formatea la fila de usuario del DB a un objeto JSON compatible con GSON/Retrofit."""
    return {
        "id": user_row['id'],
        "name": user_row['name'],
        "username": user_row['username'],
        "email": user_row['email'],
        "bio": user_row['bio'],
        "avatar_url": f"/uploads/{user_row['avatar_url']}" if user_row['avatar_url'] else None
    }

def post_row_to_json(post_row):
    host_with_port = request.host
    base_url = f"http://{host_with_port}" 
    
    avatar_filename = post_row['user_avatar'] 
    avatar_url = f"{base_url}/uploads/{avatar_filename}" if avatar_filename else None

    filename = post_row['media_url'] 
    full_media_url = f"{base_url}/uploads/{filename}"
    
    return {
        "id": post_row['id'],
        "user_id": post_row['user_id'],
        "description": post_row['description'],
        "username": post_row['username'],      
        "user_avatar": avatar_url,             
        "media_url": full_media_url, 
        "media_type": post_row['media_type'],
        "created_at": post_row['created_at']
    }


# ----------------------------------------------------
# RUTAS DE AUTENTICACIÓN (AUTH)
# ----------------------------------------------------

@app.route('/auth/register', methods=['POST'])
def register():
    """POST /auth/register: Crea un nuevo usuario."""
    data = request.json
    name = data.get('name')
    username = data.get('username')
    email = data.get('email')
    password = data.get('password')
    
    if not all([name, username, email, password]):
        return jsonify({"message": "Faltan campos requeridos"}), 400

    db = get_db()
    cursor = db.cursor()
    
    password_hash = generate_password_hash(password)
    
    try:
        cursor.execute(
            "INSERT INTO users (name, username, email, passwordHash, bio, avatar_url) VALUES (?, ?, ?, ?, ?, ?)",
            (name, username, email, password_hash, "¡Hola! Soy nuevo aquí.", None)
        )
        db.commit()
        user_id = cursor.lastrowid
        
        return jsonify({
            "token": str(user_id), 
            "user_id": user_id
        }), 201

    except sqlite3.IntegrityError:
        return jsonify({"message": "El email o nombre de usuario ya existe"}), 409
    except Exception as e:
        return jsonify({"message": f"Error al registrar: {e}"}), 500

@app.route('/auth/login', methods=['POST'])
def login():
    """POST /auth/login: Autentica un usuario."""
    data = request.json
    email = data.get('email')
    password = data.get('password')

    if not all([email, password]):
        return jsonify({"message": "Faltan campos requeridos"}), 400

    db = get_db()
    cursor = db.cursor()
    cursor.execute("SELECT * FROM users WHERE email = ?", (email,))
    user = cursor.fetchone()

    if user and check_password_hash(user['passwordHash'], password):
        user_id = user['id']
        return jsonify({
            "token": str(user_id),
            "user_id": user_id
        }), 200
    else:
        return jsonify({"message": "Credenciales inválidas"}), 401


# ----------------------------------------------------
# RUTAS DE USUARIOS (USERS)
# ----------------------------------------------------

@app.route('/users/me', methods=['GET'])
def get_me():
    """GET /users/me: Obtiene la información del usuario autenticado."""
    auth_header = request.headers.get('Authorization')
    current_user = get_user_from_token(auth_header)
    
    if not current_user:
        return jsonify({"message": "No autorizado"}), 401
    
    return jsonify(user_row_to_json(current_user)), 200

@app.route('/users/<int:user_id>', methods=['GET'])
def get_user(user_id):
    """GET /users/{id}: Obtiene la información de un usuario por ID."""
    auth_header = request.headers.get('Authorization')
    if not get_user_from_token(auth_header):
        return jsonify({"message": "No autorizado"}), 401
    
    db = get_db()
    cursor = db.cursor()
    cursor.execute("SELECT * FROM users WHERE id = ?", (user_id,))
    user = cursor.fetchone()
    
    if user:
        return jsonify(user_row_to_json(user)), 200
    return jsonify({"message": "Usuario no encontrado"}), 404

@app.route('/users/me', methods=['PUT'])
def update_profile():
    """PUT /users/me: Actualiza el campo 'bio' (Multipart)."""
    auth_header = request.headers.get('Authorization')
    current_user = get_user_from_token(auth_header)
    
    if not current_user:
        return jsonify({"message": "No autorizado"}), 401
    
    db = get_db()
    cursor = db.cursor()
    
    try:
        new_bio = request.form.get('bio')
        
        if new_bio is None:
            return jsonify({"message": "Falta el campo 'bio'"}), 400
            
        cursor.execute("UPDATE users SET bio = ? WHERE id = ?", (new_bio, current_user['id']))
        db.commit()
        
        cursor.execute("SELECT * FROM users WHERE id = ?", (current_user['id'],))
        updated_user = cursor.fetchone()
        
        return jsonify(user_row_to_json(updated_user)), 200
        
    except Exception as e:
        return jsonify({"message": f"Error al actualizar perfil: {e}"}), 500

@app.route('/users/me/avatar', methods=['PUT'])
def update_avatar():
    """PUT /users/me/avatar: Actualiza el avatar (Multipart, file)."""
    auth_header = request.headers.get('Authorization')
    current_user = get_user_from_token(auth_header)
    
    if not current_user:
        return jsonify({"message": "No autorizado"}), 401

    if 'file' not in request.files:
        return jsonify({"message": "No se encontró el archivo 'file'"}), 400
    
    file = request.files['file']
    
    if file.filename == '':
        return jsonify({"message": "Nombre de archivo vacío"}), 400
        
    if file and allowed_file(file.filename):
        try:
            filename = save_file(file)
            
            db = get_db()
            cursor = db.cursor()
            cursor.execute("UPDATE users SET avatar_url = ? WHERE id = ?", (filename, current_user['id']))
            db.commit()
            
            cursor.execute("SELECT * FROM users WHERE id = ?", (current_user['id'],))
            updated_user = cursor.fetchone()
            
            return jsonify(user_row_to_json(updated_user)), 200
        except Exception as e:
            return jsonify({"message": f"Error al subir avatar: {e}"}), 500
    
    return jsonify({"message": "Formato de archivo no permitido"}), 400


# ----------------------------------------------------
# RUTAS DE PUBLICACIONES (POSTS)
# ----------------------------------------------------

@app.route('/posts', methods=['GET'])
def get_posts():
    """GET /posts: Obtiene todos los posts."""
    auth_header = request.headers.get('Authorization')
    if not get_user_from_token(auth_header):
        return jsonify({"message": "No autorizado"}), 401
    
    db = get_db()
    cursor = db.cursor()
    
    cursor.execute("""
        SELECT 
            p.*, 
            u.username, 
            u.avatar_url AS user_avatar 
        FROM posts p
        JOIN users u ON p.user_id = u.id
        ORDER BY p.created_at DESC
    """)
    
    posts = [post_row_to_json(row) for row in cursor.fetchall()]
    
    return jsonify(posts), 200

@app.route('/users/<int:user_id>/posts', methods=['GET'])
def get_user_posts(user_id):
    """GET /users/{id}/posts: Obtiene los posts de un usuario específico."""
    auth_header = request.headers.get('Authorization')
    if not get_user_from_token(auth_header):
        return jsonify({"message": "No autorizado"}), 401
    
    db = get_db()
    cursor = db.cursor()
    cursor.execute("""
        SELECT 
            p.*, 
            u.username, 
            u.avatar_url AS user_avatar 
        FROM posts p
        JOIN users u ON p.user_id = u.id
        WHERE p.user_id = ? 
        ORDER BY p.created_at DESC
    """, (user_id,))    
    posts = [post_row_to_json(row) for row in cursor.fetchall()]
    
    return jsonify(posts), 200

@app.route('/posts/<int:post_id>', methods=['GET'])
def get_post_by_id(post_id):
    """GET /posts/{id}: Obtiene un post específico por su ID."""
    auth_header = request.headers.get('Authorization')
    if not get_user_from_token(auth_header):
        return jsonify({"message": "No autorizado"}), 401
    
    db = get_db()
    cursor = db.cursor()
    
    cursor.execute("""
        SELECT 
            p.*, 
            u.username, 
            u.avatar_url AS user_avatar 
        FROM posts p
        JOIN users u ON p.user_id = u.id
        WHERE p.id = ?
    """, (post_id,))
    
    post = cursor.fetchone()
    
    if not post:
        return jsonify({"message": "Post no encontrado"}), 404
        
    return jsonify(post_row_to_json(post)), 200


@app.route('/posts', methods=['POST'])
def create_post():
    """POST /posts: Crea una nueva publicación (Multipart, descripción y archivo)."""
    auth_header = request.headers.get('Authorization')
    current_user = get_user_from_token(auth_header)
    
    if not current_user:
        return jsonify({"message": "No autorizado"}), 401
    
    if 'file' not in request.files:
        return jsonify({"message": "No se encontró el archivo 'file'"}), 400
        
    file = request.files['file']
    description = request.form.get('description')
    
    if file.filename == '' or description is None:
        return jsonify({"message": "Faltan datos o archivo"}), 400

    if file and allowed_file(file.filename):
        try:
            filename = save_file(file)
            extension = filename.rsplit('.', 1)[1].lower()
            media_type = 'image' if extension in ['png', 'jpg', 'jpeg', 'gif'] else 'video'
            created_at = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
            
            db = get_db()
            cursor = db.cursor()
            
            cursor.execute(
                "INSERT INTO posts (user_id, description, media_url, media_type, created_at) VALUES (?, ?, ?, ?, ?)",
                (current_user['id'], description, filename, media_type, created_at)
            )
            db.commit()
            post_id = cursor.lastrowid
            
            cursor.execute("""
                SELECT
                    p.*,
                    u.username,
                    u.avatar_url AS user_avatar
                FROM posts p
                JOIN users u ON p.user_id = u.id
                WHERE p.id = ?
            """, (post_id,))
            
            new_post = cursor.fetchone()
            
            return jsonify(post_row_to_json(new_post)), 201
            
        except Exception as e:
            return jsonify({"message": f"Error al crear post: {e}"}), 500
    
    return jsonify({"message": "Formato de archivo no permitido"}), 400

@app.route('/posts/<int:post_id>', methods=['DELETE'])
def delete_post(post_id):
    """DELETE /posts/{id}: Elimina un post."""
    auth_header = request.headers.get('Authorization')
    current_user = get_user_from_token(auth_header)
    
    if not current_user:
        return jsonify({"message": "No autorizado"}), 401
        
    db = get_db()
    cursor = db.cursor()
    
    cursor.execute("SELECT user_id FROM posts WHERE id = ?", (post_id,))
    post = cursor.fetchone()
    
    if not post:
        return jsonify({"message": "Post no encontrado"}), 404
        
    if post['user_id'] != current_user['id']:
        return jsonify({"message": "No tienes permiso para eliminar este post"}), 403
        
    try:
        cursor.execute("DELETE FROM posts WHERE id = ?", (post_id,))
        db.commit()
        
        return '', 204
        
    except Exception as e:
        return jsonify({"message": f"Error al eliminar post: {e}"}), 500

@app.route('/posts/<int:post_id>', methods=['PUT'])
def update_post(post_id):
    """PUT /posts/{id}: Actualiza la descripción de un post (Multipart)."""
    auth_header = request.headers.get('Authorization')
    current_user = get_user_from_token(auth_header)
    
    if not current_user:
        return jsonify({"message": "No autorizado"}), 401
    
    db = get_db()
    cursor = db.cursor()
    
    cursor.execute("SELECT user_id FROM posts WHERE id = ?", (post_id,))
    post = cursor.fetchone()
    
    if not post:
        return jsonify({"message": "Post no encontrado"}), 404
        
    if post['user_id'] != current_user['id']:
        return jsonify({"message": "No tienes permiso para editar este post"}), 403
        
    try:
        new_description = request.form.get('description')
        
        if new_description is None:
            return jsonify({"message": "Falta el campo 'description'"}), 400
            
        cursor.execute("UPDATE posts SET description = ? WHERE id = ?", (new_description, post_id))
        db.commit()
        
        cursor.execute("""
            SELECT 
                p.*, 
                u.username, 
                u.avatar_url AS user_avatar 
            FROM posts p
            JOIN users u ON p.user_id = u.id
            WHERE p.id = ?
        """, (post_id,))
        updated_post = cursor.fetchone()
        
        return jsonify(post_row_to_json(updated_post)), 200
        
    except Exception as e:
        return jsonify({"message": f"Error al actualizar post: {e}"}), 500


# ----------------------------------------------------
# RUTA PARA SERVIR ARCHIVOS SUBIDOS
# ----------------------------------------------------

@app.route('/uploads/<filename>')
def uploaded_file(filename):
    """Sirve archivos estáticos (imágenes/videos) desde la carpeta 'uploads'."""
    return send_from_directory(app.config['UPLOAD_FOLDER'], filename)


# ----------------------------------------------------
# EJECUCIÓN DEL SERVIDOR
# ----------------------------------------------------

if __name__ == '__main__':
    print(f"Servidor iniciado. Subidas guardadas en: {UPLOAD_FOLDER}/")
    app.run(host='0.0.0.0', port=5000, debug=True)
