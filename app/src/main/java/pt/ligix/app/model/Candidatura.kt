package pt.ligix.app.model

data class Candidatura(
    val idCandidatura: String = "",
    val status: String = "pendente",
    val cvFicheiro: String? = null,
    val cartaMotivacaoFicheiro: String? = null,
    val data: String = "",
    val idOferta: String = "",
    val idAluno: String = "",
    val createdAt: String = ""
)
