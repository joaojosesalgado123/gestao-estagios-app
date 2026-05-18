package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class RelatorioFinal(
    @SerializedName("idRelatorio") val idRelatorio: String = "",
    @SerializedName("ficheiro") val ficheiro: String? = null,
    @SerializedName("data_submissao") val dataSubmissao: String = "",
    @SerializedName("observacoes") val observacoes: String? = null,
    @SerializedName("idEstagio") val idEstagio: String = "",
    @SerializedName("idUtilizador") val idUtilizador: String = "",
    @SerializedName("created_at") val createdAt: String = ""
)
