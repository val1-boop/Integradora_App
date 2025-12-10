package com.ejercicio.my_application_social.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ejercicio.my_application_social.data.api.ApiService
import com.ejercicio.my_application_social.data.model.* // Importa tus modelos (User, Post, AuthResponse, etc.)
// ⚠️ CORRECCIÓN: Eliminamos la importación incorrecta de com.google.android.gms.games...AuthResponse
// 👈 Añadido para asegurar la referencia correcta
//import com.google.android.gms.games.gamessignin.AuthResponse

import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

// 🚨 CORRECCIÓN: Quitamos las importaciones innecesarias o que causan conflicto en el helper del token
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking // Necesario para obtener el token si se llama fuera de un suspend (usaremos .first() dentro de suspend)



val Context.dataStore by preferencesDataStore(name = "settings")

// Repositorio Basado en API (Flask Backend)
class Repository(
    private val apiService: ApiService,
    private val context: Context
) {

    private val USER_TOKEN_KEY = stringPreferencesKey("auth_token")

    // --- UTILS DE SESIÓN (DataStore) ---

    val currentAuthToken: Flow<String?> = context.dataStore.data.map { it[USER_TOKEN_KEY] }

    suspend fun saveSession(token: String) {
        context.dataStore.edit {
            it[USER_TOKEN_KEY] = token
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }

    // Función helper para construir el header 'Bearer token'
    private suspend fun createBearerToken(): String {
        // Usamos .first() dentro de suspend fun para obtener el valor del Flow.
        val token = currentAuthToken.first()
            ?: throw IllegalStateException("Token de autenticación no disponible.")
        return "Bearer $token"
    }

    // =================================================================
    // AUTHENTICATION (AUTH)
    // =================================================================

    suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = apiService.login(request)
            // 🚨 CORRECCIÓN: Si el servidor devuelve 200/201, extraemos el body. Si es null, es un error.
            val authResponse = response.body() ?: throw IllegalStateException("Respuesta de login vacía")
            saveSession(authResponse.token)
            Result.success(authResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return try {
            val response = apiService.register(request)
            val authResponse = response.body() ?: throw IllegalStateException("Respuesta de registro vacía")
            saveSession(authResponse.token)
            Result.success(authResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =================================================================
    // USER PROFILE (USERS)
    // =================================================================

    suspend fun getMe(): User? {
        val tokenHeader = createBearerToken()
        // 🚨 CORRECCIÓN: Extraemos el cuerpo de la respuesta o devolvemos null
        return apiService.getMe(tokenHeader).body()
    }

    // =================================================================
    // POSTS
    // =================================================================

    suspend fun getAllPosts(): List<Post> {
        val tokenHeader = createBearerToken()
        // 🚨 CORRECCIÓN: Extraemos el cuerpo o devolvemos lista vacía
        return apiService.getPosts(tokenHeader).body() ?: emptyList()
    }

    suspend fun getUserPosts(userId: Int): List<Post> {
        val tokenHeader = createBearerToken()
        return apiService.getPostsByUser(tokenHeader, userId).body() ?: emptyList()
    }

    suspend fun createPost(desc: String, file: File): Post? {
        val tokenHeader = createBearerToken()

        val descriptionPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())
        val mediaType = file.extension.toMediaTypeOrNull() ?: "image/jpeg".toMediaTypeOrNull()
        val requestFile = file.asRequestBody(mediaType)
        val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

        return apiService.createPost(tokenHeader, descriptionPart, filePart).body()
    }

    suspend fun updatePostDescription(postId: Int, description: String): Post? {
        val tokenHeader = createBearerToken()
        val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())

        return apiService.updatePost(tokenHeader, postId, descriptionPart).body()
    }

    suspend fun deletePost(postId: Int) {
        val tokenHeader = createBearerToken()
        // No necesita .body() porque Retrofit lo maneja como Response<Unit>
        apiService.deletePost(tokenHeader, postId)
    }
    suspend fun getPostById(postId: Int): Post? {
        val tokenHeader = createBearerToken()
        // Llama al nuevo método de ApiService y extrae el cuerpo
        return apiService.getPostById(tokenHeader, postId).body()
    }
}