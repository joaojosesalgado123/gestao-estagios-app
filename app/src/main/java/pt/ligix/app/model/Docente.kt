package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class Docente(
    @SerializedName("idUtilizador") val idUtilizador: String = "",
    @SerializedName("telemovel") val telemovel: String? = null,
    @SerializedName("area") val area: String? = null,
    @SerializedName("idInstituicao") val idInstituicao: String? = null,
    @SerializedName("created_at") val createdAt: String = ""
)
