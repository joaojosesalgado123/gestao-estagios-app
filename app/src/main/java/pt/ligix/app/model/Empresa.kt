package pt.ligix.app.model

data class Empresa(
    val idUtilizador: String = "",
    val morada: String? = null,
    val nipc: String? = null,
    val telemovel: String? = null,
    val descricao: String? = null,
    val status: String = "pendente",
    val createdAt: String = ""
)
