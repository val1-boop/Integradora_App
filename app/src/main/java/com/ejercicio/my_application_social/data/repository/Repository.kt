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

class Repository(private val api: ApiService, private val context: Context) {

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

    suspend fun login(req: LoginRequest) = api.login(req)
    suspend fun register(req: RegisterRequest) = api.register(req)
    
    suspend fun getPosts(token: String) = api.getPosts("Bearer $token")
    suspend fun getUserPosts(token: String, id: Int) = api.getUserPosts("Bearer $token", id)
    suspend fun getMe(token: String) = api.getMe("Bearer $token")
    suspend fun getUser(token: String, id: Int) = api.getUser("Bearer $token", id)

    suspend fun createPost(token: String, desc: String, file: File): retrofit2.Response<Post> {
        val descPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        return api.createPost("Bearer $token", descPart, body)
    }
    
    suspend fun deletePost(token: String, id: Int) = api.deletePost("Bearer $token", id)
    
    suspend fun updateProfile(token: String, bio: String) = 
        api.updateProfile("Bearer $token", bio.toRequestBody("text/plain".toMediaTypeOrNull()))
        
    suspend fun updateAvatar(token: String, file: File): retrofit2.Response<User> {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        return api.updateAvatar("Bearer $token", body)
    }
}