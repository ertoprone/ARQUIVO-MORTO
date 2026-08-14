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
    schoolName: String = "GESTÃO DE PRONTUÁRIOS",
    currentOperatorName: String = "Rúbia Elise",
    onSave: (EgressoEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var nome by remember { mutableStateOf(initialEgresso?.nome ?: "") }
    var codigo by remember { mutableStateOf(initialEgresso?.codigo ?: "") }
    var cpf by remember { mutableStateOf(initialEgresso?.cpf ?: "") }
    var rg by remember { mutableStateOf(initialEgresso?.rg ?: "") }
    var genero by remember { mutableStateOf(initialEgresso?.genero ?: "") }
    var curso by remember { mutableStateOf(initialEgresso?.curso ?: "Ensino Médio") }
    var anoConclusao by remember { mutableStateOf(if (initialEgresso != null && initialEgresso.anoConclusao > 0) initialEgresso.anoConclusao.toString() else "") }
    var turma by remember { mutableStateOf(initialEgresso?.turma ?: "") }
    var statusDocumento by remember { mutableStateOf(initialEgresso?.statusDocumento ?: "Arquivado Completo") }
    var formatoEnvioDigital by remember { mutableStateOf(initialEgresso?.formatoEnvioDigital ?: "E-mail") }
    var dataEnvioDigital by remember { mutableStateOf(initialEgresso?.dataEnvioDigital ?: "") }
    var caixaArquivo by remember { mutableStateOf(initialEgresso?.caixaArquivo ?: "") }
    var prateleiraCorredor by remember { mutableStateOf(initialEgresso?.prateleiraCorredor ?: "") }
    var pastaProtocolo by remember { mutableStateOf(initialEgresso?.pastaProtocolo ?: "") }
    var observacoes by remember { mutableStateOf(initialEgresso?.observacoes ?: "") }
    var cadastradoPor by remember { mutableStateOf(initialEgresso?.cadastradoPor?.ifEmpty { currentOperatorName } ?: currentOperatorName) }

    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_edit_egresso_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (initialEgresso == null) "Novo Registro - $schoolName" else "Editar Dados do Egresso",
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
                    label = { Text("Nome Completo do Aluno *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it },
                        label = { Text("SGDE (Código)") },
                        placeholder = { Text("Auto (Caixa+Pasta)") },
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
                        value = rg,
                        onValueChange = { rg = it },
                        label = { Text("RG / Documento") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = anoConclusao,
                        onValueChange = { anoConclusao = it },
                        label = { Text("Ano Conclusão") },
                        placeholder = { Text("Ex: 2022") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = curso,
                        onValueChange = { curso = it },
                        label = { Text("Curso / Modalidade") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f)
                    )

                    OutlinedTextField(
                        value = turma,
                        onValueChange = { turma = it },
                        label = { Text("Turma / Turno") },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Location fields
                OutlinedTextField(
                    value = caixaArquivo,
                    onValueChange = { caixaArquivo = it },
                    label = { Text("Caixa de Arquivo Morto *") },
                    placeholder = { Text("Ex: Caixa 05 - Estante A") },
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
                        placeholder = { Text("Ex: 14") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status Dropdown
                var expandedStatus by remember { mutableStateOf(false) }
                val statusOptions = listOf(
                    "Arquivado Completo",
                    "Ativo",
                    "Pendente Certificado",
                    "Apenas Histórico",
                    "Retirado - Físico",
                    "2ª via digital"
                )

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

                // If 2ª via digital is chosen, show format (email / whatsapp) and withdrawal date
                if (statusDocumento == "2ª via digital") {
                    Spacer(modifier = Modifier.height(8.dp))

                    var expandedFormato by remember { mutableStateOf(false) }
                    val formatoOptions = listOf("E-mail", "WhatsApp", "E-mail e WhatsApp", "Outro")

                    ExposedDropdownMenuBox(
                        expanded = expandedFormato,
                        onExpandedChange = { expandedFormato = !expandedFormato }
                    ) {
                        OutlinedTextField(
                            value = formatoEnvioDigital,
                            onValueChange = { formatoEnvioDigital = it },
                            label = { Text("Formato de Envio Digital") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFormato) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedFormato,
                            onDismissRequest = { expandedFormato = false }
                        ) {
                            formatoOptions.forEach { fmt ->
                                DropdownMenuItem(
                                    text = { Text(fmt) },
                                    onClick = {
                                        formatoEnvioDigital = fmt
                                        expandedFormato = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = dataEnvioDigital,
                        onValueChange = { dataEnvioDigital = it },
                        label = { Text("Data de Retirada / Envio Digital") },
                        placeholder = { Text("Ex: 14/08/2026") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = cadastradoPor,
                    onValueChange = { cadastradoPor = it },
                    label = { Text("Operador / Usuário Responsável") },
                    placeholder = { Text("Ex: Rúbia Elise") },
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
                    val anoInt = anoConclusao.filter { it.isDigit() }.toIntOrNull() ?: 0

                    // Generate SGDE from Caixa + Pasta if left empty
                    val finalSgde = if (codigo.isNotBlank()) {
                        codigo.trim()
                    } else if (caixaArquivo.isNotBlank() || pastaProtocolo.isNotBlank()) {
                        val cxClean = caixaArquivo.filter { it.isLetterOrDigit() }.takeLast(4)
                        val ptClean = pastaProtocolo.filter { it.isLetterOrDigit() }.takeLast(4)
                        "SGDE-${cxClean.ifEmpty { "00" }}-${ptClean.ifEmpty { "00" }}".uppercase()
                    } else {
                        "SGDE-${(10000..99999).random()}"
                    }

                    val egressoToSave = EgressoEntity(
                        id = initialEgresso?.id ?: 0L,
                        nome = nome.trim(),
                        codigo = finalSgde,
                        cpf = cpf.trim(),
                        rg = rg.trim(),
                        curso = curso.trim().ifEmpty { "Ensino Médio" },
                        anoConclusao = anoInt,
                        turma = turma.trim(),
                        statusDocumento = statusDocumento,
                        formatoEnvioDigital = if (statusDocumento == "2ª via digital") formatoEnvioDigital.trim() else "",
                        dataEnvioDigital = if (statusDocumento == "2ª via digital") dataEnvioDigital.trim() else "",
                        caixaArquivo = caixaArquivo.trim(),
                        prateleiraCorredor = prateleiraCorredor.trim(),
                        pastaProtocolo = pastaProtocolo.trim(),
                        observacoes = observacoes.trim(),
                        cadastradoPor = cadastradoPor.trim().ifEmpty { currentOperatorName }
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
