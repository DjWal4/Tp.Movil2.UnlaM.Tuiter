package ar.edu.unlam.tuiter.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import ar.edu.unlam.tuiter.data.model.Post
import ar.edu.unlam.tuiter.ui.components.EmptyView
import ar.edu.unlam.tuiter.ui.components.ErrorView
import ar.edu.unlam.tuiter.ui.components.LoadingView
import ar.edu.unlam.tuiter.ui.favorites.FavoritesViewModel
import ar.edu.unlam.tuiter.ui.rememberAppContainer
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onBack: () -> Unit,
    onCreatePost: () -> Unit,
    refreshKey: Int = 0,
            onPostClick: (Int) -> Unit = {},
    // Abre la pantalla de usuarios favoritos (R6).
    onOpenFavorites: () -> Unit = {},
    // Cierra la sesión y limpia los datos locales (Paso 15).
    onLogout: () -> Unit = {}
) {
    // DI (Paso 14): el contenedor nos da los repositorios ya armados.
    val container = rememberAppContainer()

    // ViewModel del feed: recibe su repositorio desde el contenedor.
    // Antes se creaba solo con "PostRepository()" adentro del ViewModel.
    val viewModel: FeedViewModel = viewModel {
        FeedViewModel(container.postRepository)
    }

    // Observer: la UI se redibuja cuando cambia el estado del ViewModel.
    val state by viewModel.state.collectAsState()

    // ViewModel de favoritos: lo comparte con la pantalla de favoritos
    // (misma clave de clase), así los datos quedan sincronizados.
    val favoritesViewModel: FavoritesViewModel = viewModel {
        FavoritesViewModel(container.favoriteRepository)
    }
    // Estado de favoritos: lista + set de nombres para pintar ☆ o ★.
    val favorites by favoritesViewModel.state.collectAsState()

    // Al entrar por primera vez, cargamos el feed.
    // Cada vez que cambia refreshKey (por ejemplo al volver de publicar),
    // se recarga la primera página del feed.
    LaunchedEffect(refreshKey) {
        viewModel.loadFeed(reset = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feed") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Volver") }
                },
                actions = {
                    // Abre la pantalla con los usuarios favoritos (R6).
                    TextButton(onClick = onOpenFavorites) { Text("★ Favoritos") }
                    // Cierra la sesión y vuelve al login (Paso 15).
                    TextButton(onClick = onLogout) { Text("Salir") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreatePost) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Botón visible para recargar el feed.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { viewModel.loadFeed(reset = true) },
                    enabled = !state.isLoading
                ) {
                    Text(if (state.isLoading) "Actualizando..." else "Actualizar")
                }
            }

            // Si hubo un error con un like (y ya hay posts), lo mostramos acá.
            state.error?.let { error ->
                if (state.posts.isNotEmpty()) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when {
                    // Cargando y todavía no hay posts (Paso 16: componente unificado).
                    state.isLoading && state.posts.isEmpty() -> {
                        LoadingView()
                    }

                    // Error y sin posts para mostrar: mensaje claro + Reintentar.
                    state.error != null && state.posts.isEmpty() -> {
                        ErrorView(
                            message = state.error ?: "Error desconocido",
                            onRetry = { viewModel.loadFeed(reset = true) }
                        )
                    }

                    // No hay posts y no hubo error: estado vacío explicativo.
                    state.posts.isEmpty() -> {
                        EmptyView(
                            message = "Todavía no hay posts.\n" +
                                "Tocá el botón + para publicar el primero."
                        )
                    }

                    // Lista de posts.
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            // Sin "key" por ahora: si los IDs vienen repetidos o null,
                            // LazyColumn fallaría por claves duplicadas.
                            items(state.posts) { post ->
                                PostItem(
                                    post = post,
                                    onLikeClick = { viewModel.toggleLike(it) },
                                    onPostClick = { p -> p.id?.let(onPostClick) },
                                    // ¿El autor de este post ya es favorito?
                                    isFavorite = post.author?.let { favorites.favoriteNames.contains(it) } == true,
                                    // Estrella: agrega o quita al autor de favoritos.
                                    onFavoriteClick = { p ->
                                        favoritesViewModel.toggleFavorite(p.author, p.avatarUrl)
                                    }
                                )
                                HorizontalDivider()
                            }

                            item {
                                if (state.isLoading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                } else if (!state.endReached) {
                                    Button(
                                        onClick = { viewModel.loadFeed(reset = false) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text("Cargar más")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Público: lo reutiliza la pantalla de detalle.
// Los dos últimos parámetros tienen valor por defecto, así el detalle
// sigue funcionando sin tocarlo (la estrella solo aparece si se pasa la acción).
@Composable
fun PostItem(
    post: Post,
    onLikeClick: (Post) -> Unit,
    onPostClick: ((Post) -> Unit)? = null,
    isFavorite: Boolean = false,
    onFavoriteClick: ((Post) -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar del autor (Coil carga la imagen desde la URL).
        AsyncImage(
            model = post.avatarUrl,
            contentDescription = "Avatar de ${post.author?: "usuario"}",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Si author viene null, mostramos un texto genérico.
                Text(text = post.author ?: "Sin autor",
                    fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    // Si date viene null, formatDate devuelve "".
                    text = formatDate(post.date ?: ""),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Si message viene null, mostramos un texto genérico.
            // Tocar el texto también abre el detalle.
            Text(
                text = post.message ?: "(sin texto)",
                modifier = Modifier.clickable(enabled = onPostClick != null) {
                    onPostClick?.invoke(post)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Corazón clickeable: da o quita el like.
            // La estrella de favoritos va al lado, en la misma fila.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier.clickable { onLikeClick(post) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (post.liked == true) "♥" else "♡")
                    Spacer(modifier = Modifier.width(4.dp))
                    // Si likes viene null, mostramos 0.
                    Text(text = "${post.likes ?: 0}",
                        style = MaterialTheme.typography.bodySmall)
                }

                // Estrella clickeable: agrega/quita al AUTOR de los favoritos (R6).
                // Solo se muestra si la pantalla pasó una acción (en el feed sí).
                if (onFavoriteClick != null) {
                    Spacer(modifier = Modifier.width(20.dp))
                    Row(
                        modifier = Modifier.clickable { onFavoriteClick.invoke(post) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = if (isFavorite) "★" else "☆")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Favorito",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (onPostClick != null) {
                Spacer(modifier = Modifier.width(20.dp))

                // Abre el post con sus respuestas.
                Row(
                    modifier = Modifier.clickable { onPostClick.invoke(post) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💬")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Responder",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// Convierte la fecha ISO 8601 a algo legible.
// Si viene vacía o con otro formato, devuelve el texto original (o "").
private fun formatDate(iso: String): String {
    if (iso.isBlank()) return ""

    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        parser.timeZone = TimeZone.getTimeZone("UTC")
        val date = parser.parse(iso)
        if (date != null) {
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            formatter.format(date)
        } else {
            iso
        }
    } catch (e: Exception) {
        iso
    }
}