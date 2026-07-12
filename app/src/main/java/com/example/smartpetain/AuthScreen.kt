package com.example.smartpetain

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.example.smartpetain.ui.theme.*
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.launch

private enum class AuthStep {
    START,
    LOGIN,
    REGISTER_NAME,
    REGISTER_CREDENTIALS,
    VERIFY_EMAIL
}

// Colores Kawaii (Celeste & Blanco)
private val KawaiiBlue = Color(0xFF5DA9FF)
private val KawaiiBlueLight = Color(0xFFF2FAFF)
private val KawaiiBackground = Color(0xFFFFFFFF)
private val KawaiiTextDark = Color(0xFF2C2C2E)
private val KawaiiTextSecondary = Color(0xFF8E8E93)

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf(AuthStep.START) }
    
    // Form States
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var errorMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    fun getErrorMessage(e: Exception): String {
        if (e is FirebaseAuthException) {
            return when (e.errorCode) {
                "ERROR_INVALID_EMAIL" -> "El formato del correo no es válido."
                "ERROR_WRONG_PASSWORD" -> "La contraseña es incorrecta."
                "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con este correo."
                "ERROR_USER_DISABLED" -> "Esta cuenta ha sido deshabilitada."
                "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Inténtalo más tarde."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Este correo ya está registrado."
                "ERROR_WEAK_PASSWORD" -> "La contraseña es muy débil (mínimo 6 caracteres)."
                "ERROR_NETWORK_REQUEST_FAILED" -> "Error de red. Verifica tu conexión."
                else -> "Error: ${e.localizedMessage}"
            }
        }
        return e.localizedMessage ?: "Ocurrió un error inesperado."
    }

    fun handleAuth() {
        errorMsg = ""
        if (currentStep == AuthStep.LOGIN) {
            if (email.isBlank() || password.isBlank()) {
                errorMsg = "Completa todos los campos"
                return
            }
            isLoading = true
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { 
                    isLoading = false
                    onAuthSuccess() 
                }
                .addOnFailureListener { 
                    isLoading = false
                    errorMsg = getErrorMessage(it) 
                }
        } else if (currentStep == AuthStep.REGISTER_CREDENTIALS) {
            if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                errorMsg = "Completa todos los campos"
                return
            }
            if (password != confirmPassword) {
                errorMsg = "Las contraseñas no coinciden"
                return
            }
            isLoading = true
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    scope.launch {
                        try {
                            // Guardar perfil
                            FirebaseManager.saveProfile(UserProfile(name = "$firstName $lastName"))
                            // Enviar verificación
                            result.user?.sendEmailVerification()
                            currentStep = AuthStep.VERIFY_EMAIL
                        } catch (e: Exception) {
                            errorMsg = "Error al crear perfil: ${e.localizedMessage}"
                        } finally {
                            isLoading = false
                        }
                    }
                }
                .addOnFailureListener { 
                    isLoading = false
                    errorMsg = getErrorMessage(it) 
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KawaiiBackground)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (currentStep != AuthStep.START) {
            IconButton(
                onClick = { 
                    currentStep = when(currentStep) {
                        AuthStep.LOGIN -> AuthStep.START
                        AuthStep.REGISTER_NAME -> AuthStep.START
                        AuthStep.REGISTER_CREDENTIALS -> AuthStep.REGISTER_NAME
                        else -> AuthStep.START
                    }
                    errorMsg = ""
                },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = KawaiiBlue)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "Bienvenido a",
            color = KawaiiTextDark,
            fontSize = 20.sp,
            fontWeight = FontWeight.Light,
            fontFamily = FredokaFont
        )
        Text(
            text = "smartpet",
            color = KawaiiBlue,
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FredokaFont
        )
        
        Spacer(modifier = Modifier.height(40.dp))

        when (currentStep) {
            AuthStep.START -> {
                KawaiiButton(text = "Inicia sesión", onClick = { currentStep = AuthStep.LOGIN })
                Spacer(modifier = Modifier.height(16.dp))
                KawaiiButton(text = "Crear cuenta", onClick = { currentStep = AuthStep.REGISTER_NAME }, outline = true)
            }

            AuthStep.LOGIN -> {
                KawaiiTextField(value = email, onValueChange = { email = it }, label = "Correo electrónico")
                Spacer(modifier = Modifier.height(16.dp))
                KawaiiTextField(
                    value = password, 
                    onValueChange = { password = it }, 
                    label = "Contraseña",
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onVisibilityChange = { passwordVisible = !passwordVisible }
                )
                
                Text(
                    text = "¿Olvidaste tu contraseña?",
                    color = KawaiiBlue,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .clickable {
                            if (email.isNotBlank()) {
                                auth.sendPasswordResetEmail(email)
                                errorMsg = "Se ha enviado un correo para restablecer tu contraseña."
                            } else {
                                errorMsg = "Ingresa tu correo para restablecer la contraseña."
                            }
                        }
                )

                KawaiiButton(text = "Ingresar", onClick = { handleAuth() }, isLoading = isLoading)
                
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "¿Aún no tienes cuenta?",
                    color = KawaiiTextSecondary,
                    fontSize = 14.sp
                )
                Text(
                    text = "Crear cuenta",
                    color = KawaiiBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { currentStep = AuthStep.REGISTER_NAME }
                )
            }

            AuthStep.REGISTER_NAME -> {
                Text(text = "Crea tu cuenta", color = KawaiiTextDark, fontSize = 18.sp, modifier = Modifier.padding(bottom = 24.dp))
                KawaiiTextField(value = firstName, onValueChange = { firstName = it }, label = "Nombre")
                Spacer(modifier = Modifier.height(16.dp))
                KawaiiTextField(value = lastName, onValueChange = { lastName = it }, label = "Apellidos")
                Spacer(modifier = Modifier.height(24.dp))
                KawaiiButton(text = "Continuar", onClick = { 
                    if (firstName.isNotBlank() && lastName.isNotBlank()) {
                        currentStep = AuthStep.REGISTER_CREDENTIALS 
                    } else {
                        errorMsg = "Ingresa tu nombre y apellidos"
                    }
                })
            }

            AuthStep.REGISTER_CREDENTIALS -> {
                Text(text = "Escribe una clave", color = KawaiiBlue, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Úsala para mantener segura tu información.",
                    color = KawaiiTextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                    textAlign = TextAlign.Center
                )
                KawaiiTextField(value = email, onValueChange = { email = it }, label = "Correo electrónico")
                Spacer(modifier = Modifier.height(16.dp))
                KawaiiTextField(
                    value = password, 
                    onValueChange = { password = it }, 
                    label = "Contraseña",
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onVisibilityChange = { passwordVisible = !passwordVisible }
                )
                Spacer(modifier = Modifier.height(16.dp))
                KawaiiTextField(
                    value = confirmPassword, 
                    onValueChange = { confirmPassword = it }, 
                    label = "Confirmar contraseña",
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onVisibilityChange = { passwordVisible = !passwordVisible }
                )
                Spacer(modifier = Modifier.height(24.dp))
                KawaiiButton(text = "Crear cuenta", onClick = { handleAuth() }, isLoading = isLoading)
            }

            AuthStep.VERIFY_EMAIL -> {
                Icon(Icons.Default.Email, contentDescription = null, tint = KawaiiBlue, modifier = Modifier.size(64.dp))
                Text(
                    text = "¡Casi listo!",
                    color = KawaiiTextDark,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "Hemos enviado un enlace de verificación a $email. Por favor, revisa tu bandeja de entrada para activar tu cuenta.",
                    color = KawaiiTextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                KawaiiButton(text = "Ya verifiqué mi correo", onClick = { 
                    auth.currentUser?.reload()?.addOnSuccessListener {
                        if (auth.currentUser?.isEmailVerified == true) {
                            onAuthSuccess()
                        } else {
                            errorMsg = "Aún no has verificado tu correo."
                        }
                    }
                })
                
                TextButton(onClick = { 
                    auth.currentUser?.sendEmailVerification()
                    errorMsg = "Correo de verificación reenviado."
                }) {
                    Text("Reenviar correo", color = KawaiiBlue)
                }
            }
        }

        if (errorMsg.isNotEmpty()) {
            Text(
                text = errorMsg,
                color = if (errorMsg.contains("enviado")) Color(0xFF1D9E75) else Color.Red,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun KawaiiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onVisibilityChange: () -> Unit = {}
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = KawaiiTextSecondary) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(25.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = KawaiiBlue,
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
            focusedContainerColor = KawaiiBlueLight.copy(alpha = 0.3f),
            unfocusedContainerColor = Color.Transparent,
            cursorColor = KawaiiBlue,
            focusedTextColor = KawaiiTextDark,
            unfocusedTextColor = KawaiiTextDark
        ),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = onVisibilityChange) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = KawaiiTextSecondary
                    )
                }
            }
        },
        singleLine = true,
        textStyle = TextStyle(color = KawaiiTextDark)
    )
}

@Composable
private fun KawaiiButton(
    text: String,
    onClick: () -> Unit,
    outline: Boolean = false,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (outline) Color.Transparent else KawaiiBlue,
            contentColor = if (outline) KawaiiBlue else Color.White
        ),
        border = if (outline) androidx.compose.foundation.BorderStroke(1.dp, KawaiiBlue) else null,
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = if (outline) KawaiiBlue else Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
            Text(text = text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
