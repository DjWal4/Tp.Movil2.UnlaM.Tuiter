// Package (paquete) al que pertenece este archivo.
// Debe coincidir con la carpeta donde está guardado: app/src/main/java/ar/edu/unlam/tuiter/
package ar.edu.unlam.tuiter

// ---- Imports de Android / Activity ----
import android.os.Bundle                      // Bundle: guarda/restaura estado de la Activity
import androidx.activity.ComponentActivity    // Clase base para Activities modernas con Compose
import androidx.activity.compose.setContent   // setContent { }: convierte Compose en pantalla
import androidx.activity.enableEdgeToEdge     // Permite dibujar detrás de las barras del sistema

// ---- Imports de Compose: estado y efectos ----
import androidx.compose.runtime.getValue                // Necesario para "var x by remember { ... }"
import androidx.compose.runtime.mutableStateOf          // Estado observable: al cambiar, la UI se redibuja
import androidx.compose.runtime.remember                // remember: conserva el valor entre recomposiciones
import androidx.compose.runtime.rememberCoroutineScope  // Scope para lanzar corrutinas desde la UI
import androidx.compose.runtime.setValue                // Necesario para "var x by remember { ... }"

// ---- Imports del proyecto ----
import ar.edu.unlam.tuiter.ui.detail.PostDetailScreen
import ar.edu.unlam.tuiter.ui.favorites.FavoritesScreen
import ar.edu.unlam.tuiter.ui.feed.FeedScreen
import ar.edu.unlam.tuiter.ui.login.LoginScreen
import ar.edu.unlam.tuiter.ui.newpost.NewPostScreen
import ar.edu.unlam.tuiter.ui.profile.ProfileScreen
import ar.edu.unlam.tuiter.ui.register.RegisterScreen
import ar.edu.unlam.tuiter.ui.theme.UNLaMTuiterTheme         // Tema visual de la app

// ---- Import de corrutinas ----
import kotlinx.coroutines.launch                             // launch: ejecuta código asincrónico

/**
 * MainActivity: es la primera pantalla que se abre al iniciar la app.
 *
 * Su responsabilidad es "montar" el contenido de Compose y decidir
 * QUÉ pantalla se muestra. La navegación es simple: variables de estado
 * (true/false) en lugar de una librería de navegación.
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
                // ------------------------------------------------------------
                // NAVEGACIÓN POR ESTADO
                // ------------------------------------------------------------
                // HU-05 escenario 3 (sesión recordada): si ya hay un token
                // guardado en SharedPreferences, la app arranca directo en el
                // feed, sin pedir email ni contraseña otra vez.
                // `remember { ... }` calcula esto UNA sola vez al abrir la app.
                var showFeed by remember {
                    mutableStateOf(container.sessionRepository.isLoggedIn())
                }

                // Pantalla de registro (R4 / HU-04).
                var showRegister by remember { mutableStateOf(false) }

                // Pantalla de nuevo post (R2 y R3).
                var showNewPost by remember { mutableStateOf(false) }

                // Post seleccionado para ver el detalle con sus respuestas (R7).
                // null = no hay detalle abierto.
                var selectedPostId by remember { mutableStateOf<Int?>(null) }

                // Pantalla de usuarios favoritos (R6).
                var showFavorites by remember { mutableStateOf(false) }

                // Pantalla de perfil, con edición (R5).
                var showProfile by remember { mutableStateOf(false) }

                // Mensaje que se muestra en el login después de registrarse
                // (por ejemplo: "Cuenta creada. Ya podés iniciar sesión.").
                var loginMessage by remember { mutableStateOf<String?>(null) }

                // Scope para lanzar el "cerrar sesión" (es una función suspend).
                val scope = rememberCoroutineScope()

                // ------------------------------------------------------------
                // CERRAR SESIÓN (Paso 15)
                // ------------------------------------------------------------
                // Se usa desde la pantalla de perfil. Hace tres cosas:
                //   1) borra borrador, favoritos y token,
                //   2) cierra todas las pantallas abiertas,
                //   3) limpia los ViewModels en memoria.
                fun logout() {
                    scope.launch {
                        // 1) Borra borrador + favoritos + token.
                        container.sessionRepository.logout()

                        // 2) Cierra las pantallas abiertas y vuelve al login.
                        showFavorites = false
                        showNewPost = false
                        showProfile = false
                        selectedPostId = null
                        loginMessage = null
                        showFeed = false

                        // 3) Borra los ViewModels en memoria para que no quede
                        // nada del usuario anterior (posts, borrador, favoritos).
                        viewModelStore.clear()
                    }
                }

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
                    showFavorites -> {
                        FavoritesScreen(
                            onBack = { showFavorites = false }
                        )
                    }

                    // Pantalla de perfil: ver y editar (R5).
                    showProfile -> {
                        ProfileScreen(
                            onBack = { showProfile = false },
                            // "Salir" desde el perfil: cierra sesión.
                            onLogout = { logout() }
                        )
                    }

                    // Pantalla de registro (R4).
                    showRegister -> {
                        RegisterScreen(
                            onBack = { showRegister = false },
                            // Al registrarse bien, volvemos al login con un aviso.
                            onRegistered = {
                                showRegister = false
                                loginMessage = "Cuenta creada. Ya podés iniciar sesión."
                            }
                        )
                    }

                    // Pantalla del feed (pantalla principal con sesión iniciada).
                    showFeed -> {
                        FeedScreen(
                            onCreatePost = { showNewPost = true },
                            // Al tocar un post, abrimos su detalle (HU-09).
                            onPostClick = { id -> selectedPostId = id },
                            // Botón "★ Favoritos" de la barra superior.
                            onOpenFavorites = { showFavorites = true },
                            // Botón "Mi perfil" de la barra superior (R5).
                            onOpenProfile = { showProfile = true }
                        )
                    }

                    // Pantalla de login (inicial cuando no hay sesión).
                    else -> {
                        LoginScreen(
                            onLoggedIn = {
                                // Login OK: entramos al feed.
                                loginMessage = null
                                showFeed = true
                            },
                            onGoToRegister = {
                                // "Crear cuenta" -> pantalla de registro (HU-04).
                                loginMessage = null
                                showRegister = true
                            },
                            // Mensaje opcional (por ejemplo, tras registrarse).
                            infoMessage = loginMessage
                        )
                    }
                }
            }
        }
    }
}
