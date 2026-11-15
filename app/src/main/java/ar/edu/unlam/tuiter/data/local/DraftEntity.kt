package ar.edu.unlam.tuiter.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

// Tabla "drafts": guarda un único borrador (id fijo = 1).
@Entity(tableName = "drafts")
data class DraftEntity(
    @PrimaryKey val id: Int = 1,
    val message: String
)