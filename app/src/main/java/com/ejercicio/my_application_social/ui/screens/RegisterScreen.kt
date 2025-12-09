package com.ejercicio.my_application_social.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.ejercicio.my_application_social.ui.components.PrimaryButton
import com.ejercicio.my_application_social.ui.theme.PhotoFeedTheme
import com.ejercicio.my_application_social.ui.viewmodel.AuthState
import com.ejercicio.my_application_social.ui.viewmodel.AuthViewModel

// Stateful
@Composable
fun RegisterScreen(nav: NavController, viewModel: AuthViewModel) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            viewModel.resetState()
            nav.navigate("feed") { popUpTo("login") { inclusive = true } }
        }
    }

    PhotoFeedTheme (useDarkTheme = false) {
        RegisterContent(
            isLoading = state is AuthState.Loading,
            errorMsg = (state as? AuthState.Error)?.msg,
            onRegisterClick = { name, user, email, pass ->
                viewModel.register(name, user, email, pass)
            },
            onBackClick = { nav.popBackStack() }
        )
    }
}

// Stateless
@Composable
fun RegisterContent(
    isLoading: Boolean,
    errorMsg: String?,
    onRegisterClick: (String, String, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))
        Text("Registro", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre real") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Usuario") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = pass2, onValueChange = { pass2 = it }, label = { Text("Repetir contraseña") }, modifier = Modifier.fillMaxWidth())

        if (errorMsg != null) {
            Spacer(Modifier.height(8.dp))
            Text(errorMsg, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "Registrarse",
            onClick = { if (pass == pass2) onRegisterClick(name, username, email, pass) },
            isLoading = isLoading
        )

        TextButton(onClick = onBackClick) {
            Text("Volver al login")
        }
    }
}

// Preview
@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    PhotoFeedTheme(useDarkTheme = false) {
        RegisterContent(isLoading = false, errorMsg = null, onRegisterClick = {_,_,_,_ ->}, onBackClick = {})
    }
}