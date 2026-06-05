package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.content.Intent
import android.net.Uri
import pt.ligix.app.data.remote.RetrofitClient
import pt.ligix.app.data.repository.EmpresaRepository
import pt.ligix.app.model.Atividade
import pt.ligix.app.model.Presenca
import pt.ligix.app.util.SessionManager

class OrientadorDiarioAlunoViewModel(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _atividades = MutableStateFlow<List<Atividade>>(emptyList())
    val atividades: StateFlow<List<Atividade>> = _atividades

    private val _presencas = MutableStateFlow<List<Presenca>>(emptyList())
    val presencas: StateFlow<List<Presenca>> = _presencas

    private val _horasFeitas = MutableStateFlow(0)
    val horasFeitas: StateFlow<Int> = _horasFeitas

    private val _horasTotal = MutableStateFlow(0)
    val horasTotal: StateFlow<Int> = _horasTotal

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun carregarDados(idEstagio: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val api = RetrofitClient.api

                // Atividades
                val atividadesResponse = api.getAtividadesByEstagio(idEstagio = "eq.$idEstagio")
                _atividades.value = atividadesResponse.body() ?: emptyList()

                // Presenças
                val presencasResponse = api.getPresencasByEstagio(idEstagio = "eq.$idEstagio")
                val presencasList = presencasResponse.body() ?: emptyList()
                _presencas.value = presencasList

                // Horas feitas
                _horasFeitas.value = presencasList.count {
                    it.status.equals("presente", ignoreCase = true)
                } * 8

                // Horas total — valor default 480h
                _horasTotal.value = 480

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private var urlRelatorio: String? = null

    fun carregarRelatorio(idEstagio: String) {
        viewModelScope.launch {
            try {
                val resp = RetrofitClient.api.getRelatorioByEstagio(idEstagio = "eq.$idEstagio")
                urlRelatorio = resp.body()?.firstOrNull()?.ficheiroUrl
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun abrirRelatorio(context: Context) {
        val url = urlRelatorio ?: return
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun submeterFeedback(idAtividade: String, feedback: String) {
        viewModelScope.launch {
            try {
                // Implementar quando houver tabela de feedback
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class OrientadorDiarioAlunoViewModelFactory(
    private val repository: EmpresaRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return OrientadorDiarioAlunoViewModel(repository, sessionManager) as T
    }
}
