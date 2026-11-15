package ar.edu.unlam.tuiter.data.model

import com.google.gson.annotations.SerializedName

// ============================================================
// MODELOS DE AUTENTICACIÓN Y PERFIL (consigna R4 y R5)
// ============================================================
// Estos data class representan el "body" (cuerpo) que se envía a la API
// en cada request. Gson los convierte a JSON automáticamente.

// Body de POST /api/v1/users  ->  registrar un usuario nuevo (R4).
// La API pide nombre, email y contraseña.
data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

// Body de PUT /api/v1/me/profile  ->  editar el perfil (R5).
//
// Los tres campos son NULLABLE a propósito:
//  - Si un campo viene null, Gson NO lo incluye en el JSON.
//  - Así solo se envía lo que el usuario realmente cambió
//    (por ejemplo, si solo cambia el nombre, no se manda la contraseña).
// El email NO está acá porque la consigna dice que no se puede modificar.
data class UpdateProfileRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    @SerializedName("password") val password: String? = null
)
