package ar.edu.unlam.tuiter.ui.register

import android.util.Patterns
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
// REGISTRO (consigna R4 / HU-04)
// ============================================================
data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val registered: Boolean = false
) {
    // HU-04 escenario 2: si falta algún campo, el botón queda deshabilitado.
    val canSubmit: Boolean
        get() = name.isNotBlank() && email.isNotBlank() &&
            password.isNotBlank() && !isLoading

    // HU-04 escenario 3: el email tiene que tener formato válido.
    // Patterns.EMAIL_ADDRESS es el validador que trae Android.
    // Solo mostramos el error si el usuario ya escribió algo.
    val emailError: String?
        get() = if (email.isNotBlank() &&
            !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
        ) {
            "Ingresá un email válido (ejemplo: nombre@dominio.com)."
        } else {
            null
        }
}

class RegisterViewModel(
    // DI: el repositorio llega por constructor desde AppContainer.
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onNameChange(value: String) {
        _state.value = _state.value.copy(name = value, error = null)
    }

    fun onEmailChange(value: String) {
        _state.value = _state.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _state.value = _state.value.copy(password = value, error = null)
    }

    // Se llama al tocar "Registrarme".
    fun register() {
        val current = _state.value
        if (!current.canSubmit) return

        // No llamamos a la API si el email está mal formado.
        if (current.emailError != null) return

        _state.value = current.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // POST /users con nombre, email y contraseña.
                authRepository.register(
                    name = current.name.trim(),
                    email = current.email.trim(),
                    password = current.password
                )
                // Éxito: la pantalla avisa y vuelve al login.
                _state.value = _state.value.copy(isLoading = false, registered = true)
            } catch (e: HttpException) {
                // Por ejemplo 409 (email ya registrado) o 422 (datos inválidos).
                // Los datos del formulario quedan como estaban (HU-04 escenario 4).
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = ErrorMapper.toUserMessage(e)
                )
            } catch (e: Exception) {
                // Sin conexión, timeout, etc.
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = ErrorMapper.toUserMessage(e)
                )
            }
        }
    }
}
