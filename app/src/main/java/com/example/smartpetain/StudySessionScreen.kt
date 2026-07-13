package com.example.smartpetain

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.NotificationsActive
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
import com.example.smartpetain.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private enum class PomodoroPhase {
    STUDY,
    BREAK,
    ALARM 
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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    var pomodoroService by remember { mutableStateOf<PomodoroService?>(null) }
    var isBound by remember { mutableStateOf(false) }

    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as PomodoroService.PomodoroBinder
                pomodoroService = binder.getService()
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                isBound = false
                pomodoroService = null
            }
        }
    }

    DisposableEffect(Unit) {
        val intent = Intent(context, PomodoroService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        onDispose {
            if (isBound) {
                context.unbindService(connection)
            }
        }
    }

    val serviceTimeLeft by (pomodoroService?.timeLeft ?: MutableStateFlow(0L)).collectAsState()
    val serviceIsRunning by (pomodoroService?.isRunning ?: MutableStateFlow(false)).collectAsState()
    val servicePhase by (pomodoroService?.phase ?: MutableStateFlow("STUDY")).collectAsState()

    var phase by remember { mutableStateOf(if (startAsBreak) PomodoroPhase.BREAK else PomodoroPhase.STUDY) }
    var timeLeft by remember(sessionDurationMinutes, startAsBreak) {
        mutableStateOf(if (startAsBreak) 5 * 60 * 1000L else sessionDurationMinutes * 60 * 1000L)
    }
    var isRunning by remember { mutableStateOf(false) }
    var lastMinuteReported by remember { mutableStateOf(0) }
    
    var selectedSoundUri by remember { mutableStateOf<Uri?>(null) }
    var selectedSoundName by remember { mutableStateOf("Predeterminado") }
    
    var showAppBlockerDialog by remember { mutableStateOf(false) }
    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var blockedPackages by remember { mutableStateOf<Set<String>>(emptySet()) }
    var hasUsagePermission by remember { mutableStateOf(false) }
    var hasOverlayPermission by remember { mutableStateOf(false) }

    LaunchedEffect(serviceTimeLeft, serviceIsRunning, servicePhase) {
        if (isBound) {
            timeLeft = serviceTimeLeft
            isRunning = serviceIsRunning
            val currentPhase = if (servicePhase == "STUDY") PomodoroPhase.STUDY else PomodoroPhase.BREAK
            if (phase != PomodoroPhase.ALARM) {
                phase = currentPhase
            }
            
            if (serviceTimeLeft == 0L && serviceIsRunning == false && (phase == PomodoroPhase.STUDY || phase == PomodoroPhase.BREAK)) {
                phase = PomodoroPhase.ALARM
            }

            if (phase == PomodoroPhase.STUDY && isRunning) {
                val totalElapsed = (sessionDurationMinutes * 60 * 1000L) - timeLeft
                val currentMinute = (totalElapsed / 60000).toInt()
                if (currentMinute > lastMinuteReported) {
                    onMinuteStudied(1)
                    lastMinuteReported = currentMinute
                }
            }
        }
    }

    val soundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            selectedSoundUri = uri
            val ringtone = RingtoneManager.getRingtone(context, uri)
            selectedSoundName = ringtone?.getTitle(context) ?: "Silencio"
            
            scope.launch {
                val profile = FirebaseManager.loadProfile()
                FirebaseManager.saveProfile(profile.copy(alarmSoundUri = uri?.toString()))
            }
        }
    }

    LaunchedEffect(Unit) {
        val profile = FirebaseManager.loadProfile()
        profile.alarmSoundUri?.let {
            selectedSoundUri = Uri.parse(it)
            val ringtone = RingtoneManager.getRingtone(context, selectedSoundUri)
            selectedSoundName = ringtone?.getTitle(context) ?: "Alarma"
        }
        
        hasUsagePermission = AppBlockerManager.hasUsageStatsPermission(context)
        hasOverlayPermission = AppBlockerManager.hasOverlayPermission(context)
        
        if (hasUsagePermission) {
            installedApps = AppBlockerManager.getInstalledApps(context)
            // By default, if blockedPackages is empty, block everything
            if (blockedPackages.isEmpty()) {
                blockedPackages = installedApps.map { it.packageName }.toSet()
            }
        }
    }

    fun startTimer(minutes: Int) {
        val intent = Intent(context, PomodoroService::class.java).apply {
            action = "START"
            putExtra("MINUTES", minutes)
            putExtra("PHASE", if (phase == PomodoroPhase.BREAK) "BREAK" else "STUDY")
            putExtra("SOUND_URI", selectedSoundUri?.toString())
            if (hasUsagePermission) {
                putExtra("BLOCKED_APPS", blockedPackages.toTypedArray())
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun pauseTimer() {
        context.startService(Intent(context, PomodoroService::class.java).apply { action = "PAUSE" })
    }

    fun resumeTimer() {
        context.startService(Intent(context, PomodoroService::class.java).apply { action = "RESUME" })
    }

    fun stopTimer() {
        if (phase == PomodoroPhase.STUDY) {
            val totalElapsedMillis = (sessionDurationMinutes * 60 * 1000L) - timeLeft
            val totalMinutes = (totalElapsedMillis / 60000).toInt()
            if (totalMinutes > 0) {
                onSessionFinished(totalMinutes)
            }
        }
        context.startService(Intent(context, PomodoroService::class.java).apply { action = "STOP" })
        lastMinuteReported = 0
        timeLeft = sessionDurationMinutes * 60 * 1000L
        isRunning = false
        phase = PomodoroPhase.STUDY
    }

    fun stopAlarmAndSwitch() {
        context.startService(Intent(context, PomodoroService::class.java).apply { action = "STOP_ALARM" })
        if (servicePhase == "STUDY") {
            onSessionFinished(sessionDurationMinutes)
            phase = PomodoroPhase.BREAK
            timeLeft = 5 * 60 * 1000L
        } else {
            onBreakFinished(5)
            phase = PomodoroPhase.STUDY
            timeLeft = sessionDurationMinutes * 60 * 1000L
        }
    }

    val minutes = (timeLeft / 1000) / 60
    val seconds = (timeLeft / 1000) % 60

    val themeColor = when (equippedPetName) {
        "Pompompurin" -> Color(0xFFE8A900)
        "Hello Kitty" -> Color(0xFFD4537E)
        else -> Color(0xFF5DA9FF)
    }
    
    val themeBg = when (equippedPetName) {
        "Pompompurin" -> Color(0xFFFFF9C4)
        "Hello Kitty" -> Color(0xFFFCE4EC)
        else -> Color(0xFFEAF7FF)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeBg)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { stopTimer(); onBack() }, 
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
            Row {
                IconButton(
                    onClick = { 
                        if (!hasUsagePermission) {
                            AppBlockerManager.openUsageStatsSettings(context)
                        } else if (!hasOverlayPermission) {
                            AppBlockerManager.openOverlaySettings(context)
                        } else {
                            if (installedApps.isEmpty()) {
                                installedApps = AppBlockerManager.getInstalledApps(context)
                                blockedPackages = installedApps.map { it.packageName }.toSet()
                            }
                            showAppBlockerDialog = true 
                        }
                    }, 
                    modifier = Modifier.size(46.dp).background(White.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.NotificationsActive, 
                        contentDescription = "Bloqueador", 
                        tint = if (hasUsagePermission && hasOverlayPermission) themeColor else Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Selecciona Alarma")
                            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedSoundUri)
                        }
                        soundLauncher.launch(intent)
                    }, 
                    modifier = Modifier.size(46.dp).background(White.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = "Música", tint = themeColor, modifier = Modifier.size(22.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = White.copy(alpha = 0.7f)
            ) {
                Text(
                    text = when(phase) {
                        PomodoroPhase.STUDY -> "¡Tú puedes! 💙"
                        PomodoroPhase.BREAK -> "¡A descansar! 🍮"
                        PomodoroPhase.ALARM -> "¡Tiempo agotado! 🔔"
                    },
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                    color = themeColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FredokaFont,
                    fontSize = 15.sp
                )
            }
            
            if (!isRunning && phase != PomodoroPhase.ALARM && (timeLeft == sessionDurationMinutes * 60 * 1000L || timeLeft == 5 * 60 * 1000L)) {
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

        Box(
            modifier = Modifier.size(320.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(280.dp)) {
                drawCircle(
                    color = themeColor.copy(alpha = 0.2f),
                    style = Stroke(
                        width = 4.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
                val totalMillis = if (phase == PomodoroPhase.BREAK) 5 * 60 * 1000L else sessionDurationMinutes * 60 * 1000L
                val sweepAngle = 360f * (timeLeft / totalMillis.toFloat())
                drawArc(
                    color = if (phase == PomodoroPhase.ALARM) Color.Red else themeColor,
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
                    color = if (phase == PomodoroPhase.ALARM) Color.Red else themeColor
                )
                Text(
                    text = when(phase) {
                        PomodoroPhase.STUDY -> "Enfoque"
                        PomodoroPhase.BREAK -> "Descanso"
                        PomodoroPhase.ALARM -> "ALERTA"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FredokaFont,
                    color = themeColor.copy(alpha = 0.8f)
                )
                Text(
                    text = "🔊 $selectedSoundName",
                    fontSize = 10.sp,
                    fontFamily = FredokaFont,
                    color = themeColor.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            val petRes = when (equippedPetName) {
                "Pompompurin" -> R.drawable.pompompurin
                "Hello Kitty" -> R.drawable.hello_kitty
                else -> R.drawable.cinnamoroll_echado
            }
            
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = if (equippedPetName == "Pompompurin") 65.dp else 90.dp)
            ) {
                Image(
                    painter = painterResource(id = petRes),
                    contentDescription = equippedPetName,
                    modifier = Modifier.size(if (equippedPetName == "Pompompurin") 200.dp else 240.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(25.dp))

        if (phase == PomodoroPhase.ALARM) {
            Button(
                onClick = { stopAlarmAndSwitch() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF007A))
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "DETENER ALARMA",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FredokaFont
                )
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(16.dp, RoundedCornerShape(28.dp), ambientColor = Color.LightGray.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                            color = Color(0xFF3B5998)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (phase == PomodoroPhase.STUDY) "Evita distracciones y aprovecha al máximo." else "Recupera energías para seguir después.",
                            fontSize = 13.sp,
                            fontFamily = FredokaFont,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Image(
                        painter = painterResource(id = R.drawable.productividad),
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (phase != PomodoroPhase.ALARM) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 36.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        onClick = { if (isRunning) pauseTimer() else resumeTimer() },
                        modifier = Modifier.size(68.dp),
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFFFFF9C4)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color(0xFFE8A900),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Text(if (isRunning) "Pausar" else "Seguir", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FredokaFont, color = themeColor, modifier = Modifier.padding(top = 8.dp))
                }

                Surface(
                    onClick = { if (timeLeft > 0 && !isRunning && (timeLeft == sessionDurationMinutes * 60 * 1000L || timeLeft == 5 * 60 * 1000L)) startTimer(if (phase == PomodoroPhase.BREAK) 5 else sessionDurationMinutes) else if (isRunning) pauseTimer() else resumeTimer() },
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

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        onClick = { stopTimer(); onBack() },
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

    if (showAppBlockerDialog) {
        AlertDialog(
            onDismissRequest = { showAppBlockerDialog = false },
            title = { Text("Selecciona aplicaciones a BLOQUEAR", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.height(400.dp)) {
                    LazyColumn {
                        items(installedApps) { app: AppInfo ->
                            val isBlocked = blockedPackages.contains(app.packageName)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        blockedPackages = if (isBlocked) {
                                            blockedPackages - app.packageName
                                        } else {
                                            blockedPackages + app.packageName
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isBlocked,
                                    onCheckedChange = { checked ->
                                        blockedPackages = if (checked == true) {
                                            blockedPackages + app.packageName
                                        } else {
                                            blockedPackages - app.packageName
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                // App Icon using Coil
                                coil3.compose.AsyncImage(
                                    model = app.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = app.label, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showAppBlockerDialog = false }) {
                    Text("Aceptar")
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = White
        )
    }
}
