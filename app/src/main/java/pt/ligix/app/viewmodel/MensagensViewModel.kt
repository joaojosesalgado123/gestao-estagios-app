package pt.ligix.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.model.Conversa
import pt.ligix.app.model.Mensagem
import pt.ligix.app.util.SessionManager
import pt.ligix.app.util.SessionTokenProvider

data class NotificacaoMsg(
    val nomeRemetente: String,
    val conteudo: String,
    val idMensagem: String
)

class MensagensViewModel : ViewModel() {

    private val api = RetrofitClient.api

    private val _conversa = MutableStateFlow<Conversa?>(null)
    val conversa: StateFlow<Conversa?> = _conversa

    private val _mensagens = MutableStateFlow<List<Mensagem>>(emptyList())
    val mensagens: StateFlow<List<Mensagem>> = _mensagens

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _idUtilizador = MutableStateFlow("")
    val idUtilizador: StateFlow<String> = _idUtilizador

    private val _nomeEstagio = MutableStateFlow("Estágio")
    val nomeEstagio: StateFlow<String> = _nomeEstagio

    private val _nomesParticipantes = MutableStateFlow<Map<String, String>>(emptyMap())
    val nomesParticipantes: StateFlow<Map<String, String>> = _nomesParticipantes

    private val _mensagensNaoVistas = MutableStateFlow(0)
    val mensagensNaoVistas: StateFlow<Int> = _mensagensNaoVistas

    private val _novaNotificacao = MutableStateFlow<NotificacaoMsg?>(null)
    val novaNotificacao: StateFlow<NotificacaoMsg?> = _novaNotificacao

    private val _historicoNotificacoes = MutableStateFlow<List<NotificacaoMsg>>(emptyList())
    val historicoNotificacoes: StateFlow<List<NotificacaoMsg>> = _historicoNotificacoes

    // Estado do chat movido para o ViewModel
    private val _mostrarChat = MutableStateFlow(false)
    val mostrarChat: StateFlow<Boolean> = _mostrarChat

    private var ultimoCountVisto = 0
    private var chatEstaAberto = false
    private val idsJaNotificados = mutableSetOf<String>()
    private var descobertaConversaJob: Job? = null
    private var mensagensPollingJob: Job? = null

    fun abrirChat() {
        _mostrarChat.value = true
        marcarComoVisto()
    }

    fun fecharChat() {
        _mostrarChat.value = false
        chatEstaAberto = false
    }

    fun onEcraVisivel() {
        if (_mostrarChat.value) {
            chatEstaAberto = true
            marcarComoVisto()
        }
    }

    fun onEcraEscondido() {
        chatEstaAberto = false
    }

    fun dispensarNotificacao() { _novaNotificacao.value = null }
    fun limparHistoricoNotificacoes() { _historicoNotificacoes.value = emptyList() }

    fun marcarComoVisto() {
        _historicoNotificacoes.value = emptyList()
        chatEstaAberto = true
        val mensagensDeOutros = _mensagens.value.count { it.idRemetente != _idUtilizador.value }
        ultimoCountVisto = mensagensDeOutros
        _mensagensNaoVistas.value = 0
        _novaNotificacao.value = null
        _mensagens.value.forEach { idsJaNotificados.add(it.idMensagem) }
    }

    fun carregarConversaOrientador(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null
            val sessionManager = SessionManager(context)
            val idOrientador = sessionManager.idUtilizador.first() ?: run {
                _erro.value = "Sessão inválida."
                _isLoading.value = false
                return@launch
            }
            _idUtilizador.value = idOrientador
            try {
                // Busca estágios onde este utilizador é orientador
                val estagiosResp = api.getEstagiosByOrientador(idOrientador = "eq.$idOrientador")
                val estagio = estagiosResp.body()?.firstOrNull() ?: run {
                    _erro.value = null
                    _isLoading.value = false
                    return@launch
                }

                val conversaResp = api.getConversaByEstagio(idEstagio = "eq.${estagio.idEstagio}")
                val conversa = conversaResp.body()?.firstOrNull() ?: run {
                    _isLoading.value = false
                    return@launch
                }

                // Nome do estágio
                val candidaturaResp = api.getCandidaturaById(idCandidatura = "eq.${estagio.idCandidatura}")
                val candidatura = candidaturaResp.body()?.firstOrNull()
                candidatura?.idOferta?.let { idOferta ->
                    api.getOfertaById(idOferta = "eq.$idOferta").body()?.firstOrNull()?.let {
                        _nomeEstagio.value = it.titulo
                    }
                }

                // Nomes dos participantes
                val nomes = mutableMapOf<String, String>()
                candidatura?.idAluno?.let { idAluno ->
                    try { api.getUtilizadorById(id = "eq.$idAluno").body()?.firstOrNull()?.let { u -> nomes[idAluno] = u.nome } } catch (_: Exception) {}
                }
                estagio.idDocente?.let { try { api.getUtilizadorById(id = "eq.$it").body()?.firstOrNull()?.let { u -> nomes[it] = u.nome } } catch (_: Exception) {} }
                nomes[idOrientador] = sessionManager.nome.first() ?: "Orientador"
                _nomesParticipantes.value = nomes
                _conversa.value = conversa
                carregarMensagens(conversa.idConversa, primeiraVez = true)
                iniciarPollingMensagens(conversa.idConversa)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isLoading.value = false
        }
    }

