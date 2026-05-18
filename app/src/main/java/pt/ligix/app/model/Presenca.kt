package pt.ligix.app.model

import com.google.gson.annotations.SerializedName

data class Presenca(
    @SerializedName("idPresenca") val idPresenca: String = "",
    @SerializedName("data") val data: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("idEstagio") val idEstagio: String = "",
    @SerializedName("created_at") val createdAt: String = ""
)
