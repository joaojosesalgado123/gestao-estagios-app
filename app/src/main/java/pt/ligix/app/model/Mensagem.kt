package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class Mensagem(
    @SerializedName("idmensagem") val idMensagem: String = "",
    @SerializedName("idremetente") val idRemetente: String = "",
    @SerializedName("conteudo") val conteudo: String = "",
    @SerializedName("data_envio") val dataEnvio: String = "",
    @SerializedName("idconversa") val idConversa: String = "",
    @SerializedName("ficheiro_url") val ficheiroUrl: String? = null,
    @SerializedName("ficheiro_nome") val ficheiroNome: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
