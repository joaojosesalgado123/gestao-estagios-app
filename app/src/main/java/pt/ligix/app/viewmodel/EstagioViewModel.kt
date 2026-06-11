package pt.ligix.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.data.repository.AtividadesRepository
import pt.ligix.app.data.repository.OfertasRepository
import pt.ligix.app.model.Atividade
import pt.ligix.app.model.Estagio
import pt.ligix.app.model.ItemAvaliacao
import pt.ligix.app.model.Presenca
import pt.ligix.app.model.RelatorioFinal
import pt.ligix.app.model.descricaoComCategoriaAtividade
import pt.ligix.app.util.SessionManager
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class EstagioViewModel : ViewModel() {

    private val _estagio = MutableStateFlow<Estagio?>(null)
    val estagio: StateFlow<Estagio?> = _estagio

    private val _presencas = MutableStateFlow<List<Presenca>>(emptyList())
    val presencas: StateFlow<List<Presenca>> = _presencas

    private val _atividades = MutableStateFlow<List<Atividade>>(emptyList())
    val atividades: StateFlow<List<Atividade>> = _atividades

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    private val _presencaDialogDia = MutableStateFlow<LocalDate?>(null)
    val presencaDialogDia: StateFlow<LocalDate?> = _presencaDialogDia

    private val _feedbackMsg = MutableStateFlow<String?>(null)
    val feedbackMsg: StateFlow<String?> = _feedbackMsg

    private val _horasTotal = MutableStateFlow(0)
    val horasTotal: StateFlow<Int> = _horasTotal

    private val _aGuardarAtividade = MutableStateFlow(false)
    val aGuardarAtividade: StateFlow<Boolean> = _aGuardarAtividade

    private val _aEnviarRelatorio = MutableStateFlow(false)
    val aEnviarRelatorio: StateFlow<Boolean> = _aEnviarRelatorio

    private val _relatorioFinal = MutableStateFlow<RelatorioFinal?>(null)
    val relatorioFinal: StateFlow<RelatorioFinal?> = _relatorioFinal

    private val _notaEmpresa = MutableStateFlow<Double?>(null)
    val notaEmpresa: StateFlow<Double?> = _notaEmpresa

    private val _notaDocente = MutableStateFlow<Double?>(null)
    val notaDocente: StateFlow<Double?> = _notaDocente

    private val _notaFinal = MutableStateFlow<Double?>(null)
    val notaFinal: StateFlow<Double?> = _notaFinal

    private val _nomeEmpresa = MutableStateFlow("—")
    val nomeEmpresa: StateFlow<String> = _nomeEmpresa

    private val _nomeOrientadorEmpresa = MutableStateFlow("—")
    val nomeOrientadorEmpresa: StateFlow<String> = _nomeOrientadorEmpresa

    private var atividadesRepository: AtividadesRepository? = null
    private var atividadesJob: Job? = null
    private var atividadesObservationKey: String? = null
    private var idAlunoAtual: String? = null

    fun carregarDados(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _erro.value = null
            limparDadosEstagio()
            val repository = atividadesRepository
                ?: AtividadesRepository(context).also { atividadesRepository = it }
            try {
                val sessionManager = SessionManager(context)
                val idAluno = sessionManager.idUtilizador.first() ?: return@launch
                idAlunoAtual = idAluno
                val estagioEmCache = repository.obterEstagioAtivo(idAluno)
                if (estagioEmCache != null) {
                    _estagio.value = estagioEmCache
                    observarAtividades(repository, idAluno, estagioEmCache.idEstagio)
                }

                val api = RetrofitClient.api

                val candidaturasResponse = api.getCandidaturasByAluno(
                    idAluno = "eq.$idAluno"
                )
                if (!candidaturasResponse.isSuccessful) {
                    throw IllegalStateException(
                        apiError("Erro ao carregar candidaturas", candidaturasResponse)
                    )
                }
                val candidaturas = candidaturasResponse.body().orEmpty()

                val candidaturaAceite = candidaturas.firstOrNull { it.status == "aceite" }
                    ?: run {
                        limparDadosEstagio()
                        return@launch
                    }

                val estagiosResponse = api.getEstagioByCandidatura(
                    idCandidatura = "eq.${candidaturaAceite.idCandidatura}"
                )
                if (!estagiosResponse.isSuccessful) {
                    throw IllegalStateException(
                        apiError("Erro ao carregar estágio", estagiosResponse)
                    )
                }
                val estagios = estagiosResponse.body().orEmpty()

                val estagioAtual = estagios.firstOrNull() ?: run {
                    limparDadosEstagio()
                    return@launch
                }
                _estagio.value = estagioAtual
                repository.guardarEstagioAtivo(idAluno, estagioAtual)
                observarAtividades(repository, idAluno, estagioAtual.idEstagio)

                try {
                    val oferta = api.getOfertaById(
                        idOferta = "eq.${candidaturaAceite.idOferta}"
                    ).body()?.firstOrNull()
                    _horasTotal.value = oferta?.duracao?.takeIf { it > 0 } ?: 0
                    oferta?.idEmpresa?.takeIf { it.isNotBlank() }?.let { idEmpresa ->
                        _nomeEmpresa.value = api.getUtilizadorById(
                            id = "eq.$idEmpresa"
                        ).body()?.firstOrNull()?.nome?.takeIf { it.isNotBlank() } ?: "—"
                    }
                } catch (_: Exception) {}

                try {
                    estagioAtual.idOrientador?.takeIf { it.isNotBlank() }?.let { idOrientador ->
                        _nomeOrientadorEmpresa.value = api.getUtilizadorById(
                            id = "eq.$idOrientador"
                        ).body()?.firstOrNull()?.nome?.takeIf { it.isNotBlank() } ?: "—"
                    }
                } catch (_: Exception) {}

                runCatching { recarregarPresencas(estagioAtual.idEstagio) }
                repository.atualizarCacheRemoto(idAluno, estagioAtual.idEstagio)
                runCatching { recarregarRelatorioFinal(estagioAtual.idEstagio) }
                runCatching { recarregarAvaliacaoFinal(estagioAtual) }

            } catch (e: Exception) {
                if (_estagio.value == null) {
                    _erro.value = "Sem ligação. Abre o estágio online pelo menos uma vez antes de registar atividades offline."
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun limparDadosEstagio() {
        _estagio.value = null
        _presencas.value = emptyList()
        _atividades.value = emptyList()
        _horasTotal.value = 0
        _relatorioFinal.value = null
        _notaEmpresa.value = null
        _notaDocente.value = null
        _notaFinal.value = null
        _nomeEmpresa.value = "—"
        _nomeOrientadorEmpresa.value = "—"
        atividadesObservationKey = null
        atividadesJob?.cancel()
        atividadesJob = null
    }

    private fun observarAtividades(
        repository: AtividadesRepository,
        idAluno: String,
        idEstagio: String
    ) {
        val key = "$idAluno:$idEstagio"
        if (atividadesObservationKey == key) return

        atividadesObservationKey = key
        atividadesJob?.cancel()
        atividadesJob = viewModelScope.launch {
            repository.observarAtividades(idAluno, idEstagio).collect {
                _atividades.value = it
            }
        }
    }

    private suspend fun recarregarPresencas(idEstagio: String) {
        val response = RetrofitClient.api.getPresencasByEstagio(
            idEstagio = "eq.$idEstagio"
        )

        if (!response.isSuccessful) {
            throw IllegalStateException(apiError("Erro ao carregar presenças", response))
        }

        _presencas.value = response.body().orEmpty()
    }

    private suspend fun recarregarRelatorioFinal(idEstagio: String) {
        val response = RetrofitClient.api.getRelatorioByEstagio(
            idEstagio = "eq.$idEstagio"
        )

        if (!response.isSuccessful) {
            throw IllegalStateException(apiError("Erro ao carregar relatório", response))
        }

        _relatorioFinal.value = response.body().orEmpty()
            .maxByOrNull { it.dataSubmissao.ifBlank { it.createdAt } }
    }

    private suspend fun recarregarAvaliacaoFinal(estagioAtual: Estagio) {
        _notaEmpresa.value = null
        _notaDocente.value = null
        _notaFinal.value = null

        val api = RetrofitClient.api
        val response = api.getAvaliacaoByEstagio(
            idEstagio = "eq.${estagioAtual.idEstagio}"
        )

        val avaliacoes = if (response.isSuccessful) {
            response.body().orEmpty()
        } else {
            emptyList()
        }.ifEmpty {
            val fallback = api.getAvaliacaoByEstagioCamel(
                idEstagio = "eq.${estagioAtual.idEstagio}"
            )
            if (fallback.isSuccessful) fallback.body().orEmpty() else emptyList()
        }

        val avaliacao = avaliacoes
            .maxByOrNull { it.dataAvaliacao?.ifBlank { it.createdAt } ?: it.createdAt }
            ?: return

        var notaEmpresa: Double? = null
        var notaDocente: Double? = null

        if (avaliacao.idAvaliacao.isNotBlank()) {
            val itensResponse = api.getItensAvaliacaoByAvaliacao(
                idAvaliacao = "eq.${avaliacao.idAvaliacao}"
            )

            val itens = if (itensResponse.isSuccessful) {
                itensResponse.body().orEmpty()
            } else {
                emptyList()
            }.ifEmpty {
                val fallback = api.getItensAvaliacaoByAvaliacaoLower(
                    idAvaliacao = "eq.${avaliacao.idAvaliacao}"
                )
                if (fallback.isSuccessful) fallback.body().orEmpty() else emptyList()
            }

            if (itens.isNotEmpty()) {
                val idOrientador = estagioAtual.idOrientador.orEmpty().lowercase()
                val idDocente = estagioAtual.idDocente.orEmpty().lowercase()
                val itensComNotaFinal = itens
                    .filter { it.classificacao != null && it.ehItemDeNotaFinal() }
                    .sortedBy { it.dataAvaliacao.ifBlank { "" } }

                var itemEmpresa = itensComNotaFinal.firstOrNull {
                    idOrientador.isNotBlank() && it.idAvaliador.lowercase() == idOrientador
                }

                var itemDocente = itensComNotaFinal.firstOrNull {
                    idDocente.isNotBlank() && it.idAvaliador.lowercase() == idDocente
                }

                if (itemEmpresa == null) {
                    itemEmpresa = itensComNotaFinal.firstOrNull {
                        it !== itemDocente &&
                            it.comentario?.contains("empresa", ignoreCase = true) == true
                    }
                }

                if (itemDocente == null) {
                    itemDocente = itensComNotaFinal.firstOrNull {
                        it !== itemEmpresa &&
                            it.comentario?.contains("docente", ignoreCase = true) == true
                    }
                }

                if (itemEmpresa == null && itensComNotaFinal.size >= 2) {
                    itemEmpresa = itensComNotaFinal.firstOrNull {
                        it !== itemDocente &&
                            (idDocente.isBlank() || it.idAvaliador.lowercase() != idDocente)
                    } ?: itensComNotaFinal.firstOrNull { it !== itemDocente }
                }

                if (itemDocente == null && itensComNotaFinal.size >= 2) {
                    itemDocente = itensComNotaFinal.firstOrNull {
                        it !== itemEmpresa &&
                            (idOrientador.isBlank() || it.idAvaliador.lowercase() != idOrientador)
                    } ?: itensComNotaFinal.firstOrNull { it !== itemEmpresa }
                }

                notaEmpresa = itemEmpresa?.classificacao?.coerceIn(0.0, 20.0)
                notaDocente = itemDocente?.classificacao?.coerceIn(0.0, 20.0)
            }
        }

        _notaEmpresa.value = notaEmpresa
        _notaDocente.value = notaDocente
        _notaFinal.value = if (notaEmpresa != null && notaDocente != null) {
            (notaEmpresa + notaDocente) / 2.0
        } else {
            null
        }
    }

    fun abrirDialogPresenca(dia: LocalDate, mostrarErroSemEstagio: Boolean = false) {
        if (_estagio.value?.idEstagio.isNullOrBlank()) {
            if (mostrarErroSemEstagio) {
                _erro.value = "Ainda não tens um estágio atribuído. A marcação de presenças fica disponível assim que fores colocado."
            }
            return
        }
        _presencaDialogDia.value = dia
    }

    fun fecharDialogPresenca() {
        _presencaDialogDia.value = null
    }

    fun registarPresenca(dia: LocalDate, presente: Boolean) {
        viewModelScope.launch {
            try {
                val estagioId = _estagio.value?.idEstagio ?: run {
                    _erro.value = "Ainda não tens um estágio atribuído. A marcação de presenças fica disponível assim que fores colocado."
                    _presencaDialogDia.value = null
                    return@launch
                }
                val dataStr = dia.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val status = if (presente) "presente" else "ausente"
                val api = RetrofitClient.api
                _erro.value = null

                val existente = _presencas.value.firstOrNull {
                    try { LocalDate.parse(it.data) == dia } catch (e: Exception) { false }
                }

                val response = if (existente != null && existente.idPresenca.isNotBlank()) {
                    api.updatePresenca(
                        id = "eq.${existente.idPresenca}",
                        presenca = mapOf(
                            "data" to dataStr,
                            "status" to status,
                            "idestagio" to estagioId
                        )
                    )
                } else {
                    api.createPresenca(
                        presenca = mapOf(
                            "idpresenca" to UUID.randomUUID().toString(),
                            "data" to dataStr,
                            "status" to status,
                            "idestagio" to estagioId
                        )
                    )
                }

                if (!response.isSuccessful) {
                    throw IllegalStateException(apiError("Erro ao gravar presença", response))
                }

                val gravada = response.body()?.firstOrNull()
                if (gravada != null) {
                    _presencas.value = _presencas.value
                        .filterNot { it.data == dataStr }
                        .plus(gravada)
                } else {
                    _presencas.value = _presencas.value
                        .filterNot { it.data == dataStr }
                        .plus(
                            Presenca(
                                idPresenca = existente?.idPresenca.orEmpty(),
                                data = dataStr,
                                status = status,
                                idEstagio = estagioId,
                                createdAt = existente?.createdAt.orEmpty()
                            )
                        )
                }

                try {
                    recarregarPresencas(estagioId)
                } catch (_: Exception) {
                    // Mantem a atualização otimista no ecrã quando a gravação já foi aceite.
                }

                _feedbackMsg.value = if (presente) "Presença registada ✓" else "Ausência registada"
                _presencaDialogDia.value = null

            } catch (e: Exception) {
                _erro.value = e.message ?: "Erro ao registar presença"
                _presencaDialogDia.value = null
            }
        }
    }

    private fun <T> apiError(prefixo: String, response: Response<T>): String {
        val detalhe = try {
            response.errorBody()?.string()
        } catch (_: Exception) {
            null
        }.orEmpty().ifBlank { response.message() }

        if (response.code() == 401 && detalhe.contains("row-level security", ignoreCase = true)) {
            return "$prefixo: sem permissão na base de dados para esta tabela."
        }

        return "$prefixo (${response.code()}): $detalhe"
    }

    fun limparFeedback() {
        _feedbackMsg.value = null
    }

    fun limparErro() {
        _erro.value = null
    }

    fun registarAtividade(
        data: LocalDate,
        categoria: String,
        titulo: String,
        descricao: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _aGuardarAtividade.value = true
            _erro.value = null

            try {
                val estagioId = _estagio.value?.idEstagio
                    ?: throw IllegalStateException("Não foi possível identificar o estágio.")
                val dataStr = data.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val hojeStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val idAtividade = UUID.randomUUID().toString()
                val atividadeCriada = Atividade(
                    idAtividade = idAtividade,
                    dataAtividade = dataStr,
                    dataRegisto = hojeStr,
                    descricao = descricaoComCategoriaAtividade(categoria, descricao),
                    idEstagio = estagioId,
                    titulo = titulo.trim()
                )
                val idAluno = idAlunoAtual
                    ?: throw IllegalStateException("Sessão inválida.")
                val repository = atividadesRepository
                    ?: throw IllegalStateException("O modo offline ainda não foi inicializado.")
                repository.registarAtividade(idAluno, atividadeCriada)

                _feedbackMsg.value = "Atividade guardada. A sincronização será automática."
                onSuccess()

            } catch (e: Exception) {
                _erro.value = e.message ?: "Erro ao registar atividade"
            } finally {
                _aGuardarAtividade.value = false
            }
        }
    }

    fun editarAtividade(
        atividade: Atividade,
        data: LocalDate,
        categoria: String,
        titulo: String,
        descricao: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _aGuardarAtividade.value = true
            _erro.value = null

            try {
                if (atividade.idAtividade.isBlank()) {
                    throw IllegalStateException("Não foi possível identificar a atividade.")
                }

                val estagioId = atividade.idEstagio.ifBlank {
                    _estagio.value?.idEstagio ?: throw IllegalStateException("Não foi possível identificar o estágio.")
                }
                val dataStr = data.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val atividadeAtualizada = atividade.copy(
                    titulo = titulo.trim(),
                    descricao = descricaoComCategoriaAtividade(categoria, descricao),
                    dataAtividade = dataStr,
                    idEstagio = estagioId
                )
                val idAluno = idAlunoAtual
                    ?: throw IllegalStateException("Sessão inválida.")
                val repository = atividadesRepository
                    ?: throw IllegalStateException("O modo offline ainda não foi inicializado.")
                repository.editarAtividade(idAluno, atividadeAtualizada)

                _feedbackMsg.value = "Alteração guardada. A sincronização será automática."
                onSuccess()

            } catch (e: Exception) {
                _erro.value = e.message ?: "Erro ao editar atividade"
            } finally {
                _aGuardarAtividade.value = false
            }
        }
    }

    fun apagarAtividade(atividade: Atividade) {
        viewModelScope.launch {
            _aGuardarAtividade.value = true
            _erro.value = null

            try {
                if (atividade.idAtividade.isBlank()) {
                    throw IllegalStateException("Não foi possível identificar a atividade.")
                }

                val idAluno = idAlunoAtual
                    ?: throw IllegalStateException("Sessão inválida.")
                val repository = atividadesRepository
                    ?: throw IllegalStateException("O modo offline ainda não foi inicializado.")
                repository.apagarAtividade(idAluno, atividade)

                _feedbackMsg.value = "Atividade apagada. A sincronização será automática."

            } catch (e: Exception) {
                _erro.value = e.message ?: "Erro ao apagar atividade"
            } finally {
                _aGuardarAtividade.value = false
            }
        }
    }

    fun submeterRelatorioFinal(
        context: Context,
        ficheiroUri: Uri,
        nomeFicheiro: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _aEnviarRelatorio.value = true
            _erro.value = null

            try {
                val estagioId = _estagio.value?.idEstagio
                    ?: throw IllegalStateException("Não foi possível identificar o estágio.")
                val idAluno = SessionManager(context).idUtilizador.first()
                    ?: throw IllegalStateException("Sessão inválida.")

                recarregarRelatorioFinal(estagioId)
                if (_relatorioFinal.value != null) {
                    throw IllegalStateException("O relatório final já foi submetido.")
                }

                val tamanhoBytes = context.contentResolver.openAssetFileDescriptor(ficheiroUri, "r")?.use {
                    it.length
                } ?: -1L

                if (tamanhoBytes > 25L * 1024L * 1024L) {
                    throw IllegalStateException("O relatório não pode ultrapassar 25MB.")
                }

                val idRelatorio = UUID.randomUUID().toString()
                val dataSubmissao = java.time.OffsetDateTime.now().toString()
                val caminhoRelatorio = "$idAluno/$estagioId/relatorio-final-$idRelatorio.pdf"
                val bytes = context.contentResolver.openInputStream(ficheiroUri)?.use {
                    it.readBytes()
                } ?: throw IllegalStateException("Erro ao ler o ficheiro do relatório.")

                if (bytes.size > 25L * 1024L * 1024L) {
                    throw IllegalStateException("O relatório não pode ultrapassar 25MB.")
                }

                val upload = OfertasRepository().uploadFicheiro(
                    bucket = "relatorios",
                    path = caminhoRelatorio,
                    bytes = bytes
                )

                upload.getOrElse {
                    throw IllegalStateException("Erro no upload do relatório. Tenta novamente.")
                }

                val response = RetrofitClient.api.createRelatorio(
                    relatorio = mapOf(
                        "idrelatorio" to idRelatorio,
                        "ficheiro" to caminhoRelatorio,
                        "data_submissao" to dataSubmissao,
                        "observacoes" to "Relatório final submetido pelo aluno: $nomeFicheiro",
                        "idestagio" to estagioId
                    )
                )

                if (!response.isSuccessful) {
                    if (response.code() == 409) {
                        runCatching { recarregarRelatorioFinal(estagioId) }
                        throw IllegalStateException("O relatório final já foi submetido.")
                    }
                    throw IllegalStateException(apiError("Erro ao submeter relatório", response))
                }

                _relatorioFinal.value = RelatorioFinal(
                    idRelatorio = idRelatorio,
                    ficheiro = caminhoRelatorio,
                    dataSubmissao = dataSubmissao,
                    observacoes = "Relatório final submetido pelo aluno: $nomeFicheiro",
                    idEstagio = estagioId
                )
                runCatching { recarregarRelatorioFinal(estagioId) }
                _feedbackMsg.value = "Relatório submetido ✓"
                onSuccess()

            } catch (e: Exception) {
                _erro.value = e.message ?: "Erro ao submeter relatório"
            } finally {
                _aEnviarRelatorio.value = false
            }
        }
    }

    fun getHorasFeitas(): Int = _presencas.value.count {
        it.status.equals("presente", ignoreCase = true)
    } * 8
}

class EstagioViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EstagioViewModel() as T
    }
}
