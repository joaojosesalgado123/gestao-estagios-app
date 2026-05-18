package pt.ligix.app.model

data class ItemAvaliacao(
    val idAvaliacao: String = "",
    val idAvaliador: String = "",
    val classificacao: Double? = null,
    val comentario: String? = null,
    val dataAvaliacao: String = ""
)
