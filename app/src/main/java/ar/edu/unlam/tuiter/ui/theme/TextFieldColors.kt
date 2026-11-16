package ar.edu.unlam.tuiter.ui.theme

// ============================================================
// COLORES DE LOS CAMPOS DE TEXTO (mejora de legibilidad)
// ============================================================
// Problema: con el tema dinámico de Android, el texto que el usuario
// escribe en los campos quedaba con un gris muy claro y no se leía bien.
//
// Solución: un solo lugar donde definimos el color del texto escrito y
// del cursor. Cualquier pantalla que lo necesite usa `tuiterTextFieldColors()`
// y queda con el mismo aspecto (interfaz consistente).
//
// Se adapta al tema:
//  - Fondo claro -> texto gris oscuro.
//  - Fondo oscuro -> texto gris claro.

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Gris oscuro: se lee bien sobre el fondo claro del formulario.
private val TextoCampoClaro = Color(0xFF3A3A3A)

// Gris claro: se lee bien si el celular está en modo oscuro.
private val TextoCampoOscuro = Color(0xFFEDEDED)

// Devuelve la paleta de colores para un OutlinedTextField.
// Se usa así:  OutlinedTextField(..., colors = tuiterTextFieldColors())
@Composable
fun tuiterTextFieldColors(): TextFieldColors {
    // ¿El celular está en modo oscuro?
    val modoOscuro = isSystemInDarkTheme()

    // Color del texto que escribe el usuario (el mismo con o sin foco).
    val colorTexto = if (modoOscuro) TextoCampoOscuro else TextoCampoClaro

    return OutlinedTextFieldDefaults.colors(
        // Texto escrito por el usuario.
        focusedTextColor = colorTexto,
        unfocusedTextColor = colorTexto,
        // Etiqueta ("Email", "Contraseña"): violeta al enfocar, gris al resto.
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        // Cursor (la barrita que parpadea al escribir).
        cursorColor = MaterialTheme.colorScheme.primary
    )
}
