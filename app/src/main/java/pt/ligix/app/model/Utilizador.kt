package pt.ligix.app.model

data class Utilizador(
    val idUtilizador: String = "",
    val username: String = "",
    val password: String = "",
    val nome: String = "",
    val email: String = "",
    val role: String = "",
    val language: String = "pt",
    val fotografia: String? = null,
    val createdAt: String = ""
)
