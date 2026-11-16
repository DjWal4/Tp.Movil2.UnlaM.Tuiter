package ar.edu.unlam.tuiter.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.tuiter.data.error.ErrorMapper
import ar.edu.unlam.tuiter.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

// ============================================================
// PERFIL: VER Y EDITAR (consigna R5 / HU-06)
// ============================================================
data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    // Contraseña nueva. Si queda vacía, NO se cambia (así el usuario
    // puede editar el nombre sin tener que escribir la contraseña).
    val password: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    // Indica si ya se cargaron los datos al menos una vez.
    val loaded: Boolean = false
) {
    // El botón "Guardar" necesita al menos un nombre.
    val canSave: Boolean
        get() = name.isNotBlank() && !isSaving && !isLoading
}

class ProfileViewModel(
    // DI: el repositorio llega por constructor desde AppContainer.
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    // Guardamos los datos originales que devolvió la API.
    // Sirven para saber QUÉ cambió el usuario y enviar solo eso en el PUT.
    private var originalName: String = ""
    private var originalAvatarUrl: String = ""

    init {
        // Al abrir la pantalla, pedimos el perfil (GET /me/profile).
        load()
    }

    // Carga (o recarga) el perfil desde la API.
    fun load() {
        if (_state.value.isLoading) return

        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val profile = authRepository.getProfile()
                originalName = profile.name
                originalAvatarUrl = profile.avatarUrl ?: ""

                _state.value = _state.value.copy(
                    name = profile.name,
                    email = profile.email,
                    avatarUrl = profile.avatarUrl ?: "",
                    password = "",
                    isLoading = false,
                    loaded = true
                )
            } catch (e: Exception) {
                // Sin conexión, sesión vencida, etc. (mensaje amigable).
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = ErrorMapper.toUserMessage(e)
                )
            }
        }
    }

    fun onNameChange(value: String) {
        _state.value = _state.value.copy(name = value, error = null, successMessage = null)
    }

    fun onAvatarChange(value: String) {
        _state.value = _state.value.copy(avatarUrl = value, error = null, successMessage = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, error = null, successMessage = null)
    }

    // Se llama al tocar "Guardar" (PUT /me/profile).
    fun save() {
        val current = _state.value
        if (!current.canSave) return

        // IMPORTANTE (corregido durante la verificación en el celular):
        // aunque la documentación muestra los tres campos como opcionales,
        // el servidor valida `avatar_url` como OBLIGATORIO y responde
        // 500 si no llega. Por eso ahora mandamos SIEMPRE el nombre y el
        // avatar; la contraseña sigue siendo opcional.
        val newName = current.name.trim()

        // Si el campo de avatar quedó vacío, no queremos borrar la imagen:
        // mandamos la que ya tenía. Y si el perfil nunca tuvo avatar,
        // generamos uno con el nombre para que la API no lo rechace.
        val fallbackAvatar =
            "https://ui-avatars.com/api/?name=" +
                java.net.URLEncoder.encode(newName, "UTF-8")
        val newAvatar = current.avatarUrl.trim()
            .ifBlank { originalAvatarUrl.ifBlank { fallbackAvatar } }

        // La contraseña solo se envía si el usuario escribió una nueva.
        val passwordToSend = current.password.takeIf { it.isNotBlank() }

        // Si no cambió nada, avisamos y no llamamos a la API.
        val nothingChanged = newName == originalName &&
            newAvatar == originalAvatarUrl &&
            passwordToSend == null
        if (nothingChanged) {
            _state.value = current.copy(successMessage = "No hay cambios para guardar.")
            return
        }

        _state.value = current.copy(isSaving = true, error = null, successMessage = null)

        viewModelScope.launch {
            try {
                authRepository.updateProfile(
                    name = newName,
                    avatarUrl = newAvatar,
                    password = passwordToSend
                )

                // Actualizamos los "originales" para futuras comparaciones.
                originalName = newName
                originalAvatarUrl = newAvatar

                _state.value = _state.value.copy(
                    isSaving = false,
                    // La contraseña se limpia del formulario por seguridad.
                    password = "",
                    successMessage = "Perfil actualizado."
                )
            } catch (e: HttpException) {
                // La API rechazó el cambio: el formulario conserva lo escrito.
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = ErrorMapper.toUserMessage(e)
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = ErrorMapper.toUserMessage(e)
                )
            }
        }
    }
}
