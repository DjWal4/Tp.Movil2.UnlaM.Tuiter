package ar.edu.unlam.tuiter.ui.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.unlam.tuiter.ui.rememberAppContainer

// ============================================================
// PANTALLA DE INICIO DE SESIÓN (R4 / HU-05)
// ============================================================
// Reemplaza al viejo "panel de prueba" (que tenía las credenciales
// escritas en el código). Ahora el usuario escribe email y contraseña.
//
// Parámetros:
//  - onLoggedIn:      se llama cuando el login salió bien (navega al feed).
//  - onGoToRegister:  se llama al tocar "Crear cuenta" (navega al registro).
//  - infoMessage:     mensaje opcional (por ejemplo, "Cuenta creada").
@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onGoToRegister: () -> Unit,
    infoMessage: String? = null
) {
    // DI: el contenedor nos da los repositorios ya armados.
    val container = rememberAppContainer()

    // ViewModel con sus dependencias (patrón Repositorio + DI).
    val viewModel: LoginViewModel = viewModel {
        LoginViewModel(container.authRepository, container.sessionRepository)
    }

    // Observer: la UI se redibuja ante cada cambio de estado.
    val state by viewModel.state.collectAsState()

    // Cuando el login termina bien, avisamos a MainActivity para que
    // muestre el feed. Lo hacemos con LaunchedEffect para que la
    // navegación ocurra una sola vez por cambio de estado.
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoggedIn()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // permite scrollear con el teclado abierto
            .imePadding()                          // deja espacio cuando aparece el teclado
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título de la app.
        Text(
            text = "UNLaM Tuiter",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Iniciá sesión para ver el feed",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mensaje informativo (por ejemplo, después de registrarse).
        infoMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Campo de email. keyboardType = Email muestra el teclado con @ y .com
        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Campo de contraseña. PasswordVisualTransformation oculta lo que se escribe.
        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Contraseña") },
            singleLine = true,
            enabled = !state.isLoading,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón principal. Se deshabilita si falta un campo o si está cargando.
        Button(
            onClick = { viewModel.login() },
            enabled = state.canSubmit,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isLoading) {
                // Fila: spinner + texto, mientras se llama a la API.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        // Blanco para que se vea sobre el botón.
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "Ingresando...")
                }
            } else {
                Text("Ingresar")
            }
        }

        // Mensaje de error (credenciales incorrectas, sin conexión, etc.).
        state.error?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Acceso al registro (HU-04 / HU-05 escenario 4).
        TextButton(onClick = onGoToRegister, enabled = !state.isLoading) {
            Text("¿No tenés cuenta? Crear cuenta")
        }
    }
}
