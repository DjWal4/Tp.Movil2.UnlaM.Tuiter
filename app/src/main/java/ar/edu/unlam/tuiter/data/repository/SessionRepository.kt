package ar.edu.unlam.tuiter.data.repository

import ar.edu.unlam.tuiter.data.session.SessionManager

// Repositorio de sesión (Paso 15).
// Agrupa en un solo lugar todo lo que hay que hacer al "cerrar sesión":
//   1) Borrar los datos locales del usuario (borrador y favoritos).
//   2) Borrar el token de la sesión (memoria + SharedPreferences).
//
// ¿Por qué un repositorio y no hacerlo en la Activity?
// Porque la UI no debería conocer DAOs ni SharedPreferences.
// La Activity solo llama a logout() y listo.
class SessionRepository(
    private val draftRepository: DraftRepository,
    private val favoriteRepository: FavoriteRepository
) {
    // Guarda el token que devuelve el login (R4).
    // La UI no toca SessionManager directamente: le pide esto al repositorio.
    fun saveSession(token: String) {
        SessionManager.saveToken(token)
    }

    // Indica si hay una sesión activa (token guardado en memoria).
    // La usa MainActivity al arrancar para decidir si abre el feed (HU-05 esc. 3).
    fun isLoggedIn(): Boolean = SessionManager.isLoggedIn

    suspend fun logout() {
        // 1) Limpieza de la base local (Room).
        draftRepository.clearDraft()
        favoriteRepository.clearAll()

        // 2) Limpieza del token de sesión.
        SessionManager.clear()
    }
}
