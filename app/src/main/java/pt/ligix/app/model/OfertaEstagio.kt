package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class OfertaEstagio(
    @SerializedName("idOferta") val idOferta: String = "",
    @SerializedName("titulo") val titulo: String = "",
    @SerializedName("descricao") val descricao: String? = null,
    @SerializedName("area") val area: String? = null,
    @SerializedName("localizacao") val localizacao: String? = null,
    @SerializedName("duracao") val duracao: Int? = null,
    @SerializedName("data_publicacao") val dataPublicacao: String = "",
    @SerializedName("numero_vagas") val numeroVagas: Int = 1,
    @SerializedName("idEmpresa") val idEmpresa: String = "",
    @SerializedName("created_at") val createdAt: String = ""
)
