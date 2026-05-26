package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class Conversa(
    @SerializedName("idconversa") val idConversa: String = "",
    @SerializedName("idestagio") val idEstagio: String = "",
    @SerializedName("tipo_conversa") val tipoConversa: String? = null,
    @SerializedName("data_criacao") val dataCriacao: String = "",
    @SerializedName("estado") val estado: String = "ativa",
    @SerializedName("created_at") val createdAt: String = ""
)
