package pt.ligix.app.data.repository

import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Empresa
import pt.ligix.app.viewmodel.EmpresaPendenteCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import pt.ligix.app.viewmodel.ResumoAtividadeEmpresas
import pt.ligix.app.viewmodel.EmpresaDetalhe

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

    suspend fun getResumoAtividadeEmpresas(): Result<ResumoAtividadeEmpresas> {
        return try {
            // Contar pendentes
            val pendentesResp = api.getEmpresasByStatus(status = "eq.pendente")
            if (!pendentesResp.isSuccessful) {
                return Result.failure(Exception("Erro: ${pendentesResp.code()}"))
            }
            val pendentes = pendentesResp.body().orEmpty().size

            // Contar aprovadas e filtrar para o mês atual
            val aprovadasResp = api.getEmpresasByStatus(status = "eq.aprovada")
            if (!aprovadasResp.isSuccessful) {
                return Result.failure(Exception("Erro: ${aprovadasResp.code()}"))
            }
            val mesAtual = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
            val aprovadasNoMes = aprovadasResp.body().orEmpty().count { empresa ->
                empresa.createdAt?.startsWith(mesAtual) == true
            }

            // Contar rejeitadas
            val rejeitadasResp = api.getEmpresasByStatus(status = "eq.rejeitada")
            if (!rejeitadasResp.isSuccessful) {
                return Result.failure(Exception("Erro: ${rejeitadasResp.code()}"))
            }
            val rejeitadas = rejeitadasResp.body().orEmpty().size

            Result.success(
                ResumoAtividadeEmpresas(
                    pendentes = pendentes,
                    aprovadasNoMes = aprovadasNoMes,
                    rejeitadas = rejeitadas
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun getEmpresaDetalhe(idEmpresa: String): Result<EmpresaDetalhe> {
        return try {
            // 1. Buscar a empresa
            val empresaResp = api.getEmpresaById(idUtilizador = "eq.$idEmpresa")
            if (!empresaResp.isSuccessful) {
                return Result.failure(Exception("Erro: ${empresaResp.code()}"))
            }
            val empresa = empresaResp.body()?.firstOrNull()
                ?: return Result.failure(Exception("Empresa não encontrada"))

            // 2. Buscar o utilizador correspondente (para o nome)
            val utilizadoresResp = api.getUtilizadoresByIds(ids = inFilter(listOf(idEmpresa)))
            if (!utilizadoresResp.isSuccessful) {
                return Result.failure(Exception("Erro: ${utilizadoresResp.code()}"))
            }
            val nome = utilizadoresResp.body().orEmpty()
                .firstOrNull { it.idUtilizador == idEmpresa }
                ?.nome
                ?: "Empresa sem nome"

            // 3. Combinar
            Result.success(
                EmpresaDetalhe(
                    idEmpresa = empresa.idUtilizador,
                    nome = nome,
                    nipc = empresa.nipc,
                    morada = empresa.morada,
                    descricao = empresa.descricao,
                    telemovel = empresa.telemovel,
                    createdAt = empresa.createdAt,
                    status = empresa.status
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun getUtilizadoresNaoAdmin(): Result<List<pt.ligix.app.model.Utilizador>> {
        return try {
            val response = api.getUtilizadoresComFiltro(role = "neq.admin")
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty())
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
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