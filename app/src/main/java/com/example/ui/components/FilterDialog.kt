package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.FilterState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterDialog(
    filterState: FilterState,
    distinctCursos: List<String>,
    distinctCaixas: List<String>,
    distinctStatus: List<String>,
    onFilterStateChange: (FilterState) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("filter_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Filtros Avançados",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Section 1: Order / Sort
                Text(
                    text = "ORDENAR POR",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                val sortOptions = listOf(
                    "nome_asc" to "Nome (A -> Z)",
                    "nome_desc" to "Nome (Z -> A)",
                    "ano_desc" to "Ano de Conclusão (Mais Recente)",
                    "ano_asc" to "Ano de Conclusão (Mais Antigo)",
                    "codigo" to "Código SGDE"
                )

                sortOptions.forEach { (key, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onFilterStateChange(filterState.copy(sortBy = key))
                            }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = filterState.sortBy == key,
                            onClick = { onFilterStateChange(filterState.copy(sortBy = key)) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = label, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Filter by Curso
                if (distinctCursos.isNotEmpty()) {
                    Text(
                        text = "CURSO",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = filterState.curso.isEmpty(),
                            onClick = { onFilterStateChange(filterState.copy(curso = "")) },
                            label = { Text("Todos") }
                        )
                        distinctCursos.forEach { curso ->
                            FilterChip(
                                selected = filterState.curso == curso,
                                onClick = {
                                    val newCurso = if (filterState.curso == curso) "" else curso
                                    onFilterStateChange(filterState.copy(curso = newCurso))
                                },
                                label = { Text(curso) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Section 3: Filter by Status do Documento
                if (distinctStatus.isNotEmpty()) {
                    Text(
                        text = "SITUAÇÃO DO DOCUMENTO",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = filterState.status.isEmpty(),
                            onClick = { onFilterStateChange(filterState.copy(status = "")) },
                            label = { Text("Todas") }
                        )
                        distinctStatus.forEach { st ->
                            FilterChip(
                                selected = filterState.status == st,
                                onClick = {
                                    val newStatus = if (filterState.status == st) "" else st
                                    onFilterStateChange(filterState.copy(status = newStatus))
                                },
                                label = { Text(st) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Section 4: Filter by Caixa de Arquivo
                if (distinctCaixas.isNotEmpty()) {
                    Text(
                        text = "CAIXA DE ARQUIVO MORTO",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = filterState.caixa.isEmpty(),
                            onClick = { onFilterStateChange(filterState.copy(caixa = "")) },
                            label = { Text("Todas Caixas") }
                        )
                        distinctCaixas.forEach { cx ->
                            FilterChip(
                                selected = filterState.caixa == cx,
                                onClick = {
                                    val newCaixa = if (filterState.caixa == cx) "" else cx
                                    onFilterStateChange(filterState.copy(caixa = newCaixa))
                                },
                                label = { Text(cx) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Aplicar Filtros")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onClearFilters()
                }
            ) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Limpar Filtros")
            }
        }
    )
}
