package com.ejercicio.my_application_social.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ejercicio.my_application_social.data.db.AppDatabase
import com.ejercicio.my_application_social.data.model.Post
import com.ejercicio.my_application_social.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val Context.dataStore by preferencesDataStore(name = "settings")

// Repositorio Local-First (Usando Room)
class Repository(private val db: AppDatabase, private val context: Context) {

    private val USER_ID_KEY = stringPreferencesKey("user_id")

    // Sesión basada en ID local
    val currentUserId: Flow<String?> = context.dataStore.data.map { it[USER_ID_KEY] }

    suspend fun saveSession(userId: Int) {
        context.dataStore.edit { 
            it[USER_ID_KEY] = userId.toString()
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }

    // --- AUTH ---
    suspend fun login(email: String, pass: String): Result<User> {
        val user = db.userDao().getUserByEmail(email)
        return if (user != null && user.passwordHash == pass) {
            Result.success(user)
        } else {
            Result.failure(Exception("Credenciales inválidas"))
        }
    }

    suspend fun register(name: String, username: String, email: String, pass: String): Result<User> {
        if (db.userDao().getUserByEmail(email) != null) return Result.failure(Exception("Email ya registrado"))

        val newUser = User(
            name = name,
            username = username,
            email = email,
            passwordHash = pass,
            bio = "¡Hola! Soy nuevo aquí."
        )
        
        try {
            val id = db.userDao().insertUser(newUser)
            return Result.success(newUser.copy(id = id.toInt()))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
    
    suspend fun getUserById(id: Int): User? = db.userDao().getUserById(id)

    // --- POSTS ---
    fun getAllPosts(): Flow<List<Post>> = db.postDao().getAllPosts()
    
    fun getUserPosts(userId: Int): Flow<List<Post>> = db.postDao().getPostsByUser(userId)

    suspend fun createPost(userId: Int, desc: String, file: File) {
        val user = db.userDao().getUserById(userId) ?: return
        
        val newPost = Post(
            user_id = userId,
            username = user.username,
            user_avatar = user.avatar_url,
            description = desc,
            media_url = file.absolutePath, // Guardamos ruta absoluta para cargarla después
            media_type = "image",
            created_at = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        )
        db.postDao().insertPost(newPost)
    }
    
    suspend fun getPostById(postId: Int): Post? {
        return db.postDao().getPostById(postId)
    }

    suspend fun updatePostDescription(postId: Int, description: String) {
        db.postDao().updatePostDescription(postId, description)
    }

    suspend fun deletePost(postId: Int) {
        db.postDao().deletePost(postId)
    }
}