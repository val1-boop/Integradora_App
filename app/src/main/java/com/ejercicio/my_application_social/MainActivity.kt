package com.ejercicio.my_application_social

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.ejercicio.my_application_social.data.api.NetworkModule // 👈 IMPORTANTE: Añadir NetworkModule
import com.ejercicio.my_application_social.data.repository.Repository
import com.ejercicio.my_application_social.ui.screens.*
import com.ejercicio.my_application_social.ui.theme.PhotoFeedTheme
import com.ejercicio.my_application_social.ui.viewmodel.AuthViewModel
import com.ejercicio.my_application_social.ui.viewmodel.PostViewModel

class MainActivity : ComponentActivity(), ImageLoaderFactory {
    
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(30 * 1024 * 1024)
                    .build()
            }
            .networkCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .crossfade(false)
            .build()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ❌ ELIMINADO: val db = AppDatabase.getDatabase(applicationContext)

        // 1. Inicializamos el repositorio con el ApiService y el Context
        val repository = Repository(
            apiService = NetworkModule.apiService, // 👈 USAMOS LA CONEXIÓN DE RED
            context = applicationContext
        )

        // 2. Configuración de ViewModels
        // Usamos la misma fábrica, pero ahora el repositorio inyecta ApiService
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

    // 🚨 CORRECCIÓN: Ahora el Repositorio expone currentAuthToken
    val tokenState by authViewModel.isLoggedIn.collectAsState(initial = null)

    // Si el token no es nulo o vacío, la sesión está activa.
    val startDestination = if (!tokenState.isNullOrEmpty()) "feed" else "login"

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
        composable(
            route = "edit_post/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.IntType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt("postId") ?: 0
            EditPostScreen(navController, postViewModel, postId)
        }
        composable("profile") {
            ProfileScreen(navController, postViewModel, authViewModel)
        }
    }
}