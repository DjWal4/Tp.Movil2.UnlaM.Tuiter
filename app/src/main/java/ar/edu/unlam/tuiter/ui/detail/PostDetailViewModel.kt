package ar.edu.unlam.tuiter.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.tuiter.data.error.ErrorMapper
import ar.edu.unlam.tuiter.data.model.Post
import ar.edu.unlam.tuiter.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


data class PostDetailUiState(
    val post: Post? = null,
    val replies: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isReplying: Boolean = false,
    val replyText: String = "",
    val error: String? = null
)

class PostDetailViewModel(
    private val postId: Int,
    // DI (Paso 14): el repositorio llega por constructor, no se crea acá.
    private val repository: PostRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PostDetailUiState())
    val state: StateFlow<PostDetailUiState> = _state.asStateFlow()

    // Carga el post padre y sus respuestas.
    fun load() {
        if (_state.value.isLoading) return

        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val post = repository.getPost(postId)
                val replies = repository.getReplies(postId)

                _state.value = _state.value.copy(
                    post = post,
                    replies = replies,
                    isLoading = false
                )
            } catch (e: Exception) {
                // Paso 16: mensaje amigable, sin códigos HTTP crudos.
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = ErrorMapper.toUserMessage(e)
                )
            }
        }
    }

    fun onReplyChange(value: String) {
        _state.value = _state.value.copy(replyText = value, error = null)
    }

    // Publica la respuesta y recarga la lista.
    fun sendReply() {
        val text = _state.value.replyText.trim()
        if (text.isEmpty() || _state.value.isReplying) return

        _state.value = _state.value.copy(isReplying = true, error = null)

        viewModelScope.launch {
            try {
                val ok = repository.replyToPost(postId, text)
                if (ok) {
                    _state.value = _state.value.copy(isReplying = false, replyText = "")
                    load() // recargamos para traer la respuesta nueva
                } else {
                    _state.value = _state.value.copy(
                        isReplying = false,
                        error = "No se pudo publicar la respuesta"
                    )
                }
            } catch (e: Exception) {
                // Paso 16: error amigable al publicar la respuesta.
                _state.value = _state.value.copy(
                    isReplying = false,
                    error = ErrorMapper.toUserMessage(e)
                )
            }
        }
    }

    // Like / unlike optimista, igual que en el feed.
    fun toggleLike(post: Post) {
        val id = post.id ?: return

        val current = when {
            _state.value.post?.id == id -> _state.value.post!!
            else -> _state.value.replies.firstOrNull { it.id == id } ?: return
        }

        val wasLiked = current.liked == true
        val oldLikes = current.likes ?: 0
        val newLiked = !wasLiked
        val newLikes = (oldLikes + if (newLiked) 1 else -1).coerceAtLeast(0)

        updateLike(id, newLiked, newLikes)

        viewModelScope.launch {
            try {
                val ok = if (newLiked) {
                    repository.likePost(id)
                } else {
                    repository.unlikePost(id)
                }

                if (!ok) {
                    updateLike(id, wasLiked, oldLikes)
                    _state.value = _state.value.copy(error = "No se pudo actualizar el like")
                }
            } catch (e: Exception) {
                updateLike(id, wasLiked, oldLikes)
                // Paso 16: mensaje amigable al fallar el like.
                _state.value = _state.value.copy(error = ErrorMapper.toUserMessage(e))
            }
        }
    }

    private fun updateLike(id: Int, liked: Boolean, likes: Int) {
        val s = _state.value
        _state.value = s.copy(
            post = s.post?.let { if (it.id == id) it.copy(liked = liked, likes = likes) else it },
            replies = s.replies.map { if (it.id == id) it.copy(liked = liked, likes = likes) else it }
        )
    }
}