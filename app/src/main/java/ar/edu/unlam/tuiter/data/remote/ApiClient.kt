package ar.edu.unlam.tuiter.data.remote

// BuildConfig lo genera Gradle automáticamente a partir de app/build.gradle.kts.
// Ahí se define APPLICATION_TOKEN leyendo local.properties (que NO se versiona).
import ar.edu.unlam.tuiter.BuildConfig
import ar.edu.unlam.tuiter.data.session.SessionManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ApiClient {

    // Base de la API. OJO con la barra final: Retrofit la necesita.
    private const val BASE_URL = "https://tuiter.fragua.com.ar/api/v1/"

    // Token de aplicación (el que generaste en la web). NO es el token de usuario.
    //
    // SEGURIDAD (consigna, sección 4): antes estaba escrito acá y se subía al repo.
    // Ahora viene de BuildConfig, que lo lee de local.properties (ignorado por Git).
    // Si falta, queda vacío: la API responderá 401 y la app mostrará un error claro.
    private val APPLICATION_TOKEN: String = BuildConfig.APPLICATION_TOKEN

    // Permite que otras capas (por ejemplo la pantalla de login) avisen
    // si el proyecto quedó sin configurar. No se muestra el token, solo si existe.
    val isConfigured: Boolean
        get() = APPLICATION_TOKEN.isNotBlank()

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
