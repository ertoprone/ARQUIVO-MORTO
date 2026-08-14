package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SendToMobile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EgressoEntity

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EgressoCard(
    egresso: EgressoEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {}
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("egresso_card_${egresso.id}")
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onToggleSelect()
                    } else {
                        onClick()
                    }
                },
                onLongClick = {
                    onToggleSelect()
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Checkbox (if in selection mode) + Student Name & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }

                Text(
                    text = egresso.nome,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                StatusChip(status = egresso.statusDocumento)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle Row: Course & Concluding Year
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.height(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val courseYearText = buildString {
                    append(egresso.curso.ifEmpty { "Geral" })
                    if (egresso.anoConclusao > 0) {
                        append(" • ${egresso.anoConclusao}")
                    }
                    if (egresso.turma.isNotBlank()) {
                        append(" (${egresso.turma})")
                    }
                }
                Text(
                    text = courseYearText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SGDE & CPF Row with Copy Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.height(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val idText = buildString {
                        append("SGDE: ${egresso.codigo}")
                        if (egresso.cpf.isNotBlank()) {
                            append(" | CPF: ${egresso.cpf}")
                        }
                    }
                    Text(
                        text = idText,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("SGDE/Dados", "Nome: ${egresso.nome}\nSGDE: ${egresso.codigo}\nCPF: ${egresso.cpf}\nCaixa: ${egresso.caixaArquivo}\nPasta: ${egresso.pastaProtocolo}")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Dados copiados para a área de transferência", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copiar SGDE e Dados",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.height(16.dp)
                    )
                }
            }

            // If 2ª via digital, show withdrawal format and date
            if (egresso.statusDocumento == "2ª via digital" || egresso.formatoEnvioDigital.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEDE9FE), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SendToMobile,
                        contentDescription = null,
                        tint = Color(0xFF6D28D9),
                        modifier = Modifier.height(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "2ª Via: ${egresso.formatoEnvioDigital.ifEmpty { "Digital" }} ${if (egresso.dataEnvioDigital.isNotEmpty()) "• Em: ${egresso.dataEnvioDigital}" else ""}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
                        color = Color(0xFF5B21B6)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Physical Archive Location Highlight Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Archive,
                            contentDescription = "Caixa Arquivo Morto",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.height(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "LOCALIZAÇÃO NO ARQUIVO",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = egresso.caixaArquivo,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (egresso.pastaProtocolo.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FolderSpecial,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.height(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = egresso.pastaProtocolo,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (bgColor, textColor) = when (status) {
        "Arquivado Completo", "Completo" -> Color(0xFFD1FAE5) to Color(0xFF065F46)
        "Ativo" -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
        "2ª via digital" -> Color(0xFFF3E8FF) to Color(0xFF6B21A8)
        "Pendente Certificado", "Pendente" -> Color(0xFFFEF3C7) to Color(0xFF92400E)
        "Retirado - Físico", "Retirado" -> Color(0xFFFFE4E6) to Color(0xFF9F1239)
        "Apenas Histórico" -> Color(0xFFDBEAFE) to Color(0xFF1E40AF)
        else -> Color(0xFFE2E8F0) to Color(0xFF334155)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = textColor
        )
    }
}
