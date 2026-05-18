package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class OrientadorEmpresa(
    @SerializedName("idUtilizador") val idUtilizador: String = "",
    @SerializedName("idEmpresa") val idEmpresa: String = "",
    @SerializedName("area") val area: String? = null,
    @SerializedName("status") val status: String = "ativo",
    @SerializedName("created_at") val createdAt: String = ""
)
