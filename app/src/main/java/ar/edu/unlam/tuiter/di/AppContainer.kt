package ar.edu.unlam.tuiter.di

import android.content.Context
import ar.edu.unlam.tuiter.data.local.TuiterDatabase
import ar.edu.unlam.tuiter.data.remote.ApiClient
import ar.edu.unlam.tuiter.data.remote.TuiterApi
import ar.edu.unlam.tuiter.data.repository.DraftRepository
import ar.edu.unlam.tuiter.data.repository.FavoriteRepository
import ar.edu.unlam.tuiter.data.repository.PostRepository
import ar.edu.unlam.tuiter.data.repository.SessionRepository

// ============================================================
// CONTENEDOR DE DEPENDENCIAS (Paso 14)
// ============================================================
// "Inyección de dependencias" = en vez de que cada clase cree sus
// dependencias con "new" (por ejemplo PostRepository()), las recibe
// armadas desde afuera. Ese "afuera" es este contenedor.
//
// Ventajas:
//  - Una sola instancia de cada cosa (API, base de datos, repos).
//  - Las clases quedan más fáciles de testear (se les puede pasar un fake).
//  - Si cambia cómo se crea algo, se toca SOLO acá.
//
// Se crea una única vez en TuiterApplication.
class AppContainer(context: Context) {

    // API de Retrofit (una sola para toda la app).
    val api: TuiterApi = ApiClient.api

    // Base de datos Room (TuiterDatabase ya devuelve siempre la misma instancia).
    private val database: TuiterDatabase = TuiterDatabase.getInstance(context)

    // Repositorios: reciben sus dependencias por constructor.
    val postRepository: PostRepository = PostRepository(api)
    val draftRepository: DraftRepository = DraftRepository(database.draftDao())
    val favoriteRepository: FavoriteRepository = FavoriteRepository(database.favoriteUserDao())

    // Repositorio de sesión (Paso 15): usa los otros dos para limpiar la base.
    val sessionRepository: SessionRepository =
        SessionRepository(draftRepository, favoriteRepository)
}
