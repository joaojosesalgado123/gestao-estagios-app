package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class InstituicaoEnsino(
    @SerializedName("idinstituicao") val idInstituicao: String = "",
    @SerializedName("idutilizador") val idUtilizador: String? = null,
    @SerializedName("nome") val nome: String = "",
    @SerializedName("sigla") val sigla: String? = null,
    @SerializedName("morada") val morada: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("telefone") val telefone: String? = null,
    @SerializedName("nipc") val nipc: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)
