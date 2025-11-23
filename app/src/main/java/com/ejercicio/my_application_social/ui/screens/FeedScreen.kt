package com.ejercicio.my_application_social.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
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
fun FeedScreen(nav: NavController, viewModel: PostViewModel) {
    val posts by viewModel.posts.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getFeed()
    }

    FeedContent(
        posts = posts,
        onCreatePostClick = { nav.navigate("create_post") },
        onProfileClick = { nav.navigate("profile") }
    )
}

// Stateless
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedContent(
    posts: List<Post>,
    onCreatePostClick: () -> Unit,
    onProfileClick: () -> Unit
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
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            LazyColumn {
                items(posts) { post ->
                    PostCard(post = post)
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
        Post(1, 1, "usuario_demo", null, "Esta es una descripción de prueba", "", "image", "2023-10-20"),
        Post(2, 2, "otro_user", null, "Foto increíble!", "", "image", "2023-10-21")
    )
    PhotoFeedTheme {
        FeedContent(posts = dummyPosts, onCreatePostClick = {}, onProfileClick = {})
    }
}