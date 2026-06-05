package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class OrientadorEmpresa(
    @SerializedName("idutilizador") val idUtilizador: String = "",
    @SerializedName("idempresa") val idEmpresa: String = "",
    @SerializedName("area") val area: String? = null,
    @SerializedName("status") val status: String = "ativo",
    @SerializedName("telemovel") val telemovel: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
