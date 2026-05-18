package pt.ligix.app.model

data class InstituicaoEnsino(
    val idInstituicao: String = "",
    val nome: String = "",
    val sigla: String? = null,
    val morada: String? = null,
    val email: String? = null,
    val telefone: String? = null,
    val nipc: String? = null,
    val createdAt: String = ""
)
