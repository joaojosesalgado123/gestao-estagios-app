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

                // Verifica avaliação existente
                val avalResp = api.getAvaliacaoByEstagio(idEstagio = "eq.$idEstagio")
                val avaliacao = avalResp.body()?.firstOrNull()
                _avaliacaoExistente.value = avaliacao
                avaliacao?.let {
                    val itensResp = api.getItensAvaliacaoByAvaliacaoLower(idAvaliacao = "eq.${it.idAvaliacao}")
                    _itensExistentes.value = itensResp.body() ?: emptyList()
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
                val idOrientador = sessionManager.idUtilizador.first() ?: return@launch
                // Usa Map para evitar serialização de campos nulos
                val avaliacaoMap = mutableMapOf<String, Any>(
                    "idestagio" to idEstagio,
                    "classificacao" to classificacaoFinal
                )
                if (comentario.isNotBlank()) avaliacaoMap["comentario"] = comentario
                val avalResp = api.createAvaliacaoMap(body = avaliacaoMap)
                if (!avalResp.isSuccessful) {
                    val errorBody = avalResp.errorBody()?.string()
                    android.util.Log.e("OrientadorAvaliacao", "Erro: ${avalResp.code()} - $errorBody")
                    _erro.value = "Erro ao criar avaliação: ${avalResp.code()} - $errorBody"
                    return@launch
                }
                val idAvaliacao = avalResp.body()?.firstOrNull()?.idAvaliacao ?: return@launch
                val agora = java.time.Instant.now().toString()

                val criterios = listOf(
                    "Pontualidade" to pontualidade,
                    "Proatividade" to proatividade,
                    "Competência Técnica" to competenciaTecnica,
                    "Trabalho em Equipa" to trabalhoEquipa
                )
                for ((criterio, nota) in criterios) {
                    val item = ItemAvaliacao(
                        idAvaliacao = idAvaliacao,
                        idAvaliador = idOrientador,
                        classificacao = nota.toDouble(),
                        criterio = criterio,
                        dataAvaliacao = agora
                    )
                    api.createItemAvaliacao(itemAvaliacao = item)
                }
                _avaliacaoExistente.value = Avaliacao(
                    idAvaliacao = idAvaliacao,
                    idEstagio = idEstagio,
                    classificacao = classificacaoFinal,
                    comentario = comentario
                )
                _sucesso.value = true
            } catch (e: Exception) {
                _erro.value = "Erro: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun enviarAvaliacao(
        idEstagio: String,
        pontualidade: Int,
        proatividade: Int,
        competenciaTecnica: Int,
        trabalhoEquipa: Int,
        comentario: String,
        context: Context
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            _erro.value = null
            try {
                val api = RetrofitClient.api
                val idOrientador = sessionManager.idUtilizador.first() ?: return@launch
                val agora = Instant.now().toString()

                // Classificação final = média dos critérios
                val classificacaoFinal = (pontualidade + proatividade + competenciaTecnica + trabalhoEquipa) / 4.0

                // Cria avaliação principal
                val avaliacao = Avaliacao(
                    idEstagio = idEstagio,
                    classificacao = classificacaoFinal,
                    comentario = comentario,
                    dataAvaliacao = agora
                )
                val avalResp = api.createAvaliacao(avaliacao = avaliacao)
                if (!avalResp.isSuccessful) {
                    val errorBody = avalResp.errorBody()?.string()
                    android.util.Log.e("OrientadorAvaliacao", "Erro: ${avalResp.code()} - $errorBody")
                    _erro.value = "Erro ao criar avaliação: ${avalResp.code()} - $errorBody"
                    return@launch
                }

                val idAvaliacao = avalResp.body()?.firstOrNull()?.idAvaliacao ?: return@launch

                // Cria itens de avaliação
                val criterios = listOf(
                    "Pontualidade" to pontualidade,
                    "Proatividade" to proatividade,
                    "Competência Técnica" to competenciaTecnica,
                    "Trabalho em Equipa" to trabalhoEquipa
                )

                for ((criterio, nota) in criterios) {
                    val item = ItemAvaliacao(
                        idAvaliacao = idAvaliacao,
                        idAvaliador = idOrientador,
                        classificacao = nota.toDouble(),
                        criterio = criterio,
                        dataAvaliacao = agora
                    )
                    api.createItemAvaliacao(itemAvaliacao = item)
                }

                _avaliacaoExistente.value = avaliacao.copy(idAvaliacao = idAvaliacao)
                _sucesso.value = true

            } catch (e: Exception) {
                _erro.value = "Erro: ${e.message}"
            } finally {
                _isSaving.value = false
            }
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
