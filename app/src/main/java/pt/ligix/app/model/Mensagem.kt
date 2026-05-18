package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class Mensagem(
    @SerializedName("idMensagem") val idMensagem: String = "",
    @SerializedName("idRemetente") val idRemetente: String = "",
    @SerializedName("conteudo") val conteudo: String = "",
    @SerializedName("data_envio") val dataEnvio: String = "",
    @SerializedName("idConversa") val idConversa: String = "",
    @SerializedName("created_at") val createdAt: String = ""
)
