package com.ejercicio.my_application_social.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ejercicio.my_application_social.ui.screens.CreatePostScreen
import com.ejercicio.my_application_social.ui.screens.EditPostScreen
import com.ejercicio.my_application_social.ui.screens.FeedScreen
import com.ejercicio.my_application_social.ui.screens.LoginScreen
import com.ejercicio.my_application_social.ui.screens.MyPostsScreen
import com.ejercicio.my_application_social.ui.screens.ProfileScreen
import com.ejercicio.my_application_social.ui.screens.RegisterScreen
import com.ejercicio.my_application_social.ui.viewmodel.AuthViewModel
import com.ejercicio.my_application_social.ui.viewmodel.PostViewModel
//nuevo
@Composable
fun AppNavigation(authViewModel: AuthViewModel, postViewModel: PostViewModel) {
    val navController = rememberNavController()

    val tokenState by authViewModel.isLoggedIn.collectAsState(initial = null)
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