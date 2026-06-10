package pt.ligix.app.data.repository

import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Aluno
import pt.ligix.app.model.Docente
import pt.ligix.app.model.Estagio
import pt.ligix.app.model.OfertaEstagio

class DocenteRepository {

    private val api = RetrofitClient.api

    private fun inFilter(ids: Collection<String>): String =
        ids.distinct().joinToString(separator = ",", prefix = "in.(", postfix = ")")

    suspend fun getEstagiosDoDocente(idDocente: String): Result<List<Estagio>> {
        return try {
            val response = api.getEstagiosByDocente(idDocente = "eq.$idDocente")
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty())
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
                Result.success(response.body()?.firstOrNull()?.nome ?: "Desconhecido")
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
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

    suspend fun getDadosAluno(idUtilizador: String): Result<Pair<String, String?>> {
        return try {
            val responseAluno = api.getAlunoById(idUtilizador = "eq.$idUtilizador")
            if (!responseAluno.isSuccessful) {
                return Result.failure(Exception("Erro: ${responseAluno.code()}"))
            }

            val aluno = responseAluno.body()?.firstOrNull()
            val curso = aluno?.curso ?: ""
            val siglaInstituicao = aluno?.idInstituicao?.let { idInstituicao ->
                val responseInst = api.getInstituicaoById(idInstituicao = "eq.$idInstituicao")
                if (responseInst.isSuccessful) responseInst.body()?.firstOrNull()?.sigla else null
            }
            Result.success(Pair(curso, siglaInstituicao))
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
                alunos.associate { aluno: Aluno ->
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

    suspend fun getDocente(idUtilizador: String): Result<Docente?> {
        return try {
            val response = api.getDocenteById(idUtilizador = "eq.$idUtilizador")
            if (response.isSuccessful) {
                Result.success(response.body()?.firstOrNull())
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

}
