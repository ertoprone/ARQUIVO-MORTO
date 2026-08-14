package com.example.ui.components

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.EmailNotificationHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    isAccessConfigured: Boolean,
    registeredEmail: String,
    currentSchoolName: String = "GESTÃO DE PRONTUÁRIOS",
    activeOperator: String = "Rúbia Elise",
    operatorsList: List<String> = listOf("Rúbia Elise"),
    onSchoolNameChange: (String) -> Unit = {},
    onConfigureFirstAccess: (email: String, password: String, firstOperator: String, schoolName: String) -> Unit,
    onLoginWithPassword: (password: String, selectedOperator: String) -> Boolean
) {
    val context = LocalContext.current

    // Setup fields
    var emailInput by remember { mutableStateOf(registeredEmail.ifBlank { "secretariaeecv@gmail.com" }) }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var operatorInput by remember { mutableStateOf(activeOperator) }
    var schoolNameInput by remember { mutableStateOf(currentSchoolName) }

    // Operator selection in login
    var selectedOperatorInLogin by remember { mutableStateOf(activeOperator.ifBlank { operatorsList.firstOrNull() ?: "Rúbia Elise" }) }
    var isOperatorDropdownExpanded by remember { mutableStateOf(false) }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isEditingSchoolName by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Header Branding
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .height(44.dp)
                        .width(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // School / System Name
            Text(
                text = if (isAccessConfigured) currentSchoolName else schoolNameInput,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Sistema de Arquivo Morto • Controle de Egressos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Login / First Setup Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    if (!isAccessConfigured) {
                        // MODO PRIMEIRO ACESSO / CONFIGURAÇÃO DO E-MAIL ÚNICO
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                    .padding(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.height(18.dp).width(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Configuração do Acesso Único",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Primeiro Acesso • E-mail e Senha Mestra",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Defina o e-mail único e a senha que protegerão o sistema. O gerenciamento e a troca de operadores serão feitos internamente dentro do app.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        errorMessage?.let { error ->
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        // E-mail Único do Sistema
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it; errorMessage = null },
                            label = { Text("E-mail Único de Acesso *") },
                            placeholder = { Text("exemplo@email.com") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Email, contentDescription = null)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth().testTag("login_email_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Nome do 1º Operador
                        OutlinedTextField(
                            value = operatorInput,
                            onValueChange = { operatorInput = it; errorMessage = null },
                            label = { Text("Nome do 1º Operador *") },
                            placeholder = { Text("Ex: Rúbia Elise") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("login_operator_name_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Nome da Escola / Instituição
                        OutlinedTextField(
                            value = schoolNameInput,
                            onValueChange = {
                                schoolNameInput = it
                                onSchoolNameChange(it)
                            },
                            label = { Text("Nome da Instituição / Sistema") },
                            placeholder = { Text("Ex: GESTÃO DE PRONTUÁRIOS") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.School, contentDescription = null)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Senha Mestra
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it; errorMessage = null },
                            label = { Text("Criar Senha Mestra *") },
                            placeholder = { Text("Mínimo 4 caracteres") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Mostrar senha"
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("login_password_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Confirmar Senha
                        OutlinedTextField(
                            value = confirmPasswordInput,
                            onValueChange = { confirmPasswordInput = it; errorMessage = null },
                            label = { Text("Confirmar Senha Mestra *") },
                            placeholder = { Text("Repita a senha criada") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                            },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("login_confirm_password_input")
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                val trimmedEmail = emailInput.trim().lowercase()
                                val trimmedOp = operatorInput.trim()
                                val trimmedSchool = schoolNameInput.trim().ifEmpty { "GESTÃO DE PRONTUÁRIOS" }

                                if (trimmedEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                                    errorMessage = "Por favor, digite um e-mail válido para o acesso único."
                                    return@Button
                                }
                                if (trimmedOp.isBlank()) {
                                    errorMessage = "Por favor, informe o nome do primeiro operador."
                                    return@Button
                                }
                                if (passwordInput.length < 4) {
                                    errorMessage = "A senha mestra deve conter pelo menos 4 caracteres."
                                    return@Button
                                }
                                if (passwordInput != confirmPasswordInput) {
                                    errorMessage = "As senhas não coincidem. Verifique a confirmação."
                                    return@Button
                                }

                                EmailNotificationHelper.sendRegistrationEmail(
                                    context = context,
                                    userEmail = trimmedEmail,
                                    operatorName = trimmedOp,
                                    schoolName = trimmedSchool
                                )

                                onConfigureFirstAccess(
                                    trimmedEmail,
                                    passwordInput,
                                    trimmedOp,
                                    trimmedSchool
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = "Configurar e Acessar Sistema",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                    } else {
                        // MODO LOGIN COM ACESSO ÚNICO JÁ CONFIGURADO
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Acesso ao Sistema",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            TextButton(onClick = { isEditingSchoolName = !isEditingSchoolName }) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    modifier = Modifier.height(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isEditingSchoolName) "Ocultar" else "Editar Nome",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        if (isEditingSchoolName) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = schoolNameInput,
                                onValueChange = {
                                    schoolNameInput = it
                                    onSchoolNameChange(it)
                                },
                                label = { Text("Nome da Instituição") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.School, contentDescription = null)
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Card com o E-mail Único do Sistema Cadastrado
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.height(20.dp).width(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "E-mail de Acesso Autorizado",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = registeredEmail,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Dropdown de Seleção de Operador (se houver mais de 1 ou para confirmar o operador ativo)
                        ExposedDropdownMenuBox(
                            expanded = isOperatorDropdownExpanded,
                            onExpandedChange = { isOperatorDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedOperatorInLogin,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Operador de Turno") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                                },
                                trailingIcon = {
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = isOperatorDropdownExpanded,
                                onDismissRequest = { isOperatorDropdownExpanded = false }
                            ) {
                                operatorsList.forEach { op ->
                                    DropdownMenuItem(
                                        text = { Text(op) },
                                        onClick = {
                                            selectedOperatorInLogin = op
                                            isOperatorDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        errorMessage?.let { error ->
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                            ) {
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        // Senha de Acesso
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it; errorMessage = null },
                            label = { Text("Senha de Acesso *") },
                            placeholder = { Text("Digite sua senha cadastrada") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Mostrar senha"
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth().testTag("login_password_input")
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                if (passwordInput.isBlank()) {
                                    errorMessage = "Por favor, digite a senha de acesso."
                                    return@Button
                                }
                                val success = onLoginWithPassword(passwordInput, selectedOperatorInLogin)
                                if (!success) {
                                    errorMessage = "Senha incorreta! Por favor, digite a senha cadastrada para o sistema."
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = "Entrar no Sistema",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Developer Signature and Symbol Card - EXCLUSIVO NA TELA DE LOGIN
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF3E8FF).copy(alpha = 0.95f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF7C3AED), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Símbolo Desenvolvedora Rúbia Elise",
                            tint = Color.White,
                            modifier = Modifier
                                .height(22.dp)
                                .width(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Criado e Desenvolvido por Rúbia Elise",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = Color(0xFF5B21B6)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Stars,
                                contentDescription = null,
                                tint = Color(0xFF9333EA),
                                modifier = Modifier
                                    .height(14.dp)
                                    .width(14.dp)
                            )
                        }
                        Text(
                            text = "Desenvolvedora de Software • rubiaelise@gmail.com",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF6B21A8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cloud Sync Notice
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.height(20.dp).width(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sincronização em Nuvem: Dados e operadores sincronizados com segurança no Firebase.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
