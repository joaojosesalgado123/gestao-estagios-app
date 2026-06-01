package pt.ligix.app.data.repository

import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Aluno
import pt.ligix.app.model.InstituicaoEnsino
import pt.ligix.app.model.Candidatura
import pt.ligix.app.model.Estagio
import pt.ligix.app.model.OfertaEstagio


class EmpresaRepository {

    private val api = RetrofitClient.api

    suspend fun getOfertasDaEmpresa(idEmpresa: String): Result<List<OfertaEstagio>> {
        return try {
            val response = api.getOfertas()
            if (response.isSuccessful) {
                Result.success(response.body()?.filter { it.idEmpresa == idEmpresa } ?: emptyList())
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
            val todasCandidaturas = mutableListOf<Candidatura>()
            for (oferta in ofertas) {
                val response = api.getCandidaturasByOferta(idOferta = "eq.${oferta.idOferta}")
                if (response.isSuccessful) {
                    todasCandidaturas.addAll(response.body() ?: emptyList())
                }
            }
            Result.success(todasCandidaturas)
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun getEstagiosAtivos(idEmpresa: String): Result<List<Estagio>> {
        return try {
            val candidaturas = getCandidaturasDaEmpresa(idEmpresa).getOrNull() ?: return Result.success(emptyList())
            val estagios = mutableListOf<Estagio>()
            for (candidatura in candidaturas.filter { it.status == "aceite" }) {
                val response = api.getEstagioByCandidatura(idCandidatura = "eq.${candidatura.idCandidatura}")
                if (response.isSuccessful) {
                    estagios.addAll(response.body() ?: emptyList())
                }
            }
            Result.success(estagios)
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
}
