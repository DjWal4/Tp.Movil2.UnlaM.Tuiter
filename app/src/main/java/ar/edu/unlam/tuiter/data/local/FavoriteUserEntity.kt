package ar.edu.unlam.tuiter.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

// Tabla "favorite_users": guarda los usuarios marcados como favoritos (R6).
// Es la SEGUNDA tabla de la base local (la primera es "drafts", del borrador).
//
// Usamos el NOMBRE del autor como clave primaria (@PrimaryKey) porque:
//   1) El modelo Post de la API no siempre trae un author_id estable.
//   2) Así el mismo usuario no se puede guardar dos veces.
//
// avatarUrl es opcional (nullable) porque la API puede no devolver avatar.
@Entity(tableName = "favorite_users")
data class FavoriteUserEntity(
    @PrimaryKey val authorName: String,
    val avatarUrl: String? = null
)
