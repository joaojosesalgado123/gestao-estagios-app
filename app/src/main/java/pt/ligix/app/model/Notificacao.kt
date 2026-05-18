package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class Notificacao(
    @SerializedName("idNotificacao") val idNotificacao: String = "",
    @SerializedName("idUtilizador") val idUtilizador: String = "",
    @SerializedName("titulo") val titulo: String = "",
    @SerializedName("mensagem") val mensagem: String = "",
    @SerializedName("tipo") val tipo: String = "",
    @SerializedName("lida") val lida: Boolean = false,
    @SerializedName("created_at") val createdAt: String = ""
)
