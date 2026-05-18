package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class Avaliacao(
    @SerializedName("idAvaliacao") val idAvaliacao: String = "",
    @SerializedName("classificacao") val classificacao: Double? = null,
    @SerializedName("comentario") val comentario: String? = null,
    @SerializedName("data_avaliacao") val dataAvaliacao: String = "",
    @SerializedName("idEstagio") val idEstagio: String = "",
    @SerializedName("created_at") val createdAt: String = ""
)