    fun carregarConversa(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null
            val sessionManager = SessionManager(context)
            val idAluno = sessionManager.idUtilizador.first() ?: run {
                _erro.value = "Sessão inválida."
                _isLoading.value = false
                return@launch
            }
            _idUtilizador.value = idAluno

            try {
                procurarConversa(idAluno, mostrarErro = true)
            } catch (_: Exception) {
                _erro.value = "Não foi possível carregar mensagens."
            }
            if (_conversa.value == null) iniciarDescobertaConversa(idAluno)

            _isLoading.value = false
        }
    }

    private suspend fun procurarConversa(idAluno: String, mostrarErro: Boolean): Boolean {
        if (_conversa.value != null) return true

        val respCand = api.getCandidaturasByAluno(idAluno = "eq.$idAluno")
        if (!respCand.isSuccessful) {
            if (mostrarErro) _erro.value = "Não foi possível carregar candidaturas."
            return false
        }

        val candidaturaAceite = respCand.body()?.firstOrNull { it.status == "aceite" }
            ?: return false

        val respEstagio = api.getEstagioByCandidatura(idCandidatura = "eq.${candidaturaAceite.idCandidatura}")
        if (!respEstagio.isSuccessful) {
            if (mostrarErro) _erro.value = "Não foi possível carregar o estágio."
            return false
        }

        val estagio = respEstagio.body()?.firstOrNull() ?: return false
        val respConversa = api.getConversaByEstagio(idEstagio = "eq.${estagio.idEstagio}")
        if (!respConversa.isSuccessful) {
            if (mostrarErro) _erro.value = "Não foi possível carregar conversas."
            return false
        }

        val conversa = respConversa.body()?.firstOrNull() ?: return false

        val respOferta = api.getOfertaById(idOferta = "eq.${candidaturaAceite.idOferta}")
        respOferta.body()?.firstOrNull()?.let { _nomeEstagio.value = it.titulo }

        val nomes = mutableMapOf<String, String>()
        try { api.getUtilizadorById(id = "eq.$idAluno").body()?.firstOrNull()?.let { nomes[idAluno] = it.nome } } catch (_: Exception) {}
        estagio.idDocente?.let { try { api.getUtilizadorById(id = "eq.$it").body()?.firstOrNull()?.let { u -> nomes[it] = u.nome } } catch (_: Exception) {} }
        estagio.idOrientador?.let { try { api.getUtilizadorById(id = "eq.$it").body()?.firstOrNull()?.let { u -> nomes[it] = u.nome } } catch (_: Exception) {} }
        _nomesParticipantes.value = nomes

        _erro.value = null
        _conversa.value = conversa
        carregarMensagens(conversa.idConversa, primeiraVez = true)
        iniciarPollingMensagens(conversa.idConversa)
        return true
    }

    private fun iniciarDescobertaConversa(idAluno: String) {
        if (descobertaConversaJob?.isActive == true) return

        descobertaConversaJob = viewModelScope.launch {
            while (isActive && _conversa.value == null) {
                delay(5000)
                try {
                    if (procurarConversa(idAluno, mostrarErro = false)) return@launch
                } catch (_: Exception) {
                    // A descoberta volta a tentar enquanto a app do aluno estiver aberta.
                }
            }
        }
    }

    private suspend fun carregarMensagens(idConversa: String, primeiraVez: Boolean = false) {
        try {
            val resp = api.getMensagensByConversa(idConversa = "eq.$idConversa")
            if (resp.isSuccessful) {
                _erro.value = null
                val novasMensagens = resp.body() ?: emptyList()

                if (primeiraVez) {
                    _mensagens.value = novasMensagens
                    ultimoCountVisto = novasMensagens.count { it.idRemetente != _idUtilizador.value }
                    novasMensagens.forEach { idsJaNotificados.add(it.idMensagem) }
                    return
                }

                _mensagens.value = novasMensagens

                if (!chatEstaAberto) {
                    val novas = novasMensagens.filter { nova ->
                        nova.idRemetente != _idUtilizador.value &&
                        nova.idMensagem !in idsJaNotificados
                    }
                    if (novas.isNotEmpty()) {
                        val mensagensDeOutros = novasMensagens.count { it.idRemetente != _idUtilizador.value }
                        _mensagensNaoVistas.value = mensagensDeOutros - ultimoCountVisto
                        val ultima = novas.last()
                        val nome = _nomesParticipantes.value[ultima.idRemetente] ?: "Desconhecido"
                        val conteudoNotif = if (ultima.ficheiroNome != null) "📎 ${ultima.ficheiroNome}" else ultima.conteudo
                        val notif = NotificacaoMsg(nomeRemetente = nome, conteudo = conteudoNotif, idMensagem = ultima.idMensagem)
                        _novaNotificacao.value = notif
                        _historicoNotificacoes.value = (_historicoNotificacoes.value + notif).takeLast(10)
                        novas.forEach { idsJaNotificados.add(it.idMensagem) }
                    }
                } else {
                    val mensagensDeOutros = novasMensagens.count { it.idRemetente != _idUtilizador.value }
                    ultimoCountVisto = mensagensDeOutros
                    novasMensagens.forEach { idsJaNotificados.add(it.idMensagem) }
                }
            }
        } catch (_: Exception) {
            _erro.value = "Erro ao atualizar mensagens."
        }
    }

    private fun iniciarPollingMensagens(idConversa: String) {
        if (mensagensPollingJob?.isActive == true) return

        mensagensPollingJob = viewModelScope.launch {
            while (isActive) {
                delay(5000)
                carregarMensagens(idConversa)
            }
        }
    }

    fun enviarMensagem(texto: String) {
        val idConversa = _conversa.value?.idConversa ?: return
        val idRemetente = _idUtilizador.value.ifBlank { return }
        viewModelScope.launch {
            _isSending.value = true
            try {
                _erro.value = null
                val mensagem = mapOf(
                    "idremetente" to idRemetente,
                    "conteudo" to texto,
                    "idconversa" to idConversa,
                    "data_envio" to java.time.Instant.now().toString()
                )
                val resp = api.createMensagemMap(mensagem)
                if (resp.isSuccessful || resp.code() == 201) carregarMensagens(idConversa)
                else _erro.value = "Erro ao enviar mensagem."
            } catch (_: Exception) {
                _erro.value = "Erro ao enviar mensagem."
            }
            _isSending.value = false
        }
    }

    fun enviarFicheiro(bytes: ByteArray, nomeOriginal: String) {
        val idConversa = _conversa.value?.idConversa ?: return
        val idRemetente = _idUtilizador.value.ifBlank { return }
        viewModelScope.launch {
            _isSending.value = true
            try {
                _erro.value = null
                val path = "mensagens/$idConversa/${System.currentTimeMillis()}_$nomeOriginal"
                val uploadUrl = "${pt.ligix.app.util.Constants.SUPABASE_URL}/storage/v1/object/mensagens/$path"
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val requestBody = bytes.toRequestBody("application/pdf".toMediaType())
                val requestBuilder = okhttp3.Request.Builder()
                    .url(uploadUrl)
                    .header("apikey", pt.ligix.app.util.Constants.SUPABASE_KEY)
                    .header("Content-Type", "application/pdf")
                    .post(requestBody)
                SessionTokenProvider.accessToken?.let { token ->
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                val request = requestBuilder.build()
                val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                if (response.isSuccessful) {
                    val publicUrl = "${pt.ligix.app.util.Constants.SUPABASE_URL}/storage/v1/object/public/mensagens/$path"
                    val mensagem = mapOf(
                        "idremetente" to idRemetente,
                        "conteudo" to "",
                        "idconversa" to idConversa,
                        "data_envio" to java.time.Instant.now().toString(),
                        "ficheiro_url" to publicUrl,
                        "ficheiro_nome" to nomeOriginal
                    )
                    api.createMensagemMap(mensagem)
                    carregarMensagens(idConversa)
                } else {
                    _erro.value = "Erro no upload do ficheiro."
                }
            } catch (_: Exception) {
                _erro.value = "Erro no upload do ficheiro."
            }
            _isSending.value = false
        }
    }
}

class MensagensViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MensagensViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MensagensViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
