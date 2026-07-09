package com.example.smartpetain

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartpetain.ui.theme.FredokaFont
import com.example.smartpetain.ui.theme.PurpleLight
import com.example.smartpetain.ui.theme.PurplePrimary
import com.example.smartpetain.ui.theme.White
import kotlinx.coroutines.delay

private enum class PomodoroPhase {
    STUDY,
    BREAK
}

@Composable
fun StudySessionScreen(
    onBack: () -> Unit,
    equippedPetName: String = "Cinnamoroll",
    sessionDurationMinutes: Int = 25,
    startAsBreak: Boolean = false,
    onPomodoroChanged: (Int) -> Unit = {},
    onMinuteStudied: (Int) -> Unit = {},
    onSessionFinished: (Int) -> Unit = {},
    onBreakFinished: (Int) -> Unit = {}
) {
    val breakDurationSeconds = 5 * 60
    val studyDurationSeconds = sessionDurationMinutes * 60
    
    var phase by remember { mutableStateOf(if (startAsBreak) PomodoroPhase.BREAK else PomodoroPhase.STUDY) }
    var timeLeft by remember(sessionDurationMinutes, startAsBreak) {
        mutableStateOf(if (startAsBreak) breakDurationSeconds else studyDurationSeconds)
    }
    var isRunning by remember { mutableStateOf(startAsBreak) }
    var sessionTime by remember { mutableStateOf(0) }
    var lastMinuteReported by remember { mutableStateOf(0) }
    
    val context = LocalContext.current
    val totalSeconds = if (phase == PomodoroPhase.STUDY) studyDurationSeconds else breakDurationSeconds
    val progress = timeLeft / totalSeconds.toFloat()

    // Theme based on mascot (consistency with Dashboard)
    val themeColor = when (equippedPetName) {
        "Pompompurin" -> Color(0xFFE8A900)
        "Hello Kitty" -> Color(0xFFD4537E)
        else -> Color(0xFF5DA9FF) // Cinnamoroll Blue
    }
    
    val themeBg = when (equippedPetName) {
        "Pompompurin" -> Color(0xFFFFF9C4)
        "Hello Kitty" -> Color(0xFFFCE4EC)
        else -> Color(0xFFEAF7FF) // Light blue as in image_3
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(isRunning, phase) {
        while (isRunning) {
            delay(1000L)
            if (timeLeft > 0) {
                timeLeft--
                if (phase == PomodoroPhase.STUDY) {
                    sessionTime++
                    val currentMinute = sessionTime / 60
                    if (currentMinute > lastMinuteReported) {
                        lastMinuteReported = currentMinute
                        onMinuteStudied(1)
                    }
                }
            }

            if (timeLeft == 0) {
                if (phase == PomodoroPhase.STUDY) {
                    val studiedMinutes = sessionTime / 60
                    SmartPetNotificationManager.sendBreakNotification(context)
                    onSessionFinished(studiedMinutes)
                    phase = PomodoroPhase.BREAK
                    timeLeft = breakDurationSeconds
                    sessionTime = 0
                    lastMinuteReported = 0
                } else {
                    SmartPetNotificationManager.sendStudyNotification(context)
                    onBreakFinished(5)
                    phase = PomodoroPhase.STUDY
                    timeLeft = studyDurationSeconds
                    isRunning = false
                }
            }
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeBg)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar with circular backgrounds for a clean look
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack, 
                modifier = Modifier.size(46.dp).background(White.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = themeColor, modifier = Modifier.size(22.dp))
            }
            Text(
                text = "Sesión de estudio",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FredokaFont,
                color = themeColor
            )
            IconButton(
                onClick = { }, 
                modifier = Modifier.size(46.dp).background(White.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = "Música", tint = themeColor, modifier = Modifier.size(22.dp))
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Motivational Chip & Duration Adjustment Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Motivational Chip
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = White.copy(alpha = 0.7f)
            ) {
                Text(
                    text = if (phase == PomodoroPhase.STUDY) "¡Tú puedes! 💙" else "¡Merecido descanso! 🍮",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                    color = themeColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FredokaFont,
                    fontSize = 15.sp
                )
            }
            
            // Duration adjustment buttons if not running
            if (!isRunning && phase == PomodoroPhase.STUDY && sessionTime == 0) {
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = themeColor.copy(alpha = 0.15f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                        IconButton(onClick = { if (sessionDurationMinutes > 5) onPomodoroChanged(sessionDurationMinutes - 5) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = themeColor, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { if (sessionDurationMinutes < 60) onPomodoroChanged(sessionDurationMinutes + 5) }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = themeColor, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Timer Area
        Box(
            modifier = Modifier.size(320.dp),
            contentAlignment = Alignment.Center
        ) {
            // Circular Progress with dots effect
            Canvas(modifier = Modifier.size(280.dp)) {
                // Outer dotted circle
                drawCircle(
                    color = themeColor.copy(alpha = 0.2f),
                    style = Stroke(
                        width = 4.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
                
                // Active progress arc
                val sweepAngle = 360f * progress
                drawArc(
                    color = themeColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%02d:%02d".format(minutes, seconds),
                    fontSize = 76.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FredokaFont,
                    color = themeColor
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.productividad),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (phase == PomodoroPhase.STUDY) themeColor else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (phase == PomodoroPhase.STUDY) "Enfoque" else "Descanso",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FredokaFont,
                        color = themeColor.copy(alpha = 0.8f)
                    )
                }
            }

            // Mascot Image
            val petRes = R.drawable.cinnamoroll_echado
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 90.dp) // Adjusted for better framing
            ) {
                Image(
                    painter = painterResource(id = petRes),
                    contentDescription = "Cinnamoroll",
                    modifier = Modifier.size(240.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(25.dp)) // Moved card up significantly

        // Info Card with improved framing
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp) // Adjusted padding
                .shadow(16.dp, RoundedCornerShape(28.dp), ambientColor = Color.LightGray.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (phase == PomodoroPhase.STUDY) "Estás en modo enfoque" else "Estás en modo descanso",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FredokaFont,
                        color = Color(0xFF3B5998) // Darker blue for contrast as in image_2
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (phase == PomodoroPhase.STUDY) "Evita distracciones y aprovecha al máximo." else "Recupera energías para seguir después.",
                        fontSize = 14.sp,
                        fontFamily = FredokaFont,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                // Plant Icon (using productivity drawable as reference)
                Image(
                    painter = painterResource(id = R.drawable.productividad),
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Controls Area (Redesigned side squircle buttons and solid central circle)
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 36.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pausar Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier.size(68.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFFFFF9C4)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Pausar",
                            tint = Color(0xFFE8A900),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Text("Pausar", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FredokaFont, color = themeColor, modifier = Modifier.padding(top = 8.dp))
            }

            // Big Central Play/Pause
            Surface(
                onClick = { isRunning = !isRunning },
                modifier = Modifier.size(105.dp).shadow(14.dp, CircleShape),
                shape = CircleShape,
                color = themeColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = White
                    )
                }
            }

            // Terminar Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    onClick = {
                        isRunning = false
                        phase = PomodoroPhase.STUDY
                        timeLeft = studyDurationSeconds
                        sessionTime = 0
                    },
                    modifier = Modifier.size(68.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFFFFE0E0)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Terminar",
                            tint = Color(0xFFD4537E),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Text("Terminar", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FredokaFont, color = themeColor, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
