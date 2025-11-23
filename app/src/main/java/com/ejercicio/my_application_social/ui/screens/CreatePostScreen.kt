package com.ejercicio.my_application_social.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ejercicio.my_application_social.ui.components.PrimaryButton
import com.ejercicio.my_application_social.ui.viewmodel.PostState
import com.ejercicio.my_application_social.ui.viewmodel.PostViewModel
import java.io.File
import java.io.FileOutputStream

// Stateful
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(nav: NavController, viewModel: PostViewModel) {
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    LaunchedEffect(state) {
        if (state is PostState.Success) {
            viewModel.resetState()
            nav.popBackStack()
        }
    }

    CreatePostContent(
        description = description,
        onDescriptionChange = { description = it },
        imageUri = imageUri,
        onSelectImageClick = { launcher.launch("image/*") },
        isLoading = state is PostState.Loading,
        onPublishClick = {
            imageUri?.let { uri ->
                // La lógica de conversión de archivo se mantiene aquí o se mueve al VM
                // Por simplicidad en este refactor, la dejamos aquí pero idealmente va a una capa de utilidad
                val inputStream = context.contentResolver.openInputStream(uri)
                val file = File.createTempFile("upload", ".jpg", context.cacheDir)
                val outputStream = FileOutputStream(file)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                
                viewModel.createPost(description, file)
            }
        },
        onBackClick = { nav.popBackStack() }
    )
}

// Stateless
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostContent(
    description: String,
    onDescriptionChange: (String) -> Unit,
    imageUri: Uri?,
    onSelectImageClick: () -> Unit,
    isLoading: Boolean,
    onPublishClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("Crear Publicación") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        // Icono de cerrar o atrás
                        Text("X", modifier = Modifier.padding(start = 12.dp)) 
                    }
                }
            ) 
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth().height(150.dp)
            )
            Spacer(Modifier.height(16.dp))
            
            Button(onClick = onSelectImageClick) {
                Text("Seleccionar Imagen")
            }
            
            if (imageUri != null) {
                AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.height(200.dp))
            }
            
            Spacer(Modifier.height(24.dp))
            
            PrimaryButton(text = "Publicar", onClick = onPublishClick, isLoading = isLoading)
        }
    }
}

// Preview
@Preview
@Composable
fun CreatePostPreview() {
    com.ejercicio.my_application_social.ui.theme.PhotoFeedTheme {
        CreatePostContent(
            description = "Mi nueva foto",
            onDescriptionChange = {},
            imageUri = null,
            onSelectImageClick = {},
            isLoading = false,
            onPublishClick = {},
            onBackClick = {}
        )
    }
}