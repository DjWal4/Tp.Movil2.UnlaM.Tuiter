package ar.edu.unlam.tuiter.data.repository

import ar.edu.unlam.tuiter.data.model.CreatePostRequest
import ar.edu.unlam.tuiter.data.model.Post
import ar.edu.unlam.tuiter.data.remote.TuiterApi
import retrofit2.HttpException


// Patrón Repositorio: la UI/ViewModel no habla directo con Retrofit,
// sino con esta clase. Si mañana cambia la API, se toca acá.
//
// DI (Paso 14): la API ya NO se busca acá adentro con ApiClient.api.
// Ahora se RECIBE por constructor ("inyección de dependencias").
// Quien la pasa es AppContainer, y así este repositorio se puede
// reemplazar por un doble de prueba (fake) en los tests.
class PostRepository(private val api: TuiterApi) {
    // Si la página no tiene posts, la API devuelve "null" ->
    // body() es null -> lo convertimos en lista vacía.
    //
    // Paso 16: si la respuesta NO es 2xx (401, 500, etc.), Retrofit NO lanza
    // excepción por su cuenta: devuelve el Response con isSuccessful = false.
    // Antes eso se veía como "feed vacío". Ahora lanzamos HttpException
    // para que el ViewModel lo detecte y muestre un error de verdad.
    suspend fun getFeed(page: Int, onlyParents: Boolean = true): List<Post> {
        val response = api.getFeed(page = page, onlyParents = onlyParents)
        if (!response.isSuccessful) throw HttpException(response)
        return response.body() ?: emptyList()
    }

    // Devuelve true si la API respondió 2xx (post creado).
    suspend fun createPost(message: String): Boolean {
        return api.createPost(CreatePostRequest(message)).isSuccessful
    }

    // Devuelve true si la API respondió 2xx.
    suspend fun likePost(id: Int): Boolean {
        return api.likePost(id).isSuccessful
    }

    suspend fun unlikePost(id: Int): Boolean {
        return api.unlikePost(id).isSuccessful
    }

    // Trae un post puntual.
    suspend fun getPost(id: Int): Post {
        return api.getPost(id)
    }

    // Trae las respuestas de un post.
    // Si el post no tiene respuestas, la API devuelve "null" ->
    // body() es null -> lo convertimos en lista vacía.
    // Paso 16: mismo criterio que getFeed (error HTTP -> excepción).
    suspend fun getReplies(id: Int): List<Post> {
        val response = api.getReplies(id)
        if (!response.isSuccessful) throw HttpException(response)
        return response.body() ?: emptyList()
    }

    // Publica una respuesta dentro de un post.
    suspend fun replyToPost(id: Int, message: String): Boolean {
        return api.createReply(id, CreatePostRequest(message)).isSuccessful
    }
}
