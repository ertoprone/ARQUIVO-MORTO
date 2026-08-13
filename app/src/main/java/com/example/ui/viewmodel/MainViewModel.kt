package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.EgressoEntity
import com.example.data.repository.EgressoRepository
import com.example.util.ExcelCsvParser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FilterState(
    val query: String = "",
    val curso: String = "",
    val status: String = "",
    val caixa: String = "",
    val anoMin: Int = 0,
    val anoMax: Int = 0,
    val sortBy: String = "nome_asc" // "nome_asc", "nome_desc", "ano_asc", "ano_desc", "codigo"
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EgressoRepository

    init {
        val database = AppDatabase.getInstance(application)
        repository = EgressoRepository(database.egressoDao())
        viewModelScope.launch {
            repository.checkAndPreloadSampleData()
        }
    }

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _selectedEgresso = MutableStateFlow<EgressoEntity?>(null)
    val selectedEgresso: StateFlow<EgressoEntity?> = _selectedEgresso.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>("admin@arquivomorto.com")
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    fun loginUser(email: String) {
        _currentUserEmail.value = email
        _isAuthenticated.value = true
        _userFeedbackMessage.value = "Sessão iniciada como $email"
    }

    fun logoutUser() {
        _isAuthenticated.value = false
        _userFeedbackMessage.value = "Sessão encerrada com segurança."
    }

    val distinctCursos: StateFlow<List<String>> = repository.distinctCursos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distinctCaixas: StateFlow<List<String>> = repository.distinctCaixas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val distinctStatus: StateFlow<List<String>> = repository.distinctStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val egressos: StateFlow<List<EgressoEntity>> = _filterState.flatMapLatest { f ->
        repository.searchEgressos(
            query = f.query,
            curso = f.curso,
            status = f.status,
            caixa = f.caixa,
            anoMin = f.anoMin,
            anoMax = f.anoMax,
            sortBy = f.sortBy
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(newQuery: String) {
        _filterState.value = _filterState.value.copy(query = newQuery)
    }

    fun updateCursoFilter(curso: String) {
        _filterState.value = _filterState.value.copy(curso = curso)
    }

    fun updateStatusFilter(status: String) {
        _filterState.value = _filterState.value.copy(status = status)
    }

    fun updateCaixaFilter(caixa: String) {
        _filterState.value = _filterState.value.copy(caixa = caixa)
    }

    fun updateAnoRange(min: Int, max: Int) {
        _filterState.value = _filterState.value.copy(anoMin = min, anoMax = max)
    }

    fun updateSortBy(sortOption: String) {
        _filterState.value = _filterState.value.copy(sortBy = sortOption)
    }

    fun clearFilters() {
        _filterState.value = FilterState(query = _filterState.value.query)
    }

    fun selectEgresso(egresso: EgressoEntity?) {
        _selectedEgresso.value = egresso
    }

    fun clearFeedbackMessage() {
        _userFeedbackMessage.value = null
    }

    fun loadSampleDataset() {
        viewModelScope.launch {
            repository.loadSampleData()
            _userFeedbackMessage.value = "Planilha de exemplo carregada com sucesso!"
        }
    }

    fun importFromSpreadsheetUri(uri: Uri) {
        viewModelScope.launch {
            val result = ExcelCsvParser.parseFromUri(getApplication(), uri)
            if (result.errorMessage != null) {
                _userFeedbackMessage.value = result.errorMessage
            } else if (result.egressos.isEmpty()) {
                _userFeedbackMessage.value = "Nenhum estudante válido foi encontrado no arquivo."
            } else {
                repository.insertEgressos(result.egressos)
                _userFeedbackMessage.value = "Importados ${result.importedCount} egressos com sucesso!"
            }
        }
    }

    fun saveEgresso(egresso: EgressoEntity) {
        viewModelScope.launch {
            if (egresso.id == 0L) {
                repository.insertEgresso(egresso)
                _userFeedbackMessage.value = "Estudante egresso cadastrado com sucesso!"
            } else {
                repository.updateEgresso(egresso)
                _userFeedbackMessage.value = "Dados do egresso atualizados!"
            }
            if (_selectedEgresso.value?.id == egresso.id) {
                _selectedEgresso.value = egresso
            }
        }
    }

    fun deleteEgresso(egresso: EgressoEntity) {
        viewModelScope.launch {
            repository.deleteEgresso(egresso)
            _userFeedbackMessage.value = "Registro de egresso removido."
            if (_selectedEgresso.value?.id == egresso.id) {
                _selectedEgresso.value = null
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
            _userFeedbackMessage.value = "Base de dados zerada."
        }
    }
}
