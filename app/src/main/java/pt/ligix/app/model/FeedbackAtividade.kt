package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class FeedbackAtividade(
    @SerializedName("idfeedback") val idFeedback: String = "",
    @SerializedName("idatividade") val idAtividade: String = "",
    @SerializedName("idestagio") val idEstagio: String = "",
    @SerializedName("iddocente") val idDocente: String = "",
    @SerializedName("comentario") val comentario: String = "",
    @SerializedName("created_at") val createdAt: String = ""
)
