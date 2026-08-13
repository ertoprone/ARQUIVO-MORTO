package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.model.EgressoEntity
import java.io.BufferedReader
import java.io.InputStreamReader

object ExcelCsvParser {

    data class ParseResult(
        val egressos: List<EgressoEntity>,
        val totalRowsRead: Int,
        val importedCount: Int,
        val errorMessage: String? = null
    )

    fun parseFromUri(context: Context, uri: Uri): ParseResult {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return ParseResult(emptyList(), 0, 0, "Não foi possível abrir o arquivo.")
            
            val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
            val lines = reader.readLines()
            inputStream.close()

            if (lines.isEmpty()) {
                return ParseResult(emptyList(), 0, 0, "O arquivo selecionado está vazio.")
            }

            parseLines(lines)
        } catch (e: Exception) {
            ParseResult(emptyList(), 0, 0, "Erro ao ler arquivo: ${e.localizedMessage}")
        }
    }

    fun parseLines(lines: List<String>): ParseResult {
        val nonBlankLines = lines.map { it.trim() }.filter { it.isNotEmpty() }
        if (nonBlankLines.isEmpty()) {
            return ParseResult(emptyList(), 0, 0, "Nenhuma linha válida encontrada.")
        }

        // Auto-detect delimiter: ; or , or \t
        val headerLine = nonBlankLines.first()
        val delimiter = when {
            headerLine.contains(";") -> ";"
            headerLine.contains("\t") -> "\t"
            headerLine.contains(",") -> ","
            else -> ";"
        }

        val headers = headerLine.split(delimiter).map { sanitizeHeader(it) }

        val nameIdx = findHeaderIndex(headers, listOf("nome", "estudante", "aluno", "nome do aluno", "egresso"))
        val codeIdx = findHeaderIndex(headers, listOf("codigo", "código", "matricula", "matrícula", "cod", "ra"))
        val cpfIdx = findHeaderIndex(headers, listOf("cpf", "cpf do aluno", "documento"))
        val rgIdx = findHeaderIndex(headers, listOf("rg", "identidade"))
        val courseIdx = findHeaderIndex(headers, listOf("curso", "habilitação", "curso/graduação"))
        val yearIdx = findHeaderIndex(headers, listOf("ano", "conclusao", "conclusão", "saida", "saída", "ano conclusao"))
        val turmaIdx = findHeaderIndex(headers, listOf("turma", "turno"))
        val statusIdx = findHeaderIndex(headers, listOf("status", "situacao", "situação", "status documento"))
        val caixaIdx = findHeaderIndex(headers, listOf("caixa", "caixa arquivo", "caixa de arquivo", "arquivo morto", "localizacao", "localização"))
        val pratIdx = findHeaderIndex(headers, listOf("prateleira", "estante", "corredor"))
        val pastaIdx = findHeaderIndex(headers, listOf("pasta", "prontuario", "prontuário", "protocolo", "pasta/protocolo"))
        val obsIdx = findHeaderIndex(headers, listOf("observacoes", "observações", "obs", "notas"))

        val parsedList = mutableListOf<EgressoEntity>()
        var totalRows = 0

        for (i in 1 until nonBlankLines.size) {
            val line = nonBlankLines[i]
            val cols = line.split(delimiter).map { it.trim().removeSurrounding("\"") }
            if (cols.isEmpty() || cols.all { it.isEmpty() }) continue
            totalRows++

            fun getCol(idx: Int): String {
                return if (idx >= 0 && idx < cols.size) cols[idx] else ""
            }

            val nome = getCol(nameIdx)
            if (nome.isEmpty() && getCol(codeIdx).isEmpty() && getCol(cpfIdx).isEmpty()) {
                continue // Skip empty line
            }

            val codigo = getCol(codeIdx).ifEmpty { "EGR-${System.currentTimeMillis() % 10000}" }
            val cpf = formatCpf(getCol(cpfIdx))
            val rg = getCol(rgIdx)
            val curso = getCol(courseIdx).ifEmpty { "Geral" }
            
            val yearRaw = getCol(yearIdx).filter { it.isDigit() }
            val ano = yearRaw.toIntOrNull() ?: 2023

            val status = getCol(statusIdx).ifEmpty { "Arquivado Completo" }
            val caixa = getCol(caixaIdx).ifEmpty { "Caixa Geral" }
            val prat = getCol(pratIdx)
            val pasta = getCol(pastaIdx)
            val obs = getCol(obsIdx)
            val turma = getCol(turmaIdx)

            val egresso = EgressoEntity(
                codigo = codigo,
                nome = nome.ifEmpty { "Estudante sem Nome ($codigo)" },
                cpf = cpf,
                rg = rg,
                curso = curso,
                anoConclusao = ano,
                turma = turma,
                statusDocumento = status,
                caixaArquivo = caixa,
                prateleiraCorredor = prat,
                pastaProtocolo = pasta,
                observacoes = obs
            )
            parsedList.add(egresso)
        }

        return ParseResult(
            egressos = parsedList,
            totalRowsRead = totalRows,
            importedCount = parsedList.size
        )
    }

    private fun sanitizeHeader(header: String): String {
        return header.trim()
            .lowercase()
            .removeSurrounding("\"")
            .replace("á", "a")
            .replace("ã", "a")
            .replace("â", "a")
            .replace("é", "e")
            .replace("ê", "e")
            .replace("í", "i")
            .replace("ó", "o")
            .replace("õ", "o")
            .replace("ú", "u")
            .replace("ç", "c")
    }

    private fun findHeaderIndex(headers: List<String>, keywords: List<String>): Int {
        for ((index, header) in headers.withIndex()) {
            for (keyword in keywords) {
                if (header.contains(keyword)) {
                    return index
                }
            }
        }
        return -1
    }

    private fun formatCpf(rawCpf: String): String {
        val digits = rawCpf.filter { it.isDigit() }
        if (digits.length == 11) {
            return "${digits.substring(0, 3)}.${digits.substring(3, 6)}.${digits.substring(6, 9)}-${digits.substring(9, 11)}"
        }
        return rawCpf
    }
}
