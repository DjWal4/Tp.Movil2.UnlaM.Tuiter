package ar.edu.unlam.tuiter.data.repository

import ar.edu.unlam.tuiter.data.local.FavoriteUserDao
import ar.edu.unlam.tuiter.data.local.FavoriteUserEntity
import kotlinx.coroutines.flow.Flow

// Patrón Repositorio para los usuarios favoritos (R6).
// El ViewModel no habla directo con Room: pasa siempre por esta clase.
// Si mañana cambia el origen de datos (por ejemplo, favoritos en la nube),
// solo se toca este archivo.
class FavoriteRepository(private val favoriteUserDao: FavoriteUserDao) {

    // Flow con la lista de favoritos; emite de nuevo cuando cambia la tabla.
    fun observeFavorites(): Flow<List<FavoriteUserEntity>> = favoriteUserDao.observeAll()

    // Lectura puntual (no reactiva).
    suspend fun getFavorites(): List<FavoriteUserEntity> = favoriteUserDao.getAll()

    // Agrega (o actualiza) un usuario favorito.
    suspend fun addFavorite(authorName: String, avatarUrl: String?) {
        favoriteUserDao.insert(
            FavoriteUserEntity(authorName = authorName, avatarUrl = avatarUrl)
        )
    }

    // Quita un usuario favorito por su nombre.
    suspend fun removeFavorite(authorName: String) {
        favoriteUserDao.deleteByName(authorName)
    }

    // Borra todos los favoritos (al cerrar sesión, Paso 15).
    suspend fun clearAll() {
        favoriteUserDao.clearAll()
    }
}
