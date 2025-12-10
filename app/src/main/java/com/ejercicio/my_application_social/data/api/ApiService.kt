package com.ejercicio.my_application_social.data.api

import com.ejercicio.my_application_social.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("/users/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<User>

    @GET("/posts")
    suspend fun getPosts(@Header("Authorization") token: String): Response<List<Post>>

    @GET("/users/{userId}/posts")
    suspend fun getPostsByUser(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Response<List<Post>>

    @GET("/posts/{postId}")
    suspend fun getPostById(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int
    ): Response<Post>

    @Multipart
    @POST("/posts")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<Post>

    @Multipart
    @PUT("/posts/{postId}")
    suspend fun updatePost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int,
        @Part("description") description: RequestBody
    ): Response<Post>

    @DELETE("/posts/{postId}")
    suspend fun deletePost(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int
    ): Response<Unit>
}