package com.ejercicio.my_application_social.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ejercicio.my_application_social.ui.components.PrimaryButton
import com.ejercicio.my_application_social.ui.theme.PhotoFeedTheme
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
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var showPermissionDenied by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        } else {
            showPermissionDenied = true
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) imageUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempCameraUri != null) {
            imageUri = tempCameraUri
        }
        tempCameraUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val tempFile = File.createTempFile("camera_img", ".jpg", context.cacheDir)
            tempCameraUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                tempFile
            )
            cameraLauncher.launch(tempCameraUri!!)
        } else {
            showPermissionDenied = true
        }
    }

    LaunchedEffect(state) {
        if (state is PostState.Success) {
            println("[CreatePostScreen] Post creado exitosamente, navegando al feed")
            imageUri = null
            tempCameraUri = null
            description = ""
            System.gc()
            viewModel.resetState()
            nav.popBackStack()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetState()
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Seleccionar imagen") },
            text = { Text("¿Desde dónde quieres agregar la imagen?") },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                            val tempFile = File.createTempFile("camera_img", ".jpg", context.cacheDir)
                            tempCameraUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                tempFile
                            )
                            cameraLauncher.launch(tempCameraUri!!)
                        }
                        else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Text("Cámara")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) -> {
                            galleryLauncher.launch("image/*")
                        }
                        else -> galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }) {
                    Text("Galería")
                }
            }
        )
    }

    if (showPermissionDenied) {
        AlertDialog(
            onDismissRequest = { showPermissionDenied = false },
            title = { Text("Permiso denegado") },
            text = { Text("Se necesita permiso de cámara para tomar fotos. Puedes habilitarlo en Configuración.") },
            confirmButton = {
                TextButton(onClick = { showPermissionDenied = false }) {
                    Text("OK")
                }
            }
        )
    }

    CreatePostContent(
        description = description,
        onDescriptionChange = { description = it },
        imageUri = imageUri,
        onSelectImageClick = { showImageSourceDialog = true },
        isLoading = state is PostState.Loading,
        onPublishClick = {
            imageUri?.let { uri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val file = File.createTempFile("upload", ".jpg", context.cacheDir)
                    val outputStream = FileOutputStream(file)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    
                    viewModel.createPost(description, file)
                    
                    imageUri = null
                    description = ""
                } catch (e: Exception) {
                    println("[CreatePostScreen] Error al procesar imagen: ${e.message}")
                }
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
            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Descripción") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            Spacer(Modifier.height(16.dp))

            OutlinedButton(onClick = onSelectImageClick, modifier = Modifier.fillMaxWidth()) {
                Text(if (imageUri == null) "Agregar Imagen" else "Cambiar Imagen")
            }

            Spacer(Modifier.height(16.dp))

            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            PrimaryButton(
                text = "Publicar",
                onClick = onPublishClick,
                isLoading = isLoading
            )
        }
    }
}

// Preview
@Preview
@Composable
fun CreatePostPreview() {
    PhotoFeedTheme(useDarkTheme = true) {
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