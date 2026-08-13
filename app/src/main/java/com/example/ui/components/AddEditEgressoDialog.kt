package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.EgressoEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEgressoDialog(
    initialEgresso: EgressoEntity? = null,
    onSave: (EgressoEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var nome by remember { mutableStateOf(initialEgresso?.nome ?: "") }
    var codigo by remember { mutableStateOf(initialEgresso?.codigo ?: "") }
    var cpf by remember { mutableStateOf(initialEgresso?.cpf ?: "") }
    var rg by remember { mutableStateOf(initialEgresso?.rg ?: "") }
    var curso by remember { mutableStateOf(initialEgresso?.curso ?: "Engenharia Civil") }
    var anoConclusao by remember { mutableStateOf(initialEgresso?.anoConclusao?.toString() ?: "2023") }
    var turma by remember { mutableStateOf(initialEgresso?.turma ?: "") }
    var statusDocumento by remember { mutableStateOf(initialEgresso?.statusDocumento ?: "Arquivado Completo") }
    var caixaArquivo by remember { mutableStateOf(initialEgresso?.caixaArquivo ?: "Caixa 01 - Setor A") }
    var prateleiraCorredor by remember { mutableStateOf(initialEgresso?.prateleiraCorredor ?: "Estante 01") }
    var pastaProtocolo by remember { mutableStateOf(initialEgresso?.pastaProtocolo ?: "") }
    var driveUrl by remember { mutableStateOf(initialEgresso?.driveUrl ?: "") }
    var observacoes by remember { mutableStateOf(initialEgresso?.observacoes ?: "") }

    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_edit_egresso_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (initialEgresso == null) "Cadastrar no Arquivo Morto" else "Editar Dados do Egresso",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (isError) {
                    Text(
                        text = "Por favor, preencha o Nome e a Caixa de Arquivo.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome Completo *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it },
                        label = { Text("Código / Matrícula") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = cpf,
                        onValueChange = { cpf = it },
                        label = { Text("CPF") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = curso,
                        onValueChange = { curso = it },
                        label = { Text("Curso") },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )

                    OutlinedTextField(
                        value = anoConclusao,
                        onValueChange = { anoConclusao = it },
                        label = { Text("Ano") },
                        singleLine = true,
                        modifier = Modifier.weight(0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = caixaArquivo,
                    onValueChange = { caixaArquivo = it },
                    label = { Text("Caixa de Arquivo Morto *") },
                    placeholder = { Text("e.g. Caixa 04 - Bloco B") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = prateleiraCorredor,
                        onValueChange = { prateleiraCorredor = it },
                        label = { Text("Prateleira/Estante") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = pastaProtocolo,
                        onValueChange = { pastaProtocolo = it },
                        label = { Text("Pasta/Protocolo") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status Dropdown
                var expandedStatus by remember { mutableStateOf(false) }
                val statusOptions = listOf("Arquivado Completo", "Pendente Certificado", "Apenas Histórico", "Retirado")

                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = !expandedStatus }
                ) {
                    OutlinedTextField(
                        value = statusDocumento,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Situação do Documento") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        statusOptions.forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st) },
                                onClick = {
                                    statusDocumento = st
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = driveUrl,
                    onValueChange = { driveUrl = it },
                    label = { Text("Espaço / Link no Google Drive") },
                    placeholder = { Text("https://drive.google.com/drive/folders/...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = observacoes,
                    onValueChange = { observacoes = it },
                    label = { Text("Observações") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nome.isBlank() || caixaArquivo.isBlank()) {
                        isError = true
                        return@Button
                    }
                    val anoInt = anoConclusao.toIntOrNull() ?: 2023
                    val egressoToSave = EgressoEntity(
                        id = initialEgresso?.id ?: 0L,
                        nome = nome.trim(),
                        codigo = codigo.trim().ifEmpty { "EGR-${System.currentTimeMillis() % 10000}" },
                        cpf = cpf.trim(),
                        rg = rg.trim(),
                        curso = curso.trim().ifEmpty { "Geral" },
                        anoConclusao = anoInt,
                        turma = turma.trim(),
                        statusDocumento = statusDocumento,
                        caixaArquivo = caixaArquivo.trim(),
                        prateleiraCorredor = prateleiraCorredor.trim(),
                        pastaProtocolo = pastaProtocolo.trim(),
                        driveUrl = driveUrl.trim(),
                        observacoes = observacoes.trim()
                    )
                    onSave(egressoToSave)
                }
            ) {
                Text("Salvar Registro")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
