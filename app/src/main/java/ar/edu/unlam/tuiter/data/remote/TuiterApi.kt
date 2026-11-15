package ar.edu.unlam.tuiter.data.remote

import ar.edu.unlam.tuiter.data.model.CreatePostRequest
import ar.edu.unlam.tuiter.data.model.LoginRequest
import ar.edu.unlam.tuiter.data.model.LoginResponse
import ar.edu.unlam.tuiter.data.model.Post
import ar.edu.unlam.tuiter.data.model.ProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TuiterApi {
    // POST /api/v1/login -> devuelve name, email y token de usuario
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // GET /api/v1/me/profile -> devuelve name, avatar_url y email del usuario logueado
    // Necesita el header Authorization (lo agrega el interceptor de ApiClient).
    @GET("me/profile")
    suspend fun getProfile(): ProfileResponse

    // GET /api/v1/me/feed?page=1&only_parents=true
    // page: número de página
    // onlyParents: si es true, trae solo posts principales (sin respuestas)
    // OJO: si la página no tiene posts, la API responde 200 con el body "null".
    // Por eso devolvemos Response<List<Post>> y no List<Post>:
    // así el body puede venir null sin que Retrofit lance una excepción.
    @GET("me/feed")
    suspend fun getFeed(
        @Query("page") page: Int,
        @Query("only_parents") onlyParents: Boolean
    ): Response<List<Post>>

    // POST /api/v1/me/tuits  -> crea un post nuevo
    // Response<Unit> porque no necesitamos el body, solo saber si salió bien.
    @POST("me/tuits")
    suspend fun createPost(@Body request: CreatePostRequest): Response<Unit>

    // Dar like a un post.
    @POST("me/tuits/{id}/likes")
    suspend fun likePost(@Path("id") id: Int): Response<Unit>

    // Quitar el like de un post.
    @DELETE("me/tuits/{id}/likes")
    suspend fun unlikePost(@Path("id") id: Int): Response<Unit>

    // Ver un post puntual (el "padre" de las respuestas).
    @GET("me/tuits/{id}")
    suspend fun getPost(@Path("id") id: Int): Post

    // Ver las respuestas de un post (devuelve un array de posts).
    // OJO: si el post no tiene respuestas, la API responde 200 con el body "null".
    // Response<List<Post>> permite que el body venga null (mismo motivo que getFeed).
    @GET("me/tuits/{id}/replies")
    suspend fun getReplies(@Path("id") id: Int): Response<List<Post>>

    // Publicar una respuesta a un post.
    @POST("me/tuits/{id}/replies")
    suspend fun createReply(
        @Path("id") id: Int,
        @Body request: CreatePostRequest
    ): Response<Unit>
}
