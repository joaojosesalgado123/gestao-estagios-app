package pt.ligix.app.model

data class Conversa(
    val idConversa: String = "",
    val idEstagio: String = "",
    val tipoConversa: String? = null,
    val dataCriacao: String = "",
    val estado: String = "ativa",
    val createdAt: String = ""
)
