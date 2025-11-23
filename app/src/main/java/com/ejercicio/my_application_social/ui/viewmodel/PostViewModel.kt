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
    // Usamos el repositorio local
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

    fun getFeed() {
        viewModelScope.launch {
            // Recogemos el Flow de Room directamente
            repository.getAllPosts().collect { localPosts ->
                _posts.value = localPosts
            }
        }
    }
    
    fun getMyPosts() {
        viewModelScope.launch {
            val userIdStr = repository.currentUserId.first() ?: return@launch
            val userId = userIdStr.toIntOrNull() ?: return@launch
            repository.getUserPosts(userId).collect { userPosts ->
                _myPosts.value = userPosts
            }
        }
    }
    
    fun getProfile() {
        viewModelScope.launch {
            val userIdStr = repository.currentUserId.first() ?: return@launch
            val userId = userIdStr.toIntOrNull() ?: return@launch
            _currentUser.value = repository.getUserById(userId)
        }
    }

    fun getPostById(postId: Int) {
        viewModelScope.launch {
            _currentPost.value = repository.getPostById(postId)
        }
    }

    fun createPost(desc: String, file: File) {
        viewModelScope.launch {
            _state.value = PostState.Loading
            try {
                val userIdStr = repository.currentUserId.first() ?: return@launch
                val userId = userIdStr.toIntOrNull() ?: return@launch
                
                repository.createPost(userId, desc, file)
                _state.value = PostState.Success
            } catch (e: Exception) {
                _state.value = PostState.Error(e.message ?: "Error al crear post")
            }
        }
    }
    
    fun updatePost(id: Int, desc: String) {
        viewModelScope.launch {
            _state.value = PostState.Loading
            try {
                repository.updatePostDescription(id, desc)
                _state.value = PostState.Success
            } catch (e: Exception) {
                _state.value = PostState.Error(e.message ?: "Error al actualizar")
            }
        }
    }
    
    fun deletePost(id: Int) {
        viewModelScope.launch {
            repository.deletePost(id)
        }
    }
    
    fun resetState() { 
        _state.value = PostState.Idle 
        _currentPost.value = null
    }
}