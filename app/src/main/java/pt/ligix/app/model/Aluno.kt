package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class Aluno(
    @SerializedName("idutilizador") val idUtilizador: String = "",
    @SerializedName("numero_aluno") val numeroAluno: String = "",
    @SerializedName("curso") val curso: String = "",
    @SerializedName("telemovel") val telemovel: String? = null,
    @SerializedName("idinstituicao") val idInstituicao: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
