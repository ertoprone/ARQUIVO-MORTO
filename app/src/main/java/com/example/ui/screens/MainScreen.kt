package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material.icons.filled.ExitToApp
import com.example.ui.components.LoginScreen
import com.example.ui.components.AddEditEgressoDialog
import com.example.ui.components.EgressoCard
import com.example.ui.components.EgressoDetailSheet
import com.example.ui.components.FilterDialog
import com.example.ui.components.ImportExportDialog
import com.example.ui.components.StatsDialog
import com.example.ui.viewmodel.MainViewModel
import com.example.util.ExportHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
    val currentUserEmail by viewModel.currentUserEmail.collectAsStateWithLifecycle()

    if (!isAuthenticated) {
        LoginScreen(
            onLoginSuccess = { email -> viewModel.loginUser(email) }
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

    var showFilterDialog by remember { mutableStateOf(false) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var egressoToEdit by remember { mutableStateOf<EgressoEntity?>(null) }
    var showImportExportDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userFeedbackMessage) {
        userFeedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedbackMessage()
        }
    }

    val activeFilterCount = (if (filterState.curso.isNotEmpty()) 1 else 0) +
            (if (filterState.status.isNotEmpty()) 1 else 0) +
            (if (filterState.caixa.isNotEmpty()) 1 else 0)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.height(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Arquivo Morto",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "Operador: ${currentUserEmail ?: "Admin"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = { showStatsDialog = true }) {
                        Icon(imageVector = Icons.Default.Analytics, contentDescription = "Estatísticas")
                    }
                    IconButton(onClick = { showImportExportDialog = true }) {
                        Icon(imageVector = Icons.Default.TableChart, contentDescription = "Gerenciar Planilhas & Drive")
                    }
                    IconButton(onClick = { viewModel.logoutUser() }) {
                        Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Bloquear / Sair")
                    }
                }
            )
        },
        floatingActionButton = {
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
                            placeholder = { Text("Nome, Código, CPF ou Caixa...", color = Color.Gray) },
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
                    text = "Encontrados: ${egressos.size} registro(s)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (egressos.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            ExportHelper.shareCsvFile(context, egressos)
                        }
                    ) {
                        Text("Exportar CSV (${egressos.size})", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Student Records List
            if (egressos.isEmpty()) {
                EmptyStateView(
                    isFilterActive = activeFilterCount > 0 || filterState.query.isNotEmpty(),
                    onClearFilters = { viewModel.clearFilters(); viewModel.updateSearchQuery("") },
                    onLoadSample = { viewModel.loadSampleDataset() }
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
                            onClick = { viewModel.selectEgresso(egresso) }
                        )
                    }
                }
            }
        }
    }

    // Detail Bottom Sheet
    selectedEgresso?.let { egresso ->
        EgressoDetailSheet(
            egresso = egresso,
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
            onLoadSampleData = {
                viewModel.loadSampleDataset()
            },
            onExportCsv = {
                ExportHelper.shareCsvFile(context, egressos)
            },
            onClearAllData = {
                viewModel.clearAllData()
            },
            onOpenDrive = {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://drive.google.com"))
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Não foi possível abrir o Google Drive.", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showImportExportDialog = false }
        )
    }

    // Stats Dialog
    if (showStatsDialog) {
        StatsDialog(
            allEgressos = egressos,
            onDismiss = { showStatsDialog = false }
        )
    }
}

@Composable
private fun EmptyStateView(
    isFilterActive: Boolean,
    onClearFilters: () -> Unit,
    onLoadSample: () -> Unit
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
            text = if (isFilterActive) "Nenhum estudante encontrado" else "Arquivo Morto sem Registros",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isFilterActive) "Tente ajustar ou limpar seus termos de busca e filtros avançados."
            else "Importe uma planilha do Excel/CSV ou carregue a planilha de teste pré-configurada.",
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
            Button(
                onClick = onLoadSample,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(imageVector = Icons.Default.TableChart, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Carregar Planilha Exemplo")
            }
        }
    }
}
