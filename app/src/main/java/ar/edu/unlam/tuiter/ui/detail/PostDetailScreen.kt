package ar.edu.unlam.tuiter.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.unlam.tuiter.ui.components.ErrorView
import ar.edu.unlam.tuiter.ui.components.LoadingView
import ar.edu.unlam.tuiter.ui.feed.PostItem
import ar.edu.unlam.tuiter.ui.rememberAppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: Int,
    onBack: () -> Unit
) {
    // DI (Paso 14): el repositorio viene del contenedor.
    val container = rememberAppContainer()

    // Un ViewModel por post (la key evita reusar el de otro post).
    val viewModel: PostDetailViewModel = viewModel(key = "post-$postId") {
        PostDetailViewModel(postId, container.postRepository)
    }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(postId) {
        viewModel.load()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post y respuestas") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Volver") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.isLoading && state.post == null -> {
                        // Paso 16: mismo componente de carga que el resto de la app.
                        LoadingView()
                    }

                    state.error != null && state.post == null -> {
                        // Error al cargar el post: mensaje claro + Reintentar.
                        ErrorView(
                            message = state.error ?: "Error desconocido",
                            onRetry = { viewModel.load() }
                        )
                    }

                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            // 1) El post original.
                            item {
                                state.post?.let { post ->
                                    PostItem(
                                        post = post,
                                        onLikeClick = { viewModel.toggleLike(it) }
                                    )
                                }
                                HorizontalDivider()
                            }

                            // 2) Título de la sección de respuestas.
                            item {
                                Text(
                                    text = "Respuestas (${state.replies.size})",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }

                            // 3) Cada respuesta.
                            items(state.replies) { reply ->
                                PostItem(
                                    post = reply,
                                    onLikeClick = { viewModel.toggleLike(it) }
                                )
                                HorizontalDivider()
                            }

                            // 4) Si no hay respuestas.
                            if (state.replies.isEmpty()) {
                                item {
                                    Text(
                                        text = "Todavía no hay respuestas. ¡Respondé vos!",
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Error (por ejemplo al fallar un like) cuando ya hay contenido.
            state.error?.let { error ->
                if (state.post != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider()

            // Barra inferior para escribir la respuesta.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.replyText,
                    onValueChange = viewModel::onReplyChange,
                    label = { Text("Responder...") },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isReplying,
                    maxLines = 4
                )

                Button(
                    onClick = { viewModel.sendReply() },
                    enabled = state.replyText.isNotBlank() && !state.isReplying
                ) {
                    // HU-10: el botón se llama "Responder".
                    Text(if (state.isReplying) "..." else "Responder")
                }
            }
        }
    }
}

