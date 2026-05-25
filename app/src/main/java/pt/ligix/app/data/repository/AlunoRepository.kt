package pt.ligix.app.data.repository

import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Candidatura
import pt.ligix.app.model.Estagio
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.model.Presenca

class AlunoRepository {

    private val api = RetrofitClient.api

    suspend fun getCandidaturas(idAluno: String): Result<List<Candidatura>> {
        return try {
            val response = api.getCandidaturasByAluno(idAluno = "eq.$idAluno")
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
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

    suspend fun verificarCandidaturaExistente(idAluno: String, idOferta: String): Boolean {
        return try {
            val response = api.getCandidaturasByAluno(idAluno = "eq.$idAluno")
            if (response.isSuccessful) {
                response.body()?.any { it.idOferta == idOferta } ?: false
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getEstagioDoAluno(idAluno: String): Result<Estagio?> {
        return try {
            val responseCandidaturas = api.getCandidaturasByAluno(idAluno = "eq.$idAluno")
            if (responseCandidaturas.isSuccessful) {
                val aceite = responseCandidaturas.body()
                    ?.firstOrNull { it.status == "aceite" }
                if (aceite?.idCandidatura != null) {
                    val responseEstagio = api.getEstagioByCandidatura(
                        idCandidatura = "eq.${aceite.idCandidatura}"
                    )
                    if (responseEstagio.isSuccessful) {
                        Result.success(responseEstagio.body()?.firstOrNull())
                    } else {
                        Result.success(null)
                    }
                } else {
                    Result.success(null)
                }
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun getPresencas(idEstagio: String): Result<List<Presenca>> {
        return try {
            val response = api.getPresencasByEstagio(idEstagio = "eq.$idEstagio")
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }
}
