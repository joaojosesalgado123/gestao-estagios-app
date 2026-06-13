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
import pt.ligix.app.util.SessionManager

data class InstituicaoEstagioDetalhe(
    val idEstagio: String,
    val nomeEmpresa: String,
    val tituloOferta: String
)

class InstituicaoHomeViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _nomeAdmin = MutableStateFlow("")
    val nomeAdmin: StateFlow<String> = _nomeAdmin

    private val _totalUtilizadores = MutableStateFlow(0)
    val totalUtilizadores: StateFlow<Int> = _totalUtilizadores

    private val _estagiariosAtivos = MutableStateFlow(0)
    val estagiariosAtivos: StateFlow<Int> = _estagiariosAtivos

    private val _pendentes = MutableStateFlow(0)
    val pendentes: StateFlow<Int> = _pendentes

    private val _estagiosPendentes = MutableStateFlow<List<InstituicaoEstagioDetalhe>>(emptyList())
    val estagiosPendentes: StateFlow<List<InstituicaoEstagioDetalhe>> = _estagiosPendentes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun carregar(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _nomeAdmin.value = sessionManager.nome.first() ?: ""
                val idUtilizador = sessionManager.idUtilizador.first() ?: return@launch
                val api = RetrofitClient.api

                // Busca a instituição do admin logado
                val instResp = api.getInstituicaoByIdUtilizador(idUtilizador = "eq.$idUtilizador")
                val instituicao = instResp.body()?.firstOrNull() ?: return@launch
                val idInstituicao = instituicao.idInstituicao

                // Alunos da instituição
                val alunosResp = api.getAlunosByInstituicao(idInstituicao = "eq.$idInstituicao")
                val alunos = alunosResp.body() ?: emptyList()
                _totalUtilizadores.value = alunos.size

                // Estágios dos alunos desta instituição
                val todosEstagios = mutableListOf<pt.ligix.app.model.Estagio>()
                for (aluno in alunos) {
                    val cands = api.getCandidaturasByAluno(idAluno = "eq.${aluno.idUtilizador}").body() ?: continue
                    val candAceite = cands.firstOrNull { it.status == "aceite" } ?: continue
                    val estagio = api.getEstagioByCandidatura(
                        idCandidatura = "eq.${candAceite.idCandidatura}"
                    ).body()?.firstOrNull() ?: continue
                    todosEstagios.add(estagio)
                }

                _estagiariosAtivos.value = todosEstagios.size
                val semDocente = todosEstagios.filter { it.idDocente.isNullOrBlank() }
                _pendentes.value = semDocente.size

                val lista = mutableListOf<InstituicaoEstagioDetalhe>()
                for (estagio in semDocente.take(10)) {
                    try {
                        val cand = api.getCandidaturaById(
                            idCandidatura = "eq.${estagio.idCandidatura}"
                        ).body()?.firstOrNull()
                        val oferta = cand?.let {
                            api.getOfertaById(idOferta = "eq.${it.idOferta}").body()?.firstOrNull()
                        }
                        val empresa = oferta?.idEmpresa?.let {
                            api.getUtilizadorById(id = "eq.$it").body()?.firstOrNull()
                        }
                        lista.add(InstituicaoEstagioDetalhe(
                            idEstagio = estagio.idEstagio,
                            nomeEmpresa = empresa?.nome ?: "Empresa",
                            tituloOferta = oferta?.titulo ?: "Estágio"
                        ))
                    } catch (_: Exception) {
                    }
                }
                _estagiosPendentes.value = lista

            } catch (_: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
}

class InstituicaoHomeViewModelFactory(
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return InstituicaoHomeViewModel(sessionManager) as T
    }
}
