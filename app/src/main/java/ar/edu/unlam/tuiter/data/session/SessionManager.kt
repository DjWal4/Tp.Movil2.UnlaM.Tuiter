package ar.edu.unlam.tuiter.data.session

import android.content.Context
import android.content.SharedPreferences

// Manejo de la sesión del usuario (token).
//
// Paso 15: ahora el token se guarda también en SharedPreferences,
// así la sesión SOBREVIVE al cerrar la app.
// Antes era solo en memoria: al cerrar la app había que loguearse de nuevo.
object SessionManager {

    private const val PREFS_NAME = "tuiter_session"
    private const val KEY_TOKEN = "user_token"

    // SharedPreferences se inicializa una vez desde TuiterApplication.
    private var prefs: SharedPreferences? = null

    // Token de usuario (el que devuelve el login). Puede ser null si no hay sesión.
    var userToken: String? = null
        private set   // Solo se modifica desde saveToken()/clear()

    // Indica si hay un usuario logueado.
    val isLoggedIn: Boolean
        get() = userToken != null

    // Se llama UNA vez al arrancar la app (TuiterApplication.onCreate).
    // Recupera el token guardado, si existe.
    fun init(context: Context) {
        // Si ya estaba inicializado, no hacemos nada.
        if (prefs != null) return

        val p = context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = p

        // Si había un token guardado, lo recuperamos en memoria.
        userToken = p.getString(KEY_TOKEN, null)
    }

    // Guarda el token recibido del login (en memoria Y en disco).
    fun saveToken(token: String) {
        userToken = token
        prefs?.edit()?.putString(KEY_TOKEN, token)?.apply()
    }

    // Borra la sesión (lo usa "Cerrar sesión", Paso 15).
    fun clear() {
        userToken = null
        prefs?.edit()?.remove(KEY_TOKEN)?.apply()
    }
}
