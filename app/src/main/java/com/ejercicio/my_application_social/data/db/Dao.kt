package com.ejercicio.my_application_social.data.db

import androidx.room.*
import com.ejercicio.my_application_social.data.model.Post
import com.ejercicio.my_application_social.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long
}

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY created_at DESC")
    fun getAllPosts(): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE user_id = :userId ORDER BY created_at DESC")
    fun getPostsByUser(userId: Int): Flow<List<Post>>
    
    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: Int): Post?

    @Insert
    suspend fun insertPost(post: Post): Long

    @Query("DELETE FROM posts WHERE id = :postId")
    suspend fun deletePost(postId: Int)
    
    @Query("UPDATE posts SET description = :description WHERE id = :id")
    suspend fun updatePostDescription(id: Int, description: String)
}