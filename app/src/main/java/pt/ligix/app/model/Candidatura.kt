package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class Candidatura(
    @SerializedName("idcandidatura") val idCandidatura: String = "",
    @SerializedName("status") val status: String = "pendente",
    @SerializedName("cv_ficheiro") val cvFicheiro: String? = null,
    @SerializedName("carta_motivacao_ficheiro") val cartaMotivacaoFicheiro: String? = null,
    @SerializedName("data") val data: String = "",
    @SerializedName("idoferta") val idOferta: String = "",
    @SerializedName("idaluno") val idAluno: String = "",
    @SerializedName("created_at") val createdAt: String = ""
)
