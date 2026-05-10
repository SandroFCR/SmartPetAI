package com.example.smartpetain

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.example.smartpetain.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
@Composable
fun StudySessionScreen(
    onBack: () -> Unit,
    onMinuteStudied: (Int) -> Unit = {},
    onBreakTaken: () -> Unit = {}
) {
    var timeLeft by remember(sessionDurationMinutes) { mutableStateOf(sessionDurationMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var sessionTime by remember { mutableStateOf(0) }
    var lastMinuteReported by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sessionFinished by remember { mutableStateOf(false) }
    val progress = 1f - (timeLeft / (sessionDurationMinutes * 60f))

// Pedir permiso notificaciones Android 13+
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(isRunning) {
        while (isRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft--
            sessionTime++

            // Reportar cada minuto completo
            val currentMinute = sessionTime / 60
            if (currentMinute > lastMinuteReported) {
                lastMinuteReported = currentMinute
                onMinuteStudied(1)
            }
        }
        if (timeLeft == 0 && !sessionFinished) {
            isRunning = false
            sessionFinished = true
            SmartPetNotificationManager.sendBreakNotification(context)
            scope.launch {
                FirebaseManager.saveStudySession(sessionTime / 60)
            }
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val progress = 1f - (timeLeft / (25f * 60f))

    val character = when {
        sessionTime >= 25 * 60 -> "🌟 ¡Cinnamoroll está muy orgulloso!"
        sessionTime >= 10 * 60 -> "💙 Cinnamoroll: ¡Sigue así, vas genial!"
        else -> "✨ Cinnamoroll: ¡Tú puedes! Concéntrate 🌱"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onBack) {
                Text(
                    text = "← Volver",
                    color = PurplePrimary,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PurpleLight),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Text(
                text = character,
                fontSize = 15.sp,
                color = PurplePrimary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(220.dp),
                color = PurplePrimary,
                trackColor = PurpleLight,
                strokeWidth = 10.dp
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%02d:%02d".format(minutes, seconds),
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (isRunning) "En sesión" else "Listo para empezar",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { isRunning = !isRunning },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) AmberPrimary else PurplePrimary
                )
            ) {
                Text(
                    text = if (isRunning) "⏸ Pausar" else "▶ Iniciar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    isRunning = false
                    timeLeft = 25 * 60
                    sessionTime = 0
                    lastMinuteReported = 0
                    onBreakTaken()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
            ) {
                Text(
                    text = "⏹ Terminar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tiempo en sesión: %02d:%02d".format(sessionTime / 60, sessionTime % 60),
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}