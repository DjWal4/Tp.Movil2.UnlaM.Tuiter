// Import necesario para leer local.properties (archivo de configuración local).
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// ============================================================
// SEGURIDAD: Application-Token fuera del código (consigna, sección 4)
// ============================================================
// El token NO se escribe en un archivo .kt (porque eso se sube al repo).
// Se lee de local.properties, que está en .gitignore y no se versiona.
// Luego se expone a Kotlin como BuildConfig.APPLICATION_TOKEN.
//
// Si el archivo no existe o no tiene la clave, el valor queda vacío
// y la app no podrá autenticarse contra la API (ver README).
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

// applicationToken: valor de APPLICATION_TOKEN en local.properties (o "").
val applicationToken: String = localProperties.getProperty("APPLICATION_TOKEN") ?: ""

android {
    namespace = "ar.edu.unlam.tuiter"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "ar.edu.unlam.tuiter"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Genera BuildConfig.APPLICATION_TOKEN con el valor leído arriba.
        // Las comillas escapadas son necesarias: el valor en Java/Kotlin
        // tiene que quedar como un String ("abc..."), no como un identificador.
        buildConfigField("String", "APPLICATION_TOKEN", "\"$applicationToken\"")
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        // Habilita la clase BuildConfig (donde queda el token leído de local.properties).
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.coil.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Base de datos local (Room 3) con KSP.
    implementation(libs.androidx.room3.runtime)
    ksp(libs.androidx.room3.compiler)
}