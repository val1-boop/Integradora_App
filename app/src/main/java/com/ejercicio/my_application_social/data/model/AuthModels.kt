package com.ejercicio.my_application_social.data.model

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val username: String, val email: String, val password: String)

data class AuthResponse(
    val access_token: String,
    val token_type: String,
    val user_id: Int
)

data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val bio: String?,
    val avatar_url: String?
)