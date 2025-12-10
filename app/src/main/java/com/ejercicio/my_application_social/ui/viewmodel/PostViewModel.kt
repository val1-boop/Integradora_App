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

    fun getFeed() {
        viewModelScope.launch {
            _state.value = PostState.Loading
            try {
                val fetchedPosts = repository.getAllPosts()
                _posts.value = fetchedPosts
                _state.value = PostState.Success
            } catch (e: Exception) {
                _state.value = PostState.Error(e.message ?: "Error al cargar el Feed")
            }
        }
    }

    fun getMyPosts() {
        viewModelScope.launch {
            _state.value = PostState.Loading
            try {
                val userIdStr = repository.currentAuthToken.first() ?: return@launch
                val userId = userIdStr.toIntOrNull() ?: return@launch

                val userPosts = repository.getUserPosts(userId)
                _myPosts.value = userPosts
                _state.value = PostState.Success
            } catch (e: Exception) {
                _state.value = PostState.Error(e.message ?: "Error al cargar mis posts")
            }
        }
    }

    fun getProfile() {
        viewModelScope.launch {
            _state.value = PostState.Loading
            try {
                _currentUser.value = repository.getMe()
                _state.value = PostState.Success
            } catch (e: Exception) {
                _state.value = PostState.Error(e.message ?: "Error al cargar perfil")
            }
        }
    }

    fun getPostById(postId: Int) {
        // Importante: No ponemos Loading global aquí para no bloquear la UI si falla levemente
        viewModelScope.launch {
            try {
                _currentPost.value = repository.getPostById(postId)
            } catch (e: Exception) {
                _state.value = PostState.Error(e.message ?: "Error al cargar el post para editar.")
            }
        }
    }

    fun createPost(desc: String, file: File) {
        viewModelScope.launch {
            _state.value = PostState.Loading
            try {
                val newPost = repository.createPost(desc, file)
                if (newPost != null) {
                    _state.value = PostState.Success
                } else {
                    _state.value = PostState.Error("Error: Post vacío o fallo en el servidor.")
                }
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
                // Refrescamos el post actual en memoria para que se vea el cambio
                _currentPost.value = _currentPost.value?.copy(description = desc)
                getFeed() // Opcional: refrescar feed
                _state.value = PostState.Success
            } catch (e: Exception) {
                _state.value = PostState.Error(e.message ?: "Error al actualizar")
            }
        }
    }

    fun deletePost(id: Int) {
        viewModelScope.launch {
            try {
                repository.deletePost(id)
                getMyPosts()
                getFeed() // Refrescar feed también
            } catch (e: Exception) {
                _state.value = PostState.Error(e.message ?: "Error al eliminar")
            }
        }
    }

    fun resetState() {
        _state.value = PostState.Idle
        // No limpiamos _currentPost aquí para evitar parpadeo al salir de EditScreen
    }
}