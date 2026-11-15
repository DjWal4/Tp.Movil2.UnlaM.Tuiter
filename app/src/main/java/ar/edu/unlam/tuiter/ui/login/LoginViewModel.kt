package ar.edu.unlam.tuiter.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.unlam.tuiter.data.error.ErrorMapper
import ar.edu.unlam.tuiter.data.remote.ApiClient
import ar.edu.unlam.tuiter.data.repository.AuthRepository
import ar.edu.unlam.tuiter.data.repository.SessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

// ============================================================
// LOGIN (consigna R4 / HU-05)
// ============================================================
// Estado que observa la pantalla (patrón Observer con StateFlow).
// Cada vez que cambia, Compose vuelve a dibujar la UI.
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
) {
    // El botón "Ingresar" solo se habilita si los dos campos tienen algo
    // y no hay un login en curso (así no se manda dos veces).
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}

class LoginViewModel(
    // DI: los repositorios llegan por constructor desde AppContainer.
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onEmailChange(value: String) {
        // Al escribir, borramos el error anterior (ya no aplica).
        _state.value = _state.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, error = null)
    }

    // Se llama al tocar "Ingresar".
    fun login() {
        val current = _state.value
        // No hacemos nada si falta algún dato o si ya está cargando.
        if (!current.canSubmit) return

        // Si el proyecto no tiene el Application-Token configurado,
        // avisamos con un mensaje claro en vez de un 401 sin explicación.
        if (!ApiClient.isConfigured) {
            _state.value = current.copy(
                error = "Falta configurar el Application-Token en local.properties. " +
                    "Mirá el README (sección \"Configurar el token\")."
            )
            return
        }

        _state.value = current.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // 1) Pedimos el token de usuario a la API.
                val response = authRepository.login(
                    email = current.email.trim(),
                    password = current.password
                )

                // 2) Lo guardamos en el repositorio de sesión
                //    (memoria + SharedPreferences, así sobrevive al cierre).
                sessionRepository.saveSession(response.token)

                // 3) Avisamos que ya hay sesión: la pantalla navega al feed.
                _state.value = _state.value.copy(isLoading = false, isLoggedIn = true)
            } catch (e: HttpException) {
                // 401 en el login NO es "sesión vencida": son datos incorrectos.
                val message = if (e.code() == 401) {
                    "Email o contraseña incorrectos."
                } else {
                    ErrorMapper.toUserMessage(e)
                }
                _state.value = _state.value.copy(isLoading = false, error = message)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = ErrorMapper.toUserMessage(e)
                )
            }
        }
    }
}
