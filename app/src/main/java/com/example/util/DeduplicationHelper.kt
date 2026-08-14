package com.example.util

import com.example.data.model.EgressoEntity
import java.text.Normalizer
import java.util.Locale

data class DeduplicationResult(
    val initialTotal: Int,
    val finalTotal: Int,
    val duplicatesRemovedCount: Int,
    val groupsMergedCount: Int,
    val mergedEntities: List<EgressoEntity>
)

object DeduplicationHelper {

    fun normalizeText(text: String): String {
        val nfd = Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
        return nfd.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .uppercase(Locale.ROOT)
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun cleanDigits(text: String): String {
        return text.replace(Regex("[^0-9]"), "")
    }

    /**
     * Groups egressos that refer to the same student based on:
     * 1. CPF (digits matching, minimum 9 digits)
     * 2. SGDE / Código (exact normalized match, if not blank and not a generic placeholder)
     * 3. Full Name (normalized match without accents, when length >= 3)
     */
    fun deduplicateAndMerge(list: List<EgressoEntity>): DeduplicationResult {
        if (list.isEmpty()) {
            return DeduplicationResult(0, 0, 0, 0, emptyList())
        }

        // Use Disjoint Set Union (DSU) / Union-Find to group all connected entities
        val parent = IntArray(list.size) { it }
        fun find(i: Int): Int {
            var root = i
            while (root != parent[root]) {
                parent[root] = parent[parent[root]] // path compression
                root = parent[root]
            }
            return root
        }
        fun union(i: Int, j: Int) {
            val rootI = find(i)
            val rootJ = find(j)
            if (rootI != rootJ) {
                parent[rootI] = rootJ
            }
        }

        val cpfMap = mutableMapOf<String, Int>()
        val codigoMap = mutableMapOf<String, Int>()
        val nameMap = mutableMapOf<String, Int>()

        for (i in list.indices) {
            val item = list[i]

            // Match by CPF
            val cpfDigits = cleanDigits(item.cpf)
            if (cpfDigits.length >= 9) {
                val existing = cpfMap[cpfDigits]
                if (existing != null) {
                    union(i, existing)
                } else {
                    cpfMap[cpfDigits] = i
                }
            }

            // Match by Codigo (SGDE / Matricula)
            val normCodigo = normalizeText(item.codigo)
            if (normCodigo.isNotBlank() &&
                normCodigo != "0" &&
                normCodigo != "SEM CODIGO" &&
                normCodigo != "SEM SGDE" &&
                normCodigo != "NAO INFORMADO"
            ) {
                val existing = codigoMap[normCodigo]
                if (existing != null) {
                    union(i, existing)
                } else {
                    codigoMap[normCodigo] = i
                }
            }

            // Match by Name
            val normName = normalizeText(item.nome)
            if (normName.length >= 3) {
                val existing = nameMap[normName]
                if (existing != null) {
                    union(i, existing)
                } else {
                    nameMap[normName] = i
                }
            }
        }

        // Group by cluster root
        val clusters = mutableMapOf<Int, MutableList<EgressoEntity>>()
        for (i in list.indices) {
            val root = find(i)
            clusters.getOrPut(root) { mutableListOf() }.add(list[i])
        }

        var groupsMergedCount = 0
        val unifiedList = mutableListOf<EgressoEntity>()

        for ((_, group) in clusters) {
            if (group.size > 1) {
                groupsMergedCount++
                unifiedList.add(mergeGroup(group))
            } else {
                unifiedList.add(group.first())
            }
        }

        val duplicatesRemoved = list.size - unifiedList.size
        return DeduplicationResult(
            initialTotal = list.size,
            finalTotal = unifiedList.size,
            duplicatesRemovedCount = duplicatesRemoved,
            groupsMergedCount = groupsMergedCount,
            mergedEntities = unifiedList
        )
    }

    /**
     * Merges a group of duplicate EgressoEntity objects into one single entity with all unified details.
     */
    private fun mergeGroup(group: List<EgressoEntity>): EgressoEntity {
        // Base entity from the one with the smallest id (or earliest created)
        val base = group.minByOrNull { if (it.id > 0) it.id else Long.MAX_VALUE } ?: group.first()

        val id = if (base.id > 0) base.id else 0L

        // Best name: longest trimmed name or best formatted
        val bestName = group.map { it.nome.trim() }
            .filter { it.isNotBlank() }
            .maxByOrNull { it.length } ?: base.nome

        // Best codigo: prefer real/longer SGDE, avoid temporary placeholders if real exists
        val bestCodigo = group.map { it.codigo.trim() }
            .filter { it.isNotBlank() && !it.startsWith("TEMP_") && !it.startsWith("AUT_") }
            .maxByOrNull { it.length }
            ?: group.firstOrNull { it.codigo.isNotBlank() }?.codigo
            ?: base.codigo

        // Best CPF
        val bestCpf = group.map { it.cpf.trim() }
            .filter { cleanDigits(it).length >= 9 }
            .maxByOrNull { it.length }
            ?: group.firstOrNull { it.cpf.isNotBlank() }?.cpf
            ?: ""

        // Best RG
        val bestRg = group.map { it.rg.trim() }
            .filter { it.isNotBlank() }
            .maxByOrNull { it.length }
            ?: ""

        // Best Sexo / Gênero
        val bestGenero = group.map { it.genero.trim() }
            .firstOrNull { it.isNotBlank() } ?: ""

        // Best Curso
        val bestCurso = group.map { it.curso.trim() }
            .filter { it.isNotBlank() }
            .maxByOrNull { it.length }
            ?: ""

        // Best Ano
        val bestAno = group.map { it.anoConclusao }
            .filter { it > 0 }
            .maxOrNull() ?: 0

        // Best Turma
        val bestTurma = group.map { it.turma.trim() }
            .filter { it.isNotBlank() }
            .maxByOrNull { it.length }
            ?: ""

        // Best Status: prioritize specific status over generic "Arquivado Completo"
        val statusPriority = listOf("Retirado - Físico", "2ª via digital", "Pendente Certificado", "Apenas Histórico", "Ativo", "Arquivado Completo")
        val bestStatus = group.map { it.statusDocumento.trim() }
            .filter { it.isNotBlank() }
            .minByOrNull { statusPriority.indexOf(it).let { idx -> if (idx >= 0) idx else 99 } }
            ?: "Arquivado Completo"

        // Formato Envio Digital
        val bestFormato = group.map { it.formatoEnvioDigital.trim() }
            .firstOrNull { it.isNotBlank() } ?: ""

        // Data Envio Digital
        val bestDataEnvio = group.map { it.dataEnvioDigital.trim() }
            .firstOrNull { it.isNotBlank() } ?: ""

        // Caixa Arquivo
        val bestCaixa = group.map { it.caixaArquivo.trim() }
            .filter { it.isNotBlank() && !it.equals("Geral", ignoreCase = true) }
            .maxByOrNull { it.length }
            ?: group.firstOrNull { it.caixaArquivo.isNotBlank() }?.caixaArquivo
            ?: "Caixa 01"

        // Prateleira / Corredor
        val bestPrateleira = group.map { it.prateleiraCorredor.trim() }
            .filter { it.isNotBlank() }
            .maxByOrNull { it.length }
            ?: ""

        // Pasta / Protocolo
        val bestPasta = group.map { it.pastaProtocolo.trim() }
            .filter { it.isNotBlank() }
            .maxByOrNull { it.length }
            ?: ""

        // Observações: combine all unique observation snippets
        val combinedObs = group.map { it.observacoes.trim() }
            .filter { it.isNotBlank() }
            .flatMap { it.split("\n", " • ", " | ") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(" • ")

        val bestCadastradoPor = group.map { it.cadastradoPor.trim() }
            .firstOrNull { it.isNotBlank() } ?: ""

        val earliestCadastro = group.map { it.dataCadastro }
            .filter { it > 0 }
            .minOrNull() ?: System.currentTimeMillis()

        return EgressoEntity(
            id = id,
            codigo = bestCodigo,
            nome = bestName,
            cpf = bestCpf,
            rg = bestRg,
            genero = bestGenero,
            curso = bestCurso,
            anoConclusao = bestAno,
            turma = bestTurma,
            statusDocumento = bestStatus,
            formatoEnvioDigital = bestFormato,
            dataEnvioDigital = bestDataEnvio,
            caixaArquivo = bestCaixa,
            prateleiraCorredor = bestPrateleira,
            pastaProtocolo = bestPasta,
            observacoes = combinedObs,
            cadastradoPor = bestCadastradoPor,
            dataCadastro = earliestCadastro
        )
    }
}
