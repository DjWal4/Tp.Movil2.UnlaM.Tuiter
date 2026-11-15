package ar.edu.unlam.tuiter.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("token") val token: String
)