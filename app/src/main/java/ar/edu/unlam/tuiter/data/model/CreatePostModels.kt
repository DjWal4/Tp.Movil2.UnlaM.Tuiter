package ar.edu.unlam.tuiter.data.model

import com.google.gson.annotations.SerializedName


// Body de POST /api/v1/me/tuits
data class CreatePostRequest(@SerializedName("message") val message: String
)