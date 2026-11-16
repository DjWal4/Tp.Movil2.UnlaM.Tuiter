package ar.edu.unlam.tuiter.ui.newpost

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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.unlam.tuiter.ui.components.TuiterTopBar
import ar.edu.unlam.tuiter.ui.rememberAppContainer
import ar.edu.unlam.tuiter.ui.theme.tuiterTextFieldColors

@Composable
fun NewPostScreen(
    onBack: () -> Unit,
    onPublished: () -> Unit,
) {
    // DI (Paso 14): el contenedor nos da los repositorios ya armados.
    val container = rememberAppContainer()

    // Creamos (o reutilizamos) el ViewModel con sus dependencias.
    val viewModel: NewPostViewModel = viewModel {
        NewPostViewModel(container.postRepository, container.draftRepository)
    }

    val state by viewModel.state.collectAsState()

    // Cuando el post se publica, avisamos y volvemos al feed.
    LaunchedEffect(state.published) {
        if (state.published) {
            viewModel.consumePublished()
            onPublished()
        }
    }

    Scaffold(
        topBar = {
            // Wireframe: flecha "Volver" a la izquierda y "Nuevo post" como título.
            TuiterTopBar(
                title = "Nuevo post",
                onBack = onBack,
                // HU-02 escenario 3: mientras se publica no se puede volver,
                // así el usuario no sale a mitad del envío.
                backEnabled = !state.isPublishing
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Campo de texto grande del wireframe. Si había un borrador,
            // el ViewModel ya lo cargó y aparece escrito acá.
            OutlinedTextField(
                value = state.message,
                onValueChange = viewModel::onMessageChange,
                label = { Text("¿Qué estás pensando?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 6,
                enabled = !state.isPublishing,
                // Mismo color de texto legible que el login.
                colors = tuiterTextFieldColors()
            )

            // Fila con los DOS botones: Guardar borrador | Publicar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.saveDraft() },
                    // Wireframe: deshabilitado mientras el texto está vacío.
                    enabled = state.message.isNotBlank() && !state.isPublishing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar borrador")
                }

                Button(
                    onClick = { viewModel.publish() },
                    // Wireframe: deshabilitado si el texto está vacío
                    // o mientras se está enviando.
                    enabled = state.message.isNotBlank() && !state.isPublishing,
                    modifier = Modifier.weight(1f)
                ) {
                    if (state.isPublishing) {
                        // HU-02 escenario 3: indicador de progreso mientras se envía.
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Publicando...")
                    } else {
                        Text("Publicar")
                    }
                }
            }

            // Mensaje de estado del borrador ("Borrador guardado" /
            // "Borrador recuperado"), con un tilde para que se note que salió bien.
            state.draftStatus?.let { status ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Mensaje de error, si hubo ("No se pudo publicar", etc.).
            // Paso 16: en color de error para que se distinga del resto.
            state.error?.let { error ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Aclaración del wireframe: el texto se puede volver a enviar
            // si falla, y el borrador queda guardado en el celular.
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "El borrador se guarda en este dispositivo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
