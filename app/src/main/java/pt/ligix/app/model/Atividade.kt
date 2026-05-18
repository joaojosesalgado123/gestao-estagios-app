package pt.ligix.app.model

data class Atividade(
    val idAtividade: String = "",
    val titulo: String = "",
    val descricao: String? = null,
    val dataAtividade: String? = null,
    val dataRegisto: String = "",
    val idEstagio: String = "",
    val createdAt: String = ""
)
