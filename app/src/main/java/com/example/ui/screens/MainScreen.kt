package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.EgressoEntity
import com.example.ui.components.AddEditEgressoDialog
import com.example.ui.components.BatchStatusDialog
import com.example.ui.components.EgressoCard
import com.example.ui.components.EgressoDetailSheet
import com.example.ui.components.FilterDialog
import com.example.ui.components.ImportExportDialog
import com.example.ui.components.LoginScreen
import com.example.ui.components.OperatorsManagementDialog
import com.example.ui.components.SchoolConfigDialog
import com.example.ui.components.StatsDialog
import com.example.ui.components.SystemConfigBottomBar
import com.example.ui.viewmodel.MainViewModel
import com.example.util.ExportHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
    val isAccessConfigured by viewModel.isAccessConfigured.collectAsStateWithLifecycle()
    val registeredEmail by viewModel.registeredEmail.collectAsStateWithLifecycle()
    val currentUserEmail by viewModel.currentUserEmail.collectAsStateWithLifecycle()
    val schoolName by viewModel.schoolName.collectAsStateWithLifecycle()
    val operatorName by viewModel.activeOperator.collectAsStateWithLifecycle()
    val operatorsList by viewModel.operatorsList.collectAsStateWithLifecycle()

    if (!isAuthenticated) {
        LoginScreen(
            isAccessConfigured = isAccessConfigured,
            registeredEmail = registeredEmail,
            currentSchoolName = schoolName,
            activeOperator = operatorName,
            operatorsList = operatorsList,
            onSchoolNameChange = { viewModel.updateSchoolName(it) },
            onConfigureFirstAccess = { email, pass, op, school ->
                viewModel.configureFirstAccess(email, pass, op, school)
            },
            onLoginWithPassword = { pass, selectedOp ->
                viewModel.loginWithPassword(pass, selectedOp)
            }
        )
        return
    }

    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val egressos by viewModel.egressos.collectAsStateWithLifecycle()
    val selectedEgresso by viewModel.selectedEgresso.collectAsStateWithLifecycle()
    val userFeedbackMessage by viewModel.userFeedbackMessage.collectAsStateWithLifecycle()

    val distinctCursos by viewModel.distinctCursos.collectAsStateWithLifecycle()
    val distinctCaixas by viewModel.distinctCaixas.collectAsStateWithLifecycle()
    val distinctStatus by viewModel.distinctStatus.collectAsStateWithLifecycle()

    val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()

    var showFilterDialog by remember { mutableStateOf(false) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showBatchStatusDialog by remember { mutableStateOf(false) }
    var egressoToEdit by remember { mutableStateOf<EgressoEntity?>(null) }
    var showImportExportDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showSchoolConfigDialog by remember { mutableStateOf(false) }
    var showOperatorsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userFeedbackMessage) {
        userFeedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedbackMessage()
        }
    }

    val activeFilterCount = (if (filterState.curso.isNotEmpty()) 1 else 0) +
            (if (filterState.status.isNotEmpty()) 1 else 0) +
            (if (filterState.caixa.isNotEmpty()) 1 else 0)

    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = {
                        Text(
                            text = "${selectedIds.size} selecionado(s)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancelar Seleção",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionIconContentColor = MaterialTheme.colorScheme.primary
                    ),
                    actions = {
                        TextButton(
                            onClick = {
                                if (selectedIds.size == egressos.size && egressos.isNotEmpty()) {
                                    viewModel.clearSelection()
                                } else {
                                    viewModel.selectAll(egressos)
                                }
                            }
                        ) {
                            Text(
                                text = if (selectedIds.size == egressos.size && egressos.isNotEmpty()) "Desmarcar" else "Todos (${egressos.size})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = { showBatchStatusDialog = true },
                            enabled = selectedIds.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DriveFileRenameOutline,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Alterar Situação", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Column(
                            modifier = Modifier
                                .clickable { showSchoolConfigDialog = true }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.height(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = schoolName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar Nome da Escola",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                    modifier = Modifier.height(14.dp)
                                )
                            }

                            // Operador Clicável para Troca Rápida
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { showOperatorsDialog = true }
                                    .padding(vertical = 2.dp)
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.height(12.dp).width(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Op: $operatorName",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Icon(
                                            imageVector = Icons.Default.Group,
                                            contentDescription = "Trocar Operador",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.height(11.dp).width(11.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "• $currentUserEmail",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.primary
                    ),
                    actions = {
                        IconButton(onClick = { viewModel.setSelectionMode(true) }) {
                            Icon(imageVector = Icons.Default.Checklist, contentDescription = "Seleção em Lote")
                        }
                        IconButton(
                            onClick = { viewModel.syncWithCloud() },
                            enabled = !isSyncing
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .height(20.dp)
                                        .width(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(imageVector = Icons.Default.CloudSync, contentDescription = "Sincronizar Nuvem")
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            SystemConfigBottomBar(
                schoolName = schoolName,
                operatorName = operatorName,
                userEmail = currentUserEmail,
                isSyncing = isSyncing,
                onOpenSchoolConfig = { showSchoolConfigDialog = true },
                onOpenOperators = { showOperatorsDialog = true },
                onSyncCloud = { viewModel.syncWithCloud() },
                onOpenStats = { showStatsDialog = true },
                onOpenImportExport = { showImportExportDialog = true },
                onLogout = { viewModel.logoutUser() }
            )
        },
        floatingActionButton = {
            if (isSelectionMode) {
                if (selectedIds.isNotEmpty()) {
                    FloatingActionButton(
                        onClick = { showBatchStatusDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("batch_status_fab")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DriveFileRenameOutline, contentDescription = "Alterar Situação")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Alterar Situação (${selectedIds.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                FloatingActionButton(
                    onClick = {
                        egressoToEdit = null
                        showAddEditDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.testTag("add_egresso_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Cadastrar Egresso")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar & Filter Button Container
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = filterState.query,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Nome, SGDE, CPF ou Caixa...", color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                if (filterState.query.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpar busca")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_text_field")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Filter Button with Badge
                        IconButton(
                            onClick = { showFilterDialog = true },
                            modifier = Modifier
                                .background(
                                    color = if (activeFilterCount > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(4.dp)
                                .testTag("filter_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (activeFilterCount > 0) {
                                        Badge { Text(activeFilterCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filtros Avançados",
                                    tint = if (activeFilterCount > 0) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Active Filter Chips Horizontal Row
            if (activeFilterCount > 0 || filterState.query.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (filterState.query.isNotEmpty()) {
                        item {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.updateSearchQuery("") },
                                label = { Text("Busca: \"${filterState.query}\"") },
                                trailingIcon = { Icon(imageVector = Icons.Default.Clear, contentDescription = null) }
                            )
                        }
                    }
                    if (filterState.curso.isNotEmpty()) {
                        item {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.updateCursoFilter("") },
                                label = { Text("Curso: ${filterState.curso}") },
                                trailingIcon = { Icon(imageVector = Icons.Default.Clear, contentDescription = null) }
                            )
                        }
                    }
                    if (filterState.status.isNotEmpty()) {
                        item {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.updateStatusFilter("") },
                                label = { Text("Status: ${filterState.status}") },
                                trailingIcon = { Icon(imageVector = Icons.Default.Clear, contentDescription = null) }
                            )
                        }
                    }
                    if (filterState.caixa.isNotEmpty()) {
                        item {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.updateCaixaFilter("") },
                                label = { Text("Caixa: ${filterState.caixa}") },
                                trailingIcon = { Icon(imageVector = Icons.Default.Clear, contentDescription = null) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // Results Counter & Quick Export Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSelectionMode) "${selectedIds.size} de ${egressos.size} selecionado(s)" else "Encontrados: ${egressos.size} registro(s)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelectionMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (egressos.isNotEmpty()) {
                        if (!isSelectionMode) {
                            TextButton(
                                onClick = { viewModel.setSelectionMode(true) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Checklist,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lote", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        } else {
                            TextButton(
                                onClick = {
                                    if (selectedIds.size == egressos.size) {
                                        viewModel.clearSelection()
                                    } else {
                                        viewModel.selectAll(egressos)
                                    }
                                }
                            ) {
                                Text(
                                    text = if (selectedIds.size == egressos.size) "Desmarcar" else "Todos",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Button(
                            onClick = {
                                ExportHelper.shareGeneralReport(context, egressos, schoolName, operatorName)
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Emitir Relatório", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        TextButton(
                            onClick = {
                                ExportHelper.shareCsvFile(context, egressos, schoolName)
                            }
                        ) {
                            Text("CSV", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Student Records List
            if (egressos.isEmpty()) {
                EmptyStateView(
                    schoolName = schoolName,
                    isFilterActive = activeFilterCount > 0 || filterState.query.isNotEmpty(),
                    onClearFilters = { viewModel.clearFilters(); viewModel.updateSearchQuery("") },
                    onImportClick = { showImportExportDialog = true },
                    onAddClick = {
                        egressoToEdit = null
                        showAddEditDialog = true
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("egresso_list")
                ) {
                    items(
                        items = egressos,
                        key = { it.id }
                    ) { egresso ->
                        EgressoCard(
                            egresso = egresso,
                            onClick = { viewModel.selectEgresso(egresso) },
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedIds.contains(egresso.id),
                            onToggleSelect = { viewModel.toggleSelection(egresso.id) }
                        )
                    }
                }
            }
        }
    }

    // Batch Status Update Dialog (Allows ONLY "Ativo" and "Arquivado Completo")
    if (showBatchStatusDialog && selectedIds.isNotEmpty()) {
        BatchStatusDialog(
            selectedCount = selectedIds.size,
            onApplyStatus = { newStatus ->
                viewModel.updateBatchStatus(newStatus)
                showBatchStatusDialog = false
            },
            onDismiss = { showBatchStatusDialog = false }
        )
    }

    // Detail Bottom Sheet
    selectedEgresso?.let { egresso ->
        EgressoDetailSheet(
            egresso = egresso,
            schoolName = schoolName,
            operatorName = operatorName,
            onDismiss = { viewModel.selectEgresso(null) },
            onEdit = {
                egressoToEdit = it
                viewModel.selectEgresso(null)
                showAddEditDialog = true
            },
            onDelete = {
                viewModel.deleteEgresso(it)
            }
        )
    }

    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            filterState = filterState,
            distinctCursos = distinctCursos,
            distinctCaixas = distinctCaixas,
            distinctStatus = distinctStatus,
            onFilterStateChange = { newFilter ->
                if (newFilter.curso != filterState.curso) viewModel.updateCursoFilter(newFilter.curso)
                if (newFilter.status != filterState.status) viewModel.updateStatusFilter(newFilter.status)
                if (newFilter.caixa != filterState.caixa) viewModel.updateCaixaFilter(newFilter.caixa)
                if (newFilter.sortBy != filterState.sortBy) viewModel.updateSortBy(newFilter.sortBy)
            },
            onClearFilters = {
                viewModel.clearFilters()
            },
            onDismiss = { showFilterDialog = false }
        )
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        AddEditEgressoDialog(
            initialEgresso = egressoToEdit,
            schoolName = schoolName,
            currentOperatorName = operatorName,
            onSave = { egresso ->
                viewModel.saveEgresso(egresso)
                showAddEditDialog = false
            },
            onDismiss = { showAddEditDialog = false }
        )
    }

    // Import / Export Dialog
    if (showImportExportDialog) {
        ImportExportDialog(
            onImportFileSelected = { uri ->
                viewModel.importFromSpreadsheetUri(uri)
            },
            onExportGeneralReport = {
                ExportHelper.shareGeneralReport(context, egressos, schoolName, operatorName)
            },
            onExportCsv = {
                ExportHelper.shareCsvFile(context, egressos, schoolName)
            },
            onSyncCloud = {
                viewModel.syncWithCloud()
            },
            onDeduplicateAndMerge = {
                viewModel.deduplicateAndMergeAll()
            },
            onDismiss = { showImportExportDialog = false }
        )
    }

    // Stats Dialog
    if (showStatsDialog) {
        StatsDialog(
            allEgressos = egressos,
            schoolName = schoolName,
            operatorName = operatorName,
            onDismiss = { showStatsDialog = false }
        )
    }

    // School Config Dialog
    if (showSchoolConfigDialog) {
        SchoolConfigDialog(
            currentSchoolName = schoolName,
            currentOperatorName = operatorName,
            currentEmail = currentUserEmail ?: "secretariaeecv@gmail.com",
            onOpenOperatorsManager = {
                showSchoolConfigDialog = false
                showOperatorsDialog = true
            },
            onSaveConfig = { newSchool ->
                viewModel.updateSchoolName(newSchool)
                showSchoolConfigDialog = false
            },
            onDismiss = { showSchoolConfigDialog = false }
        )
    }

    // Operators Management Dialog (Máximo 3 operadores)
    if (showOperatorsDialog) {
        OperatorsManagementDialog(
            operatorsList = operatorsList,
            activeOperator = operatorName,
            onSelectActiveOperator = { op ->
                viewModel.selectActiveOperator(op)
            },
            onAddOperator = { newOp ->
                viewModel.addOperator(newOp)
            },
            onEditOperator = { oldOp, newOp ->
                viewModel.editOperator(oldOp, newOp)
            },
            onRemoveOperator = { opToRemove ->
                viewModel.removeOperator(opToRemove)
            },
            onDismiss = { showOperatorsDialog = false }
        )
    }
}

@Composable
private fun EmptyStateView(
    schoolName: String,
    isFilterActive: Boolean,
    onClearFilters: () -> Unit,
    onImportClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isFilterActive) Icons.Default.FolderOpen else Icons.Default.Archive,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            modifier = Modifier
                .height(72.dp)
                .width(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isFilterActive) "Nenhum estudante encontrado" else "Arquivo Morto Vazio",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isFilterActive) "Tente ajustar ou limpar seus termos de busca e filtros avançados."
            else "Importe sua planilha de egressos de $schoolName ou cadastre um registro manualmente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isFilterActive) {
            Button(
                onClick = onClearFilters,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Limpar Filtros")
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onImportClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.TableChart, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Importar Planilha")
                }

                OutlinedButton(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cadastrar")
                }
            }
        }
    }
}
