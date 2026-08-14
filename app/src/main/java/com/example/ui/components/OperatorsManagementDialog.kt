package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OperatorsManagementDialog(
    operatorsList: List<String>,
    activeOperator: String,
    onSelectActiveOperator: (String) -> Unit,
    onAddOperator: (String) -> Boolean,
    onEditOperator: (oldName: String, newName: String) -> Boolean,
    onRemoveOperator: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newOperatorName by remember { mutableStateOf("") }
    var operatorToEdit by remember { mutableStateOf<String?>(null) }
    var editOperatorInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val maxOperators = 3
    val canAddMore = operatorsList.size < maxOperators

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .height(22.dp)
                                .width(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Cadastro de Operadores",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Limite: máximo 3 operadores",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = if (operatorsList.size >= maxOperators) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${operatorsList.size}/$maxOperators",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (operatorsList.size >= maxOperators) Color(0xFFB45309) else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Selecione o operador ativo que assinará os cadastros e alterações no sistema:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                errorMessage?.let { error ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Lista de Operadores Cadastrados
                operatorsList.forEach { operator ->
                    val isActive = operator.equals(activeOperator, ignoreCase = true)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onSelectActiveOperator(operator)
                                errorMessage = null
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = if (isActive) "Operador Ativo" else "Selecionar Operador",
                                    tint = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray,
                                    modifier = Modifier
                                        .height(20.dp)
                                        .width(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = operator,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isActive) {
                                        Text(
                                            text = "• Operador Ativo no Momento",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        operatorToEdit = operator
                                        editOperatorInput = operator
                                    },
                                    modifier = Modifier
                                        .height(36.dp)
                                        .width(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar Nome do Operador",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.height(18.dp)
                                    )
                                }

                                if (operatorsList.size > 1) {
                                    IconButton(
                                        onClick = {
                                            onRemoveOperator(operator)
                                            errorMessage = null
                                        },
                                        modifier = Modifier
                                            .height(36.dp)
                                            .width(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Remover Operador",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.height(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Adicionar novo operador (se tiver menos de 3)
                if (canAddMore) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Cadastrar Novo Operador (${operatorsList.size + 1} de $maxOperators)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newOperatorName,
                                    onValueChange = {
                                        newOperatorName = it
                                        errorMessage = null
                                    },
                                    placeholder = { Text("Nome do novo operador") },
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Person, contentDescription = null)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("new_operator_name_input")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val trimmed = newOperatorName.trim()
                                        if (trimmed.isBlank()) {
                                            errorMessage = "Por favor, digite o nome do operador."
                                            return@Button
                                        }
                                        if (operatorsList.any { it.equals(trimmed, ignoreCase = true) }) {
                                            errorMessage = "Já existe um operador com este nome."
                                            return@Button
                                        }
                                        val added = onAddOperator(trimmed)
                                        if (added) {
                                            newOperatorName = ""
                                            errorMessage = null
                                        } else {
                                            errorMessage = "Limite máximo de $maxOperators operadores atingido."
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.height(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Limite de $maxOperators operadores atingido. Edite ou remova um operador para cadastrar outro.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Concluir")
            }
        }
    )

    // Modal de Edição de Nome do Operador
    operatorToEdit?.let { oldOp ->
        AlertDialog(
            onDismissRequest = { operatorToEdit = null },
            title = {
                Text(
                    text = "Editar Nome do Operador",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Informe o novo nome para $oldOp:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editOperatorInput,
                        onValueChange = { editOperatorInput = it },
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = editOperatorInput.trim()
                        if (trimmed.isNotBlank()) {
                            onEditOperator(oldOp, trimmed)
                            operatorToEdit = null
                        }
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { operatorToEdit = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
