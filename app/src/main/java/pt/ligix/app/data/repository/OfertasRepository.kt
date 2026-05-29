package pt.ligix.app.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.OfertaEstagio
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
            val response = api.getTodasOfertas()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun getOfertaPorId(idOferta: String): Result<OfertaEstagio?> {
        return try {
            val response = api.getOfertaPorId(idOferta = "eq.$idOferta")
            if (response.isSuccessful) {
                Result.success(response.body()?.firstOrNull())
            } else {
                Result.failure(Exception("Erro: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Sem ligação à internet"))
        }
    }

    suspend fun criarCandidatura(
        idAluno: String,
        idOferta: String,
        cvFicheiro: String,
        cartaFicheiro: String
    ): Result<Unit> {
        return try {
            val body = mapOf(
                "idaluno" to idAluno,
                "idoferta" to idOferta,
                "status" to "pendente",
                "cv_ficheiro" to cvFicheiro,
                "carta_motivacao_ficheiro" to cartaFicheiro,
                "data" to java.time.LocalDate.now().toString()
            )
            val response = api.createCandidaturaMap(body)
            if (response.isSuccessful || response.code() == 201) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erro ao criar candidatura: ${response.code()}"))
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
            Log.d("UPLOAD", "A fazer upload para: $url (${bytes.size} bytes)")

            val requestBody = bytes.toRequestBody(contentType.toMediaType())
            val request = okhttp3.Request.Builder()
                .url(url)
                .header("apikey", pt.ligix.app.util.Constants.SUPABASE_KEY)
                .header("Authorization", "Bearer ${pt.ligix.app.util.Constants.SUPABASE_KEY}")
                .header("Content-Type", contentType)
                .post(requestBody)
                .build()

            val response = uploadClient.newCall(request).execute()
            val responseBody = response.body?.string()
            Log.d("UPLOAD", "Resposta ${response.code}: $responseBody")

            if (response.isSuccessful) {
                Result.success("$bucket/$path")
            } else {
                Result.failure(Exception("Erro no upload: ${response.code} - $responseBody"))
            }
        } catch (e: Exception) {
            Log.e("UPLOAD", "Exceção: ${e.message}", e)
            Result.failure(Exception("Erro no upload: ${e.message}"))
        }
    }
}
