package ar.edu.unlam.tuiter.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.unlam.tuiter.ui.components.ErrorView
import ar.edu.unlam.tuiter.ui.components.LoadingView
import ar.edu.unlam.tuiter.ui.rememberAppContainer
import coil.compose.AsyncImage

// ============================================================
// PANTALLA DE PERFIL (R5 / HU-06)
// ============================================================
// Muestra nombre, email y avatar, y permite editarlos.
// El email se ve pero NO se puede modificar (lo pide la consigna).
// También tiene el botón "Salir" (cerrar sesión).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val container = rememberAppContainer()

    val viewModel: ProfileViewModel = viewModel {
        ProfileViewModel(container.authRepository)
    }

    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Volver") }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                // Primera carga: spinner (mismo componente que el resto de la app).
                state.isLoading && !state.loaded -> LoadingView()

                // Error antes de poder mostrar nada: mensaje + Reintentar.
                state.error != null && !state.loaded -> ErrorView(
                    message = state.error ?: "Error desconocido",
                    onRetry = { viewModel.load() }
                )

                else -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- Avatar actual (se actualiza solo si cambia la URL) ---
                    AsyncImage(
                        model = state.avatarUrl.ifBlank { null },
                        contentDescription = "Avatar de ${state.name}",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Text(
                        text = state.name.ifBlank { "Sin nombre" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // --- Nombre (editable) ---
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("Nombre") },
                        singleLine = true,
                        enabled = !state.isSaving,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // --- Email (solo lectura) ---
                    // enabled = false: se ve pero no se puede editar (HU-06 esc. 3).
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { /* no hace nada: el email no se modifica */ },
                        label = { Text("Email") },
                        singleLine = true,
                        enabled = false,
                        supportingText = { Text("El email no se puede modificar.") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // --- URL del avatar (editable) ---
                    OutlinedTextField(
                        value = state.avatarUrl,
                        onValueChange = viewModel::onAvatarChange,
                        label = { Text("URL del avatar") },
                        singleLine = true,
                        enabled = !state.isSaving,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // --- Contraseña nueva (opcional) ---
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Contraseña nueva (opcional)") },
                        singleLine = true,
                        enabled = !state.isSaving,
                        visualTransformation = PasswordVisualTransformation(),
                        supportingText = { Text("Dejala vacía si no querés cambiarla.") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // --- Botón Guardar ---
                    Button(
                        onClick = { viewModel.save() },
                        enabled = state.canSave,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isSaving) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Guardando...")
                            }
                        } else {
                            Text("Guardar")
                        }
                    }

                    // --- Confirmación (HU-06 esc. 2) ---
                    state.successMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }

                    // --- Error de la API (HU-06 esc. 5) ---
                    // Los datos que escribió el usuario siguen en el formulario.
                    state.error?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()

                    // --- Cerrar sesión (funcionalidad opcional de la consigna) ---
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Salir")
                    }
                    Text(
                        text = "Salir borra el borrador, los favoritos y la sesión guardada.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
