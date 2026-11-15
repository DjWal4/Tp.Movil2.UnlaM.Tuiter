package ar.edu.unlam.tuiter

import android.app.Application
import ar.edu.unlam.tuiter.data.session.SessionManager
import ar.edu.unlam.tuiter.di.AppContainer

// Application propia: es la clase que Android crea UNA sola vez
// cuando arranca la app (antes de cualquier Activity).
// La usamos como "composition root": acá se arma el contenedor de DI.
//
// Para que Android la use, está declarada en AndroidManifest.xml:
//   <application android:name=".TuiterApplication" ...>
class TuiterApplication : Application() {

    // Contenedor de dependencias (Paso 14).
    // lateinit: se inicializa en onCreate, no en el constructor.
    // private set: nadie de afuera puede reemplazarlo.
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        // Paso 15: recuperamos la sesión guardada ANTES de crear el contenedor.
        // Así, si el usuario ya se había logueado, el token está disponible
        // desde el primer momento (el interceptor de Retrofit lo lee en cada request).
        SessionManager.init(this)

        // Se crea una sola vez para toda la vida del proceso.
        container = AppContainer(this)
    }
}
