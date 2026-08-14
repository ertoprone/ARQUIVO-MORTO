package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.model.EgressoEntity
import java.io.BufferedReader
import java.io.ByteArrayInputStream
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
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return ParseResult(emptyList(), 0, 0, "Não foi possível abrir o arquivo.")

            if (bytes.isEmpty()) {
                return ParseResult(emptyList(), 0, 0, "O arquivo selecionado está vazio.")
            }

            // Detect UTF-8 vs ISO-8859-1 / Windows-1252
            var charset = Charsets.UTF_8
            val utf8Test = String(bytes, Charsets.UTF_8)
            if (utf8Test.contains("")) {
                charset = Charsets.ISO_8859_1
            }

            val reader = BufferedReader(InputStreamReader(ByteArrayInputStream(bytes), charset))
            val lines = reader.readLines()

            if (lines.isEmpty()) {
                return ParseResult(emptyList(), 0, 0, "O arquivo selecionado está vazio.")
            }

            parseLines(lines)
        } catch (e: Exception) {
            ParseResult(emptyList(), 0, 0, "Erro ao processar arquivo: ${e.localizedMessage}")
        }
    }

    fun parseLines(lines: List<String>): ParseResult {
        val nonBlankLines = lines.map { it.trim() }.filter { it.isNotEmpty() }
        if (nonBlankLines.isEmpty()) {
            return ParseResult(emptyList(), 0, 0, "Nenhuma linha com dados encontrada.")
        }

        // Auto-detect delimiter: ; or , or \t
        val headerLine = nonBlankLines.first().removePrefix("\uFEFF")
        val delimiter = when {
            headerLine.contains(";") -> ";"
            headerLine.contains("\t") -> "\t"
            headerLine.contains(",") -> ","
            else -> ";"
        }

        val headers = splitCsvRow(headerLine, delimiter).map { sanitizeHeader(it) }

        val nameIdx = findHeaderIndex(headers, listOf("nome", "estudante", "aluno", "nome do aluno", "egresso"))
        val sgdeIdx = findHeaderIndex(headers, listOf("sgde", "codigo", "código", "matricula", "matrícula", "cod", "ra"))
        val cpfIdx = findHeaderIndex(headers, listOf("cpf", "cpf do aluno", "documento"))
        val rgIdx = findHeaderIndex(headers, listOf("rg", "identidade", "doc"))
        val generoIdx = findHeaderIndex(headers, listOf("sexo", "genero", "gênero", "sex", "gender"))
        val courseIdx = findHeaderIndex(headers, listOf("curso", "habilitação", "modalidade", "turma/curso"))
        val yearIdx = findHeaderIndex(headers, listOf("ano", "conclusao", "conclusão", "saida", "saída", "ano conclusao"))
        val turmaIdx = findHeaderIndex(headers, listOf("turma", "turno", "periodo", "período"))
        val statusIdx = findHeaderIndex(headers, listOf("status", "situacao", "situação", "status documento", "situacao do documento"))
        val formatoIdx = findHeaderIndex(headers, listOf("formato", "envio", "formato envio", "meio"))
        val dataRetiradaIdx = findHeaderIndex(headers, listOf("data retirada", "retirada", "data envio", "data"))
        val caixaIdx = findHeaderIndex(headers, listOf("caixa", "caixa arquivo", "caixa de arquivo", "arquivo morto", "localizacao", "localização"))
        val pratIdx = findHeaderIndex(headers, listOf("prateleira", "estante", "corredor"))
        val pastaIdx = findHeaderIndex(headers, listOf("pasta", "prontuario", "prontuário", "protocolo", "pasta/protocolo"))
        val obsIdx = findHeaderIndex(headers, listOf("observacoes", "observações", "obs", "notas", "anotações"))

        val parsedList = mutableListOf<EgressoEntity>()
        var totalRows = 0

        for (i in 1 until nonBlankLines.size) {
            val line = nonBlankLines[i]
            val cols = splitCsvRow(line, delimiter)
            if (cols.isEmpty() || cols.all { it.isEmpty() }) continue
            totalRows++

            fun getCol(idx: Int): String {
                return if (idx >= 0 && idx < cols.size) cols[idx].trim() else ""
            }

            val nome = getCol(nameIdx)
            val rawSgde = getCol(sgdeIdx)
            val cpf = formatCpf(getCol(cpfIdx))
            val rg = getCol(rgIdx)
            val genero = getCol(generoIdx)
            val curso = getCol(courseIdx).ifEmpty { "Geral" }

            if (nome.isEmpty() && rawSgde.isEmpty() && cpf.isEmpty()) {
                continue // Skip empty line
            }

            val yearRaw = getCol(yearIdx).filter { it.isDigit() }
            val ano = yearRaw.toIntOrNull() ?: 0

            var rawStatus = getCol(statusIdx).ifEmpty { "Arquivado Completo" }
            if (rawStatus.equals("Retirado", ignoreCase = true)) {
                rawStatus = "Retirado - Físico"
            }

            val formatoEnvio = getCol(formatoIdx)
            val dataEnvio = getCol(dataRetiradaIdx)
            val caixa = getCol(caixaIdx).ifEmpty { "Caixa Geral" }
            val prat = getCol(pratIdx)
            val pasta = getCol(pastaIdx)
            val obs = getCol(obsIdx)
            val turma = getCol(turmaIdx)

            // Generate SGDE from Caixa + Pasta if empty
            val finalSgde = if (rawSgde.isNotBlank()) {
                rawSgde
            } else if (caixa.isNotBlank() || pasta.isNotBlank()) {
                val cxClean = caixa.filter { it.isLetterOrDigit() }.takeLast(4)
                val ptClean = pasta.filter { it.isLetterOrDigit() }.takeLast(4)
                "SGDE-${cxClean.ifEmpty { "00" }}-${ptClean.ifEmpty { "00" }}".uppercase()
            } else {
                "SGDE-${(10000..99999).random()}"
            }

            val egresso = EgressoEntity(
                codigo = finalSgde,
                nome = nome.ifEmpty { "Estudante ($finalSgde)" },
                cpf = cpf,
                rg = rg,
                genero = genero,
                curso = curso,
                anoConclusao = ano,
                turma = turma,
                statusDocumento = rawStatus,
                formatoEnvioDigital = formatoEnvio,
                dataEnvioDigital = dataEnvio,
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

    private fun splitCsvRow(line: String, delimiter: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '\"' -> inQuotes = !inQuotes
                char.toString() == delimiter && !inQuotes -> {
                    result.add(current.toString().trim().removeSurrounding("\""))
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString().trim().removeSurrounding("\""))
        return result
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
