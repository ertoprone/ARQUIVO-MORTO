package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.EgressoEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    private fun getFormattedDate(): String {
        return SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault()).format(Date())
    }

    /**
     * Generates and shares a complete official text/document report of student records
     */
    fun shareGeneralReport(
        context: Context,
        egressos: List<EgressoEntity>,
        schoolName: String = "GESTÃO DE PRONTUÁRIOS",
        operatorName: String = "Secretaria"
    ) {
        if (egressos.isEmpty()) {
            Toast.makeText(context, "Nenhum prontuário encontrado para emitir relatório.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val totalCount = egressos.size
            val caixas = egressos.map { it.caixaArquivo.ifBlank { "Sem Caixa" } }.distinct()
            val totalCaixas = caixas.size
            val completas = egressos.count { it.statusDocumento.contains("Completo", ignoreCase = true) }
            val pendentes = egressos.count { it.statusDocumento.contains("Pendente", ignoreCase = true) }
            val retirados = egressos.count { it.statusDocumento.contains("Retirado", ignoreCase = true) }
            val ativos = egressos.count { it.statusDocumento.equals("Ativo", ignoreCase = true) }
            val digitais = egressos.count { it.statusDocumento.contains("digital", ignoreCase = true) }

            val sb = StringBuilder()
            sb.append("====================================================\n")
            sb.append("         ${schoolName.uppercase()}\n")
            sb.append("       RELATÓRIO GERAL DO ARQUIVO MORTO\n")
            sb.append("====================================================\n")
            sb.append("📅 Emitido em: ${getFormattedDate()}\n")
            sb.append("👤 Operador Responsável: $operatorName\n")
            sb.append("📊 Total de Prontuários Listados: $totalCount\n")
            sb.append("📦 Caixas de Arquivo Envolvidas: $totalCaixas\n")
            sb.append("----------------------------------------------------\n")
            sb.append("RESUMO POR SITUAÇÃO DOCUMENTAL:\n")
            sb.append(" • Arquivados Completos: $completas\n")
            sb.append(" • Ativos: $ativos\n")
            sb.append(" • Pendentes: $pendentes\n")
            sb.append(" • 2ª Via Digital: $digitais\n")
            sb.append(" • Retirados: $retirados\n")
            sb.append("====================================================\n")
            sb.append("RELAÇÃO DE ESTUDANTES E LOCALIZAÇÃO:\n")
            sb.append("====================================================\n\n")

            egressos.forEachIndexed { index, e ->
                sb.append("${index + 1}. ${e.nome.uppercase()}\n")
                sb.append("   • SGDE / Matrícula: ${e.codigo.ifEmpty { "N/I" }}\n")
                if (e.cpf.isNotEmpty()) sb.append("   • CPF: ${e.cpf}\n")
                if (e.rg.isNotEmpty()) sb.append("   • RG: ${e.rg}\n")
                sb.append("   • Curso: ${e.curso.ifEmpty { "Geral" }} ${if (e.anoConclusao > 0) "(${e.anoConclusao})" else ""}\n")
                sb.append("   • Caixa: ${e.caixaArquivo.ifEmpty { "Caixa 01" }} | Prateleira: ${e.prateleiraCorredor.ifEmpty { "N/I" }} | Pasta: ${e.pastaProtocolo.ifEmpty { "N/I" }}\n")
                sb.append("   • Situação: ${e.statusDocumento.ifEmpty { "Arquivado" }}\n")
                if (e.dataEnvioDigital.isNotEmpty() || e.formatoEnvioDigital.isNotEmpty()) {
                    sb.append("   • Retirada / Envio: ${e.dataEnvioDigital} via ${e.formatoEnvioDigital}\n")
                }
                if (e.observacoes.isNotEmpty()) {
                    sb.append("   • Obs: ${e.observacoes.replace("\n", " ")}\n")
                }
                sb.append("----------------------------------------------------\n")
            }

            sb.append("\n====================================================\n")
            sb.append("Relatório emitido pelo Sistema de Arquivo Morto\n")
            sb.append("${schoolName.uppercase()}\n")

            val reportContent = sb.toString()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "$schoolName - Relatório do Arquivo Morto ($totalCount registros)")
                putExtra(Intent.EXTRA_TEXT, reportContent)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Emitir Relatório - $schoolName").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            Toast.makeText(context, "Relatório gerado com sucesso!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            fallbackCopyToClipboard(context, "Relatório", e.message ?: "")
        }
    }

    /**
     * Generates and shares a statistical summary report
     */
    fun shareStatsReport(
        context: Context,
        egressos: List<EgressoEntity>,
        schoolName: String = "GESTÃO DE PRONTUÁRIOS",
        operatorName: String = "Secretaria"
    ) {
        if (egressos.isEmpty()) {
            Toast.makeText(context, "Nenhum dado disponível para estatísticas.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val totalCount = egressos.size
            val caixasMap = egressos.groupBy { it.caixaArquivo.ifBlank { "Caixa 01" } }.mapValues { it.value.size }
            val completas = egressos.count { it.statusDocumento.contains("Completo", ignoreCase = true) }
            val pendentes = egressos.count { it.statusDocumento.contains("Pendente", ignoreCase = true) }
            val retirados = egressos.count { it.statusDocumento.contains("Retirado", ignoreCase = true) }
            val ativos = egressos.count { it.statusDocumento.equals("Ativo", ignoreCase = true) }
            val digitais = egressos.count { it.statusDocumento.contains("digital", ignoreCase = true) }

            val sb = StringBuilder()
            sb.append("====================================================\n")
            sb.append("         ${schoolName.uppercase()}\n")
            sb.append("     RELATÓRIO ESTATÍSTICO DE ARQUIVAMENTO\n")
            sb.append("====================================================\n")
            sb.append("📅 Emitido em: ${getFormattedDate()}\n")
            sb.append("👤 Operador Responsável: $operatorName\n\n")
            sb.append("📊 TOTAL DE PRONTUÁRIOS: $totalCount\n")
            sb.append("📦 TOTAL DE CAIXAS UTILIZADAS: ${caixasMap.size}\n\n")
            sb.append("----------------------------------------------------\n")
            sb.append("SITUAÇÃO DOS DOCUMENTOS:\n")
            sb.append(" • Arquivados Completos: $completas (${if(totalCount > 0) completas * 100 / totalCount else 0}%)\n")
            sb.append(" • Ativos: $ativos (${if(totalCount > 0) ativos * 100 / totalCount else 0}%)\n")
            sb.append(" • Pendentes: $pendentes (${if(totalCount > 0) pendentes * 100 / totalCount else 0}%)\n")
            sb.append(" • 2ª Via Digital: $digitais (${if(totalCount > 0) digitais * 100 / totalCount else 0}%)\n")
            sb.append(" • Retirados: $retirados (${if(totalCount > 0) retirados * 100 / totalCount else 0}%)\n\n")
            sb.append("----------------------------------------------------\n")
            sb.append("DISTRIBUIÇÃO POR CAIXAS:\n")
            caixasMap.entries.sortedByDescending { it.value }.forEach { (caixa, qtd) ->
                sb.append(" • $caixa: $qtd pasta(s)\n")
            }
            sb.append("====================================================\n")

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "$schoolName - Estatísticas de Arquivo")
                putExtra(Intent.EXTRA_TEXT, sb.toString())
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Emitir Estatísticas - $schoolName").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao emitir estatísticas: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Exports full CSV with UTF-8 BOM
     */
    fun shareCsvFile(
        context: Context,
        egressos: List<EgressoEntity>,
        schoolName: String = "GESTÃO DE PRONTUÁRIOS"
    ) {
        if (egressos.isEmpty()) {
            Toast.makeText(context, "Nenhum prontuário para exportar.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val sb = StringBuilder()
            // UTF-8 BOM so Excel opens with proper Portuguese accents
            sb.append("\uFEFF")
            sb.append("Nome;SGDE;CPF;RG;Sexo/Gênero;Curso;Ano de Conclusão;Turma;Caixa de Arquivo;Prateleira;Pasta/Protocolo;Situação do Documento;Formato Envio Digital;Data de Retirada;Cadastrado Por;Observações\n")

            for (e in egressos) {
                val nome = e.nome.replace(";", ",")
                val sgde = e.codigo.replace(";", ",")
                val cpf = e.cpf.replace(";", ",")
                val rg = e.rg.replace(";", ",")
                val genero = e.genero.replace(";", ",")
                val curso = e.curso.replace(";", ",")
                val ano = if (e.anoConclusao > 0) e.anoConclusao.toString() else ""
                val turma = e.turma.replace(";", ",")
                val caixa = e.caixaArquivo.replace(";", ",")
                val prat = e.prateleiraCorredor.replace(";", ",")
                val pasta = e.pastaProtocolo.replace(";", ",")
                val status = e.statusDocumento.replace(";", ",")
                val formato = e.formatoEnvioDigital.replace(";", ",")
                val dataRet = e.dataEnvioDigital.replace(";", ",")
                val cadPor = e.cadastradoPor.replace(";", ",")
                val obs = e.observacoes.replace(";", ",").replace("\n", " ")

                sb.append("${nome};${sgde};${cpf};${rg};${genero};${curso};${ano};${turma};${caixa};${prat};${pasta};${status};${formato};${dataRet};${cadPor};${obs}\n")
            }

            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val cleanSchool = schoolName.replace(" ", "_").replace(Regex("[^a-zA-Z0-9_]"), "")
            val file = File(exportDir, "${cleanSchool}_Arquivo_Morto_${System.currentTimeMillis()}.csv")
            FileOutputStream(file).use { out ->
                out.write(sb.toString().toByteArray(Charsets.UTF_8))
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "$schoolName - Planilha de Egressos em Arquivo Morto")
                putExtra(Intent.EXTRA_TEXT, "Planilha de egressos cadastrados em $schoolName (${egressos.size} registros).\nSistema de Arquivo Morto.")
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Exportar Planilha $schoolName").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)
            Toast.makeText(context, "Planilha CSV gerada (${egressos.size} registros)!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao gerar arquivo CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Generates and shares an individual Withdrawal Receipt Slip
     */
    fun shareLocationSlip(
        context: Context,
        egresso: EgressoEntity,
        schoolName: String = "GESTÃO DE PRONTUÁRIOS",
        operatorName: String = "Secretaria"
    ) {
        try {
            val digitalInfo = if (egresso.statusDocumento.contains("digital", ignoreCase = true) || egresso.formatoEnvioDigital.isNotEmpty()) {
                """
                ----------------------------------------------
                DETALHES DE ENVIO DIGITAL (2ª VIA):
                📲 FORMATO DE ENVIO: ${egresso.formatoEnvioDigital.ifEmpty { "N/I" }}
                📅 DATA DE RETIRADA / ENVIO: ${egresso.dataEnvioDigital.ifEmpty { "N/I" }}
                """.trimIndent()
            } else {
                ""
            }

            val slipText = """
                ==============================================
                ${schoolName.uppercase()}
                TERMO DE RETIRADA DE DOCUMENTO
                ==============================================
                ESTUDANTE: ${egresso.nome.uppercase()}
                SGDE (CÓDIGO): ${egresso.codigo.ifEmpty { "N/I" }}
                CPF: ${egresso.cpf.ifEmpty { "N/I" }}
                RG: ${egresso.rg.ifEmpty { "N/I" }}
                SEXO / GÊNERO: ${egresso.genero.ifEmpty { "N/I" }}
                CURSO: ${egresso.curso.ifEmpty { "Geral" }} ${if (egresso.anoConclusao > 0) "(${egresso.anoConclusao})" else ""}
                TURMA/TURNO: ${egresso.turma.ifEmpty { "N/I" }}
                ----------------------------------------------
                LOCALIZAÇÃO NO ARQUIVO:
                📦 Caixa: ${egresso.caixaArquivo.ifEmpty { "Caixa 01" }}
                📍 Prateleira/Corredor: ${egresso.prateleiraCorredor.ifEmpty { "N/I" }}
                📂 Pasta/Protocolo: ${egresso.pastaProtocolo.ifEmpty { "N/I" }}
                ----------------------------------------------
                SITUAÇÃO DO DOCUMENTO: ${egresso.statusDocumento.ifEmpty { "Arquivado" }}
                $digitalInfo
                ----------------------------------------------
                OBSERVAÇÕES: ${egresso.observacoes.ifEmpty { "Nenhuma observação cadastrada." }}
                ==============================================
                ______________________________________________
                Assinatura do Solicitante / Egresso
                
                ______________________________________________
                Operador Responsável: $operatorName
                ==============================================
                Emitido em: ${getFormattedDate()}
                Sistema de Arquivo Morto
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "$schoolName - Termo de Retirada: ${egresso.nome}")
                putExtra(Intent.EXTRA_TEXT, slipText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, "Compartilhar Termo de Retirada").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Erro ao emitir termo de retirada: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fallbackCopyToClipboard(context: Context, title: String, errorMsg: String) {
        try {
            Toast.makeText(context, "Não foi possível abrir o compartilhador: $errorMsg", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // ignore
        }
    }
}

