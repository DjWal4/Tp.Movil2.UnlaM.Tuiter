package ar.edu.unlam.tuiter.data.error

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

// Traduce los errores técnicos (excepciones) a mensajes claros
// para mostrarle al usuario. Así no aparece "Error 401: ..." crudo.
//
// Lo usan todos los ViewModels (Paso 16), para que el manejo sea uniforme.
object ErrorMapper {

    fun toUserMessage(error: Throwable): String {
        return when (error) {
            // Sin internet / no se encuentra el servidor / conexión rechazada.
            // Van ANTES que IOException porque son subclases de ella.
            is UnknownHostException,
            is ConnectException -> "Sin conexión a internet. Revisá tu red e intentá de nuevo."

            // La conexión existe pero tardó demasiado.
            is SocketTimeoutException -> "La conexión tardó demasiado. Intentá de nuevo."

            // Cualquier otro problema de red.
            is IOException -> "Sin conexión a internet. Revisá tu red e intentá de nuevo."

            // La API respondió con un código HTTP de error.
            is HttpException -> when (error.code()) {
                400 -> "Los datos enviados no son válidos."
                401 -> "Tu sesión venció. Volvé a iniciar sesión."
                403 -> "No tenés permiso para hacer eso."
                404 -> "No se encontró el contenido. Puede haber sido eliminado."
                in 500..599 -> "El servidor tuvo un problema. Intentá más tarde."
                else -> "Ocurrió un error (código ${error.code()}). Intentá de nuevo."
            }

            // Cualquier otra cosa.
            else -> "Ocurrió un error inesperado. Intentá de nuevo."
        }
    }
}
