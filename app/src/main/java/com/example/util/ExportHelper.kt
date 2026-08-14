package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.EgressoEntity
import java.io.File
import java.io.FileOutputStream

object ExportHelper {

    fun shareCsvFile(context: Context, egressos: List<EgressoEntity>, schoolName: String = "GESTÃO DE PRONTUÁRIOS") {
        try {
            val sb = StringBuilder()
            // UTF-8 BOM so Excel opens with proper accents
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
                putExtra(Intent.EXTRA_SUBJECT, "$schoolName - Relatório de Egressos em Arquivo Morto")
                putExtra(Intent.EXTRA_TEXT, "Planilha de egressos cadastrados em $schoolName (${egressos.size} registros).\nSistema de Arquivo Morto.")
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Exportar Planilha $schoolName"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareLocationSlip(context: Context, egresso: EgressoEntity, schoolName: String = "GESTÃO DE PRONTUÁRIOS") {
        val digitalInfo = if (egresso.statusDocumento == "2ª via digital" || egresso.formatoEnvioDigital.isNotEmpty()) {
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
            ESTUDANTE: ${egresso.nome}
            SGDE (CÓDIGO): ${egresso.codigo}
            CPF: ${egresso.cpf.ifEmpty { "N/I" }}
            RG: ${egresso.rg.ifEmpty { "N/I" }}
            SEXO / GÊNERO: ${egresso.genero.ifEmpty { "N/I" }}
            CURSO: ${egresso.curso} ${if (egresso.anoConclusao > 0) "(${egresso.anoConclusao})" else ""}
            TURMA/TURNO: ${egresso.turma.ifEmpty { "N/I" }}
            ----------------------------------------------
            SITUAÇÃO DO DOCUMENTO: ${egresso.statusDocumento}
            $digitalInfo
            ----------------------------------------------
            OBSERVAÇÕES: ${egresso.observacoes.ifEmpty { "Nenhuma observação cadastrada." }}
            ==============================================
            ______________________________________________
            Assinatura do Solicitante / Egresso
            
            ______________________________________________
            Operador Responsável
            ==============================================
            Emitido em: ${java.text.SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}
            Sistema de Arquivo Morto
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$schoolName - Termo de Retirada: ${egresso.nome}")
            putExtra(Intent.EXTRA_TEXT, slipText)
        }
        context.startActivity(Intent.createChooser(intent, "Compartilhar Termo de Retirada"))
    }
}
