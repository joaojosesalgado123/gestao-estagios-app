package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class Atividade(
    @SerializedName("idAtividade") val idAtividade: String = "",
    @SerializedName("titulo") val titulo: String = "",
    @SerializedName("descricao") val descricao: String? = null,
    @SerializedName("data_atividade") val dataAtividade: String? = null,
    @SerializedName("data_registo") val dataRegisto: String = "",
    @SerializedName("idEstagio") val idEstagio: String = "",
    @SerializedName("created_at") val createdAt: String = ""
)
