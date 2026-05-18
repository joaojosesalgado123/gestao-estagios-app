package pt.ligix.app.model

data class Avaliacao(
    val idAvaliacao: String = "",
    val classificacao: Double? = null,
    val comentario: String? = null,
    val dataAvaliacao: String = "",
    val idEstagio: String = "",
    val createdAt: String = ""
)
