package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class Empresa(
    @SerializedName("idUtilizador") val idUtilizador: String = "",
    @SerializedName("morada") val morada: String? = null,
    @SerializedName("NIPC") val nipc: String? = null,
    @SerializedName("telemovel") val telemovel: String? = null,
    @SerializedName("descricao") val descricao: String? = null,
    @SerializedName("status") val status: String = "pendente",
    @SerializedName("created_at") val createdAt: String = ""
)
