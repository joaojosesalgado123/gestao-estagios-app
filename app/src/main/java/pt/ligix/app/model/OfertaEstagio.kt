package pt.ligix.app.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class OfertaEstagio(
    @SerializedName("idoferta") val idOferta: String = "",
    @SerializedName("titulo") val titulo: String = "",
    @SerializedName("descricao") val descricao: String? = null,
    @SerializedName("area") val area: String? = null,
    @SerializedName("localizacao") val localizacao: String? = null,
    @SerializedName("duracao") val duracao: Int? = null,
    @SerializedName("data_publicacao") val dataPublicacao: String = "",
    @SerializedName("numero_vagas") val numeroVagas: Int = 1,
    @SerializedName("idempresa") val idEmpresa: String = "",
    @IgnoredOnParcel @Transient val nomeEmpresa: String? = null,
    @SerializedName("created_at") val createdAt: String = ""
) : Parcelable
