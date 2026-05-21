package pt.ligix.app.data.repository

import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Aluno
import pt.ligix.app.model.Docente
import pt.ligix.app.model.Empresa
import pt.ligix.app.model.Utilizador

class AuthRepository {

    private val api = RetrofitClient.api

    // RF02 - Login
    suspend fun login(email: String, password: String): Result<Utilizador> {
        return try {
            val response = api.getUtilizadorByEmail(
                email = "eq.$email"
            )
            if (response.isSuccessful) {
                val utilizadores = response.body()
                if (!utilizadores.isNullOrEmpty()) {
                    val utilizador = utilizadores[0]
                    if (utilizador.password == password) {
                        Result.success(utilizador)
                    } else {
                        Result.failure(Exception("Password incorreta"))
                    }
                } else {
                    Result.failure(Exception("Email não encontrado"))
                }
            } else {
                Result.failure(Exception("Erro ao fazer login: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    // RF01 - Registar Aluno
    suspend fun registarAluno(
        username: String,
        nome: String,
        email: String,
        password: String,
        telemovel: String,
        curso: String,
        numeroAluno: String
    ): Result<Utilizador> {
        return try {
            val utilizador = Utilizador(
                username = username,
                nome = nome,
                email = email,
                password = password,
                role = "aluno"
            )
            val responseUtilizador = api.createUtilizador(utilizador = utilizador)

            if (responseUtilizador.isSuccessful) {
                val novoUtilizador = responseUtilizador.body()?.firstOrNull()
                    ?: return Result.failure(Exception("Erro ao criar utilizador"))

                val idGerado = novoUtilizador.idUtilizador
                    ?: return Result.failure(Exception("ID do utilizador não foi gerado"))

                val aluno = Aluno(
                    idUtilizador = idGerado,
                    numeroAluno = numeroAluno,
                    curso = curso,
                    telemovel = telemovel
                )
                api.createAluno(aluno = aluno)

                Result.success(novoUtilizador)
            } else {
                Result.failure(Exception("Erro ao registar: ${responseUtilizador.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    // RF01 - Registar Docente
    suspend fun registarDocente(
        username: String,
        nome: String,
        email: String,
        password: String,
        telemovel: String,
        area: String
    ): Result<Utilizador> {
        return try {
            val utilizador = Utilizador(
                username = username,
                nome = nome,
                email = email,
                password = password,
                role = "docente"
            )
            val responseUtilizador = api.createUtilizador(utilizador = utilizador)

            if (responseUtilizador.isSuccessful) {
                val novoUtilizador = responseUtilizador.body()?.firstOrNull()
                    ?: return Result.failure(Exception("Erro ao criar utilizador"))

                val idGerado = novoUtilizador.idUtilizador
                    ?: return Result.failure(Exception("ID do utilizador não foi gerado"))

                val docente = Docente(
                    idUtilizador = idGerado,
                    telemovel = telemovel,
                    area = area
                )
                api.createDocente(docente = docente)

                Result.success(novoUtilizador)
            } else {
                Result.failure(Exception("Erro ao registar: ${responseUtilizador.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    // RF10 - Registar Empresa
    suspend fun registarEmpresa(
        username: String,
        nome: String,
        email: String,
        password: String,
        nipc: String,
        morada: String,
        descricao: String
    ): Result<Utilizador> {
        return try {
            val utilizador = Utilizador(
                username = username,
                nome = nome,
                email = email,
                password = password,
                role = "empresa"
            )
            val responseUtilizador = api.createUtilizador(utilizador = utilizador)

            if (responseUtilizador.isSuccessful) {
                val novoUtilizador = responseUtilizador.body()?.firstOrNull()
                    ?: return Result.failure(Exception("Erro ao criar utilizador"))

                val idGerado = novoUtilizador.idUtilizador
                    ?: return Result.failure(Exception("ID do utilizador não foi gerado"))

                val empresa = Empresa(
                    idUtilizador = idGerado,
                    nipc = nipc,
                    morada = morada,
                    descricao = descricao,
                    status = "pendente"
                )
                api.createEmpresa(empresa = empresa)

                Result.success(novoUtilizador)
            } else {
                Result.failure(Exception("Erro ao registar: ${responseUtilizador.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    // RF04 - Recuperar password
    suspend fun recuperarPassword(email: String): Result<Boolean> {
        return try {
            val response = api.getUtilizadorByEmail(email = "eq.$email")
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                Result.success(true)
            } else {
                Result.failure(Exception("Email não encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }
}
