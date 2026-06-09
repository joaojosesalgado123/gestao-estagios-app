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
