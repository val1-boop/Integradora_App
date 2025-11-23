package com.ejercicio.my_application_social.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val user_id: Int,
    val username: String,     // Guardamos nombre para no hacer joins complejos ahora
    val user_avatar: String?,
    val description: String,
    val media_url: String,    // Ruta local del archivo
    val media_type: String,
    val created_at: String
)