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
import com.ejercicio.my_application_social.data.db.AppDatabase
import com.ejercicio.my_application_social.data.repository.Repository
import com.ejercicio.my_application_social.ui.screens.*
import com.ejercicio.my_application_social.ui.theme.PhotoFeedTheme
import com.ejercicio.my_application_social.ui.viewmodel.AuthViewModel
import com.ejercicio.my_application_social.ui.viewmodel.PostViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializamos la base de datos Room (Local)
        val db = AppDatabase.getDatabase(applicationContext)
        
        // 2. Inicializamos el repositorio con la BD local
        val repository = Repository(db, applicationContext)

        // 3. Configuración de ViewModels
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
    
    // isLoggedIn es Flow<String?> (user ID local)
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