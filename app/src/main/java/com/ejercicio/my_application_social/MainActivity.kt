package com.ejercicio.my_application_social

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ejercicio.my_application_social.data.api.ApiService
import com.ejercicio.my_application_social.data.repository.Repository
import com.ejercicio.my_application_social.ui.screens.*
import com.ejercicio.my_application_social.ui.theme.PhotoFeedTheme
import com.ejercicio.my_application_social.ui.viewmodel.AuthViewModel
import com.ejercicio.my_application_social.ui.viewmodel.PostViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DI Manual (Simple)
        val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        
        // --- CAMBIAR IP AQUÍ ---
        // Emulador: 10.0.2.2
        // Físico: IP de tu PC (ej. 192.168.1.X)
        val baseUrl = "http://10.0.2.2:8000/" 
        
        val api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        val repository = Repository(api, applicationContext)

        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AuthViewModel::class.java)) return AuthViewModel(repository) as T
                if (modelClass.isAssignableFrom(PostViewModel::class.java)) return PostViewModel(repository) as T
                throw IllegalArgumentException("Unknown ViewModel")
            }
        }

        val authViewModel: AuthViewModel by viewModels { viewModelFactory }
        val postViewModel: PostViewModel by viewModels { viewModelFactory }

        setContent {
            PhotoFeedTheme {
                AppNavigation(authViewModel, postViewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(authViewModel: AuthViewModel, postViewModel: PostViewModel) {
    val navController = rememberNavController()
    
    // CORRECCIÓN: Obtenemos el token (String?) y verificamos si existe para decidir el destino inicial
    val tokenState = authViewModel.isLoggedIn.collectAsState(initial = null)
    val startDestination = if (!tokenState.value.isNullOrEmpty()) "feed" else "login"

    NavHost(navController, startDestination = startDestination) {
        composable("login") { 
            LoginScreen(navController, authViewModel) 
        }
        composable("register") { 
            RegisterScreen(navController, authViewModel) 
        }
        composable("feed") { 
            FeedScreen(navController, postViewModel) 
        }
        composable("create_post") { 
            CreatePostScreen(navController, postViewModel) 
        }
        composable("my_posts") { 
            MyPostsScreen(navController, postViewModel) 
        }
        composable("profile") { 
            ProfileScreen(navController, postViewModel, authViewModel) 
        }
    }
}