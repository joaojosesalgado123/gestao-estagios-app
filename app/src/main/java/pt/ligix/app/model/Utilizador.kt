package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class Utilizador(
    @SerializedName("idUtilizador") val idUtilizador: String = "",
    @SerializedName("username") val username: String = "",
    @SerializedName("password") val password: String = "",
    @SerializedName("nome") val nome: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("role") val role: String = "",
    @SerializedName("language") val language: String = "pt",
    @SerializedName("fotografia") val fotografia: String? = null,
    @SerializedName("created_at") val createdAt: String = ""
)
