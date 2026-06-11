package pt.ligix.app.data.repository

import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Empresa
import pt.ligix.app.viewmodel.EmpresaPendenteCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import pt.ligix.app.viewmodel.ResumoAtividadeEmpresas
import pt.ligix.app.viewmodel.EmpresaDetalhe
import pt.ligix.app.viewmodel.EmpresaListagem
import pt.ligix.app.viewmodel.EstatisticasDashboard
import pt.ligix.app.viewmodel.UtilizadorEdicao

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

    suspend fun getUtilizadorParaEdicao(idutilizador: String, role: String): Result<UtilizadorEdicao> {
        return try {
            val utilResp = api.getUtilizadoresByIds(ids = inFilter(listOf(idutilizador)))
            if (!utilResp.isSuccessful) {
                return Result.failure(Exception("Erro: ${utilResp.code()}"))
            }
            val utilizador = utilResp.body()?.firstOrNull()
                ?: return Result.failure(Exception("Utilizador não encontrado"))

            val extras: List<Pair<String, String?>> = when (role) {
                "aluno" -> fetchCamposAluno(idutilizador)
                "docente" -> fetchCamposDocente(idutilizador)
                "orientador" -> fetchCamposOrientador(idutilizador)
                "empresa" -> fetchCamposEmpresa(idutilizador)
                else -> emptyList()
            }

            Result.success(
                UtilizadorEdicao(
                    idUtilizador = utilizador.idUtilizador ?: "",
                    nome = utilizador.nome,
                    email = utilizador.email,
                    role = utilizador.role,
                    username = utilizador.username,
                    language = utilizador.language,
                    createdAt = utilizador.createdAt,
                    camposExtras = extras
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun atualizarDadosBasicosUtilizador(
        idutilizador: String,
        novoNome: String,
        novoEmail: String
    ): Result<pt.ligix.app.model.Utilizador> {
        return try {
            val getResp = api.getUtilizadoresByIds(ids = inFilter(listOf(idutilizador)))
            if (!getResp.isSuccessful) {
                return Result.failure(Exception("Erro: ${getResp.code()}"))
            }
            val atual = getResp.body()?.firstOrNull()
                ?: return Result.failure(Exception("Utilizador não encontrado"))

            val novo = atual.copy(nome = novoNome, email = novoEmail)

            val updResp = api.adminUpdateUtilizador(
                id = "eq.$idutilizador",
                utilizador = novo
            )
            if (updResp.isSuccessful) {
                Result.success(updResp.body()?.firstOrNull() ?: novo)
            } else {
                Result.failure(Exception("Erro: ${updResp.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    private suspend fun fetchCamposAluno(id: String): List<Pair<String, String?>> {
        return try {
            val resp = api.getAlunoById(idUtilizador = "eq.$id")
            val aluno = resp.body()?.firstOrNull() ?: return emptyList()
            listOf(
                "NÚMERO DE ALUNO" to aluno.numeroAluno,
                "CURSO" to aluno.curso,
                "TELEMÓVEL" to aluno.telemovel
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchCamposDocente(id: String): List<Pair<String, String?>> {
        return try {
            val resp = api.getDocenteById(idUtilizador = "eq.$id")
            val docente = resp.body()?.firstOrNull() ?: return emptyList()
            listOf(
                "ÁREA" to docente.area,
                "TELEMÓVEL" to docente.telemovel
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchCamposOrientador(id: String): List<Pair<String, String?>> {
        return try {
            val resp = api.getOrientadoresPorUtilizador(idUtilizador = "eq.$id")
            val orientador = resp.body()?.firstOrNull() ?: return emptyList()
            listOf(
                "ÁREA" to orientador.area,
                "TELEMÓVEL" to orientador.telemovel,
                "ESTADO" to orientador.status
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchCamposEmpresa(id: String): List<Pair<String, String?>> {
        return try {
            val resp = api.getEmpresaById(idUtilizador = "eq.$id")
            val empresa = resp.body()?.firstOrNull() ?: return emptyList()
            listOf(
                "NIPC" to empresa.nipc,
                "MORADA" to empresa.morada,
                "TELEMÓVEL" to empresa.telemovel,
                "DESCRIÇÃO" to empresa.descricao
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun eliminarUtilizador(idutilizador: String): Result<Unit> {
        return try {
            val response = api.eliminarUtilizador(body = mapOf("p_idutilizador" to idutilizador))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun getTodasEmpresasComNome(): Result<List<EmpresaListagem>> {
        return try {
            // 1. Buscar todas as empresas
            val empresasResp = api.getTodasEmpresas()
            if (!empresasResp.isSuccessful) {
                return Result.failure(Exception("Erro: ${empresasResp.code()}"))
            }
            val empresas = empresasResp.body().orEmpty()
            if (empresas.isEmpty()) return Result.success(emptyList())

            // 2. Buscar os utilizadores (para os nomes)
            val ids = empresas.map { it.idUtilizador }
            val utilizadoresResp = api.getUtilizadoresByIds(ids = inFilter(ids))
            if (!utilizadoresResp.isSuccessful) {
                return Result.failure(Exception("Erro: ${utilizadoresResp.code()}"))
            }
            val nomesPorId = utilizadoresResp.body().orEmpty()
                .associateBy({ it.idUtilizador }, { it.nome })

            // 3. Combinar
            val listagem = empresas.map { empresa ->
                EmpresaListagem(
                    idEmpresa = empresa.idUtilizador,
                    nome = nomesPorId[empresa.idUtilizador] ?: "Empresa sem nome",
                    descricao = empresa.descricao,
                    status = empresa.status,
                    createdAt = empresa.createdAt
                )
            }
            Result.success(listagem)
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun getEstatisticasDashboard(): Result<EstatisticasDashboard> {
        return try {
            // Total de utilizadores (excluindo admin para alinhar com listagem)
            val utilResp = api.getUtilizadoresComFiltro(role = "neq.admin")
            if (!utilResp.isSuccessful) {
                return Result.failure(Exception("Erro: ${utilResp.code()}"))
            }
            val totalUtilizadores = utilResp.body().orEmpty().size

            // Estagiários ativos
            val estagiosResp = api.getEstagiosByStatus(status = "eq.ativo")
            if (!estagiosResp.isSuccessful) {
                return Result.failure(Exception("Erro: ${estagiosResp.code()}"))
            }
            val estagiariosAtivos = estagiosResp.body().orEmpty().size

            // Empresas pendentes
            val empresasResp = api.getEmpresasByStatus(status = "eq.pendente")
            if (!empresasResp.isSuccessful) {
                return Result.failure(Exception("Erro: ${empresasResp.code()}"))
            }
            val empresasPendentes = empresasResp.body().orEmpty().size

            Result.success(
                EstatisticasDashboard(
                    totalUtilizadores = totalUtilizadores,
                    estagiariosAtivos = estagiariosAtivos,
                    empresasPendentes = empresasPendentes
                )
            )
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