package ar.edu.unlam.tuiter.data.model

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    // Lo dejamos nullable por si la API devuelve null o no viene el campo.
    @SerializedName("avatar_url") val avatarUrl: String?
)