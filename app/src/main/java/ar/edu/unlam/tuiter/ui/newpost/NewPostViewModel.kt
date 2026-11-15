package ar.edu.unlam.tuiter.ui.newpost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.tuiter.data.error.ErrorMapper
import ar.edu.unlam.tuiter.data.repository.DraftRepository
import ar.edu.unlam.tuiter.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NewPostUiState(
    val message: String = "",
    val isPublishing: Boolean = false,
    val error: String? = null,
    val published: Boolean = false,
    val draftStatus: String? = null
)

class NewPostViewModel(
    // DI (Paso 14): los repositorios llegan por constructor.
    // Antes se creaban acá adentro y hacía falta el Application para Room.
    private val postRepository: PostRepository,
    private val draftRepository: DraftRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NewPostUiState())
    val state: StateFlow<NewPostUiState> = _state.asStateFlow()

    init {
        // Al crear el ViewModel, intentamos recuperar un borrador guardado.
        loadDraft()
    }

    private fun loadDraft() {
        viewModelScope.launch {
            try {
                val draft = draftRepository.getDraft()
                if (!draft.isNullOrBlank()) {
                    _state.value = _state.value.copy(
                        message = draft,
                        draftStatus = "Borrador recuperado"
                    )
                }
            } catch (e: Exception) {
                // Si falla la lectura, seguimos con el campo vacío.
            }
        }
    }


        // Se llama cada vez que el usuario escribe en el campo de texto.
        fun onMessageChange(value: String) {
            _state.value = _state.value.copy(message = value, error = null, draftStatus = null)
        }

        fun saveDraft() {
            val text = _state.value.message
            if (text.isBlank()) return

            viewModelScope.launch {
                try {
                    draftRepository.saveDraft(text)
                    _state.value = _state.value.copy(draftStatus = "Borrador guardado")
                } catch (e: Exception) {
                    _state.value =
                        _state.value.copy(error = "No se pudo guardar el borrador: ${e.message}")
                }
            }
        }

        fun publish() {
            val text = _state.value.message.trim()

            // No publicamos si está vacío o si ya se está publicando.
            if (text.isEmpty() || _state.value.isPublishing) return

            _state.value = _state.value.copy(isPublishing = true, error = null, draftStatus = null)

            viewModelScope.launch {
                try {
                    val ok = postRepository.createPost(text)
                    // Si se publicó, el borrador local ya no es necesario.
                    if (ok) {
                        draftRepository.clearDraft()
                        _state.value = _state.value.copy(isPublishing = false, published = true, message = "", draftStatus = null)
                    } else {
                        _state.value = _state.value.copy(
                            isPublishing = false,
                            error = "No se pudo publicar"
                        )
                    }
                } catch (e: Exception) {
                    // Paso 16: mensaje amigable al publicar (sin "Error 401: ...").
                    _state.value = _state.value.copy(
                        isPublishing = false,
                        error = ErrorMapper.toUserMessage(e)
                    )
                }
            }
        }

        // La pantalla la usa para avisar que ya navegó y no volver a hacerlo.
        fun consumePublished() {
            _state.value = _state.value.copy(published = false)
        }
    }
