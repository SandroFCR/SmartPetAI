package com.example.smartpetain

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.example.smartpetain.ui.theme.*

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Google Sign In
    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { onAuthSuccess() }
                .addOnFailureListener { errorMsg = "Error con Google: ${it.message}" }
        } catch (e: ApiException) {
            errorMsg = "Error con Google: ${e.message}"
        }
    }

    fun loginWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        googleLauncher.launch(client.signInIntent)
    }

    fun submitEmail() {
        if (email.isBlank() || password.isBlank()) {
            errorMsg = "Completa todos los campos"
            return
        }
        isLoading = true
        errorMsg = ""
        if (isLogin) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { isLoading = false; onAuthSuccess() }
                .addOnFailureListener { isLoading = false; errorMsg = "Error: ${it.message}" }
        } else {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { isLoading = false; onAuthSuccess() }
                .addOnFailureListener { isLoading = false; errorMsg = "Error: ${it.message}" }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🐾 SmartPetAI", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
        Text(
            text = "Tu compañero de estudio",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        // Tabs Login / Registro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Iniciar sesión" to true, "Registrarse" to false).forEach { (label, value) ->
                Button(
                    onClick = { isLogin = value; errorMsg = "" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLogin == value) PurplePrimary else PurpleLight
                    )
                ) {
                    Text(
                        label,
                        color = if (isLogin == value) White else PurplePrimary,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        if (errorMsg.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { submitEmail() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = White, modifier = Modifier.size(20.dp))
            } else {
                Text(
                    if (isLogin) "Iniciar sesión" else "Crear cuenta",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(modifier = Modifier.weight(1f), color = PurpleLight)
            Text("  o  ", color = TextSecondary, fontSize = 13.sp)
            Divider(modifier = Modifier.weight(1f), color = PurpleLight)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { loginWithGoogle() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Text("🔵  Continuar con Google", fontSize = 15.sp, color = TextPrimary)
        }
    }
}
