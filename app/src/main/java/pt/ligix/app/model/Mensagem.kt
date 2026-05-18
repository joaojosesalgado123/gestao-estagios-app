package pt.ligix.app.model

data class Mensagem(
    val idMensagem: String = "",
    val idRemetente: String = "",
    val conteudo: String = "",
    val dataEnvio: String = "",
    val idConversa: String = "",
    val createdAt: String = ""
)
