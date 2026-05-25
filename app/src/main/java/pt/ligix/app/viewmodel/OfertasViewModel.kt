package pt.ligix.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ligix.app.data.repository.OfertasRepository
import pt.ligix.app.model.OfertaEstagio

class OfertasViewModel(
    private val repository: OfertasRepository
) : ViewModel() {

    private val _todasOfertas = MutableStateFlow<List<OfertaEstagio>>(emptyList())

    private val _ofertasFiltradas = MutableStateFlow<List<OfertaEstagio>>(emptyList())
    val ofertasFiltradas: StateFlow<List<OfertaEstagio>> = _ofertasFiltradas

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filtroArea = MutableStateFlow<String?>(null)
    val filtroArea: StateFlow<String?> = _filtroArea

    private val _filtroLocalizacao = MutableStateFlow<String?>(null)
    val filtroLocalizacao: StateFlow<String?> = _filtroLocalizacao

    private val _filtroDuracao = MutableStateFlow<Int?>(null)
    val filtroDuracao: StateFlow<Int?> = _filtroDuracao

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    // Listas únicas para os filtros
    private val _areas = MutableStateFlow<List<String>>(emptyList())
    val areas: StateFlow<List<String>> = _areas

    private val _localizacoes = MutableStateFlow<List<String>>(emptyList())
    val localizacoes: StateFlow<List<String>> = _localizacoes

    private val _duracoes = MutableStateFlow<List<Int>>(emptyList())
    val duracoes: StateFlow<List<Int>> = _duracoes

    init {
        carregarOfertas()
    }

    fun carregarOfertas() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getOfertas().onSuccess { lista ->
                _todasOfertas.value = lista
                _areas.value = lista.mapNotNull { it.area }.distinct().sorted()
                _localizacoes.value = lista.mapNotNull { it.localizacao }.distinct().sorted()
                _duracoes.value = lista.mapNotNull { it.duracao }.distinct().sorted()
                aplicarFiltros()
            }.onFailure {
                _erro.value = it.message
            }
            _isLoading.value = false
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        aplicarFiltros()
    }

    fun onFiltroAreaChange(area: String?) {
        _filtroArea.value = area
        aplicarFiltros()
    }

    fun onFiltroLocalizacaoChange(localizacao: String?) {
        _filtroLocalizacao.value = localizacao
        aplicarFiltros()
    }

    fun onFiltroDuracaoChange(duracao: Int?) {
        _filtroDuracao.value = duracao
        aplicarFiltros()
    }

    fun limparFiltros() {
        _filtroArea.value = null
        _filtroLocalizacao.value = null
        _filtroDuracao.value = null
        _searchQuery.value = ""
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        var lista = _todasOfertas.value

        val query = _searchQuery.value.trim().lowercase()
        if (query.isNotEmpty()) {
            lista = lista.filter {
                it.titulo.lowercase().contains(query) ||
                it.area?.lowercase()?.contains(query) == true ||
                it.localizacao?.lowercase()?.contains(query) == true ||
                it.descricao?.lowercase()?.contains(query) == true
            }
        }

        _filtroArea.value?.let { area ->
            lista = lista.filter { it.area == area }
        }

        _filtroLocalizacao.value?.let { loc ->
            lista = lista.filter { it.localizacao == loc }
        }

        _filtroDuracao.value?.let { dur ->
            lista = lista.filter { it.duracao == dur }
        }

        _ofertasFiltradas.value = lista
    }
}
