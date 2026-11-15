// Package (paquete) al que pertenece este archivo.
// Debe coincidir con la carpeta donde está guardado: app/src/main/java/ar/edu/unlam/tuiter/
package ar.edu.unlam.tuiter

// ---- Imports de Android / Activity ----
import android.os.Bundle                      // Bundle: guarda/restaura estado de la Activity
import androidx.activity.ComponentActivity    // Clase base para Activities modernas con Compose
import androidx.activity.compose.setContent   // setContent { }: convierte Compose en pantalla
import androidx.activity.enableEdgeToEdge     // Permite dibujar detrás de las barras del sistema

// ---- Imports de Compose: layout ----
import androidx.compose.foundation.layout.Column        // Column: apila elementos verticalmente
import androidx.compose.foundation.layout.fillMaxSize   // Modifier.fillMaxSize(): ocupa toda la pantalla
import androidx.compose.foundation.layout.padding       // Modifier.padding(): agrega espacio interno

// ---- Imports de Compose: componentes Material 3 ----
import androidx.compose.material3.Button      // Button: botón clickeable
import androidx.compose.material3.Scaffold    // Scaffold: estructura base (barras, padding, contenido)
import androidx.compose.material3.Text        // Text: muestra un texto en pantalla

// ---- Imports de Compose: estado y efectos ----
import androidx.compose.runtime.Composable              // @Composable: marca una función de UI
import androidx.compose.runtime.getValue                // Necesario para "var x by remember { ... }"
import androidx.compose.runtime.mutableStateOf          // Estado observable: al cambiar, la UI se redibuja
import androidx.compose.runtime.remember                // remember: conserva el valor entre recomposiciones
import androidx.compose.runtime.rememberCoroutineScope  // Scope para lanzar corrutinas desde la UI
import androidx.compose.runtime.setValue                // Necesario para "var x by remember { ... }"

// ---- Imports de Compose: utilidades ----
import androidx.compose.ui.Modifier                          // Modifier: modificadores de tamaño/espaciado/etc.
import androidx.compose.ui.tooling.preview.Preview           // @Preview: vista previa en Android Studio
import androidx.compose.ui.unit.dp

// ---- Imports del proyecto ----
import ar.edu.unlam.tuiter.data.model.LoginRequest           // Modelo del body del login (email, password)
import ar.edu.unlam.tuiter.data.error.ErrorMapper            // Traduce excepciones a mensajes claros (Paso 16)
import ar.edu.unlam.tuiter.data.session.SessionManager
import ar.edu.unlam.tuiter.ui.detail.PostDetailScreen
import ar.edu.unlam.tuiter.ui.favorites.FavoritesScreen
import ar.edu.unlam.tuiter.ui.feed.FeedScreen
import ar.edu.unlam.tuiter.ui.newpost.NewPostScreen
import ar.edu.unlam.tuiter.ui.theme.UNLaMTuiterTheme         // Tema visual de la app

// ---- Import de corrutinas ----
import kotlinx.coroutines.launch                             // launch: ejecuta código asincrónico
import retrofit2.HttpException

/**
 * MainActivity: es la primera pantalla que se abre al iniciar la app.
 * Su única responsabilidad acá es "montar" el contenido de Compose.
 */
class MainActivity : ComponentActivity() {

    // DI (Paso 14): contenedor de dependencias de la app.
    // "by lazy" lo obtiene la primera vez que se usa (ya con la Application creada).
    // Así la Activity no crea el cliente Retrofit ni la base por su cuenta.
    private val container by lazy { (application as TuiterApplication).container }

    // onCreate: se ejecuta cuando se crea la pantalla.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) // Llama a la implementación base (obligatorio)
        enableEdgeToEdge()                 // Dibuja de borde a borde (debajo de las barras del sistema)

