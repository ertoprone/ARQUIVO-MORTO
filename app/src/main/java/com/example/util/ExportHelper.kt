package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.EgressoEntity
import java.io.File
import java.io.FileOutputStream

object ExportHelper {

    fun shareCsvFile(context: Context, egressos: List<EgressoEntity>) {
        try {
            val csvContent = SampleDataGenerator.generateCSV(egressos)
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val file = File(exportDir, "egressos_arquivo_morto_${System.currentTimeMillis()}.csv")
            FileOutputStream(file).use { out ->
                out.write(csvContent.toByteArray(Charsets.UTF_8))
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Relatório do Arquivo Morto - Egressos")
                putExtra(Intent.EXTRA_TEXT, "Segue em anexo a lista exportada de estudantes egressos em arquivo morto (${egressos.size} registros).")
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Exportar Planilha de Egressos"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareLocationSlip(context: Context, egresso: EgressoEntity) {
        val slipText = """
            ==============================================
            GUIA DE LOCALIZAÇÃO - ARQUIVO MORTO
            ==============================================
            NOME COMPLETO: ${egresso.nome}
            CÓDIGO/MATRÍCULA: ${egresso.codigo}
            CPF: ${egresso.cpf}
            CURSO: ${egresso.curso} (${egresso.anoConclusao})
            TURMA/TURNO: ${egresso.turma.ifEmpty { "N/I" }}
            ----------------------------------------------
            LOCALIZAÇÃO FÍSICA NO ARQUIVO:
            📍 CAIXA: ${egresso.caixaArquivo}
            📚 PRATELEIRA/ESTANTE: ${egresso.prateleiraCorredor.ifEmpty { "N/I" }}
            📂 PASTA/PROTOCOLO: ${egresso.pastaProtocolo.ifEmpty { "N/I" }}
            STATUS DOCUMENTAL: ${egresso.statusDocumento}
            ----------------------------------------------
            OBSERVAÇÕES: ${egresso.observacoes.ifEmpty { "Nenhuma observação cadastrada." }}
            ==============================================
            Emitido via Aplicativo de Arquivo Morto
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Guia de Arquivo: ${egresso.nome}")
            putExtra(Intent.EXTRA_TEXT, slipText)
        }
        context.startActivity(Intent.createChooser(intent, "Compartilhar Guia de Localização"))
    }
}
