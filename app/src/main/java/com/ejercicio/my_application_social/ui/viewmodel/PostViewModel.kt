package com.ejercicio.my_application_social.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ejercicio.my_application_social.data.model.Post
import com.ejercicio.my_application_social.data.model.User
import com.ejercicio.my_application_social.data.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first // Mantenemos first() para obtener el token/ID del DataStore
import kotlinx.coroutines.launch
import java.io.File

sealed class PostState {
    object Idle : PostState()
    object Loading : PostState()
    object Success : PostState()
    data class Error(val msg: String) : PostState()
}

class PostViewModel(private val repository: Repository) : ViewModel() {

    // Mantenemos los StateFlow para exponer los datos a la UI.
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

    // --- FUNCIONES DE CONSULTA (GET) ---

    fun getFeed() {
        viewModelScope.launch {
            _state.value = PostState.Loading
            try {
                println("[PostViewModel] Cargando feed...")
                val fetchedPosts = repository.getAllPosts()
                println("[PostViewModel] Feed cargado: ${fetchedPosts.size} posts")
                _posts.value = fetchedPosts
                _state.value = PostState.Success
            } catch (e: Exception) {
                println("[PostViewModel] Error al cargar feed: ${e.message}")
                e.printStackTrace()
                _state.value = PostState.Error(e.message ?: "Error al cargar el Feed")
            }
        }
    }

    fun getMyPosts() {
        viewModelScope.launch {
            _state.value = PostState.Loading
            try {
                // Obtenemos el token/ID del usuario para hacer la consulta
                val userIdStr = repository.currentAuthToken.first() ?: return@launch
                val userId = userIdStr.toIntOrNull() ?: return@launch

                // 🚨 CORRECCIÓN: Llamamos a la función suspend de la API
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
                // 🚨 CORRECCIÓN: Llamamos a getMe() que usa el token de sesión
                _currentUser.value = repository.getMe()
                _state.value = PostState.Success
            } catch (e: Exception) {
                _state.value = PostState.Error(e.message ?: "Error al cargar perfil")
            }
        }
    }
    fun getPostById(postId: Int) {
        viewModelScope.launch {
            _state.value = PostState.Loading
            try {
                // Llama a la función del Repository (que ahora usa la API)
                _currentPost.value = repository.getPostById(postId)
                _state.value = PostState.Idle
            } catch (e: Exception) {
                _state.value = PostState.Error(e.message ?: "Error al cargar el post para editar.")
            }
        }
    }

    // Ya no se usa para la API, los posts se obtienen en los listados
    /* fun getPostById(postId: Int) {
        // ...
    } */

    // --- FUNCIONES DE MUTACIÓN (POST/PUT/DELETE) ---

    fun createPost(desc: String, file: File) {
        viewModelScope.launch {
            _state.value = PostState.Loading
            try {
                println("[PostViewModel] Creando post...")
                val newPost = repository.createPost(desc, file)
                println("[PostViewModel] Post creado: ${newPost?.id}")

                if (newPost != null) {
                    _state.value = PostState.Success
                } else {
                    _state.value = PostState.Error("Error: Post vacío o fallo en el servidor.")
                }
            } catch (e: Exception) {
                println("[PostViewModel] Error al crear post: ${e.message}")
                e.printStackTrace()
                _state.value = PostState.Error(e.message ?: "Error al crear post")
            }
        }
    }

    fun updatePost(id: Int, desc: String) {
        viewModelScope.launch {
            _state.value = PostState.Loading
            try {
                // 🚨 CORRECCIÓN: Llamamos a la función API
                repository.updatePostDescription(id, desc)

                // Opcional: Refrescar el feed después de actualizar
                getFeed()
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
            } catch (e: Exception) {
                _state.value = PostState.Error(e.message ?: "Error al eliminar")
            }
        }
    }

    fun resetState() {
        _state.value = PostState.Idle
        _currentPost.value = null
    }
}