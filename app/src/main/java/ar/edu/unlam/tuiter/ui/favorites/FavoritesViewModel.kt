package ar.edu.unlam.tuiter.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.tuiter.data.error.ErrorMapper
import ar.edu.unlam.tuiter.data.local.FavoriteUserEntity
import ar.edu.unlam.tuiter.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado que observan las pantallas (el feed y la pantalla de favoritos).
data class FavoritesUiState(
    // Lista completa, para dibujar la pantalla de favoritos.
    val favorites: List<FavoriteUserEntity> = emptyList(),
    // Set de nombres, para preguntar rápido "¿este autor es favorito?"
    // (buscar en un Set es más rápido que recorrer la lista).
    val favoriteNames: Set<String> = emptySet(),
    // Último error, si hubo.
    val error: String? = null
)

// ViewModel normal (ya no necesita Application): las dependencias
// llegan desde AppContainer (DI, Paso 14).
class FavoritesViewModel(
    // DI (Paso 14): el repositorio llega por constructor, no se crea acá.
    private val repository: FavoriteRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FavoritesUiState())
    val state: StateFlow<FavoritesUiState> = _state.asStateFlow()

    init {
        // Observer: nos quedamos escuchando el Flow de Room.
        // Cada vez que se agrega o quita un favorito, esto se ejecuta solo.
        viewModelScope.launch {
            try {
                repository.observeFavorites().collect { lista ->
                    _state.value = _state.value.copy(
                        favorites = lista,
                        favoriteNames = lista.map { it.authorName }.toSet()
                    )
                }
            } catch (e: Exception) {
                // Paso 16: mensaje amigable también para errores de la base local.
                _state.value = _state.value.copy(
                    error = "No se pudieron cargar los favoritos. " +
                        ErrorMapper.toUserMessage(e)
                )
            }
        }
    }

    // Agrega o quita un usuario según cómo esté ahora.
    // La usa la estrella ☆/★ del feed.
    fun toggleFavorite(authorName: String?, avatarUrl: String?) {
        // Si el post no trae autor, no hay nada que guardar.
        val name = authorName?.trim()
        if (name.isNullOrEmpty()) return

        // Nos fijamos en el estado actual (no en la base) para decidir.
        val yaEsFavorito = _state.value.favoriteNames.contains(name)

        viewModelScope.launch {
            try {
                if (yaEsFavorito) {
                    repository.removeFavorite(name)
                } else {
                    repository.addFavorite(name, avatarUrl)
                }
            } catch (e: Exception) {
                // Paso 16: mensaje amigable (ErrorMapper).
                _state.value = _state.value.copy(
                    error = "No se pudo actualizar el favorito. " +
                        ErrorMapper.toUserMessage(e)
                )
            }
        }
    }

    // Quita un favorito desde la pantalla de favoritos.
    fun removeFavorite(authorName: String) {
        viewModelScope.launch {
            try {
                repository.removeFavorite(authorName)
            } catch (e: Exception) {
                // Paso 16: mensaje amigable (ErrorMapper).
                _state.value = _state.value.copy(
                    error = "No se pudo quitar el favorito. " +
                        ErrorMapper.toUserMessage(e)
                )
            }
        }
    }
}
