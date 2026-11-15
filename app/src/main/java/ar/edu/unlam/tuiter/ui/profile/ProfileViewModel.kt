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

        // Solo mandamos los campos que cambiaron.
        // Si el nombre quedó igual, se envía null y Gson lo omite del JSON.
        val newName = current.name.trim()
        val newAvatar = current.avatarUrl.trim()

        val nameToSend = if (newName != originalName) newName else null

        // El avatar vacío significa "no lo toques" (evita dejar el perfil sin imagen).
        val avatarToSend =
            if (newAvatar.isNotBlank() && newAvatar != originalAvatarUrl) newAvatar else null

        // La contraseña solo se envía si el usuario escribió una nueva.
        val passwordToSend = current.password.takeIf { it.isNotBlank() }

        // Si no cambió nada, avisamos y no llamamos a la API.
        if (nameToSend == null && avatarToSend == null && passwordToSend == null) {
            _state.value = current.copy(successMessage = "No hay cambios para guardar.")
            return
        }

        _state.value = current.copy(isSaving = true, error = null, successMessage = null)

        viewModelScope.launch {
            try {
                authRepository.updateProfile(
                    name = nameToSend,
                    avatarUrl = avatarToSend,
                    password = passwordToSend
                )

                // Actualizamos los "originales" para futuras comparaciones.
                originalName = newName
                if (avatarToSend != null) originalAvatarUrl = avatarToSend

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
