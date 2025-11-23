package com.ejercicio.my_application_social.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val bio: String? = null,
    val avatar_url: String? = null
)

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val username: String, val email: String, val password: String)

// 🚨 CÓDIGO FALTANTE: Debe tener 'token' y 'user_id'
data class AuthResponse(
    val token: String,
    val user_id: Int
)