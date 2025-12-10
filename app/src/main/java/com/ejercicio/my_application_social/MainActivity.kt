package com.ejercicio.my_application_social

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.ejercicio.my_application_social.data.api.NetworkModule
import com.ejercicio.my_application_social.data.repository.Repository
import com.ejercicio.my_application_social.navigation.AppNavigation
import com.ejercicio.my_application_social.ui.theme.PhotoFeedTheme
import com.ejercicio.my_application_social.ui.viewmodel.AuthViewModel
import com.ejercicio.my_application_social.ui.viewmodel.PostViewModel

class MainActivity : ComponentActivity(), ImageLoaderFactory {
    
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.10)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(20 * 1024 * 1024)
                    .build()
            }
            .networkCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false)
            .crossfade(false)
            .allowHardware(false)
            .build()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = Repository(
            apiService = NetworkModule.apiService,
            context = applicationContext
        )
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