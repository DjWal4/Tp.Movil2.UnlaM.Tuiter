package ar.edu.unlam.tuiter.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.tuiter.data.error.ErrorMapper
import ar.edu.unlam.tuiter.data.model.Post
import ar.edu.unlam.tuiter.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado que la pantalla observa (patrón Observer con StateFlow).
data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val endReached: Boolean = false
)

class FeedViewModel(
    // DI (Paso 14): el repositorio llega por constructor, no se crea acá.
    private val repository: PostRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FeedUiState())
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    fun loadFeed(reset: Boolean = true) {
        // Evita llamadas duplicadas mientras ya está cargando.
        if (_state.value.isLoading) return

        val nextPage = if (reset) 1 else _state.value.page + 1
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val newPosts = repository.getFeed(page = nextPage, onlyParents = true)
                val allPosts = if (reset) newPosts else _state.value.posts + newPosts

                _state.value = _state.value.copy(
                    posts = allPosts,
                    isLoading = false,
                    page = nextPage,
                    endReached = newPosts.isEmpty()
                )
            } catch (e: Exception) {
                // Paso 16: ErrorMapper traduce la excepción a un mensaje claro
                // (sin conexión, sesión vencida, error del servidor, etc.).
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = ErrorMapper.toUserMessage(e)
                )
            }
        }
    }
    // Marca o desmarca un post como "me gusta".
    fun toggleLike(post: Post) {
        val id = post.id ?: return

        // Tomamos la versión más actual del post desde el estado.
        val current = _state.value.posts.firstOrNull { it.id == id } ?: post
        val wasLiked = current.liked == true
        val oldLikes = current.likes ?: 0
        val newLiked = !wasLiked
        val newLikes = (oldLikes + if (newLiked) 1 else -1).coerceAtLeast(0)

        // 1) Actualización optimista: la UI cambia al instante.
        updatePostLike(id, newLiked, newLikes)

        viewModelScope.launch {
            try {
                val ok = if (newLiked) {
                    repository.likePost(id)
                } else {
                    repository.unlikePost(id)
                }

                // 2) Si la API falló, revertimos el cambio.
                if (!ok) {
                    updatePostLike(id, wasLiked, oldLikes)
                    _state.value = _state.value.copy(error = "No se pudo actualizar el like")
                }
            } catch (e: Exception) {
                // Si hubo excepción, deshacemos el cambio optimista.
                updatePostLike(id, wasLiked, oldLikes)
                // Paso 16: mensaje amigable (ErrorMapper), no "Error 500: ...".
                _state.value = _state.value.copy(error = ErrorMapper.toUserMessage(e))
            }
        }
    }

    private fun updatePostLike(id: Int, liked: Boolean, likes: Int) {
        _state.value = _state.value.copy(
            posts = _state.value.posts.map { p ->
                if (p.id == id) p.copy(liked = liked, likes = likes) else p
            }
        )
    }
}