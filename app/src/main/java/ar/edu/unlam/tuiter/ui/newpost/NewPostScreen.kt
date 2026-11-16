package ar.edu.unlam.tuiter.ui.newpost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

    // Aviso inferior (snackbar): el wireframe muestra ahí los mensajes de
    // confirmación ("Borrador guardado") y de error ("No se pudo publicar").
    val snackbarHostState = remember { SnackbarHostState() }

    // Cuando el ViewModel informa el estado del borrador, lo mostramos abajo.
    // Después de mostrarlo lo consumimos para que no se repita al recomponer.
    LaunchedEffect(state.draftStatus) {
        state.draftStatus?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.consumeDraftStatus()
        }
    }

    // Igual con el error: se muestra como aviso inferior y se consume.
    LaunchedEffect(state.error) {
        state.error?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                // Más tiempo porque el usuario necesita leer el motivo.
                duration = SnackbarDuration.Long
            )
            viewModel.consumeError()
        }
    }

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
        },
        // Contenedor del aviso inferior (mensajes del wireframe).
        snackbarHost = { SnackbarHost(snackbarHostState) }
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

            // Los mensajes ("Borrador guardado", "No se pudo publicar") ya no
            // van dentro del formulario: el wireframe los muestra como aviso
            // inferior (snackbar), manejado arriba con snackbarHostState.
        }
    }
}
