package com.ejercicio.my_application_social.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ejercicio.my_application_social.ui.components.PrimaryButton
import com.ejercicio.my_application_social.ui.theme.PhotoFeedTheme
import com.ejercicio.my_application_social.ui.viewmodel.AuthState
import com.ejercicio.my_application_social.ui.viewmodel.AuthViewModel

// 1. Composable con Lógica (Conectado al ViewModel)
@Composable
fun LoginScreen(nav: NavController, viewModel: AuthViewModel) {
    val state by viewModel.state.collectAsState()

    // Manejo de efectos de navegación
    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            viewModel.resetState()
            nav.navigate("feed") { popUpTo("login") { inclusive = true } }
        }
    }

    PhotoFeedTheme (useDarkTheme = false) {
        // Llamamos al contenido visual pasando los datos y eventos
        LoginContent(
            isLoading = state is AuthState.Loading,
            errorMsg = (state as? AuthState.Error)?.msg,
            onLoginClick = { email, pass -> viewModel.login(email, pass) },
            onRegisterClick = { nav.navigate("register") }
        )
    }
}

// 2. Composable Visual (Sin ViewModel, solo UI)
@Composable
fun LoginContent(
    isLoading: Boolean,
    errorMsg: String?,
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("EL XX", fontSize = 32.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMsg != null) {
            Spacer(Modifier.height(8.dp))
            Text(errorMsg, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "Iniciar Sesión",
            onClick = { onLoginClick(email, pass) },
            isLoading = isLoading
        )

        TextButton(onClick = onRegisterClick) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}

// 3. Previews
@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    PhotoFeedTheme(useDarkTheme = false) {
        LoginContent(
            isLoading = false,
            errorMsg = null,
            onLoginClick = { _, _ -> },
            onRegisterClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginLoadingPreview() {
    PhotoFeedTheme {
        LoginContent(
            isLoading = true,
            errorMsg = null,
            onLoginClick = { _, _ -> },
            onRegisterClick = {}
        )
    }
}