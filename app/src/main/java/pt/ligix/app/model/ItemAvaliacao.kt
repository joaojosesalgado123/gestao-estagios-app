package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class ItemAvaliacao(
    @SerializedName("idavaliacao") val idAvaliacao: String = "",
    @SerializedName("idavaliador") val idAvaliador: String = "",
    @SerializedName("classificacao") val classificacao: Double? = null,
    @SerializedName("comentario") val comentario: String? = null,
    @SerializedName("data_avaliacao") val dataAvaliacao: String = ""
)
