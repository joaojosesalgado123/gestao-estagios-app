package pt.ligix.app.data.repository

import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Aluno
import pt.ligix.app.model.InstituicaoEnsino
import pt.ligix.app.model.Candidatura
import pt.ligix.app.model.Estagio
import pt.ligix.app.model.OfertaEstagio


class EmpresaRepository {

    private val api = RetrofitClient.api

    private fun inFilter(ids: Collection<String>): String =
        ids.distinct().joinToString(separator = ",", prefix = "in.(", postfix = ")")

    suspend fun getOfertasDaEmpresa(idEmpresa: String): Result<List<OfertaEstagio>> {
        return try {
            val response = api.getOfertasByEmpresa(idEmpresa = "eq.$idEmpresa")
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty())
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun getCandidaturasDaEmpresa(idEmpresa: String): Result<List<Candidatura>> {
        return try {
            val ofertas = getOfertasDaEmpresa(idEmpresa).getOrNull() ?: return Result.success(emptyList())
            getCandidaturasDasOfertas(ofertas.map { it.idOferta })
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun getCandidaturasDasOfertas(idsOfertas: Collection<String>): Result<List<Candidatura>> {
        return try {
            val ids = idsOfertas.filter { it.isNotBlank() }.distinct()
            if (ids.isEmpty()) return Result.success(emptyList())

            val response = api.getCandidaturasByOfertas(idOfertas = inFilter(ids))
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty())
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun eliminarOferta(idOferta: String): Result<Unit> {
        return try {
            val rpcResponse = api.eliminarOfertaEmpresa(
                params = mapOf("p_idoferta" to idOferta)
            )
            val rpcDetalhe = detalheErro(rpcResponse)
            if (rpcResponse.isSuccessful) {
                return if (rpcResponse.body() == true) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Não foi possível eliminar a oferta na base de dados. Confirma que esta oferta pertence à tua empresa."))
                }
            } else if (!funcaoEliminarOfertaIndisponivel(rpcResponse.code(), rpcDetalhe)) {
                return Result.failure(Exception(mensagemErroEliminarOferta(rpcResponse.code(), rpcDetalhe)))
            }

            val response = api.deleteOferta(id = "eq.$idOferta")
            if (response.isSuccessful && response.body().orEmpty().isNotEmpty()) {
                Result.success(Unit)
            } else if (response.isSuccessful) {
                Result.failure(Exception("Não foi possível eliminar a oferta na base de dados. Confirma que esta oferta pertence à tua empresa."))
            } else {
                Result.failure(Exception(mensagemErroEliminarOferta(response.code(), detalheErro(response))))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Sem ligação à internet"))
        }
    }

    private fun detalheErro(response: retrofit2.Response<*>): String {
        return try {
            response.errorBody()?.string()
        } catch (_: Exception) {
            null
        }.orEmpty()
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

    private fun mensagemErroEliminarOferta(code: Int, detalhe: String): String {
        return when {
            detalhe.contains("estagio associado", ignoreCase = true) ||
                detalhe.contains("estágio associado", ignoreCase = true) ->
                "Não é possível eliminar esta oferta porque já existe um estágio associado."
            code == 409 || detalhe.contains("foreign key", ignoreCase = true) ->
                "Não foi possível eliminar esta oferta porque já tem candidaturas ou estágios associados."
            else -> "Erro ao eliminar oferta ($code)."
        }
    }

    suspend fun getEstagiosAtivos(idEmpresa: String): Result<List<Estagio>> {
        return try {
            val candidaturas = getCandidaturasDaEmpresa(idEmpresa).getOrNull() ?: return Result.success(emptyList())
            getEstagiosDasCandidaturas(
                candidaturas
                    .filter { it.status == "aceite" }
                    .map { it.idCandidatura }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun getEstagiosDasCandidaturas(idsCandidaturas: Collection<String>): Result<List<Estagio>> {
        return try {
            val ids = idsCandidaturas.filter { it.isNotBlank() }.distinct()
            if (ids.isEmpty()) return Result.success(emptyList())

            val response = api.getEstagiosByCandidaturas(idCandidaturas = inFilter(ids))
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty())
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun getOferta(idOferta: String): Result<OfertaEstagio?> {
        return try {
            val response = api.getOfertaById(idOferta = "eq.$idOferta")
            if (response.isSuccessful) {
                Result.success(response.body()?.firstOrNull())
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun getNomeUtilizador(idUtilizador: String): Result<String> {
        return try {
            val response = api.getUtilizadorById(id = "eq.$idUtilizador")
            if (response.isSuccessful) {
                val nome = response.body()?.firstOrNull()?.nome ?: "Desconhecido"
                Result.success(nome)
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun getDadosAluno(idUtilizador: String): Result<Pair<String, String?>> {
        return try {
            val responseAluno = api.getAlunoById(idUtilizador = "eq.$idUtilizador")
            if (responseAluno.isSuccessful) {
                val aluno = responseAluno.body()?.firstOrNull()
                val curso = aluno?.curso ?: ""
                val siglaInstituicao = aluno?.idInstituicao?.let { idInst ->
                    val responseInst = api.getInstituicaoById(idInstituicao = "eq.$idInst")
                    if (responseInst.isSuccessful) {
                        responseInst.body()?.firstOrNull()?.sigla
                    } else null
                }
                Result.success(Pair(curso, siglaInstituicao))
            } else {
                Result.failure(Exception("Erro: ${responseAluno.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun getNomesUtilizadores(idsUtilizadores: Collection<String>): Result<Map<String, String>> {
        return try {
            val ids = idsUtilizadores.filter { it.isNotBlank() }.distinct()
            if (ids.isEmpty()) return Result.success(emptyMap())

            val response = api.getUtilizadoresByIds(
                ids = inFilter(ids),
                select = "idutilizador,nome"
            )
            if (response.isSuccessful) {
                Result.success(
                    response.body().orEmpty()
                        .mapNotNull { utilizador ->
                            utilizador.idUtilizador?.let { id -> id to utilizador.nome }
                        }
                        .toMap()
                )
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun getDadosAlunos(idsUtilizadores: Collection<String>): Result<Map<String, Pair<String, String?>>> {
        return try {
            val ids = idsUtilizadores.filter { it.isNotBlank() }.distinct()
            if (ids.isEmpty()) return Result.success(emptyMap())

            val responseAlunos = api.getAlunosByIds(
                ids = inFilter(ids),
                select = "idutilizador,curso,idinstituicao"
            )
            if (!responseAlunos.isSuccessful) {
                return Result.failure(Exception("Erro: ${responseAlunos.code()}"))
            }

            val alunos = responseAlunos.body().orEmpty()
            val idsInstituicoes = alunos.mapNotNull { it.idInstituicao }.filter { it.isNotBlank() }
            val instituicoes = if (idsInstituicoes.isEmpty()) {
                emptyMap()
            } else {
                val responseInstituicoes = api.getInstituicoesByIds(
                    ids = inFilter(idsInstituicoes),
                    select = "idinstituicao,sigla"
                )
                if (responseInstituicoes.isSuccessful) {
                    responseInstituicoes.body().orEmpty()
                        .associate { it.idInstituicao to it.sigla }
                } else {
                    emptyMap()
                }
            }

            Result.success(
                alunos.associate { aluno ->
                    aluno.idUtilizador to Pair(
                        aluno.curso,
                        aluno.idInstituicao?.let { instituicoes[it] }
                    )
                }
            )
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }
}