        // setContent define la UI de esta pantalla usando Compose.
        setContent {
            // Aplica el tema (colores, tipografías) definido en ui/theme.
            UNLaMTuiterTheme {
                // Navegación simple: false = login, true = feed.
                var showFeed by remember { mutableStateOf(false) }
                var showNewPost by remember { mutableStateOf(false) }
                // Post seleccionado para ver el detalle con sus respuestas (R7).
                // null = no hay detalle abierto.
                var selectedPostId by remember { mutableStateOf<Int?>(null) }
                // Pantalla de usuarios favoritos (R6).
                // false = no está abierta.
                var showFavorites by remember { mutableStateOf(false) }

                // Scope para lanzar el "cerrar sesión" (es una función suspend).
                val scope = rememberCoroutineScope()

                when {
                    // Pantalla de nuevo post.
                    showNewPost -> {
                        NewPostScreen(
                            onBack = { showNewPost = false },
                            onPublished = { showNewPost = false }
                        )
                    }

                    // Pantalla de detalle: el post original + sus respuestas.
                    selectedPostId != null -> {
                        val postId = selectedPostId!!
                        PostDetailScreen(
                            postId = postId,
                            onBack = { selectedPostId = null }
                        )
                    }

                    // Pantalla de usuarios favoritos (R6).
                    // Va antes del feed porque se abre desde el feed.
                    showFavorites -> {
                        FavoritesScreen(
                            onBack = { showFavorites = false }
                        )
                    }

                    // Pantalla del feed.
                    showFeed -> {
                        FeedScreen(
                            onBack = { showFeed = false },
                            onCreatePost = { showNewPost = true },
                            // Al tocar "Responder" en un post, abrimos su detalle.
                            onPostClick = { id -> selectedPostId = id },
                            // Botón "★ Favoritos" de la barra superior.
                            onOpenFavorites = { showFavorites = true },
                            // Botón "Salir": cierra sesión y limpia datos locales (Paso 15).
                            onLogout = {
                                scope.launch {
                                    // 1) Borra borrador + favoritos + token.
                                    container.sessionRepository.logout()

                                    // 2) Cierra las pantallas abiertas.
                                    showFavorites = false
                                    showNewPost = false
                                    selectedPostId = null
                                    showFeed = false

                                    // 3) Borra los ViewModels en memoria para que no quede
                                    // nada del usuario anterior (posts, borrador, favoritos).
                                    viewModelStore.clear()
                                }
                            }
                        )
                    }
                    // Pantalla de login (inicial).
                    else -> {
                        // Scaffold es la "estructura" de la pantalla.
                        // fillMaxSize: ocupa toda la pantalla.
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            // innerPadding: espacio que ocupan las barras del sistema.
                            // Se lo pasamos a la pantalla para que el contenido no quede tapado.
                            LoginTestScreen(
                                { showFeed = true },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Pantalla de prueba para el login.
     * Es @Composable: puede usar remember, Column, Button, etc.
     */
    @Composable
    fun LoginTestScreen(onGoToFeed: () -> Unit, modifier: Modifier = Modifier) {

        // rememberCoroutineScope: permite lanzar corrutinas (trabajo asincrónico) desde la UI.
        // Las llamadas a la API NO deben hacerse en el hilo principal.
        val scope = rememberCoroutineScope()

        // Estado "mensaje": texto que se muestra debajo del botón.
        // "by remember { mutableStateOf(...) }" hace que al cambiar el valor, la UI se actualice sola.
        // Estado para el resultado del login.
        var mensaje by remember { mutableStateOf("Sin probar") }

        // Estado para el resultado del perfil.
        var perfil by remember { mutableStateOf("Perfil sin cargar") }

        var loggedIn by remember { mutableStateOf(SessionManager.isLoggedIn) }

        // Column: apila el botón y el texto uno debajo del otro.
        // Usamos el modifier recibido para respetar el padding del Scaffold.
        Column(modifier = modifier.padding(16.dp)) {

            // --- Botón 1: login ---
            Button(onClick = {
                scope.launch {
                    try {
                        val resp = container.api.login(
                            LoginRequest(
                                email = "wloiacono@alumno.unlam.edu.ar",
                                password = "W2GHLikCbz4NXPP"
                            )
                        )
                        // Guardamos el token de usuario para los endpoints /me/...
                        SessionManager.saveToken(resp.token)
                        loggedIn = true
                        mensaje = "Login OK: ${resp.name} (token guardado)"
                    } catch (e: HttpException) {
                        // Paso 16: mensaje amigable. En el login, 401 significa
                        // "credenciales incorrectas" (no "sesión vencida").
                        mensaje = if (e.code() == 401) {
                            "Email o contraseña incorrectos."
                        } else {
                            ErrorMapper.toUserMessage(e)
                        }
                    } catch (e: Exception) {
                        mensaje = ErrorMapper.toUserMessage(e)
                    }
                }
            }) {
                Text("Probar login")
            }

            // --- Botón 2: perfil ---
            Button(onClick = {
                scope.launch {
                    // Si no hay token, no tiene sentido llamar al perfil.
                    if (!SessionManager.isLoggedIn) {
                        perfil = "Primero iniciá sesión"
                        return@launch
                    }
                    try {
                        val resp = container.api.getProfile()
                        perfil = "Perfil: ${resp.name} | ${resp.email} | ${resp.avatarUrl ?: "sin avatar"}"
                    } catch (e: Exception) {
                        // Paso 16: mensaje amigable también al pedir el perfil.
                        perfil = ErrorMapper.toUserMessage(e)
                    }
                }
            }, enabled = loggedIn) {
                Text("Probar perfil")
            }
            // Botón nuevo: pasa a la pantalla del feed.
            Button(onClick = onGoToFeed, enabled = loggedIn) {
                Text("Ir al feed")
            }

            // Resultados en pantalla.
            Text(mensaje)
            Text(perfil)
        }
    }

    /**
     * @Preview permite ver esta pantalla en el panel de vista previa de Android Studio
     * sin necesidad de ejecutar la app en un emulador.
     */
    @Preview(showBackground = true)
    @Composable
    fun LoginTestScreenPreview() {
        UNLaMTuiterTheme {
            LoginTestScreen(onGoToFeed = {})
        }
    }
}

