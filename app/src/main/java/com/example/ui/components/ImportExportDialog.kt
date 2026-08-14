package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ImportExportDialog(
    onImportFileSelected: (Uri) -> Unit,
    onExportCsv: () -> Unit,
    onSyncCloud: () -> Unit,
    onDeduplicateAndMerge: () -> Unit,
    onDismiss: () -> Unit
) {
    var showHelpInfo by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportFileSelected(uri)
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("import_export_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Gerenciar Planilhas & Nuvem",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Option 0: Cloud Sync
                OptionCard(
                    icon = Icons.Default.CloudSync,
                    title = "Sincronizar com a Nuvem",
                    description = "Baixa e envia todos os cadastros para acessar em outro dispositivo",
                    onClick = {
                        onSyncCloud()
                        onDismiss()
                    }
                )

                // Option 1: Import file
                OptionCard(
                    icon = Icons.Default.FileUpload,
                    title = "Importar Planilha do Aparelho",
                    description = "Carregue planilha (.csv, .txt ou .tsv) com seus estudantes",
                    onClick = {
                        filePickerLauncher.launch("*/*")
                    }
                )

                // Option 2: Export CSV
                OptionCard(
                    icon = Icons.Default.FileDownload,
                    title = "Exportar Planilha (CSV)",
                    description = "Gera arquivo com acentuação UTF-8 pronto para o Excel",
                    onClick = {
                        onExportCsv()
                        onDismiss()
                    }
                )

                // Option 3: Explanation of fields
                OptionCard(
                    icon = Icons.Default.HelpOutline,
                    title = "Quais campos a planilha deve conter?",
                    description = "Clique para ver as colunas suportadas na importação",
                    onClick = {
                        showHelpInfo = !showHelpInfo
                    }
                )

                if (showHelpInfo) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "COLUNAS ACEITAS NA PLANILHA:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Nome / Estudante\n" +
                                       "• SGDE (ou Matrícula/Código - se ausente, será gerado automaticamente)\n" +
                                       "• CPF / RG\n" +
                                       "• Curso / Modalidade\n" +
                                       "• Ano de Conclusão / Saída\n" +
                                       "• Turma / Turno\n" +
                                       "• Caixa de Arquivo (ex: Caixa 01)\n" +
                                       "• Prateleira / Estante\n" +
                                       "• Pasta / Protocolo\n" +
                                       "• Situação do Documento (Arquivado Completo, Ativo, 2ª via digital, Retirado - Físico...)\n" +
                                       "• Formato de Envio (E-mail ou WhatsApp)\n" +
                                       "• Data de Retirada / Envio\n" +
                                       "• Observações",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Option 4: Deduplicate and Merge Info
                OptionCard(
                    icon = Icons.Default.AutoFixHigh,
                    title = "Unificar e Remover Duplicações",
                    description = "Identifica cadastros repetidos (SGDE, CPF ou Nome) e une todas as informações num único registro",
                    isDestructive = false,
                    onClick = {
                        onDeduplicateAndMerge()
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar")
            }
        }
    )
}

@Composable
private fun OptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = if (isDestructive) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

    val iconColor = if (isDestructive) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = iconColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
