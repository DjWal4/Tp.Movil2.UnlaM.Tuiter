package ar.edu.unlam.tuiter.ui.theme

import android.app.Activity
import android.os.Build
// Se quitó el import de isSystemInDarkTheme: ya no lo usamos porque la app
// fuerza el tema claro (ver la explicación en UNLaMTuiterTheme).
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun UNLaMTuiterTheme(
    // ============================================================
    // ARREGLO DE LEGIBILIDAD (por qué el tema está fijo en claro)
    // ============================================================
    // Antes: darkTheme = isSystemInDarkTheme(). El celular de prueba está en
    // modo oscuro, así que Compose elegía DarkColorScheme (textos claros:
    // #D0BCFF, #CAC4D0), pero la ventana de Android seguía pintándose clara
    // (themes.xml usa un tema Light). Resultado: texto claro sobre fondo
    // claro = casi invisible.
    //
    // Ahora forzamos el tema claro para que los colores de Compose y el fondo
    // real de la ventana coincidan siempre, sin importar el modo del celular.
    darkTheme: Boolean = false,
    // Dynamic color is available on Android 12+
    // También lo apagamos: el color dinámico toma los colores del fondo de
    // pantalla del celular y puede devolver combinaciones poco legibles.
    // Con false usamos siempre nuestra paleta (LightColorScheme).
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        // Surface pinta TODO el fondo con colorScheme.background.
        // Sin esto, las pantallas que no dibujan su propio fondo dejaban ver
        // el fondo de la ventana (claro) y los textos del tema quedaban sin
        // contraste. Con Surface, fondo y colores siempre son del mismo tema.
        content = {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                content()
            }
        }
    )
}