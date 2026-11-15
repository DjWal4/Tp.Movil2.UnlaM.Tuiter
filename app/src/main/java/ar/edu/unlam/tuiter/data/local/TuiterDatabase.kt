package ar.edu.unlam.tuiter.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(
    // Ahora hay DOS tablas: borradores (R3) y usuarios favoritos (R6).
    entities = [DraftEntity::class, FavoriteUserEntity::class],
    // Subimos de 1 a 2 porque cambió el esquema (tabla nueva).
    version = 2,
    exportSchema = false
)
abstract class TuiterDatabase : RoomDatabase() {

    abstract fun draftDao(): DraftDao

    // DAO nuevo para la tabla de favoritos.
    abstract fun favoriteUserDao(): FavoriteUserDao

    companion object {
        @Volatile
        private var INSTANCE: TuiterDatabase? = null

        // MIGRACIÓN 1 -> 2.
        // Room 3 usa la API nueva: migrate(connection: SQLiteConnection)
        // y la extensión execSQL() para ejecutar SQL.
        //
        // ¿Por qué una migración y no "fallbackToDestructiveMigration"?
        // Porque destruir la base borraría el borrador del usuario (R3).
        // Con esta migración, solo AGREGAMOS la tabla nueva y conservamos drafts.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override suspend fun migrate(connection: SQLiteConnection) {
                // La tabla debe coincidir con FavoriteUserEntity:
                // authorName TEXT NOT NULL (PK) y avatarUrl TEXT (nullable).
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `favorite_users` (" +
                        "`authorName` TEXT NOT NULL, " +
                        "`avatarUrl` TEXT, " +
                        "PRIMARY KEY(`authorName`))"
                )
            }
        }

        fun getInstance(context: Context): TuiterDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TuiterDatabase::class.java,
                    "tuiter.db"
                )
                    // Registramos la migración para que Room sepa cómo pasar de 1 a 2.
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
