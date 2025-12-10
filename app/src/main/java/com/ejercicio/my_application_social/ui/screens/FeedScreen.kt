package com.ejercicio.my_application_social.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.ejercicio.my_application_social.data.model.Post
import com.ejercicio.my_application_social.ui.components.PostCard
import com.ejercicio.my_application_social.ui.theme.PhotoFeedTheme
import com.ejercicio.my_application_social.ui.viewmodel.PostState
import com.ejercicio.my_application_social.ui.viewmodel.PostViewModel

// Stateful
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(nav: NavController, viewModel: PostViewModel) {
    val posts by viewModel.posts.collectAsState()
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var hasError by remember { mutableStateOf(false) }
    var isNavigating by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getFeed()
        isNavigating = false
    }

    LaunchedEffect(state) {
        when (state) {
            is PostState.Error -> {
                hasError = true
                snackbarHostState.showSnackbar(
                    message = (state as PostState.Error).msg,
                    actionLabel = "OK"
                )
                viewModel.resetState()
            }
            is PostState.Success -> {
                hasError = false
            }
            else -> {}
        }
    }

    if (hasError) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Error al cargar el feed")
                Button(onClick = { viewModel.getFeed() }) {
                    Text("Reintentar")
                }
            }
        }
    } else {
        PhotoFeedTheme(useDarkTheme = true) {
            FeedContent(
                posts = posts,
                isLoading = state is PostState.Loading,
                onCreatePostClick = {
                    if (!isNavigating) {
                        isNavigating = true
                        nav.navigate("create_post")
                    }
                },
                onProfileClick = {
                    if (!isNavigating) {
                        isNavigating = true
                        nav.navigate("profile")
                    }
                },
                snackbarHostState = snackbarHostState
            )
        }
    }

}

// Stateless
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedContent(
    posts: List<Post>,
    isLoading: Boolean,
    onCreatePostClick: () -> Unit,
    onProfileClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PhotoFeed") },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreatePostClick,
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else if (posts.isEmpty()) {
                Text("No hay publicaciones, ¡sé el primero!", Modifier.align(Alignment.Center))
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(
                        items = posts.filter { it.id > 0 }.take(20),
                        key = { post -> post.id }
                    ) { post ->
                        key(post.id) {
                            PostCard(post = post)
                        }
                    }
                }
            }
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun FeedPreview() {
    val dummyPosts = listOf(
        Post(
            1,
            1,
            "usuario_demo",
            null,
            "Esta es una descripción de prueba",
            "",
            "image",
            "2023-10-20"
        ),
        Post(2, 2, "otro_user", null, "Foto increíble!", "", "image", "2023-10-21")
    )
    PhotoFeedTheme(useDarkTheme = true) {
        FeedContent(
            posts = dummyPosts,
            isLoading = false,
            onCreatePostClick = {},
            onProfileClick = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}