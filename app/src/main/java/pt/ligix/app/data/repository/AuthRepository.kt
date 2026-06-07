package pt.ligix.app.data.repository

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import pt.ligix.app.data.remote.AuthenticatedUtilizador
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.data.remote.SupabaseAuthErrorResponse
import pt.ligix.app.data.remote.SupabaseAuthClient
import pt.ligix.app.data.remote.SupabasePasswordLoginRequest
import pt.ligix.app.data.remote.SupabaseRecoverPasswordRequest
import pt.ligix.app.data.remote.SupabaseSignUpRequest
import pt.ligix.app.model.Utilizador
import pt.ligix.app.util.SessionTokenProvider

class AuthRepository {

    private val api = RetrofitClient.api
    private val authApi = SupabaseAuthClient.api
    private val gson = Gson()

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
                return Result.failure(Exception(mapAuthError(authResponse.code(), errorBody)))
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
                    ?: return Result.failure(Exception("Perfil de utilizador não encontrado. Body: ${perfilResponse.body()}"))
                Result.success(
                    AuthenticatedUtilizador(
                        utilizador = utilizador,
                        session = session
                    )
                )
            } else {
                Result.failure(Exception("Erro ao obter perfil: ${perfilResponse.code()} - ${perfilResponse.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    private fun mapAuthError(statusCode: Int, errorBody: String): String {
        val parsedError = parseAuthError(errorBody)
        val errorCode = parsedError?.errorCode.orEmpty()
        val apiMessage = parsedError?.message ?: parsedError?.fallbackMessage

        return when {
            errorCode.equals("invalid_credentials", ignoreCase = true) ||
                errorBody.contains("invalid_credentials", ignoreCase = true) ||
                apiMessage?.contains("invalid login credentials", ignoreCase = true) == true ->
                "E-mail ou palavra-passe incorretos"
            errorCode.equals("email_not_confirmed", ignoreCase = true) ||
                errorBody.contains("email_not_confirmed", ignoreCase = true) ||
                apiMessage?.contains("email not confirmed", ignoreCase = true) == true ->
                "Confirme o seu email antes de iniciar sessão"
            statusCode == 429 ->
                "Demasiadas tentativas. Tente novamente dentro de alguns minutos"
            statusCode in 500..599 ->
                "O serviço de autenticação está temporariamente indisponível"
            else ->
                "Não foi possível iniciar sessão. Tente novamente"
        }
    }

    private fun parseAuthError(errorBody: String): SupabaseAuthErrorResponse? {
        if (errorBody.isBlank()) return null

        return try {
            gson.fromJson(errorBody, SupabaseAuthErrorResponse::class.java)
        } catch (_: JsonSyntaxException) {
            null
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
        area: String,
        idInstituicao: String
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
                    "area" to area,
                    "idinstituicao" to idInstituicao
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
