package pt.ligix.app.model

data class RelatorioFinal(
    val idRelatorio: String = "",
    val ficheiro: String? = null,
    val dataSubmissao: String = "",
    val observacoes: String? = null,
    val idEstagio: String = "",
    val idUtilizador: String = "",
    val createdAt: String = ""
)
