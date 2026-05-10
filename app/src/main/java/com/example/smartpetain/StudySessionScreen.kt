package com.example.smartpetain

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartpetain.ui.theme.AmberPrimary
import com.example.smartpetain.ui.theme.Background
import com.example.smartpetain.ui.theme.PinkPrimary
import com.example.smartpetain.ui.theme.PurpleLight
import com.example.smartpetain.ui.theme.PurplePrimary
import com.example.smartpetain.ui.theme.TextPrimary
import com.example.smartpetain.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun StudySessionScreen(
    onBack: () -> Unit,
    sessionDurationMinutes: Int = 25,
    onPomodoroChanged: (Int) -> Unit = {},
    onMinuteStudied: (Int) -> Unit = {},
    onSessionFinished: (Int) -> Unit = {},
    onBreakTaken: () -> Unit = {}
) {
    val sessionDurationSeconds = sessionDurationMinutes * 60
    var timeLeft by remember(sessionDurationMinutes) { mutableStateOf(sessionDurationSeconds) }
    var isRunning by remember { mutableStateOf(false) }
    var sessionTime by remember { mutableStateOf(0) }
    var lastMinuteReported by remember { mutableStateOf(0) }
    var sessionFinished by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val progress = 1f - (timeLeft / sessionDurationSeconds.toFloat())

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

            val currentMinute = sessionTime / 60
            if (currentMinute > lastMinuteReported) {
                lastMinuteReported = currentMinute
                onMinuteStudied(1)
            }
        }

        if (timeLeft == 0 && !sessionFinished) {
            val studiedMinutes = sessionTime / 60
            isRunning = false
            sessionFinished = true
            SmartPetNotificationManager.sendBreakNotification(context)
            onSessionFinished(studiedMinutes)
            onBreakTaken()
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val character = when {
        sessionTime >= sessionDurationSeconds -> "Cinnamoroll esta muy orgulloso!"
        sessionTime >= 10 * 60 -> "Cinnamoroll: Sigue asi, vas genial!"
        else -> "Cinnamoroll: Tu puedes! Concentrate."
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
                Text(text = "<- Volver", color = PurplePrimary, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PurpleLight),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = character,
                    fontSize = 15.sp,
                    color = PurplePrimary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Duracion Pomodoro", fontSize = 13.sp, color = TextSecondary)
                        Text(
                            "$sessionDurationMinutes minutos",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            enabled = !isRunning && sessionDurationMinutes > 5,
                            onClick = { onPomodoroChanged(sessionDurationMinutes - 5) }
                        ) {
                            Text("-", fontSize = 22.sp, color = PurplePrimary, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            enabled = !isRunning && sessionDurationMinutes < 60,
                            onClick = { onPomodoroChanged(sessionDurationMinutes + 5) }
                        ) {
                            Text("+", fontSize = 22.sp, color = PurplePrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

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
                    text = if (isRunning) "En sesion" else "Listo para empezar",
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
                    text = if (isRunning) "Pausar" else "Iniciar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    isRunning = false
                    timeLeft = sessionDurationSeconds
                    sessionTime = 0
                    lastMinuteReported = 0
                    sessionFinished = false
                    onBreakTaken()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PinkPrimary)
            ) {
                Text(text = "Terminar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Tiempo en sesion: %02d:%02d".format(sessionTime / 60, sessionTime % 60),
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}
