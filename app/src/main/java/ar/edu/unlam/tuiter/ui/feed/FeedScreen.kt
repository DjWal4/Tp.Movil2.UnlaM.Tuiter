package ar.edu.unlam.tuiter.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import ar.edu.unlam.tuiter.ui.components.TuiterTopBar
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
            // Barra superior del wireframe: nombre de la app a la izquierda
            // y los accesos a favoritos y perfil como íconos a la derecha.
            TuiterTopBar(
                title = "UNLaM Tuiter",
                actions = {
                    // Recarga manual del feed (se mantiene la función de "Actualizar"
                    // que ya existía, ahora como ícono para respetar el wireframe).
                    IconButton(
                        onClick = { viewModel.loadFeed(reset = true) },
                        enabled = !state.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                    // Abre la pantalla con los usuarios favoritos (R6).
                    IconButton(onClick = onOpenFavorites) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Usuarios favoritos"
                        )
                    }
                    // Abre la pantalla de perfil (R5).
                    IconButton(onClick = onOpenProfile) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Mi perfil"
                        )
                    }
                }
            )
        },
        // Contenedor del mensajito de confirmación (snackbar).
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // Botón flotante "+" del wireframe: abre la pantalla de publicación.
            FloatingActionButton(onClick = onCreatePost) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Nuevo post"
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                // Cargando y todavía no hay posts (Paso 16: componente unificado).
                state.isLoading && state.posts.isEmpty() -> {
                    LoadingView(message = "Cargando publicaciones...")
                }

                // Error y sin posts para mostrar: mensaje claro + Reintentar.
                state.error != null && state.posts.isEmpty() -> {
                    ErrorView(
                        title = "No se pudo cargar el feed",
                        message = state.error ?: "Error desconocido",
                        onRetry = { viewModel.loadFeed(reset = true) }
                    )
                }

                // No hay posts y no hubo error: estado vacío explicativo.
                state.posts.isEmpty() -> {
                    EmptyView(
                        title = "Todavía no hay posts.",
                        message = "Tocá el botón + para publicar el primero."
                    )
                }

                // Lista de posts.
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Si hubo un error con un like (y ya hay posts), lo mostramos
                        // como una franja arriba de la lista.
                        state.error?.let { error ->
                            ErrorBanner(message = error)
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            // Deja aire abajo para que el botón flotante "+"
                            // no tape el último post.
                            contentPadding = PaddingValues(bottom = 88.dp)
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
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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

// Franja de error arriba de la lista (cuando ya hay posts en pantalla).
// Antes era un texto suelto; ahora usa el color de error del tema para
// que se distinga sin romper la estructura del feed.
@Composable
private fun ErrorBanner(message: String) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // HU-09: tocar cualquier parte de la tarjeta abre el detalle.
            // Los botones internos (like, estrella, responder) siguen funcionando
            // igual: ellos "consumen" el toque antes de que llegue a esta columna.
            .clickable(enabled = onPostClick != null) {
                onPostClick?.invoke(post)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Fila superior del wireframe: avatar a la izquierda,
        // autor + fecha arriba y el texto del post debajo.
        Row(verticalAlignment = Alignment.Top) {
            // Avatar del autor (Coil carga la imagen desde la URL).
            AsyncImage(
                model = post.avatarUrl,
                contentDescription = "Avatar de ${post.author ?: "usuario"}",
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Si author viene null, mostramos un texto genérico.
                    Text(
                        text = post.author ?: "Sin autor",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    // La fecha va a la derecha, en gris (como en el wireframe).
                    Text(
                        // Si date viene null, formatDate devuelve "".
                        text = formatDate(post.date ?: ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Si message viene null, mostramos un texto genérico.
                Text(
                    text = post.message ?: "(sin texto)",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fila de acciones del wireframe: me gusta + contador, favorito y
        // responder, todo en UNA sola línea (antes "Responder" iba aparte).
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Corazón clickeable: da o quita el like.
            PostAction(
                icon = if (post.liked == true) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                // Si likes viene null, mostramos 0.
                label = "${post.likes ?: 0}",
                contentDescription = if (post.liked == true) "Quitar me gusta" else "Me gusta",
                tint = if (post.liked == true) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                onClick = { onLikeClick(post) }
            )

            // Estrella clickeable: agrega/quita al AUTOR de los favoritos (R6).
            // Solo se muestra si la pantalla pasó una acción (en el feed sí).
            if (onFavoriteClick != null) {
                PostAction(
                    icon = Icons.Filled.Star,
                    label = "Favorito",
                    contentDescription = if (isFavorite) {
                        "Quitar a ${post.author ?: "este usuario"} de favoritos"
                    } else {
                        "Agregar a ${post.author ?: "este usuario"} a favoritos"
                    },
                    // Rellena cuando el autor ya es favorito (estado del wireframe).
                    tint = if (isFavorite) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    onClick = { onFavoriteClick.invoke(post) }
                )
            }

            if (onPostClick != null) {
                // Abre el post con sus respuestas.
                PostAction(
                    icon = Icons.AutoMirrored.Filled.Send,
                    label = "Responder",
                    contentDescription = "Abrir el post y responder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { onPostClick.invoke(post) }
                )
            }
        }
    }
}

// Acción chica de la tarjeta: ícono + texto, con área de toque cómoda.
@Composable
private fun PostAction(
    icon: ImageVector,
    label: String,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = tint
        )
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
