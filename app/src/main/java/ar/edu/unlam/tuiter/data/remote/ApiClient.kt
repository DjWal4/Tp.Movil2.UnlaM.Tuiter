package ar.edu.unlam.tuiter.data.remote

import ar.edu.unlam.tuiter.data.session.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ApiClient {

    // Base de la API. OJO con la barra final: Retrofit la necesita.
    private const val BASE_URL = "https://tuiter.fragua.com.ar/api/v1/"

    // Token de aplicación (el que generaste en la web). NO es el token de usuario.
    private const val APPLICATION_TOKEN = "70ff5833c7c6785f3e52b57dcf86e7ff12f6ad85587d4d9db5c7d768b5eb9714"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            // builder permite agregar headers a la request original.
            val builder = chain.request().newBuilder()
                // Toda request lleva el token de aplicación.
                .addHeader("Application-Token", APPLICATION_TOKEN)

            // Si hay usuario logueado, agregamos también su token.
            SessionManager.userToken?.let { userToken ->
                builder.addHeader("Authorization", userToken)
            }

            // Continúa con la request ya modificada.
            chain.proceed(builder.build())
        }
        .build()

    val api: TuiterApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TuiterApi::class.java)
}