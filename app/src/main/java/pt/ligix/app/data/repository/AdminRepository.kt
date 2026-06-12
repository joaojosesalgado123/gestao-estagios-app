package pt.ligix.app.data.repository

import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.data.remote.SupabaseAuthClient
import pt.ligix.app.data.remote.SupabaseSignUpRequest
import pt.ligix.app.model.Empresa
import pt.ligix.app.model.InstituicaoEnsino
import pt.ligix.app.viewmodel.EmpresaDetalhe
import pt.ligix.app.viewmodel.EmpresaListagem
import pt.ligix.app.viewmodel.EmpresaPendenteCard
import pt.ligix.app.viewmodel.EstatisticasDashboard
import pt.ligix.app.viewmodel.NovaInstituicaoEnsino
import pt.ligix.app.viewmodel.ResumoAtividadeEmpresas
import pt.ligix.app.viewmodel.UtilizadorEdicao
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        return try {
            val rpcResponse = api.rejeitarEmpresaAdmin(
                params = mapOf("p_idempresa" to idEmpresa)
            )
            val detalhe = detalheErro(rpcResponse)

            if (rpcResponse.isSuccessful) {
                if (rpcResponse.body() == true) {
                    obterEmpresa(idEmpresa)
                } else {
                    Result.failure(Exception("Empresa não encontrada"))
                }
            } else if (funcaoRejeitarEmpresaIndisponivel(rpcResponse.code(), detalhe)) {
                val limpeza = limparDadosEmpresaRejeitada(idEmpresa)
                if (limpeza.isFailure) {
                    Result.failure(limpeza.exceptionOrNull() ?: Exception("Erro ao limpar dados da empresa"))
                } else {
                    atualizarStatusEmpresa(idEmpresa, "rejeitada")
                }
            } else {
                Result.failure(Exception(mensagemErroRejeitarEmpresa(rpcResponse.code(), detalhe)))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
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

    suspend fun getInstituicoesEnsino(): Result<List<InstituicaoEnsino>> {
        return try {
            val response = api.getInstituicoes(
                select = "idinstituicao,idutilizador,nome,sigla,morada,email,telefone,nipc"
            )
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty())
            } else {
                Result.failure(Exception("Erro ao carregar instituições: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun criarInstituicaoEnsino(nova: NovaInstituicaoEnsino): Result<Unit> {
        return try {
            val metadata = mutableMapOf(
                "role" to "instituicao",
                "nome" to nova.nome,
                "username" to nova.username,
                "sigla" to nova.sigla,
                "email_institucional" to nova.email
            )
            nova.idInstituicao?.takeIf { it.isNotBlank() }?.let { metadata["idinstituicao"] = it }
            nova.telefone?.takeIf { it.isNotBlank() }?.let { metadata["telefone"] = it }
            nova.morada?.takeIf { it.isNotBlank() }?.let { metadata["morada"] = it }
            nova.nipc?.takeIf { it.isNotBlank() }?.let { metadata["nipc"] = it }

            val authResponse = SupabaseAuthClient.api.signUp(
                SupabaseSignUpRequest(
                    email = nova.email,
                    password = nova.password,
                    data = metadata
                )
            )

            if (!authResponse.isSuccessful) {
                return Result.failure(
                    Exception(
                        mensagemErroCriarContaInstituicao(
                            statusCode = authResponse.code(),
                            detalhe = detalheErro(authResponse)
                        )
                    )
                )
            }

            val idNovoUtilizador = authResponse.body()?.user?.id?.takeIf { it.isNotBlank() }
                ?: authResponse.body()?.id?.takeIf { it.isNotBlank() }
                ?: return Result.failure(Exception("A conta foi criada, mas não foi possível obter o ID do utilizador."))

            val body = mutableMapOf<String, Any?>(
                "idutilizador" to idNovoUtilizador,
                "nome" to nova.nome,
                "sigla" to nova.sigla,
                "email" to nova.email
            )
            nova.telefone?.takeIf { it.isNotBlank() }?.let { body["telefone"] = it }
            nova.morada?.takeIf { it.isNotBlank() }?.let { body["morada"] = it }
            nova.nipc?.takeIf { it.isNotBlank() }?.let { body["nipc"] = it }

            val idInstituicao = nova.idInstituicao?.takeIf { it.isNotBlank() }
            val instituicaoResponse = if (idInstituicao != null) {
                api.updateInstituicaoMap(
                    id = "eq.$idInstituicao",
                    body = body
                )
            } else {
                api.createInstituicaoEnsino(body = body)
            }
            if (instituicaoResponse.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception(
                        mensagemErroCriarInstituicao(
                            statusCode = instituicaoResponse.code(),
                            detalhe = detalheErro(instituicaoResponse)
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Sem ligação à internet"))
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
                "instituicao" -> fetchCamposInstituicao(idutilizador)
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

    private suspend fun fetchCamposInstituicao(id: String): List<Pair<String, String?>> {
        return try {
            val resp = api.getInstituicaoByIdUtilizador(idUtilizador = "eq.$id")
            val instituicao = resp.body()?.firstOrNull() ?: return emptyList()
            listOf(
                "SIGLA" to instituicao.sigla,
                "NIPC" to instituicao.nipc,
                "MORADA" to instituicao.morada,
                "TELEFONE" to instituicao.telefone,
                "EMAIL INSTITUCIONAL" to instituicao.email
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

    private suspend fun obterEmpresa(idEmpresa: String): Result<Empresa> {
        return try {
            val response = api.getEmpresaById(idUtilizador = "eq.$idEmpresa")
            if (response.isSuccessful) {
                response.body()?.firstOrNull()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empresa não encontrada"))
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    private suspend fun limparDadosEmpresaRejeitada(idEmpresa: String): Result<Unit> {
        return try {
            val ofertasResp = api.getOfertasByEmpresa(idEmpresa = "eq.$idEmpresa")
            if (!ofertasResp.isSuccessful) {
                return Result.failure(Exception("Erro ao carregar ofertas da empresa: ${ofertasResp.code()}"))
            }

            ofertasResp.body().orEmpty().forEach { oferta ->
                eliminarOfertaComFallback(oferta.idOferta).getOrElse { erro ->
                    return Result.failure(erro)
                }
            }

            val orientadoresResp = api.getOrientadoresByEmpresa(idEmpresa = "eq.$idEmpresa")
            if (!orientadoresResp.isSuccessful) {
                return Result.failure(Exception("Erro ao carregar orientadores da empresa: ${orientadoresResp.code()}"))
            }

            orientadoresResp.body().orEmpty().forEach { orientador ->
                val deleteUserResp = api.eliminarUtilizador(
                    body = mapOf("p_idutilizador" to orientador.idUtilizador)
                )
                if (!deleteUserResp.isSuccessful) {
                    val deleteLinkResp = api.deleteOrientadorEmpresa(
                        idOrientador = "eq.${orientador.idUtilizador}"
                    )
                    if (!deleteLinkResp.isSuccessful) {
                        return Result.failure(
                            Exception("Erro ao remover orientador da empresa: ${deleteLinkResp.code()}")
                        )
                    }
                }
            }

            val deleteLinksResp = api.deleteOrientadoresByEmpresa(idEmpresa = "eq.$idEmpresa")
            if (!deleteLinksResp.isSuccessful) {
                return Result.failure(Exception("Erro ao limpar orientadores da empresa: ${deleteLinksResp.code()}"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Sem ligação à internet"))
        }
    }

    private suspend fun eliminarOfertaComFallback(idOferta: String): Result<Unit> {
        val rpcResponse = api.eliminarOfertaEmpresa(
            params = mapOf("p_idoferta" to idOferta)
        )
        val rpcDetalhe = detalheErro(rpcResponse)
        if (rpcResponse.isSuccessful) {
            return if (rpcResponse.body() == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Não foi possível eliminar a oferta $idOferta."))
            }
        } else if (!funcaoEliminarOfertaIndisponivel(rpcResponse.code(), rpcDetalhe)) {
            return Result.failure(Exception(mensagemErroEliminarOferta(rpcResponse.code(), rpcDetalhe)))
        }

        val response = api.deleteOferta(id = "eq.$idOferta")
        return if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception(mensagemErroEliminarOferta(response.code(), detalheErro(response))))
        }
    }

    private fun detalheErro(response: retrofit2.Response<*>): String {
        return try {
            response.errorBody()?.string()
        } catch (_: Exception) {
            null
        }.orEmpty()
    }

    private fun funcaoRejeitarEmpresaIndisponivel(code: Int, detalhe: String): Boolean {
        return code == 404 ||
            detalhe.contains("rejeitar_empresa_admin", ignoreCase = true) &&
            (
                detalhe.contains("not find", ignoreCase = true) ||
                detalhe.contains("not found", ignoreCase = true) ||
                detalhe.contains("PGRST202", ignoreCase = true)
            )
    }

    private fun funcaoEliminarOfertaIndisponivel(code: Int, detalhe: String): Boolean {
        return code == 404 ||
            detalhe.contains("eliminar_oferta_empresa", ignoreCase = true) &&
            (
                detalhe.contains("not find", ignoreCase = true) ||
                detalhe.contains("not found", ignoreCase = true) ||
                detalhe.contains("PGRST202", ignoreCase = true)
            )
    }

    private fun mensagemErroRejeitarEmpresa(code: Int, detalhe: String): String {
        return when {
            code == 401 || code == 403 -> "Não tem permissões para rejeitar esta empresa."
            detalhe.isNotBlank() -> "Erro ao rejeitar empresa ($code): $detalhe"
            else -> "Erro ao rejeitar empresa ($code)."
        }
    }

    private fun mensagemErroEliminarOferta(code: Int, detalhe: String): String {
        return when {
            detalhe.contains("estagio associado", ignoreCase = true) ||
                detalhe.contains("estágio associado", ignoreCase = true) ->
                "Não é possível eliminar uma das ofertas porque já existe um estágio associado."
            code == 409 || detalhe.contains("foreign key", ignoreCase = true) ->
                "Não foi possível eliminar uma das ofertas porque já tem candidaturas ou estágios associados."
            else -> "Erro ao eliminar oferta ($code)."
        }
    }

    private fun mensagemErroCriarContaInstituicao(statusCode: Int, detalhe: String): String {
        return when {
            detalhe.contains("weak_password", ignoreCase = true) ||
                detalhe.contains("at least 6 characters", ignoreCase = true) ->
                "A palavra-passe deve ter pelo menos 6 caracteres."
            detalhe.contains("invalid_email", ignoreCase = true) ||
                detalhe.contains("invalid email", ignoreCase = true) ->
                "Insira um email institucional válido."
            detalhe.contains("already", ignoreCase = true) ||
                detalhe.contains("registered", ignoreCase = true) ->
                "Já existe uma conta com este email."
            statusCode == 429 ->
                "Demasiadas tentativas. Tente novamente dentro de alguns minutos."
            statusCode in 500..599 ->
                "O serviço de autenticação está temporariamente indisponível."
            detalhe.isNotBlank() ->
                "Não foi possível criar a conta institucional ($statusCode): $detalhe"
            else ->
                "Não foi possível criar a conta institucional. Tente novamente."
        }
    }

    private fun mensagemErroCriarInstituicao(statusCode: Int, detalhe: String): String {
        return when {
            statusCode == 401 || statusCode == 403 ->
                "A conta Auth foi criada, mas o admin não tem permissões para criar o registo da instituição. Aplique o script Supabase atualizado."
            detalhe.contains("row-level security", ignoreCase = true) ->
                "A conta Auth foi criada, mas a política RLS bloqueou o registo da instituição. Aplique o script Supabase atualizado."
            detalhe.isNotBlank() ->
                "A conta Auth foi criada, mas não foi possível criar o registo da instituição ($statusCode): $detalhe"
            else ->
                "A conta Auth foi criada, mas não foi possível criar o registo da instituição ($statusCode)."
        }
    }
}
