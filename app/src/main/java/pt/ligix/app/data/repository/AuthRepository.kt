package pt.ligix.app.data.repository

import pt.ligix.app.data.remote.AuthenticatedUtilizador
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.data.remote.SupabaseAuthClient
import pt.ligix.app.data.remote.SupabasePasswordLoginRequest
import pt.ligix.app.data.remote.SupabaseRecoverPasswordRequest
import pt.ligix.app.data.remote.SupabaseSignUpRequest
import pt.ligix.app.model.Utilizador
import pt.ligix.app.util.SessionTokenProvider

class AuthRepository {

    private val api = RetrofitClient.api
    private val authApi = SupabaseAuthClient.api

    // RF02 - Login
    suspend fun login(email: String, password: String): Result<AuthenticatedUtilizador> {
        return try {
            val authResponse = authApi.loginWithPassword(
                request = SupabasePasswordLoginRequest(
                    email = email,
                    password = password
                )
            )

            if (!authResponse.isSuccessful) {
                val errorBody = authResponse.errorBody()?.string().orEmpty()
                val message = if (errorBody.contains("email_not_confirmed", ignoreCase = true)) {
                    "Confirme o seu email antes de iniciar sessão"
                } else {
                    "Email ou password inválidos"
                }
                return Result.failure(Exception(message))
            }

            val authBody = authResponse.body()
                ?: return Result.failure(Exception("Resposta de autenticação inválida"))
            val authUserId = authBody.user?.id.orEmpty()
            val session = authBody.resolvedSession()
                ?: return Result.failure(Exception("Não foi possível obter o JWT da sessão"))

            SessionTokenProvider.update(session.accessToken)

            val perfilResponse = api.getUtilizadorById(id = "eq.$authUserId")
            if (perfilResponse.isSuccessful) {
                val utilizador = perfilResponse.body()?.firstOrNull()
                    ?: return Result.failure(Exception("Perfil de utilizador não encontrado"))
                Result.success(
                    AuthenticatedUtilizador(
                        utilizador = utilizador,
                        session = session
                    )
                )
            } else {
                Result.failure(Exception("Erro ao obter perfil: ${perfilResponse.code()}"))
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
            val idGerado = criarContaAuth(
                email = email,
                password = password,
                metadata = mapOf(
                    "role" to "aluno",
                    "nome" to nome,
                    "username" to username,
                    "numero_aluno" to numeroAluno,
                    "curso" to curso,
                    "telemovel" to telemovel
                )
            ).getOrElse { erro ->
                return Result.failure(erro)
            }

            SessionTokenProvider.clear()
            Result.success(
                Utilizador(
                    idUtilizador = idGerado,
                    username = username,
                    nome = nome,
                    email = email,
                    role = "aluno"
                )
            )
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
            val idGerado = criarContaAuth(
                email = email,
                password = password,
                metadata = mapOf(
                    "role" to "docente",
                    "nome" to nome,
                    "username" to username,
                    "telemovel" to telemovel,
                    "area" to area
                )
            ).getOrElse { erro ->
                return Result.failure(erro)
            }

            SessionTokenProvider.clear()
            Result.success(
                Utilizador(
                    idUtilizador = idGerado,
                    username = username,
                    nome = nome,
                    email = email,
                    role = "docente"
                )
            )
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
            val idGerado = criarContaAuth(
                email = email,
                password = password,
                metadata = mapOf(
                    "role" to "empresa",
                    "nome" to nome,
                    "username" to username,
                    "nipc" to nipc,
                    "morada" to morada,
                    "descricao" to descricao,
                    "status" to "pendente"
                )
            ).getOrElse { erro ->
                return Result.failure(erro)
            }

            SessionTokenProvider.clear()
            Result.success(
                Utilizador(
                    idUtilizador = idGerado,
                    username = username,
                    nome = nome,
                    email = email,
                    role = "empresa"
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    // RF04 - Recuperar password
    suspend fun recuperarPassword(email: String): Result<Boolean> {
        return try {
            val response = authApi.recoverPassword(
                request = SupabaseRecoverPasswordRequest(email = email)
            )
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Não foi possível enviar recuperação de password"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    private suspend fun criarContaAuth(
        email: String,
        password: String,
        metadata: Map<String, String>
    ): Result<String> {
        val response = authApi.signUp(
            request = SupabaseSignUpRequest(
                email = email,
                password = password,
                data = metadata
            )
        )

        if (!response.isSuccessful) {
            return Result.failure(Exception("Erro ao criar conta Auth: ${response.code()}"))
        }

        val body = response.body()
            ?: return Result.failure(Exception("Resposta de registo inválida"))
        val id = body.user?.id?.takeIf { it.isNotBlank() }
            ?: body.id?.takeIf { it.isNotBlank() }
            ?: return Result.failure(Exception("ID do utilizador Auth não foi gerado"))

        SessionTokenProvider.update(body.resolvedSession()?.accessToken)
        return Result.success(id)
    }
}
