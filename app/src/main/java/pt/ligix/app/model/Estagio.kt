package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class Estagio(
    @SerializedName("idestagio") val idEstagio: String = "",
    @SerializedName("start_date") val startDate: String? = null,
    @SerializedName("end_date") val endDate: String? = null,
    @SerializedName("status") val status: String = "ativo",
    @SerializedName("classificacaofinal") val classificacaoFinal: Double? = null,
    @SerializedName("iddocente") val idDocente: String? = null,
    @SerializedName("idorientador") val idOrientador: String? = null,
    @SerializedName("idcandidatura") val idCandidatura: String = "",
    @SerializedName("created_at") val createdAt: String = ""
)
