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
import pt.ligix.app.data.repository.DocenteRepository
import pt.ligix.app.util.SessionManager

data class NotificacaoDocente(
    val id: String,
    val titulo: String,
    val mensagem: String,
    val tipo: String
)

class DocenteNotificacoesViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _notificacoes = MutableStateFlow<List<NotificacaoDocente>>(emptyList())
    val notificacoes: StateFlow<List<NotificacaoDocente>> = _notificacoes

    private val _novaNotificacao = MutableStateFlow<NotificacaoDocente?>(null)
    val novaNotificacao: StateFlow<NotificacaoDocente?> = _novaNotificacao

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
            val idDocente = sessionManager.idUtilizador.first() ?: return
            val api = RetrofitClient.api
            val estagios = api.getEstagiosByDocente(idDocente = "eq.$idDocente").body().orEmpty()
            estagios.forEach { estagio ->
                api.getAtividadesByEstagio(idEstagio = "eq.${estagio.idEstagio}")
                    .body().orEmpty()
                    .forEach { idsAtividadesVistas.add(it.idAtividade) }
            }
        } catch (_: Exception) {
        }
    }

    private fun iniciarPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(10000)
                verificarNovasAtividades()
            }
        }
    }

    private suspend fun verificarNovasAtividades() {
        try {
            val idDocente = sessionManager.idUtilizador.first() ?: return
            val api = RetrofitClient.api
            val repository = DocenteRepository()
            val estagios = api.getEstagiosByDocente(idDocente = "eq.$idDocente").body().orEmpty()

            for (estagio in estagios) {
                val atividades = api.getAtividadesByEstagio(
                    idEstagio = "eq.${estagio.idEstagio}"
                ).body().orEmpty()
                val novas = atividades.filter { it.idAtividade !in idsAtividadesVistas }

                for (nova in novas) {
                    idsAtividadesVistas.add(nova.idAtividade)

                    val candidatura = api.getCandidaturaById(
                        idCandidatura = "eq.${estagio.idCandidatura}"
                    ).body()?.firstOrNull()
                    val nomeAluno = candidatura?.idAluno?.let { idAluno ->
                        repository.getNomeUtilizador(idAluno).getOrNull()
                    } ?: "Aluno"

                    val notificacao = NotificacaoDocente(
                        id = nova.idAtividade,
                        titulo = "Nova atividade",
                        mensagem = "$nomeAluno submeteu uma nova atividade: \"${nova.titulo}\"",
                        tipo = "atividade"
                    )
                    _notificacoes.value = (_notificacoes.value + notificacao).takeLast(20)
                    _novaNotificacao.value = notificacao
                }
            }
        } catch (_: Exception) {
        }
    }

    fun dispensarNotificacao() {
        _novaNotificacao.value = null
    }

    fun limparNotificacoes() {
        _notificacoes.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}

class DocenteNotificacoesViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DocenteNotificacoesViewModel(sessionManager) as T
    }
}
