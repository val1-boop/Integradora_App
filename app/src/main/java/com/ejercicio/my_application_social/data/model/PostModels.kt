package com.ejercicio.my_application_social.data.model

data class Post(
    val id: Int = 0,
    val user_id: Int,
    val username: String,
    val user_avatar: String?,
    val description: String,
    val media_url: String,
    val media_type: String,
    val created_at: String
)