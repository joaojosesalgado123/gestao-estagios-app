package pt.ligix.app.data.repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.OfertaEstagio
import pt.ligix.app.util.SessionTokenProvider
import java.util.concurrent.TimeUnit

class OfertasRepository {

    private val api = RetrofitClient.api

    private val uploadClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getOfertas(): Result<List<OfertaEstagio>> {
        return try {
            val response = api.getOfertasDisponiveisAluno()
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty().comNomesEmpresa())
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun ofertaTemVagas(idOferta: String): Result<Boolean> {
        return try {
            val response = api.ofertaTemVagas(
                params = mapOf("p_idoferta" to idOferta)
            )
            if (response.isSuccessful) {
                Result.success(response.body() == true)
            } else {
                Result.failure(Exception("Não foi possível confirmar as vagas da oferta."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun getNomeEmpresa(idEmpresa: String): Result<String?> {
        return try {
            val response = api.getUtilizadorById(
                id = "eq.$idEmpresa",
                select = "idutilizador,nome"
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.firstOrNull()?.nome)
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun criarCandidatura(
        idOferta: String,
        cvFicheiro: String,
        cartaFicheiro: String
    ): Result<Unit> {
        return try {
            val body = mapOf(
                "p_idoferta" to idOferta,
                "p_cv_ficheiro" to cvFicheiro,
                "p_carta_motivacao_ficheiro" to cartaFicheiro
            )
            val response = api.criarCandidaturaAluno(body)
            if (response.isSuccessful && response.body() == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Esta oferta já não tem vagas disponíveis ou já tens uma candidatura ativa."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun uploadFicheiro(
        bucket: String,
        path: String,
        bytes: ByteArray,
        contentType: String = "application/pdf"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "${pt.ligix.app.util.Constants.SUPABASE_URL}/storage/v1/object/$bucket/$path"

            val requestBody = bytes.toRequestBody(contentType.toMediaType())
            val requestBuilder = okhttp3.Request.Builder()
                .url(url)
                .header("apikey", pt.ligix.app.util.Constants.SUPABASE_KEY)
                .header("Content-Type", contentType)
                .post(requestBody)

            SessionTokenProvider.accessToken?.let { token ->
                requestBuilder.header("Authorization", "Bearer $token")
            }

            val request = requestBuilder.build()

            val response = uploadClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                Result.success("$bucket/$path")
            } else {
                Result.failure(Exception("Erro no upload: ${response.code} - $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Erro no upload: ${e.message}"))
        }
    }

    private suspend fun List<OfertaEstagio>.comNomesEmpresa(): List<OfertaEstagio> {
        val idsEmpresa = mapNotNull { it.idEmpresa.takeIf(String::isNotBlank) }.distinct()
        if (idsEmpresa.isEmpty()) return this

        val response = api.getUtilizadoresByIds(
            ids = "in.(${idsEmpresa.joinToString(",")})",
            select = "idutilizador,nome"
        )
        if (!response.isSuccessful) return this

        val nomesPorId = response.body()
            .orEmpty()
            .mapNotNull { utilizador ->
                utilizador.idUtilizador?.takeIf { it.isNotBlank() }?.let { id ->
                    id to utilizador.nome
                }
            }
            .toMap()

        return map { oferta ->
            oferta.copy(nomeEmpresa = nomesPorId[oferta.idEmpresa])
        }
    }
}
