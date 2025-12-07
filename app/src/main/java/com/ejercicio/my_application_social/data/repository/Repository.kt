package com.ejercicio.my_application_social.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ejercicio.my_application_social.data.api.ApiService
import com.ejercicio.my_application_social.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

val Context.dataStore by preferencesDataStore(name = "settings")

// Repositorio conectado a la API Python
class Repository(
    private val api: ApiService, 
    private val context: Context,
    private val baseUrl: String // Recibimos la URL base para arreglar links de imagenes
) {

    private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }
    val currentUserId: Flow<String?> = context.dataStore.data.map { it[USER_ID_KEY] }

    suspend fun saveSession(token: String, userId: Int) {
        context.dataStore.edit { 
            it[TOKEN_KEY] = token
            it[USER_ID_KEY] = userId.toString()
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }

    // AUTH
    suspend fun login(req: LoginRequest) = api.login(req)
    suspend fun register(req: RegisterRequest) = api.register(req)
    
    // POSTS
    suspend fun getPosts(token: String): retrofit2.Response<List<Post>> {
        val response = api.getPosts("Bearer $token")
        if (response.isSuccessful) {
            // Ajustamos las URLs de las imágenes
            val fixedPosts = response.body()?.map { fixPostUrl(it) } ?: emptyList()
            return retrofit2.Response.success(fixedPosts)
        }
        return response
    }

    suspend fun getUserPosts(token: String, id: Int): retrofit2.Response<List<Post>> {
        val response = api.getUserPosts("Bearer $token", id)
        if (response.isSuccessful) {
            val fixedPosts = response.body()?.map { fixPostUrl(it) } ?: emptyList()
            return retrofit2.Response.success(fixedPosts)
        }
        return response
    }

    suspend fun createPost(token: String, desc: String, file: File): retrofit2.Response<Post> {
        val descPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        return api.createPost("Bearer $token", descPart, body)
    }
    
    suspend fun getPostById(token: String, id: Int): retrofit2.Response<Post> {
        val response = api.getPostById("Bearer $token", id)
        if (response.isSuccessful && response.body() != null) {
            return retrofit2.Response.success(fixPostUrl(response.body()!!))
        }
        return response
    }

    suspend fun updatePost(token: String, id: Int, desc: String): retrofit2.Response<Post> {
        val descPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())
        return api.updatePost("Bearer $token", id, descPart)
    }

    suspend fun deletePost(token: String, id: Int) = api.deletePost("Bearer $token", id)
    
    // USERS
    suspend fun getMe(token: String): retrofit2.Response<User> {
        val response = api.getMe("Bearer $token")
        if (response.isSuccessful && response.body() != null) {
            return retrofit2.Response.success(fixUserUrl(response.body()!!))
        }
        return response
    }
    
    suspend fun getUser(token: String, id: Int) = api.getUser("Bearer $token", id)

    // --- HELPER PARA ARREGLAR URLS ---
    private fun fixPostUrl(post: Post): Post {
        // Si viene "/uploads/xxx.jpg", lo convertimos a "http://10.0.2.2:5000/uploads/xxx.jpg"
        val fullUrl = if (post.media_url.startsWith("http")) post.media_url else "$baseUrl${post.media_url.removePrefix("/")}"
        return post.copy(media_url = fullUrl)
    }

    private fun fixUserUrl(user: User): User {
        val url = user.avatar_url
        if (url.isNullOrEmpty()) return user
        val fullUrl = if (url.startsWith("http")) url else "$baseUrl${url.removePrefix("/")}"
        return user.copy(avatar_url = fullUrl)
    }
}