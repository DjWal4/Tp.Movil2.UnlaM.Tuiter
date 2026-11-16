package ar.edu.unlam.tuiter.ui.components

// ============================================================
// BARRA SUPERIOR COMPARTIDA (estructura del wireframe)
// ============================================================
// El wireframe del Anexo IV muestra, en todas las pantallas, la misma
// estructura arriba: un título a la izquierda y, a la derecha, los accesos
// (en el feed: favoritos y perfil). En las pantallas internas, a la
// izquierda va la flecha para volver.
//
// Tenerla en un solo lugar hace que todas las pantallas se vean iguales
// y evita repetir el mismo TopAppBar en cada archivo.

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

// title: texto que se muestra a la izquierda.
// onBack: si NO es null, muestra la flecha "Volver" a la izquierda.
// backEnabled: permite deshabilitar la flecha (por ejemplo, mientras se publica).
// actions: íconos/botones de la derecha (se pasan como contenido).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuiterTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    backEnabled: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        // Título en negrita, como en el wireframe.
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            // Solo aparece si la pantalla nos dice cómo volver.
            if (onBack != null) {
                IconButton(onClick = onBack, enabled = backEnabled) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
            }
        },
        actions = actions,
        // Fondo del mismo color que el resto de la pantalla (sin sombra
        // gris del tema por defecto), para que se vea plano como el wireframe.
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}
