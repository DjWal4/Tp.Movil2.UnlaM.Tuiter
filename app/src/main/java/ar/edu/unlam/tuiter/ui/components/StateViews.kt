package ar.edu.unlam.tuiter.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ============================================================
// COMPONENTES DE ESTADO (Paso 16)
// ============================================================
// Los usan todas las pantallas para mostrar lo mismo:
//   - cargando
//   - error (con botón Reintentar opcional)
//   - vacío (sin datos)
// Así el manejo queda unificado y no se repite código en cada pantalla.
//
// Estructura tomada del wireframe (Anexo IV): en los tres casos el
// contenido va centrado en la pantalla, con un ícono arriba, un título,
// un texto de ayuda y (en el error) el botón "Reintentar".

// Spinner centrado mientras se cargan datos.
// message: texto debajo del spinner (por ejemplo "Cargando publicaciones...").
@Composable
fun LoadingView(
    message: String = "Cargando...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        // Texto secundario: se lee bien sobre el fondo del tema.
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// Mensaje de error centrado, con botón "Reintentar" si se pasa una acción.
// title: frase corta y clara ("No se pudo cargar el feed").
// message: detalle que devolvió el repositorio.
@Composable
fun ErrorView(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "Algo salió mal",
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Ícono de advertencia en color de error.
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        // Si hay una acción para reintentar, mostramos el botón.
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

// Mensaje centrado para cuando no hay datos (feed vacío, sin favoritos, etc.).
// title: mensaje principal.
// message: aclaración opcional (qué hacer para que aparezcan datos).
// icon: ícono que representa la pantalla (por defecto, un sobre).
@Composable
fun EmptyView(
    title: String,
    message: String? = null,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Email
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            // Si hay aclaración, la mostramos debajo en color secundario.
            if (message != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
