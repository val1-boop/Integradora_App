package com.ejercicio.my_application_social.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ejercicio.my_application_social.data.model.Post
import com.ejercicio.my_application_social.ui.components.PrimaryButton
import com.ejercicio.my_application_social.ui.theme.PhotoFeedTheme
import com.ejercicio.my_application_social.ui.viewmodel.PostState
import com.ejercicio.my_application_social.ui.viewmodel.PostViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostScreen(nav: NavController, viewModel: PostViewModel, postId: Int) {
    val post by viewModel.currentPost.collectAsState()
    val state by viewModel.state.collectAsState()
    var description by remember { mutableStateOf("") }

    // Reseteamos estado al entrar
    LaunchedEffect(Unit) {
        viewModel.resetState()
        viewModel.getPostById(postId)
    }

    LaunchedEffect(post) {
        post?.let { description = it.description }
    }

    LaunchedEffect(state) {
        if (state is PostState.Success) {
            viewModel.resetState()
            nav.popBackStack()
        }
    }

    PhotoFeedTheme (useDarkTheme = true) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Editar Publicación") },
                    navigationIcon = {
                        IconButton(onClick = { nav.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                )
            }
        ) { padding ->
            if (post != null) {
                Column(
                    Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val model =
                        if (File(post!!.media_url).exists()) File(post!!.media_url) else post!!.media_url

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(model)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )

                    Spacer(Modifier.height(24.dp))

                    PrimaryButton(
                        text = "Guardar Cambios",
                        onClick = { viewModel.updatePost(post!!.id, description) },
                        isLoading = state is PostState.Loading
                    )
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}