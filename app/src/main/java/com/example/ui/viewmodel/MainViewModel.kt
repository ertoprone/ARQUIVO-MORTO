package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.EgressoEntity
import com.example.data.repository.EgressoRepository
import com.example.util.ExcelCsvParser
import com.example.util.PreferencesHelper
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
    private val prefsHelper = PreferencesHelper(application)

    private var realtimeListener: com.google.firebase.firestore.ListenerRegistration? = null

    init {
        val database = AppDatabase.getInstance(application)
        repository = EgressoRepository(database.egressoDao())
        
        // Auto-clean any legacy "Rúbia Elise" operator from local prefs if present
        val currentLocalOps = prefsHelper.operatorsList.filter { !it.contains("Rúbia", ignoreCase = true) && !it.contains("Rubia", ignoreCase = true) }
        val sanitizedOps = if (currentLocalOps.isEmpty()) listOf("Secretaria") else currentLocalOps
        if (sanitizedOps != prefsHelper.operatorsList) {
            prefsHelper.operatorsList = sanitizedOps
            if (prefsHelper.activeOperatorName.contains("Rúbia", ignoreCase = true) || prefsHelper.activeOperatorName.contains("Rubia", ignoreCase = true)) {
                prefsHelper.activeOperatorName = sanitizedOps.first()
            }
        }

        startRealtimeListener()
        fetchCloudSystemConfig()
    }

    private fun fetchCloudSystemConfig() {
        viewModelScope.launch {
            try {
                val cloudCfg = repository.fetchSystemConfig()
                if (cloudCfg != null) {
                    val sName = cloudCfg["schoolName"] as? String
                    val rEmail = cloudCfg["registeredEmail"] as? String
                    val rPass = cloudCfg["registeredPassword"] as? String
                    @Suppress("UNCHECKED_CAST")
                    val ops = cloudCfg["operatorsList"] as? List<String>

                    if (!sName.isNullOrBlank() && prefsHelper.schoolName == PreferencesHelper.DEFAULT_SCHOOL_NAME) {
                        prefsHelper.schoolName = sName
                        _schoolName.value = sName
                    }
                    if (!rPass.isNullOrBlank() && prefsHelper.registeredPassword.isBlank()) {
                        prefsHelper.registeredPassword = rPass
                        prefsHelper.isAccessConfigured = true
                        _isAccessConfigured.value = true
                    }
                    if (!rEmail.isNullOrBlank()) {
                        prefsHelper.registeredEmail = rEmail
                        _registeredEmail.value = rEmail
                    }
                    if (!ops.isNullOrEmpty()) {
                        val cleanOps = ops.filter { !it.contains("Rúbia", ignoreCase = true) && !it.contains("Rubia", ignoreCase = true) }
                        val finalOps = if (cleanOps.isEmpty()) listOf("Secretaria") else cleanOps
                        prefsHelper.operatorsList = finalOps
                        _operatorsList.value = finalOps
                        if (_activeOperator.value.contains("Rúbia", ignoreCase = true) || _activeOperator.value.contains("Rubia", ignoreCase = true)) {
                            _activeOperator.value = finalOps.first()
                            prefsHelper.activeOperatorName = finalOps.first()
                        }
                        if (cleanOps.size != ops.size) {
                            pushConfigToCloud()
                        }
                    }
                }
            } catch (e: Exception) {
                // ignore network errors
            }
        }
    }

    private fun pushConfigToCloud() {
        viewModelScope.launch {
            try {
                repository.pushSystemConfig(
                    schoolName = prefsHelper.schoolName,
                    email = prefsHelper.registeredEmail,
                    password = prefsHelper.registeredPassword,
                    operators = prefsHelper.operatorsList
                )
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    private fun startRealtimeListener() {
        if (realtimeListener != null) return
        realtimeListener = repository.startRealtimeSync { count ->
            // Cloud updated local records automatically
        }
    }

    override fun onCleared() {
        super.onCleared()
        realtimeListener?.remove()
        realtimeListener = null
    }

    private val _schoolName = MutableStateFlow(prefsHelper.schoolName)
    val schoolName: StateFlow<String> = _schoolName.asStateFlow()

    private val _activeOperator = MutableStateFlow(prefsHelper.activeOperatorName)
    val activeOperator: StateFlow<String> = _activeOperator.asStateFlow()

    val operatorName: StateFlow<String> = _activeOperator.asStateFlow()

    private val _operatorsList = MutableStateFlow(prefsHelper.operatorsList)
    val operatorsList: StateFlow<List<String>> = _operatorsList.asStateFlow()

    private val _isAccessConfigured = MutableStateFlow(prefsHelper.isAccessConfigured)
    val isAccessConfigured: StateFlow<Boolean> = _isAccessConfigured.asStateFlow()

    private val _registeredEmail = MutableStateFlow(prefsHelper.registeredEmail)
    val registeredEmail: StateFlow<String> = _registeredEmail.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _selectedEgresso = MutableStateFlow<EgressoEntity?>(null)
    val selectedEgresso: StateFlow<EgressoEntity?> = _selectedEgresso.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(prefsHelper.registeredEmail)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    fun updateSchoolName(newName: String) {
        val trimmed = newName.trim().ifEmpty { "GESTÃO DE PRONTUÁRIOS" }
        prefsHelper.schoolName = trimmed
        _schoolName.value = trimmed
        _userFeedbackMessage.value = "Nome da instituição atualizado para: $trimmed"
        pushConfigToCloud()
    }

    fun configureFirstAccess(
        email: String,
        password: String,
        firstOperator: String,
        school: String
    ): Boolean {
        val cleanEmail = email.trim().lowercase()
        val cleanPassword = password.trim()
        val cleanOp = firstOperator.trim().ifEmpty { "Operador 01" }
        val cleanSchool = school.trim().ifEmpty { "GESTÃO DE PRONTUÁRIOS" }

        if (cleanEmail.isBlank() || cleanPassword.isBlank()) return false

        prefsHelper.registeredEmail = cleanEmail
        prefsHelper.registeredPassword = cleanPassword
        prefsHelper.activeOperatorName = cleanOp
        prefsHelper.operatorsList = listOf(cleanOp)
        prefsHelper.schoolName = cleanSchool
        prefsHelper.isAccessConfigured = true

        _registeredEmail.value = cleanEmail
        _isAccessConfigured.value = true
        _activeOperator.value = cleanOp
        _operatorsList.value = listOf(cleanOp)
        _schoolName.value = cleanSchool
        _currentUserEmail.value = cleanEmail
        _isAuthenticated.value = true
        _userFeedbackMessage.value = "Sistema configurado com sucesso! Bem-vindo(a), $cleanOp."

        pushConfigToCloud()
        syncWithCloud(silent = false)
        return true
    }

    fun loginWithPassword(password: String, selectedOperator: String? = null): Boolean {
        val savedPass = prefsHelper.registeredPassword
        // Se ainda não configurado (ou senha vazia), não permite login sem configuração
        if (!prefsHelper.isAccessConfigured && savedPass.isBlank()) {
            return false
        }

        if (password == savedPass) {
            if (!selectedOperator.isNullOrBlank()) {
                selectActiveOperator(selectedOperator)
            }
            _currentUserEmail.value = prefsHelper.registeredEmail
            _isAuthenticated.value = true
            _userFeedbackMessage.value = "Acesso autorizado! Operador ativo: ${_activeOperator.value}"
            syncWithCloud(silent = false)
            return true
        }
        return false
    }

    fun selectActiveOperator(operator: String) {
        val trimmed = operator.trim()
        if (trimmed.isNotBlank() && _operatorsList.value.contains(trimmed)) {
            prefsHelper.activeOperatorName = trimmed
            _activeOperator.value = trimmed
            _userFeedbackMessage.value = "Operador ativo alterado para: $trimmed"
        }
    }

    fun addOperator(newOperator: String): Boolean {
        val trimmed = newOperator.trim()
        if (trimmed.isBlank()) return false
        val current = _operatorsList.value.toMutableList()
        if (current.size >= PreferencesHelper.MAX_OPERATORS) {
            _userFeedbackMessage.value = "Limite máximo de ${PreferencesHelper.MAX_OPERATORS} operadores atingido."
            return false
        }
        if (current.any { it.equals(trimmed, ignoreCase = true) }) {
            _userFeedbackMessage.value = "Operador \"$trimmed\" já está cadastrado."
            return false
        }
        current.add(trimmed)
        prefsHelper.operatorsList = current
        _operatorsList.value = current
        _userFeedbackMessage.value = "Operador \"$trimmed\" cadastrado com sucesso!"
        pushConfigToCloud()
        return true
    }

    fun editOperator(oldName: String, newName: String): Boolean {
        val trimmedNew = newName.trim()
        if (trimmedNew.isBlank()) return false
        val current = _operatorsList.value.toMutableList()
        val index = current.indexOfFirst { it.equals(oldName, ignoreCase = true) }
        if (index == -1) return false
        current[index] = trimmedNew
        prefsHelper.operatorsList = current
        _operatorsList.value = current
        if (_activeOperator.value.equals(oldName, ignoreCase = true)) {
            prefsHelper.activeOperatorName = trimmedNew
            _activeOperator.value = trimmedNew
        }
        _userFeedbackMessage.value = "Nome do operador atualizado para \"$trimmedNew\""
        pushConfigToCloud()
        return true
    }

    fun removeOperator(operatorToRemove: String) {
        val current = _operatorsList.value.toMutableList()
        if (current.size <= 1) {
            _userFeedbackMessage.value = "Não é possível remover: o sistema deve ter pelo menos 1 operador."
            return
        }
        current.removeAll { it.equals(operatorToRemove, ignoreCase = true) }
        prefsHelper.operatorsList = current
        _operatorsList.value = current
        if (_activeOperator.value.equals(operatorToRemove, ignoreCase = true)) {
            val fallback = current.first()
            prefsHelper.activeOperatorName = fallback
            _activeOperator.value = fallback
        }
        _userFeedbackMessage.value = "Operador \"$operatorToRemove\" removido."
        pushConfigToCloud()
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

    fun syncWithCloud(silent: Boolean = false) {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = repository.syncWithCloud(_currentUserEmail.value)
            _isSyncing.value = false
            if (!silent) {
                _userFeedbackMessage.value = result.message
            }
        }
    }

    fun importFromSpreadsheetUri(uri: Uri) {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = ExcelCsvParser.parseFromUri(getApplication(), uri)
            if (result.errorMessage != null) {
                _userFeedbackMessage.value = result.errorMessage
            } else if (result.egressos.isEmpty()) {
                _userFeedbackMessage.value = "Nenhum estudante válido foi encontrado no arquivo selecionado."
            } else {
                repository.insertEgressos(result.egressos, _currentUserEmail.value)
                _userFeedbackMessage.value = "Importados ${result.importedCount} egressos e sincronizados com a nuvem!"
            }
            _isSyncing.value = false
        }
    }

    fun saveEgresso(egresso: EgressoEntity) {
        viewModelScope.launch {
            val entityToSave = if (egresso.cadastradoPor.isBlank()) {
                egresso.copy(cadastradoPor = _activeOperator.value.ifEmpty { _currentUserEmail.value ?: "Operador" })
            } else {
                egresso
            }
            if (entityToSave.id == 0L) {
                repository.insertEgresso(entityToSave, _currentUserEmail.value)
                _userFeedbackMessage.value = "Estudante cadastrado e salvo na nuvem!"
            } else {
                repository.updateEgresso(entityToSave, _currentUserEmail.value)
                _userFeedbackMessage.value = "Dados atualizados e sincronizados na nuvem!"
            }
            if (_selectedEgresso.value?.id == entityToSave.id) {
                _selectedEgresso.value = entityToSave
            }
        }
    }

    fun deleteEgresso(egresso: EgressoEntity) {
        viewModelScope.launch {
            repository.deleteEgresso(egresso)
            _userFeedbackMessage.value = "Registro removido localmente e da nuvem."
            if (_selectedEgresso.value?.id == egresso.id) {
                _selectedEgresso.value = null
            }
        }
    }

    fun deduplicateAndMergeAll() {
        viewModelScope.launch {
            _isSyncing.value = true
            val result = repository.deduplicateAndMergeAll(_currentUserEmail.value)
            _isSyncing.value = false
            if (result.duplicatesRemovedCount > 0) {
                _userFeedbackMessage.value = "Sucesso: ${result.duplicatesRemovedCount} duplicações foram eliminadas unindo as informações em ${result.groupsMergedCount} cadastros consolidados!"
            } else {
                _userFeedbackMessage.value = "Nenhuma duplicação encontrada. Todos os ${result.initialTotal} registros já são únicos e estão organizados."
            }
        }
    }

    fun setSelectionMode(enabled: Boolean) {
        _isSelectionMode.value = enabled
        if (!enabled) {
            _selectedIds.value = emptySet()
        }
    }

    fun toggleSelection(id: Long) {
        val current = _selectedIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
            if (current.isEmpty()) {
                _isSelectionMode.value = false
            }
        } else {
            current.add(id)
            _isSelectionMode.value = true
        }
        _selectedIds.value = current
    }

    fun selectAll(list: List<EgressoEntity>) {
        _isSelectionMode.value = true
        _selectedIds.value = list.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
        _isSelectionMode.value = false
    }

    fun updateBatchStatus(newStatus: String) {
        if (newStatus != "Ativo" && newStatus != "Arquivado Completo") {
            _userFeedbackMessage.value = "Opção inválida. Apenas 'Ativo' ou 'Arquivado Completo' são permitidos."
            return
        }
        val idsToUpdate = _selectedIds.value.toList()
        if (idsToUpdate.isEmpty()) {
            _userFeedbackMessage.value = "Nenhum prontuário selecionado para alteração."
            return
        }

        viewModelScope.launch {
            _isSyncing.value = true
            val operator = _activeOperator.value.ifEmpty { _currentUserEmail.value ?: "Operador" }
            val count = repository.updateBatchStatus(idsToUpdate, newStatus, operator)
            clearSelection()
            _isSyncing.value = false
            _userFeedbackMessage.value = "$count prontuário(s) alterado(s) para \"$newStatus\" com sucesso!"
        }
    }
}
