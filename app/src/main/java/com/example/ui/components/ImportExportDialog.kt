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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ImportExportDialog(
    onImportFileSelected: (Uri) -> Unit,
    onLoadSampleData: () -> Unit,
    onExportCsv: () -> Unit,
    onClearAllData: () -> Unit,
    onOpenDrive: () -> Unit = {},
    onDismiss: () -> Unit
) {
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
                text = "Gerenciar Planilhas & Dados",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Option 1: Import file
                OptionCard(
                    icon = Icons.Default.FileUpload,
                    title = "Importar Planilha do Aparelho",
                    description = "Carregue arquivo .csv, .tsv ou Excel exportado",
                    onClick = {
                        filePickerLauncher.launch("*/*")
                    }
                )

                // Option 2: Pre-load sample
                OptionCard(
                    icon = Icons.Default.TableChart,
                    title = "Carregar Planilha de Exemplo",
                    description = "Preenche o banco de dados com 15 registros para teste imediato",
                    onClick = {
                        onLoadSampleData()
                        onDismiss()
                    }
                )

                // Option 3: Export CSV
                OptionCard(
                    icon = Icons.Default.FileDownload,
                    title = "Exportar Resultados (CSV)",
                    description = "Gera planilha compatível com Microsoft Excel e Google Sheets",
                    onClick = {
                        onExportCsv()
                        onDismiss()
                    }
                )

                // Option 3.5: Google Drive
                OptionCard(
                    icon = Icons.Default.Folder,
                    title = "Espaço no Google Drive",
                    description = "Acessar pasta em nuvem para arquivos digitalizados e backups",
                    onClick = {
                        onOpenDrive()
                        onDismiss()
                    }
                )

                // Option 4: Clear DB
                OptionCard(
                    icon = Icons.Default.DeleteSweep,
                    title = "Limpar Toda a Base",
                    description = "Remove todos os registros cadastrados do aplicativo",
                    isDestructive = true,
                    onClick = {
                        onClearAllData()
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
