package com.example.util

import com.example.data.model.EgressoEntity

object SampleDataGenerator {

    fun getSampleEgressos(): List<EgressoEntity> {
        return listOf(
            EgressoEntity(
                codigo = "201810401",
                nome = "Ana Beatriz Souza Ribeiro",
                cpf = "123.456.789-01",
                rg = "12.345.678-SSP/SP",
                genero = "Feminino",
                curso = "Engenharia Civil",
                anoConclusao = 2022,
                turma = "Matutino - T01",
                statusDocumento = "Arquivado Completo",
                caixaArquivo = "Caixa 01 - Setor A",
                prateleiraCorredor = "Estante 03 - Corredor 1",
                pastaProtocolo = "Pasta 14 / Prot. 4520",
                observacoes = "Diploma emitido e histórico assinado armazenado na pasta original."
            ),
            EgressoEntity(
                codigo = "201720812",
                nome = "Bruno Castro Alencar",
                cpf = "234.567.890-12",
                rg = "23.456.789-SSP/RJ",
                genero = "Masculino",
                curso = "Direito",
                anoConclusao = 2021,
                turma = "Noturno - T02",
                statusDocumento = "Pendente Certificado",
                caixaArquivo = "Caixa 02 - Setor A",
                prateleiraCorredor = "Estante 03 - Corredor 1",
                pastaProtocolo = "Pasta 88 / Prot. 3190",
                observacoes = "Aguardando envio do certificado de horas complementares pelo egresso."
            ),
            EgressoEntity(
                codigo = "201910103",
                nome = "Camila Fernandes Lima",
                cpf = "345.678.901-23",
                rg = "34.567.890-SSP/MG",
                genero = "Feminino",
                curso = "Administração",
                anoConclusao = 2023,
                turma = "Vespertino - T01",
                statusDocumento = "Arquivado Completo",
                caixaArquivo = "Caixa 05 - Setor B",
                prateleiraCorredor = "Estante 01 - Corredor 2",
                pastaProtocolo = "Pasta 03 / Prot. 5812",
                observacoes = "Documentação digitalizada no sistema acadêmico em 2024."
            ),
            EgressoEntity(
                codigo = "201620550",
                nome = "Diego Martins Carvalho",
                cpf = "456.789.012-34",
                rg = "45.678.901-SSP/PR",
                curso = "Sistemas de Informação",
                anoConclusao = 2020,
                turma = "Noturno - T01",
                statusDocumento = "Retirado",
                caixaArquivo = "Caixa 12 - Setor C",
                prateleiraCorredor = "Estante 04 - Corredor 2",
                pastaProtocolo = "Pasta 112 / Prot. 2201",
                observacoes = "Histórico original retirado pelo egresso mediante procuração em 10/05/2023."
            ),
            EgressoEntity(
                codigo = "201510332",
                nome = "Elena Vasconcelos Nogueira",
                cpf = "567.890.123-45",
                rg = "56.789.012-SSP/RS",
                curso = "Pedagogia",
                anoConclusao = 2019,
                turma = "Matutino - T02",
                statusDocumento = "Arquivado Completo",
                caixaArquivo = "Caixa 01 - Setor A",
                prateleiraCorredor = "Estante 02 - Corredor 1",
                pastaProtocolo = "Pasta 45 / Prot. 1980",
                observacoes = "Contém pasta de estágio supervisionado com fichas de avaliação."
            ),
            EgressoEntity(
                codigo = "202010901",
                nome = "Felipe Gabriel Barbosa",
                cpf = "678.901.234-56",
                rg = "67.890.123-SSP/BA",
                curso = "Técnico em Enfermagem",
                anoConclusao = 2022,
                turma = "Noturno - T01",
                statusDocumento = "Apenas Histórico",
                caixaArquivo = "Caixa 08 - Setor B",
                prateleiraCorredor = "Estante 05 - Corredor 1",
                pastaProtocolo = "Pasta 201 / Prot. 6610",
                observacoes = "Aguardando homologação de diploma pelo conselho regional."
            ),
            EgressoEntity(
                codigo = "201820443",
                nome = "Gabriela Rocha Santos",
                cpf = "789.012.345-67",
                rg = "78.901.234-SSP/PE",
                curso = "Arquitetura e Urbanismo",
                anoConclusao = 2023,
                turma = "Integral - T01",
                statusDocumento = "Arquivado Completo",
                caixaArquivo = "Caixa 10 - Setor C",
                prateleiraCorredor = "Estante 02 - Corredor 3",
                pastaProtocolo = "Pasta 55 / Prot. 7203",
                observacoes = "Prontuário de formando arquivado em caixa selada."
            ),
            EgressoEntity(
                codigo = "201710221",
                nome = "Heitor Mendes Oliveira",
                cpf = "890.123.456-78",
                rg = "89.012.345-SSP/SC",
                curso = "Engenharia Civil",
                anoConclusao = 2021,
                turma = "Noturno - T02",
                statusDocumento = "Pendente Certificado",
                caixaArquivo = "Caixa 01 - Setor A",
                prateleiraCorredor = "Estante 03 - Corredor 1",
                pastaProtocolo = "Pasta 92 / Prot. 4110",
                observacoes = "Pendente cópia autenticada do título de eleitor."
            ),
            EgressoEntity(
                codigo = "201920778",
                nome = "Isabela Duarte Gomide",
                cpf = "901.234.567-89",
                rg = "90.123.456-SSP/GO",
                curso = "Direito",
                anoConclusao = 2024,
                turma = "Matutino - T01",
                statusDocumento = "Arquivado Completo",
                caixaArquivo = "Caixa 15 - Setor D",
                prateleiraCorredor = "Estante 01 - Corredor 4",
                pastaProtocolo = "Pasta 18 / Prot. 8920",
                observacoes = "Documentação completa com entrega de TCC e atestado de colação de grau."
            ),
            EgressoEntity(
                codigo = "201610119",
                nome = "João Pedro Moreira",
                cpf = "012.345.678-90",
                rg = "01.234.567-SSP/DF",
                curso = "Administração",
                anoConclusao = 2020,
                turma = "Noturno - T01",
                statusDocumento = "Arquivado Completo",
                caixaArquivo = "Caixa 05 - Setor B",
                prateleiraCorredor = "Estante 01 - Corredor 2",
                pastaProtocolo = "Pasta 31 / Prot. 2890",
                observacoes = "Conclusão de curso regular sem pendências."
            ),
            EgressoEntity(
                codigo = "201520664",
                nome = "Larissa Araujo Farias",
                cpf = "111.222.333-44",
                rg = "11.222.333-SSP/CE",
                curso = "Pedagogia",
                anoConclusao = 2019,
                turma = "Vespertino - T01",
                statusDocumento = "Retirado",
                caixaArquivo = "Caixa 02 - Setor A",
                prateleiraCorredor = "Estante 02 - Corredor 1",
                pastaProtocolo = "Pasta 12 / Prot. 1772",
                observacoes = "Retirado diploma em vias físicas pelo próprio aluno em 15/02/2021."
            ),
            EgressoEntity(
                codigo = "202020331",
                nome = "Lucas Emanuel Teodoro",
                cpf = "222.333.444-55",
                rg = "22.333.444-SSP/ES",
                curso = "Sistemas de Informação",
                anoConclusao = 2024,
                turma = "Noturno - T02",
                statusDocumento = "Arquivado Completo",
                caixaArquivo = "Caixa 12 - Setor C",
                prateleiraCorredor = "Estante 04 - Corredor 2",
                pastaProtocolo = "Pasta 77 / Prot. 9104",
                observacoes = "Arquivo morto recente cadastrado no protocolo digital."
            ),
            EgressoEntity(
                codigo = "201810889",
                nome = "Mariana Xavier Costa",
                cpf = "333.444.555-66",
                rg = "33.444.555-SSP/MS",
                curso = "Técnico em Enfermagem",
                anoConclusao = 2020,
                turma = "Matutino - T01",
                statusDocumento = "Apenas Histórico",
                caixaArquivo = "Caixa 08 - Setor B",
                prateleiraCorredor = "Estante 05 - Corredor 1",
                pastaProtocolo = "Pasta 140 / Prot. 3401",
                observacoes = "Histórico escolar do ensino médio anexado ao prontuário."
            ),
            EgressoEntity(
                codigo = "201720311",
                nome = "Nicolas Ramos Guimarães",
                cpf = "444.555.666-77",
                rg = "44.555.666-SSP/MT",
                curso = "Engenharia Civil",
                anoConclusao = 2022,
                turma = "Noturno - T01",
                statusDocumento = "Arquivado Completo",
                caixaArquivo = "Caixa 01 - Setor A",
                prateleiraCorredor = "Estante 03 - Corredor 1",
                pastaProtocolo = "Pasta 105 / Prot. 5002",
                observacoes = "Caixa 01 totalmente catalogada."
            ),
            EgressoEntity(
                codigo = "201910602",
                nome = "Patricia Paiva Siqueira",
                cpf = "555.666.777-88",
                rg = "55.666.777-SSP/PA",
                curso = "Direito",
                anoConclusao = 2023,
                turma = "Vespertino - T01",
                statusDocumento = "Pendente Certificado",
                caixaArquivo = "Caixa 15 - Setor D",
                prateleiraCorredor = "Estante 01 - Corredor 4",
                pastaProtocolo = "Pasta 41 / Prot. 7630",
                observacoes = "Falta declaração de quitação de débitos da biblioteca."
            )
        )
    }

    fun generateCSV(egressos: List<EgressoEntity>): String {
        val sb = StringBuilder()
        sb.append("Nome;Matrícula;CPF;RG;Sexo/Gênero;Curso;Ano de Conclusão;Turma;Caixa de Arquivo;Prateleira;Pasta/Protocolo;Status do Documento;Observações\n")
        for (e in egressos) {
            val nome = e.nome.replace(";", ",")
            val cod = e.codigo.replace(";", ",")
            val cpf = e.cpf.replace(";", ",")
            val rg = e.rg.replace(";", ",")
            val genero = e.genero.replace(";", ",")
            val curso = e.curso.replace(";", ",")
            val turma = e.turma.replace(";", ",")
            val caixa = e.caixaArquivo.replace(";", ",")
            val prat = e.prateleiraCorredor.replace(";", ",")
            val pasta = e.pastaProtocolo.replace(";", ",")
            val status = e.statusDocumento.replace(";", ",")
            val obs = e.observacoes.replace(";", ",").replace("\n", " ")
            
            sb.append("${nome};${cod};${cpf};${rg};${genero};${curso};${e.anoConclusao};${turma};${caixa};${prat};${pasta};${status};${obs}\n")
        }
        return sb.toString()
    }
}
