package pt.ligix.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.util.AppLanguage
import pt.ligix.app.util.SessionManager

data class NotificacaoOrientador(
    val id: String,
    val titulo: String,
    val mensagem: String,
    val tipo: String // "atividade" ou "mensagem"
)

class OrientadorNotificacoesViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _notificacoes = MutableStateFlow<List<NotificacaoOrientador>>(emptyList())
    val notificacoes: StateFlow<List<NotificacaoOrientador>> = _notificacoes

    private val _novaNotificacao = MutableStateFlow<NotificacaoOrientador?>(null)
    val novaNotificacao: StateFlow<NotificacaoOrientador?> = _novaNotificacao

    private val idsAtividadesVistas = mutableSetOf<String>()
    private var pollingJob: Job? = null

    fun iniciar(context: Context) {
        viewModelScope.launch {
            carregarInicial()
            iniciarPolling()
        }
    }

    private suspend fun carregarInicial() {
        try {
            val idOrientador = sessionManager.idUtilizador.first() ?: return
            val api = RetrofitClient.api

            val estagiosResp = api.getEstagiosByOrientador(idOrientador = "eq.$idOrientador")
            val estagios = estagiosResp.body() ?: return

            for (estagio in estagios) {
                val atividadesResp = api.getAtividadesByEstagio(idEstagio = "eq.${estagio.idEstagio}")
                val atividades = atividadesResp.body() ?: continue
                atividades.forEach { idsAtividadesVistas.add(it.idAtividade) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun iniciarPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(10000) // 10 segundos
                verificarNovasAtividades()
            }
        }
    }

    private suspend fun verificarNovasAtividades() {
        try {
            val idOrientador = sessionManager.idUtilizador.first() ?: return
            val api = RetrofitClient.api
            val repository = pt.ligix.app.data.repository.EmpresaRepository()

            val estagiosResp = api.getEstagiosByOrientador(idOrientador = "eq.$idOrientador")
            val estagios = estagiosResp.body() ?: return

            for (estagio in estagios) {
                // Verifica novas atividades
                val atividadesResp = api.getAtividadesByEstagio(idEstagio = "eq.${estagio.idEstagio}")
                val atividades = atividadesResp.body() ?: continue
                val novas = atividades.filter { it.idAtividade !in idsAtividadesVistas }

                for (nova in novas) {
                    idsAtividadesVistas.add(nova.idAtividade)
                    val english = sessionManager.language.first() == AppLanguage.EN

                    // Busca nome do aluno
                    val candResp = api.getCandidaturaById(idCandidatura = "eq.${estagio.idCandidatura}")
                    val idAluno = candResp.body()?.firstOrNull()?.idAluno ?: ""
                    val nomeAluno = repository.getNomeUtilizador(idAluno).getOrNull() ?: if (english) "Student" else "Aluno"

                    val notif = NotificacaoOrientador(
                        id = nova.idAtividade,
                        titulo = if (english) "New activity" else "Nova atividade",
                        mensagem = if (english) {
                            "$nomeAluno registered a new activity: \"${nova.titulo}\""
                        } else {
                            "$nomeAluno registou uma nova atividade: \"${nova.titulo}\""
                        },
                        tipo = "atividade"
                    )
                    _notificacoes.value = (_notificacoes.value + notif).takeLast(20)
                    _novaNotificacao.value = notif
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun dispensarNotificacao() { _novaNotificacao.value = null }
    fun limparNotificacoes() { _notificacoes.value = emptyList() }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}

class OrientadorNotificacoesViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return OrientadorNotificacoesViewModel(sessionManager) as T
    }
}
