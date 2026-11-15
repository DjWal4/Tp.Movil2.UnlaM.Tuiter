package ar.edu.unlam.tuiter.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import ar.edu.unlam.tuiter.TuiterApplication
import ar.edu.unlam.tuiter.di.AppContainer

// Atajo para obtener el contenedor de dependencias desde una pantalla Compose.
//
// ¿Por qué no usar "new" acá? Porque el contenedor ya existe en la Application.
// Así todas las pantallas comparten exactamente las mismas dependencias.
@Composable
fun rememberAppContainer(): AppContainer {
    val context = LocalContext.current
    // applicationContext es la Application real, casteada a la nuestra.
    return (context.applicationContext as TuiterApplication).container
}
