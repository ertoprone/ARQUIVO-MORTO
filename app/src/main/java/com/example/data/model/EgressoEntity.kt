package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "egressos")
data class EgressoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val codigo: String,                      // SGDE (Código do Aluno)
    val nome: String,                        // Nome Completo
    val cpf: String = "",                    // CPF
    val rg: String = "",                     // RG / Doc
    val genero: String = "",                 // Sexo / Gênero (Feminino, Masculino, Outro, etc.)
    val curso: String = "",                  // Curso / Modalidade
    val anoConclusao: Int = 0,               // Ano de Conclusão / Saída
    val turma: String = "",                  // Turma / Turno
    val statusDocumento: String = "Arquivado Completo", // "Arquivado Completo", "Ativo", "Pendente Certificado", "Apenas Histórico", "Retirado - Físico", "2ª via digital"
    val formatoEnvioDigital: String = "",    // "E-mail" ou "WhatsApp"
    val dataEnvioDigital: String = "",       // Data de Retirada / Envio digital (DD/MM/AAAA)
    val caixaArquivo: String,                // Localização: Caixa de Arquivo Morto
    val prateleiraCorredor: String = "",    // Prateleira/Estante
    val pastaProtocolo: String = "",        // Número da Pasta / Prontuário
    val observacoes: String = "",            // Observações
    val cadastradoPor: String = "",          // Usuário / Operador responsável
    val dataCadastro: Long = System.currentTimeMillis()
)
