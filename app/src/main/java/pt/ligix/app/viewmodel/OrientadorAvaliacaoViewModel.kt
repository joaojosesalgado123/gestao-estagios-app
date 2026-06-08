package pt.ligix.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.data.remote.SupabaseApi
import pt.ligix.app.model.Avaliacao
import pt.ligix.app.model.ItemAvaliacao
import pt.ligix.app.util.SessionManager
import java.time.Instant

class OrientadorAvaliacaoViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _sucesso = MutableStateFlow(false)
    val sucesso: StateFlow<Boolean> = _sucesso

    private val _avaliacaoExistente = MutableStateFlow<Avaliacao?>(null)
    val avaliacaoExistente: StateFlow<Avaliacao?> = _avaliacaoExistente

    private val _minhaAvaliacao = MutableStateFlow<ItemAvaliacao?>(null)
    val minhaAvaliacao: StateFlow<ItemAvaliacao?> = _minhaAvaliacao

    private val _itensExistentes = MutableStateFlow<List<ItemAvaliacao>>(emptyList())
    val itensExistentes: StateFlow<List<ItemAvaliacao>> = _itensExistentes

    private val _relatorioSubmetido = MutableStateFlow(false)
    val relatorioSubmetido: StateFlow<Boolean> = _relatorioSubmetido

    private val _horasCompletas = MutableStateFlow(false)
    val horasCompletas: StateFlow<Boolean> = _horasCompletas

    fun carregarAvaliacao(idEstagio: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val api = RetrofitClient.api
                val idAvaliador = sessionManager.idUtilizador.first().orEmpty()
                _erro.value = null
                _avaliacaoExistente.value = null
                _minhaAvaliacao.value = null
                _itensExistentes.value = emptyList()

                val avalResp = api.getAvaliacaoByEstagio(idEstagio = "eq.$idEstagio")
                val avaliacao = avalResp.body().orEmpty().avaliacaoMaisRecente()
                _avaliacaoExistente.value = avaliacao
                avaliacao?.let {
                    val itens = carregarItensAvaliacao(api, it.idAvaliacao)
                    _itensExistentes.value = itens
                    _minhaAvaliacao.value = itens.avaliacaoDoUtilizador(idAvaliador)
                }

                // Verifica relatório submetido
                val relResp = api.getRelatorioByEstagio(idEstagio = "eq.$idEstagio")
                _relatorioSubmetido.value = (relResp.body() ?: emptyList()).isNotEmpty()

                // Verifica horas completas
                val presencasResp = api.getPresencasByEstagio(idEstagio = "eq.$idEstagio")
                val horasFeitas = (presencasResp.body() ?: emptyList())
                    .count { it.status.equals("presente", ignoreCase = true) } * 8
                _horasCompletas.value = horasFeitas >= 480

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun enviarAvaliacaoCompleta(
        idEstagio: String,
        pontualidade: Int,
        proatividade: Int,
        competenciaTecnica: Int,
        trabalhoEquipa: Int,
        classificacaoFinal: Double,
        comentario: String,
        context: Context
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _erro.value = null
            try {
                val api = RetrofitClient.api
                val idAvaliador = sessionManager.idUtilizador.first() ?: return@launch
                val papel = sessionManager.role.first()?.let { role ->
                    if (role.equals("docente", ignoreCase = true)) "Docente" else "Orientador Empresa"
                } ?: "Avaliador"
                val notaFinal = classificacaoFinal.coerceIn(0.0, 20.0)
                val agora = Instant.now().toString()

                val avaliacao = obterOuCriarAvaliacao(api, idEstagio, agora)
                val itensAtuais = carregarItensAvaliacao(api, avaliacao.idAvaliacao)

                if (itensAtuais.avaliacaoDoUtilizador(idAvaliador) != null) {
                    _minhaAvaliacao.value = itensAtuais.avaliacaoDoUtilizador(idAvaliador)
                    _erro.value = "A sua avaliação já foi submetida para este estágio."
                    return@launch
                }

                val itemMap = mapOf(
                    "idavaliacao" to avaliacao.idAvaliacao,
                    "idavaliador" to idAvaliador,
                    "classificacao" to notaFinal,
                    "criterio" to "Avaliação final",
                    "comentario" to comentarioItemAvaliacao(
                        papel = papel,
                        comentario = comentario,
                        pontualidade = pontualidade,
                        proatividade = proatividade,
                        competenciaTecnica = competenciaTecnica,
                        trabalhoEquipa = trabalhoEquipa
                    ),
                    "data_avaliacao" to agora
                )

                val itemResp = api.createItemAvaliacaoMap(body = itemMap)
                if (!itemResp.isSuccessful) {
                    val errorBody = itemResp.errorBody()?.string().orEmpty()
                    android.util.Log.e("OrientadorAvaliacao", "Erro item: ${itemResp.code()} - $errorBody")
                    _erro.value = mensagemErroAvaliacao(itemResp.code(), errorBody)
                    return@launch
                }

                val itemCriado = itemResp.body()?.firstOrNull()
                    ?: ItemAvaliacao(
                        idAvaliacao = avaliacao.idAvaliacao,
                        idAvaliador = idAvaliador,
                        classificacao = notaFinal,
                        criterio = "Avaliação final",
                        comentario = itemMap["comentario"] as String,
                        dataAvaliacao = agora
                    )

                _avaliacaoExistente.value = avaliacao
                _itensExistentes.value = itensAtuais + itemCriado
                _minhaAvaliacao.value = itemCriado
                _sucesso.value = true
            } catch (e: Exception) {
                _erro.value = e.message ?: "Não foi possível submeter a avaliação."
            } finally {
                _isSaving.value = false
            }
        }
    }

    private suspend fun obterOuCriarAvaliacao(
        api: SupabaseApi,
        idEstagio: String,
        dataAvaliacao: String
    ): Avaliacao {
        _avaliacaoExistente.value?.takeIf { it.idAvaliacao.isNotBlank() }?.let { return it }

        val existenteResp = api.getAvaliacaoByEstagio(idEstagio = "eq.$idEstagio")
        val existente = existenteResp.body().orEmpty().avaliacaoMaisRecente()
        if (existente != null) {
            _avaliacaoExistente.value = existente
            return existente
        }

        val avaliacaoMap = mapOf(
            "idestagio" to idEstagio,
            "comentario" to "Avaliação final do estágio",
            "data_avaliacao" to dataAvaliacao
        )
        val criarResp = api.createAvaliacaoMap(body = avaliacaoMap)
        if (!criarResp.isSuccessful) {
            val errorBody = criarResp.errorBody()?.string().orEmpty()
            android.util.Log.e("OrientadorAvaliacao", "Erro avaliação: ${criarResp.code()} - $errorBody")
            throw IllegalStateException(mensagemErroAvaliacao(criarResp.code(), errorBody))
        }

        return criarResp.body()?.firstOrNull()?.also { _avaliacaoExistente.value = it }
            ?: throw IllegalStateException("Não foi possível preparar a avaliação do estágio.")
    }

    private suspend fun carregarItensAvaliacao(
        api: SupabaseApi,
        idAvaliacao: String
    ): List<ItemAvaliacao> {
        if (idAvaliacao.isBlank()) return emptyList()
        val response = api.getItensAvaliacaoByAvaliacaoLower(idAvaliacao = "eq.$idAvaliacao")
        return if (response.isSuccessful) response.body().orEmpty() else emptyList()
    }

    private fun List<Avaliacao>.avaliacaoMaisRecente(): Avaliacao? =
        maxByOrNull { it.dataAvaliacao?.ifBlank { it.createdAt } ?: it.createdAt }

    private fun List<ItemAvaliacao>.avaliacaoDoUtilizador(idAvaliador: String): ItemAvaliacao? =
        filter {
            it.idAvaliador.equals(idAvaliador, ignoreCase = true) &&
                it.ehItemDeNotaFinal()
        }
            .maxByOrNull { it.dataAvaliacao.ifBlank { "" } }

    private fun ItemAvaliacao.ehItemDeNotaFinal(): Boolean {
        val criterioNormalizado = criterio.trim()
        val comentarioNormalizado = comentario.orEmpty()
        return criterioNormalizado.isBlank() ||
            criterioNormalizado.contains("final", ignoreCase = true) ||
            comentarioNormalizado.contains("Avaliador:", ignoreCase = true)
    }

    private fun comentarioItemAvaliacao(
        papel: String,
        comentario: String,
        pontualidade: Int,
        proatividade: Int,
        competenciaTecnica: Int,
        trabalhoEquipa: Int
    ): String = buildList {
        add("Avaliador: $papel")
        if (comentario.isNotBlank()) add(comentario.trim())
        add("Pontualidade: $pontualidade/5")
        add("Proatividade: $proatividade/5")
        add("Competência Técnica: $competenciaTecnica/5")
        add("Trabalho em Equipa: $trabalhoEquipa/5")
    }.joinToString("\n")

    private fun mensagemErroAvaliacao(statusCode: Int, errorBody: String): String {
        return when {
            statusCode == 401 || statusCode == 403 ->
                "Não tem permissões para submeter esta avaliação."
            statusCode == 409 ||
                errorBody.contains("duplicate", ignoreCase = true) ||
                errorBody.contains("already exists", ignoreCase = true) ->
                "A sua avaliação já foi submetida para este estágio."
            errorBody.contains("row-level security", ignoreCase = true) ->
                "Não foi possível submeter a avaliação. Verifique as permissões no Supabase."
            else ->
                "Não foi possível submeter a avaliação. Tente novamente."
        }
    }

    fun resetSucesso() { _sucesso.value = false }
}

class OrientadorAvaliacaoViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return OrientadorAvaliacaoViewModel(sessionManager) as T
    }
}
