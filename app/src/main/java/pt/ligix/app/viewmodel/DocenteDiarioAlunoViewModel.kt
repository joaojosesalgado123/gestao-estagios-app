package pt.ligix.app.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Atividade
import pt.ligix.app.model.Presenca
import pt.ligix.app.util.Constants
import pt.ligix.app.util.SessionManager

class DocenteDiarioAlunoViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _atividades = MutableStateFlow<List<Atividade>>(emptyList())
    val atividades: StateFlow<List<Atividade>> = _atividades

    private val _presencas = MutableStateFlow<List<Presenca>>(emptyList())
    val presencas: StateFlow<List<Presenca>> = _presencas

    private val _horasFeitas = MutableStateFlow(0)
    val horasFeitas: StateFlow<Int> = _horasFeitas

    private val _horasTotal = MutableStateFlow(480)
    val horasTotal: StateFlow<Int> = _horasTotal

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val storageClient = OkHttpClient()
    private var urlRelatorio: String? = null
    private var caminhoRelatorio: String? = null

    fun carregarDados(idEstagio: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null
            try {
                val api = RetrofitClient.api
                _atividades.value = api.getAtividadesByEstagio(
                    idEstagio = "eq.$idEstagio"
                ).body().orEmpty()

                val presencasList = api.getPresencasByEstagio(
                    idEstagio = "eq.$idEstagio"
                ).body().orEmpty()
                _presencas.value = presencasList
                _horasFeitas.value = presencasList.count {
                    it.status.equals("presente", ignoreCase = true)
                } * 8

            } catch (e: Exception) {
                _erro.value = "Não foi possível carregar o diário do aluno."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun carregarRelatorio(idEstagio: String) {
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.api.getRelatorioByEstagio(idEstagio = "eq.$idEstagio")
                val relatorio = resp.body().orEmpty()
                    .maxByOrNull { it.dataSubmissao.ifBlank { it.createdAt } }
                urlRelatorio = relatorio?.ficheiroUrl?.takeIf { it.isNotBlank() }
                caminhoRelatorio = relatorio?.ficheiro
                    ?.removePrefix("relatorios/")
                    ?.takeIf { it.isNotBlank() }
            } catch (_: Exception) {
            }
        }
    }

    fun abrirRelatorio(context: Context) {
        viewModelScope.launch {
            val url = urlRelatorio ?: caminhoRelatorio?.let { caminho ->
                gerarUrlAssinada(caminho)
            }

            if (url == null) {
                Toast.makeText(context, "Relatório indisponível.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(context, "Não foi possível abrir o relatório.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun gerarUrlAssinada(caminho: String): String? = withContext(Dispatchers.IO) {
        try {
            val token = sessionManager.obterAccessTokenValido() ?: return@withContext null
            val requestBody = """{"expiresIn":3600}"""
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("${Constants.SUPABASE_URL}/storage/v1/object/sign/relatorios/$caminho")
                .header("apikey", Constants.SUPABASE_KEY)
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            storageClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)
                val signedUrl = json.optString("signedURL")
                    .ifBlank { json.optString("signedUrl") }
                    .takeIf { it.isNotBlank() }
                    ?: return@withContext null
                when {
                    signedUrl.startsWith("http") -> signedUrl
                    signedUrl.startsWith("/storage/") -> Constants.SUPABASE_URL + signedUrl
                    signedUrl.startsWith("/object/") -> "${Constants.SUPABASE_URL}/storage/v1$signedUrl"
                    signedUrl.startsWith("/") -> Constants.SUPABASE_URL + signedUrl
                    else -> "${Constants.SUPABASE_URL}/$signedUrl"
                }
            }
        } catch (_: Exception) {
            null
        }
    }

}

class DocenteDiarioAlunoViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DocenteDiarioAlunoViewModel(sessionManager) as T
    }
}
