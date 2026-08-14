package com.example

import com.example.data.model.EgressoEntity
import com.example.util.DeduplicationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun deduplication_mergesDuplicateRecordsAndPreservesAllInfo() {
        val r1 = EgressoEntity(
            id = 1,
            codigo = "SGDE12345",
            nome = "Maria Clara de Souza",
            cpf = "123.456.789-00",
            curso = "Ensino Médio",
            anoConclusao = 2020,
            caixaArquivo = "Caixa 01",
            observacoes = "Histórico entregue em 2021"
        )
        val r2 = EgressoEntity(
            id = 2,
            codigo = "SGDE12345",
            nome = "MARIA CLARA DE SOUZA",
            cpf = "",
            rg = "MG-12.345.678",
            curso = "Ensino Médio Regular",
            anoConclusao = 2020,
            statusDocumento = "2ª via digital",
            formatoEnvioDigital = "WhatsApp",
            dataEnvioDigital = "10/05/2023",
            caixaArquivo = "Caixa 01",
            prateleiraCorredor = "Estante A",
            pastaProtocolo = "Pasta 14",
            observacoes = "Enviada segunda via autenticada"
        )
        val r3 = EgressoEntity(
            id = 3,
            codigo = "SGDE99999",
            nome = "João Pedro Santos",
            cpf = "999.888.777-66",
            caixaArquivo = "Caixa 02"
        )

        val result = DeduplicationHelper.deduplicateAndMerge(listOf(r1, r2, r3))

        assertEquals(3, result.initialTotal)
        assertEquals(2, result.finalTotal)
        assertEquals(1, result.duplicatesRemovedCount)
        assertEquals(1, result.groupsMergedCount)

        val mergedMaria = result.mergedEntities.first { it.codigo == "SGDE12345" }
        assertEquals("123.456.789-00", mergedMaria.cpf)
        assertEquals("MG-12.345.678", mergedMaria.rg)
        assertEquals("2ª via digital", mergedMaria.statusDocumento)
        assertEquals("WhatsApp", mergedMaria.formatoEnvioDigital)
        assertEquals("10/05/2023", mergedMaria.dataEnvioDigital)
        assertEquals("Estante A", mergedMaria.prateleiraCorredor)
        assertEquals("Pasta 14", mergedMaria.pastaProtocolo)
        assertTrue(mergedMaria.observacoes.contains("Histórico entregue em 2021"))
        assertTrue(mergedMaria.observacoes.contains("Enviada segunda via autenticada"))
    }
}
