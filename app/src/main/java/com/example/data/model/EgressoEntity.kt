package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "egressos")
data class EgressoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val codigo: String,                      // Matrícula / Código (e.g. "20191024")
    val nome: String,                        // Nome Completo
    val cpf: String,                         // CPF (e.g. "123.456.789-00")
    val rg: String = "",                     // RG / Doc
    val curso: String,                       // Curso (e.g. "Engenharia Civil")
    val anoConclusao: Int,                   // Ano de Conclusão / Saída
    val turma: String = "",                  // Turma / Turno
    val statusDocumento: String,             // e.g. "Arquivado Completo", "Pendente Certificado", "Apenas Histórico", "Retirado"
    val caixaArquivo: String,                // Localização principal: Caixa de Arquivo Morto (e.g. "Caixa 42 - Bloco A")
    val prateleiraCorredor: String = "",    // Localização secundária: Prateleira/Estante
    val pastaProtocolo: String = "",        // Número da Pasta / Prontuário
    val driveUrl: String = "",              // Link para pasta/documento digital no Google Drive
    val observacoes: String = "",            // Notas de arquivo
    val dataCadastro: Long = System.currentTimeMillis()
)
