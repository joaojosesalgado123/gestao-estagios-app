package pt.ligix.app.data.repository

import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Empresa

class AdminRepository {

    private val api = RetrofitClient.api

    suspend fun getEmpresasPendentes(): Result<List<Empresa>> {
        return try {
            val response = api.getEmpresasPendentes()
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty())
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
            }
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
