package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EmailNotificationHelper {

    fun sendRegistrationEmail(
        context: Context,
        userEmail: String,
        operatorName: String,
        schoolName: String
    ) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date())

        val subject = "Confirmação de Cadastro - $schoolName"
        val body = """
            ==================================================
            $schoolName - CONTROLE DE ACESSO
            NOTIFICAÇÃO DE NOVO CADASTRO DE USUÁRIO
            ==================================================
            
            Olá, $operatorName!
            
            Seu cadastro no sistema "$schoolName" foi realizado com sucesso.
            
            DADOS DO CADASTRO:
            --------------------------------------------------
            • Nome do Usuário/Operador: $operatorName
            • E-mail Cadastrado: $userEmail
            • Instituição/Sistema: $schoolName
            • Data e Hora de Cadastro: $formattedDate
            • Status do Acesso: Ativo e Sincronizado
            
            --------------------------------------------------
            Você já pode acessar o sistema para consultar,
            cadastrar, editar e exportar os prontuários de
            estudantes egressos.
            
            Atenciosamente,
            Equipe de Gestão e Administração
            Sistema $schoolName
            ==================================================
        """.trimIndent()

        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(userEmail.trim()))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Enviar e-mail de confirmação de cadastro"))
        } catch (e: Exception) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(userEmail.trim()))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, body)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(fallbackIntent, "Enviar e-mail de confirmação"))
            } catch (ex: Exception) {
                Toast.makeText(context, "Cadastro efetuado para $userEmail!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
