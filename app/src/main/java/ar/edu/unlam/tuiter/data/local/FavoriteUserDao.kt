package ar.edu.unlam.tuiter.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

// DAO = Data Access Object: acá van las consultas SQL de la tabla favorite_users.
@Dao
interface FavoriteUserDao {

    // Devuelve un Flow: Room avisa AUTOMÁTICAMENTE cada vez que la tabla cambia.
    // Eso es el patrón Observer aplicado a la base de datos:
    // la pantalla no tiene que "refrescar" a mano, se redibuja sola.
    @Query("SELECT * FROM favorite_users ORDER BY authorName ASC")
    fun observeAll(): Flow<List<FavoriteUserEntity>>

    // Versión de una sola lectura (sin Flow), por si se necesita puntualmente.
    @Query("SELECT * FROM favorite_users ORDER BY authorName ASC")
    suspend fun getAll(): List<FavoriteUserEntity>

    // Inserta el favorito. Si ya existe (mismo authorName), lo reemplaza
    // y así actualiza el avatar sin duplicar filas.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteUserEntity)

    // Quita un favorito buscando por nombre de autor.
    @Query("DELETE FROM favorite_users WHERE authorName = :authorName")
    suspend fun deleteByName(authorName: String)

    // Borra TODOS los favoritos.
    // Lo usa el "cerrar sesión" (Paso 15) para no dejar datos del usuario anterior.
    @Query("DELETE FROM favorite_users")
    suspend fun clearAll()
}
