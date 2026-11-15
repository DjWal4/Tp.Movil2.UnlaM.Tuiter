package ar.edu.unlam.tuiter.ui.favorites

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ar.edu.unlam.tuiter.ui.components.EmptyView
import ar.edu.unlam.tuiter.ui.rememberAppContainer
import coil.compose.AsyncImage

// Pantalla R6: lista de usuarios favoritos guardados en Room.
// Se abre desde el botón "★ Favoritos" del feed.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(onBack: () -> Unit) {

    // DI (Paso 14): el contenedor nos da el repositorio ya armado.
    val container = rememberAppContainer()

    // Mismo patrón que NewPostScreen: creamos el ViewModel con su dependencia.
    // Al usar la misma clave (el nombre de la clase), esta instancia es la MISMA
    // que usa el feed, así los dos ven los mismos datos.
    val viewModel: FavoritesViewModel = viewModel {
        FavoritesViewModel(container.favoriteRepository)
    }

    // Observer: la UI se redibuja cuando cambia el estado.
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Usuarios favoritos") },
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
        ) {
            // Si hubo un error (por ejemplo al leer la base), lo mostramos.
            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (state.favorites.isEmpty()) {
                // Todavía no hay favoritos: estado vacío unificado (Paso 16),
                // con el mismo estilo que el feed y el detalle.
                EmptyView(
                    message = "Todavía no tenés usuarios favoritos.\n" +
                        "Tocá la ☆ en un post del feed para agregarlos."
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.favorites) { favorite ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar del usuario favorito (Coil carga la URL).
                            AsyncImage(
                                model = favorite.avatarUrl,
                                contentDescription = "Avatar de ${favorite.authorName}",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Nombre del usuario favorito.
                            Text(
                                text = favorite.authorName,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            // Botón para sacarlo de favoritos.
                            TextButton(
                                onClick = { viewModel.removeFavorite(favorite.authorName) }
                            ) {
                                Text("Quitar")
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
