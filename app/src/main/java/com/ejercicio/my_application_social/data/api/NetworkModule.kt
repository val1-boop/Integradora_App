package com.ejercicio.my_application_social.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

// 1. CONFIGURACIÓN DE LA URL BASE
// IMPORTANTE:
// - Para el Emulador de Android Studio (AVD): usa 10.0.2.2
// - Para un Dispositivo Android Físico: usa tu IP local (ej: 192.168.1.5)
private const val BASE_URL = "http://192.168.111.154:5000/"//cambiar direccion de acuerdo a la red en la que se encuentra

/**
 * Objeto Kotlin que inicializa Retrofit y el ApiService.
 */
object NetworkModule {

    // Cliente HTTP para manejar tiempos de espera
    private val okHttpClient = OkHttpClient.Builder()
        // Opcional: Aumentar el tiempo de espera para subidas de archivos grandes (POST/PUT multipart)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Objeto Retrofit configurado para usar Gson para la deserialización JSON.
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * Inicializa tu ApiService usando la configuración de Retrofit.
     * Este es el objeto que inyectarás en tu Repositorio.
     */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}