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

    private val _state = MutableStateFlow<PostState>(PostState.Idle)
    val state = _state.asStateFlow()

    fun getFeed() {
        viewModelScope.launch {
            _state.value = PostState.Loading
            try {
                val token = repository.token.first() ?: return@launch
                val res = repository.getPosts(token)
                if (res.isSuccessful) {
                    _posts.value = res.body() ?: emptyList()
                    _state.value = PostState.Success
                } else {
                    _state.value = PostState.Error("Failed to load feed")
                }
            } catch (e: Exception) {
                _state.value = PostState.Error(e.message ?: "Error")
            }
        }
    }
    
    fun getMyPosts() {
        viewModelScope.launch {
            val token = repository.token.first() ?: return@launch
            val userIdStr = repository.currentUserId.first() ?: return@launch
            val userId = userIdStr.toIntOrNull() ?: return@launch
            
            try {
                val res = repository.getUserPosts(token, userId)
                if (res.isSuccessful) {
                    _myPosts.value = res.body() ?: emptyList()
                }
            } catch (e: Exception) {}
        }
    }
    
    fun getProfile() {
        viewModelScope.launch {
            val token = repository.token.first() ?: return@launch
            try {
                val res = repository.getMe(token)
                if (res.isSuccessful) {
                    _currentUser.value = res.body()
                }
            } catch (e: Exception) {}
        }
    }

    fun createPost(desc: String, file: File) {
        viewModelScope.launch {
            _state.value = PostState.Loading
            try {
                val token = repository.token.first() ?: return@launch
                val res = repository.createPost(token, desc, file)
                if (res.isSuccessful) {
                    getFeed()
                    _state.value = PostState.Success
                } else {
                    _state.value = PostState.Error("Error uploading")
                }
            } catch (e: Exception) {
                _state.value = PostState.Error(e.message ?: "Error")
            }
        }
    }
    
    fun deletePost(id: Int) {
        viewModelScope.launch {
            val token = repository.token.first() ?: return@launch
            repository.deletePost(token, id)
            getMyPosts()
            getFeed()
        }
    }
    
    fun updateProfile(bio: String, avatar: File?) {
        viewModelScope.launch {
            _state.value = PostState.Loading
            try {
                val token = repository.token.first() ?: return@launch
                repository.updateProfile(token, bio)
                if (avatar != null) {
                    repository.updateAvatar(token, avatar)
                }
                getProfile()
                _state.value = PostState.Success
            } catch (e: Exception) {
                _state.value = PostState.Error(e.message ?: "Error")
            }
        }
    }
    
    fun resetState() { _state.value = PostState.Idle }
}