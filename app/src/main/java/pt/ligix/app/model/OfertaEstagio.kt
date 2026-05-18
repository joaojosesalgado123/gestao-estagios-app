package pt.ligix.app.model

data class OfertaEstagio(
    val idOferta: String = "",
    val titulo: String = "",
    val descricao: String? = null,
    val area: String? = null,
    val localizacao: String? = null,
    val duracao: Int? = null,
    val dataPublicacao: String = "",
    val numeroVagas: Int = 1,
    val idEmpresa: String = "",
    val createdAt: String = ""
)
