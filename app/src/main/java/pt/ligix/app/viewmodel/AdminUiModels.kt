package pt.ligix.app.viewmodel

/**
 * Modelo de UI para um cartão de empresa pendente.
 * Combina dados de `utilizador` (nome) e `empresa` (descrição, etc.).
 * Existe separado de `Empresa` porque a UI quer dados já compostos
 * e prontos a apresentar, sem ter de juntar entidades cada vez que recompõe.
 */
data class EmpresaPendenteCard(
    val idEmpresa: String,
    val nome: String,
    val descricao: String?,
    val createdAt: String?
)

/**
 * Resumo de atividade das aprovações de empresas, mostrado no fim do
 * ecrã de Aprovações. As "aprovadas no mês" referem-se ao mês atual.
 */
data class ResumoAtividadeEmpresas(
    val pendentes: Int,
    val aprovadasNoMes: Int,
    val rejeitadas: Int
)

/**
 * Tudo o que o ecrã de edição de utilizador precisa: dados básicos (editáveis)
 * + uma lista de pares (label, valor) com os campos específicos da role,
 * mostrados como leitura apenas.
 */
data class UtilizadorEdicao(
    val idUtilizador: String,
    val nome: String,
    val email: String,
    val role: String,
    val username: String,
    val language: String,
    val createdAt: String?,
    val camposExtras: List<Pair<String, String?>>
)
