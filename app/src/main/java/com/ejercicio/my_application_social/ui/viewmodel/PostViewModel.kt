package com.ejercicio.my_application_social.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ejercicio.my_application_social.data.model.Post
import com.ejercicio.my_application_social.data.model.User
import com.ejercicio.my_application_social.data.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

sealed class PostState {
    object Idle : PostState()
    object Loading : PostState()
    object Success : PostState()
    data class Error(val msg: String) : PostState()
}

class PostViewModel(private val repository: Repository) : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts = _posts.asStateFlow()
    
    private val _myPosts = MutableStateFlow<List<Post>>(emptyList())
    val myPosts = _myPosts.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()
    
    private val _currentPost = MutableStateFlow<Post?>(null)
    val currentPost = _currentPost.asStateFlow()

    private val _state = MutableStateFlow<PostState>(PostState.Idle)
    val state = _state.asStateFlow()

    // Carga todos los posts (Feed)
    fun getFeed() {
        viewModelScope.launch {
            _state.value = PostState.Loading
            try {
                val token = repository.token.first() ?: return@launch
                val response = repository.getPosts(token)
                if (response.isSuccessful) {
                    _posts.value = response.body() ?: emptyList()
                    _state.value = PostState.Success
                } else {
                    _state.value = PostState.Error("Error al cargar feed")
                }
            } catch (e: Exception) {
                _state.value = PostState.Error("Error: ${e.message}")
            }
        }
    }
    
    // Carga los posts del usuario actual
    fun getMyPosts() {
        viewModelScope.launch {
            val token = repository.token.first() ?: return@launch
            try {
                val userIdStr = repository.currentUserId.first() ?: return@launch
                val userId = userIdStr.toIntOrNull() ?: return@launch
                
                val response = repository.getUserPosts(token, userId)
                if (response.isSuccessful) {
                    _myPosts.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Manejar error silenciosamente o mostrar toast
            }
        }
    }
    
    fun getProfile() {
        viewModelScope.launch {
            val token = repository.token.first() ?: return@launch
            try {
                val response = repository.getMe(token)
                if (response.isSuccessful) {
                    _currentUser.value = response.body()
                }
            } catch (e: Exception) { }
        }
    }

    // Obtener un post por ID para editarlo
    fun getPostById(postId: Int) {
        viewModelScope.launch {
             val token = repository.token.first() ?: return@launch
             try {
                 val response = repository.getPostById(token, postId)
                 if (response.isSuccessful) {
                     _currentPost.value = response.body()
                 }
             } catch (e: Exception) { }
        }
    }

    fun createPost(desc: String, file: File) {
        viewModelScope.launch {
            _state.value = PostState.Loading
            try {
                val token = repository.token.first() ?: return@launch
                val response = repository.createPost(token, desc, file)
                
                if (response.isSuccessful) {
                    getFeed() // Recargar feed
                    _state.value = PostState.Success
                } else {
                    _state.value = PostState.Error("Error al subir imagen")
                }
            } catch (e: Exception) {
                _state.value = PostState.Error(e.message ?: "Error desconocido")
            }
        }
    }
    
    fun updatePost(id: Int, desc: String) {
        viewModelScope.launch {
            _state.value = PostState.Loading
            try {
                val token = repository.token.first() ?: return@launch
                val response = repository.updatePost(token, id, desc)
                
                if (response.isSuccessful) {
                    _state.value = PostState.Success
                } else {
                    _state.value = PostState.Error("Error al actualizar")
                }
            } catch (e: Exception) {
                _state.value = PostState.Error(e.message ?: "Error al conectar")
            }
        }
    }
    
    fun deletePost(id: Int) {
        viewModelScope.launch {
            try {
                val token = repository.token.first() ?: return@launch
                val response = repository.deletePost(token, id)
                if (response.isSuccessful) {
                    getMyPosts() // Recargar lista
                    getFeed()
                }
            } catch (e: Exception) {}
        }
    }
    
    fun resetState() { 
        _state.value = PostState.Idle 
        _currentPost.value = null
    }
}