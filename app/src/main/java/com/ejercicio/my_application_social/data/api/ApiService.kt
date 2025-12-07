package com.ejercicio.my_application_social.data.api

import com.ejercicio.my_application_social.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // AUTH
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // USERS
    @GET("users/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<User>
    
    @GET("users/{id}")
    suspend fun getUser(@Header("Authorization") token: String, @Path("id") id: Int): Response<User>

    // POSTS
    @GET("posts")
    suspend fun getPosts(@Header("Authorization") token: String): Response<List<Post>>

    @GET("users/{id}/posts")
    suspend fun getUserPosts(@Header("Authorization") token: String, @Path("id") id: Int): Response<List<Post>>
    
    @GET("posts/{id}")
    suspend fun getPostById(@Header("Authorization") token: String, @Path("id") id: Int): Response<Post>

    @Multipart
    @POST("posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<Post>

    @Multipart
    @PUT("posts/{id}")
    suspend fun updatePost(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Part("description") description: RequestBody
    ): Response<Post>

    @DELETE("posts/{id}")
    suspend fun deletePost(@Header("Authorization") token: String, @Path("id") id: Int): Response<Unit>
}