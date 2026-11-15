package ar.edu.unlam.tuiter.ui.newpost

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.layout.imePadding
import ar.edu.unlam.tuiter.ui.rememberAppContainer

@OptIn(ExperimentalMaterial3Api::class)
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
            TopAppBar(
                title = { Text("Nuevo post") },
                navigationIcon = {
                    // HU-02 escenario 3: mientras se publica no se puede volver,
                    // así el usuario no sale a mitad del envío.
                    TextButton(onClick = onBack, enabled = !state.isPublishing) {
                        Text("Volver")
                    }
                }
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
            OutlinedTextField(
                value = state.message,
                onValueChange = viewModel::onMessageChange,
                label = { Text("¿Qué estás pensando?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                enabled = !state.isPublishing
            )
            // Fila con los DOS botones: Guardar borrador | Publicar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.saveDraft() },
                    enabled = state.message.isNotBlank() && !state.isPublishing,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Guardar borrador")
                }

                Button(
                    onClick = { viewModel.publish() },
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
                // Mensaje de estado del borrador.
                state.draftStatus?.let { status ->
                    Text(text = status)
                }
                // Mensaje de error, si hubo.
                // Paso 16: en color de error para que se distinga del resto.
                state.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
        }
    }
}