package com.ejercicio.my_application_social.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ejercicio.my_application_social.data.model.Post
import java.io.File

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, isLoading: Boolean = false) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        enabled = !isLoading
    ) {
        if (isLoading) CircularProgressIndicator(color = Color.White)
        else Text(text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCard(post: Post, isMine: Boolean = false, onDelete: () -> Unit = {}, onEdit: () -> Unit = {}) {
    var showFullImage by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(
                text = "@${post.username.takeIf { it.isNotBlank() } ?: "usuario"}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            
            if (post.media_url.isNotBlank()) {
                val context = LocalContext.current
                val model = try {
                    if (File(post.media_url).exists()) {
                        File(post.media_url)
                    } else {
                        post.media_url
                    }
                } catch (e: Exception) {
                    post.media_url
                }
                
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(model)
                        .size(1080, 1080)
                        .crossfade(false)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clickable { showFullImage = true },
                    contentScale = ContentScale.Crop,
                    error = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery),
                    placeholder = androidx.compose.ui.res.painterResource(android.R.drawable.ic_menu_gallery)
                )
            }
            
            Spacer(Modifier.height(8.dp))
            Text(text = post.description.takeIf { it.isNotBlank() } ?: "Sin descripción")
            
            if (isMine) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onEdit) { Text("Editar") }
                    TextButton(onClick = onDelete) { Text("Eliminar", color = Color.Red) }
                }
            }
        }
    }
    
    if (showFullImage) {
        Dialog(onDismissRequest = { showFullImage = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showFullImage = false },
                contentAlignment = Alignment.Center
            ) {
                LocalContext.current
                val model = try {
                    if (File(post.media_url).exists()) File(post.media_url) else post.media_url
                } catch (e: Exception) {
                    post.media_url
                }
                
                AsyncImage(
                    model = model,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}