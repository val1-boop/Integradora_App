package com.ejercicio.my_application_social.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.ejercicio.my_application_social.data.model.Post
import com.ejercicio.my_application_social.ui.components.PostCard
import com.ejercicio.my_application_social.ui.theme.PhotoFeedTheme
import com.ejercicio.my_application_social.ui.viewmodel.PostViewModel

// Stateful
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPostsScreen(nav: NavController, viewModel: PostViewModel) {
    val posts by viewModel.myPosts.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getMyPosts()
    }

    MyPostsContent(
        posts = posts,
        onBackClick = { nav.popBackStack() },
        onDeleteClick = { id -> viewModel.deletePost(id) },
        onEditClick = { /* Navegar a editar */ }
    )
}

// Stateless
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPostsContent(
    posts: List<Post>,
    onBackClick: () -> Unit,
    onDeleteClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Publicaciones") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            LazyColumn {
                items(posts) { post ->
                    PostCard(
                        post = post,
                        isMine = true,
                        onDelete = { onDeleteClick(post.id) },
                        onEdit = { onEditClick(post.id) }
                    )
                }
            }
        }
    }
}

// Preview
@Preview
@Composable
fun MyPostsPreview() {
    val dummyPosts = listOf(
        Post(1, 1, "mi_usuario", null, "Mi primera foto", "", "image", "2023-10-20")
    )
    PhotoFeedTheme {
        MyPostsContent(posts = dummyPosts, onBackClick = {}, onDeleteClick = {}, onEditClick = {})
    }
}