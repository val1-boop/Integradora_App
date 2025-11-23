package com.ejercicio.my_application_social.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val username: String,
    val email: String,
    val passwordHash: String, // Guardaremos la contraseña aquí para el login local
    val bio: String? = null,
    val avatar_url: String? = null
)

// Modelos auxiliares para la UI (no se guardan en BD)
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val username: String, val email: String, val password: String)