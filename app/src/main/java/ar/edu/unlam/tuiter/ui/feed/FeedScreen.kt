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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import ar.edu.unlam.tuiter.data.model.Post
import ar.edu.unlam.tuiter.ui.components.EmptyView
import ar.edu.unlam.tuiter.ui.components.ErrorView
import ar.edu.unlam.tuiter.ui.components.LoadingView
import ar.edu.unlam.tuiter.ui.favorites.FavoritesViewModel
import ar.edu.unlam.tuiter.ui.rememberAppContainer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onCreatePost: () -> Unit,
    onPostClick: (Int) -> Unit = {},
    // Abre la pantalla de usuarios favoritos (R6).
    onOpenFavorites: () -> Unit = {},
    // Abre la pantalla de perfil (R5).
    onOpenProfile: () -> Unit = {}
) {
    // DI (Paso 14): el contenedor nos da los repositorios ya armados.
    val container = rememberAppContainer()

    // ViewModel del feed: recibe su repositorio desde el contenedor.
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

    // Estado del scroll de la lista (lo usa el paginado automático).
    val listState = rememberLazyListState()

    // Snackbar: mensajito abajo que confirma "agregaste a favoritos" (HU-07).
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Al entrar por primera vez, cargamos el feed.
    LaunchedEffect(Unit) {
        viewModel.loadFeed(reset = true)
    }

    // ------------------------------------------------------------
    // PAGINADO AUTOMÁTICO (HU-01 escenario 3)
    // ------------------------------------------------------------
    // snapshotFlow observa el scroll: cada vez que el último ítem visible
    // está cerca del final de la lista, pedimos la página siguiente.
    // Se lee el estado actual con viewModel.state.value para no reiniciar
    // el efecto en cada cambio.
    LaunchedEffect(listState) {
        snapshotFlow {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
            info.totalItemsCount > 0 && lastVisible >= info.totalItemsCount - 2
        }
            .distinctUntilChanged() // solo reacciona cuando cambia "llegué al final"
            .collect { atEnd ->
                if (atEnd) {
                    val current = viewModel.state.value
                    if (!current.endReached && !current.isLoading && current.posts.isNotEmpty()) {
                        viewModel.loadFeed(reset = false)
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feed") },
                actions = {
                    // Abre la pantalla con los usuarios favoritos (R6).
                    TextButton(onClick = onOpenFavorites) { Text("★ Favoritos") }
                    // Abre la pantalla de perfil (R5).
                    TextButton(onClick = onOpenProfile) { Text("Perfil") }
                }
            )
        },
        // Contenedor del mensajito de confirmación (snackbar).
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Sin "key" por ahora: si los IDs vienen repetidos o null,
                            // LazyColumn fallaría por claves duplicadas.
                            items(state.posts) { post ->
                                PostItem(
                                    post = post,
                                    onLikeClick = { viewModel.toggleLike(it) },
                                    // Tocar la tarjeta abre el detalle (HU-09).
                                    onPostClick = { p -> p.id?.let(onPostClick) },
                                    // ¿El autor de este post ya es favorito?
                                    isFavorite = post.author?.let { favorites.favoriteNames.contains(it) } == true,
                                    // Estrella: agrega o quita al autor de favoritos (R6).
                                    onFavoriteClick = { p ->
                                        val author = p.author?.trim()
                                        if (!author.isNullOrEmpty()) {
                                            // Sabemos si estaba o no ANTES de tocarla,
                                            // para mostrar el mensaje correcto.
                                            val wasFavorite =
                                                favorites.favoriteNames.contains(author)
                                            favoritesViewModel.toggleFavorite(author, p.avatarUrl)
                                            scope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (wasFavorite) {
                                                        "Quitaste a $author de favoritos"
                                                    } else {
                                                        "Agregaste a $author a favoritos"
                                                    }
                                                )
                                            }
                                        }
                                    }
                                )
                                HorizontalDivider()
                            }

                            // Pie de la lista: spinner mientras carga la página
                            // siguiente, o botón Reintentar si esa carga falló.
                            item {
                                when {
                                    state.isLoading -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Cargando más...")
                                        }
                                    }

                                    state.endReached -> {
                                        Text(
                                            text = "No hay más posts",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    // Si falló la página siguiente, damos un botón
                                    // para reintentar (el error se ve arriba).
                                    state.error != null -> {
                                        Button(
                                            onClick = { viewModel.loadFeed(reset = false) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp)
                                        ) {
                                            Text("Reintentar")
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
            // HU-09: tocar cualquier parte de la tarjeta abre el detalle.
            // Los botones internos (like, estrella) siguen funcionando igual:
            // ellos "consumen" el toque antes de que llegue a esta fila.
            .clickable(enabled = onPostClick != null) {
                onPostClick?.invoke(post)
            }
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
            Text(text = post.message ?: "(sin texto)")

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
                // Espacio vertical entre la fila de like/favorito y "Responder".
                Spacer(modifier = Modifier.height(8.dp))

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
