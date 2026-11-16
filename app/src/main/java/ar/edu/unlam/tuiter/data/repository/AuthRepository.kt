package ar.edu.unlam.tuiter.data.repository

import ar.edu.unlam.tuiter.data.model.LoginRequest
import ar.edu.unlam.tuiter.data.model.LoginResponse
import ar.edu.unlam.tuiter.data.model.ProfileResponse
import ar.edu.unlam.tuiter.data.model.RegisterRequest
import ar.edu.unlam.tuiter.data.model.UpdateProfileRequest
import ar.edu.unlam.tuiter.data.remote.TuiterApi
import retrofit2.HttpException

// ============================================================
// AuthRepository (consigna R4 y R5)
// ============================================================
// Patrón Repositorio: la UI y los ViewModels NO conocen Retrofit.
// Todo el acceso a los endpoints de usuarios pasa por acá.
//
// Si mañana cambia la API (por ejemplo, otra ruta o un campo más),
// se modifica SOLO este archivo.
//
// DI: la API se recibe por constructor (la provee AppContainer).
class AuthRepository(private val api: TuiterApi) {

    // Inicia sesión. Devuelve el nombre, el email y el TOKEN de usuario.
    // Si la API responde 401 (credenciales incorrectas), Retrofit lanza
    // HttpException y el ViewModel muestra el mensaje correspondiente.
    suspend fun login(email: String, password: String): LoginResponse {
        return api.login(LoginRequest(email = email, password = password))
    }

    // Registra un usuario nuevo (POST /users).
    // Si la API responde con error (por ejemplo, email ya registrado),
    // lanzamos HttpException para que el ViewModel muestre un mensaje claro
    // en lugar de "no pasó nada".
    suspend fun register(name: String, email: String, password: String): Boolean {
        val response = api.register(
            RegisterRequest(name = name, email = email, password = password)
        )
        if (!response.isSuccessful) throw HttpException(response)
        return true
    }

    // Trae los datos del perfil del usuario logueado.
    suspend fun getProfile(): ProfileResponse {
        return api.getProfile()
    }

    // Edita el perfil (PUT /me/profile).
    // Los campos son opcionales en Kotlin (los null se omiten del JSON),
    // pero la API exige `name` y `avatar_url`: el ProfileViewModel siempre
    // manda los dos y agrega `password` solo si el usuario escribió una nueva.
    // Devuelve true si la API confirmó el cambio.
    suspend fun updateProfile(
        name: String? = null,
        avatarUrl: String? = null,
        password: String? = null
    ): Boolean {
        val response = api.updateProfile(
            UpdateProfileRequest(
                name = name,
                avatarUrl = avatarUrl,
                password = password
            )
        )
        if (!response.isSuccessful) throw HttpException(response)
        return true
    }
}
