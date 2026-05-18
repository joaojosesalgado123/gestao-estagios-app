package pt.ligix.app.model

data class Estagio(
    val idEstagio: String = "",
    val startDate: String? = null,
    val endDate: String? = null,
    val status: String = "ativo",
    val classificacaoFinal: Double? = null,
    val idDocente: String? = null,
    val idOrientador: String? = null,
    val idCandidatura: String = "",
    val createdAt: String = ""
)
