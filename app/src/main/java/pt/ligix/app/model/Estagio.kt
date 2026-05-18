package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class Estagio(
    @SerializedName("idEstagio") val idEstagio: String = "",
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("end_date") val endDate: String? = null,
    @SerializedName("status") val status: String = "ativo",
    @SerializedName("classificacaoFinal") val classificacaoFinal: Double? = null,
    @SerializedName("idDocente") val idDocente: String? = null,
    @SerializedName("idOrientador") val idOrientador: String? = null,
    @SerializedName("idCandidatura") val idCandidatura: String = "",
    @SerializedName("created_at") val createdAt: String = ""
)
