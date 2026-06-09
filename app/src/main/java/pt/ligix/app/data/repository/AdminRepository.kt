package pt.ligix.app.data.repository

import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Empresa
import pt.ligix.app.viewmodel.EmpresaPendenteCard

class AdminRepository {

    private val api = RetrofitClient.api

    private fun inFilter(ids: Collection<String>): String =
        ids.distinct().joinToString(separator = ",", prefix = "in.(", postfix = ")")

    suspend fun getEmpresasPendentesComNome(): Result<List<EmpresaPendenteCard>> {
        return try {
            // 1. Buscar empresas com status = pendente
            val empresasResp = api.getEmpresasPendentes()
            if (!empresasResp.isSuccessful) {
                return Result.failure(Exception("Erro: ${empresasResp.code()}"))
            }
            val empresas = empresasResp.body().orEmpty()
            if (empresas.isEmpty()) return Result.success(emptyList())

            // 2. Buscar os utilizadores correspondentes (para obter o nome)
            val ids = empresas.map { it.idUtilizador }
            val utilizadoresResp = api.getUtilizadoresByIds(ids = inFilter(ids))
            if (!utilizadoresResp.isSuccessful) {
                return Result.failure(Exception("Erro: ${utilizadoresResp.code()}"))
            }
            val nomesPorId = utilizadoresResp.body().orEmpty()
                .associateBy({ it.idUtilizador }, { it.nome })

            // 3. Combinar num modelo de UI
            val cards = empresas.map { empresa ->
                EmpresaPendenteCard(
                    idEmpresa = empresa.idUtilizador,
                    nome = nomesPorId[empresa.idUtilizador] ?: "Empresa sem nome",
                    descricao = empresa.descricao,
                    createdAt = empresa.createdAt
                )
            }
            Result.success(cards)
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun aprovarEmpresa(idEmpresa: String): Result<Empresa> {
        return atualizarStatusEmpresa(idEmpresa, "aprovada")
    }

    suspend fun rejeitarEmpresa(idEmpresa: String): Result<Empresa> {
        return atualizarStatusEmpresa(idEmpresa, "rejeitada")
    }

    private suspend fun atualizarStatusEmpresa(
        idEmpresa: String,
        novoStatus: String
    ): Result<Empresa> {
        return try {
            val response = api.updateEmpresaStatus(
                id = "eq.$idEmpresa",
                status = mapOf("status" to novoStatus)
            )
            if (response.isSuccessful) {
                val empresaActualizada = response.body()?.firstOrNull()
                if (empresaActualizada != null) {
                    Result.success(empresaActualizada)
                } else {
                    Result.failure(Exception("Empresa não encontrada"))
                }
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }
}