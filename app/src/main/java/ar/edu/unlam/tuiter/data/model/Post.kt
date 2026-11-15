package ar.edu.unlam.tuiter.data.model

import com.google.gson.annotations.SerializedName

// Modelo de un post/tuit.
// Usamos "alternate" para aceptar tanto los nombres del Anexo IV (ID, Message...)
// como posibles variantes en minúscula (id, message...).
// Todos los campos son nullable para evitar crashes si la API no manda alguno.
data class Post(
    @SerializedName(value = "ID", alternate = ["id"])
    val id: Int? = null,

    @SerializedName(value = "Message", alternate = ["message"])
    val message: String? = null,

    @SerializedName(value = "ParentID", alternate = ["parent_id", "parentId"])
    val parentId: Int? = null,

    @SerializedName(value = "Author", alternate = ["author"])
    val author: String? = null,

    @SerializedName(value = "AvatarURL", alternate = ["avatar_url", "avatarUrl"])
    val avatarUrl: String? = null,

    @SerializedName(value = "Likes", alternate = ["likes"])
    val likes: Int? = null,

    @SerializedName(value = "Liked", alternate = ["liked"])
    val liked: Boolean? = null,

    @SerializedName(value = "Date", alternate = ["date"])
    val date: String? = null
)