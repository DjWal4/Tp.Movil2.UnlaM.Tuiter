package ar.edu.unlam.tuiter.ui.theme

// ============================================================
// COLORES DE LOS CAMPOS DE TEXTO (mejora de legibilidad)
// ============================================================
// Problema: el texto que el usuario escribe en los campos quedaba con un
// gris muy claro y no se leía bien sobre el fondo claro del formulario.
//
// Solución: un solo lugar donde definimos el color del texto escrito y
// del cursor. Cualquier pantalla que lo necesite usa `tuiterTextFieldColors()`
// y queda con el mismo aspecto (interfaz consistente).
//
// IMPORTANTE (corrección): antes decidíamos con `isSystemInDarkTheme()`,
// que mira si el CELULAR está en modo oscuro. Eso era un error: el celular
// puede estar en modo oscuro mientras la app usa el tema claro. Ahora
// decidimos según la LUMINANCIA del fondo real del tema (colorScheme).
// Así el color del texto siempre contrasta con el fondo que se ve de verdad.

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

// Gris muy oscuro: máximo contraste sobre el fondo claro del formulario.
private val TextoCampoClaro = Color(0xFF1F1F1F)

// Gris claro: se lee bien si algún día se usa un fondo oscuro.
private val TextoCampoOscuro = Color(0xFFF2F2F2)

// Gris para la etiqueta ("Email", "Contraseña") cuando el campo NO tiene foco.
// Es más oscuro que el onSurfaceVariant del tema, para que se lea bien.
private val EtiquetaCampoClaro = Color(0xFF44474E)
private val EtiquetaCampoOscuro = Color(0xFFCAC4D0)

// Devuelve la paleta de colores para un OutlinedTextField.
// Se usa así:  OutlinedTextField(..., colors = tuiterTextFieldColors())
@Composable
fun tuiterTextFieldColors(): TextFieldColors {
    // ¿El fondo del tema es oscuro? Lo medimos con su luminancia:
    // 0 = negro, 1 = blanco. Menos de 0.5 significa fondo oscuro.
    // Esto es más confiable que preguntarle al sistema operativo.
    val fondoOscuro = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Color del texto que escribe el usuario (el mismo con o sin foco).
    val colorTexto = if (fondoOscuro) TextoCampoOscuro else TextoCampoClaro

    // Color de la etiqueta cuando el campo está sin foco.
    val colorEtiqueta = if (fondoOscuro) EtiquetaCampoOscuro else EtiquetaCampoClaro

    return OutlinedTextFieldDefaults.colors(
        // Texto escrito por el usuario.
        focusedTextColor = colorTexto,
        unfocusedTextColor = colorTexto,
        // Etiqueta ("Email", "Contraseña"): violeta al enfocar, gris legible al resto.
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = colorEtiqueta,
        // Texto de ayuda/placeholder, por si se agrega más adelante.
        focusedPlaceholderColor = colorEtiqueta,
        unfocusedPlaceholderColor = colorEtiqueta,
        // Borde del campo: se ve claramente cuál está enfocado.
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = colorEtiqueta,
        // Cursor (la barrita que parpadea al escribir).
        cursorColor = MaterialTheme.colorScheme.primary
    )
}
