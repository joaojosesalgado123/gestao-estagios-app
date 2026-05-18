package pt.ligix.app.model

data class OrientadorEmpresa(
    val idUtilizador: String = "",
    val idEmpresa: String = "",
    val area: String? = null,
    val status: String = "ativo",
    val createdAt: String = ""
)
