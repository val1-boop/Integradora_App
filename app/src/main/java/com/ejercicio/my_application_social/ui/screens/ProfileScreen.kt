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
import com.ejercicio.my_application_social.data.model.User
import com.ejercicio.my_application_social.ui.components.PrimaryButton
import com.ejercicio.my_application_social.ui.theme.PhotoFeedTheme
import com.ejercicio.my_application_social.ui.viewmodel.AuthViewModel
import com.ejercicio.my_application_social.ui.viewmodel.PostViewModel

// Stateful
@Composable
fun ProfileScreen(nav: NavController, viewModel: PostViewModel, authViewModel: AuthViewModel) {
    val user by viewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getProfile()
    }

    ProfileContent(
        user = user,
        onBackClick = { nav.popBackStack() },
        onMyPostsClick = { nav.navigate("my_posts") },
        onLogoutClick = {
            authViewModel.logout()
            nav.navigate("login") { popUpTo(0) { inclusive = true } }
        }
    )
}

// Stateless
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    user: User?,
    onBackClick: () -> Unit,
    onMyPostsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (user != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("http://10.0.2.2:8000${user.avatar_url}")
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(16.dp))
                Text("@${user.username}", style = MaterialTheme.typography.headlineSmall)
                Text(user.name, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp))
                Text(user.bio ?: "Sin descripción", style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(32.dp))

                PrimaryButton(text = "Ver Mis Publicaciones", onClick = onMyPostsClick)
                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar Sesión")
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

// Preview
@Preview
@Composable
fun ProfilePreview() {
    val dummyUser = User(
        1, "Juan Pérez", "juanp", "juan@test.com", "Amante de la fotografía", null
    )
    PhotoFeedTheme {
        ProfileContent(user = dummyUser, onBackClick = {}, onMyPostsClick = {}, onLogoutClick = {})
    }
}