package com.example.smartpetain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.smartpetain.ui.theme.*
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SmartPetNotificationManager.createChannels(this)

        setContent {
            SmartPetAInTheme {
                val auth = FirebaseAuth.getInstance()
                var isLoggedIn by remember {
                    mutableStateOf(auth.currentUser != null)
                }

                if (isLoggedIn) {
                    AppNavigator()
                } else {
                    AuthScreen(onAuthSuccess = { isLoggedIn = true })
                }
            }
        }
    }
}

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun AppNavigator() {
    val scope = rememberCoroutineScope()

    var currentScreen     by remember { mutableStateOf("dashboard") }
    var totalStudyMinutes by remember { mutableStateOf(0) }
    var minutesSinceBreak by remember { mutableStateOf(0) }
    var pendingTasks      by remember { mutableStateOf(4) }
    var completedTasks    by remember { mutableStateOf(0) }
    var pomodoroDuration  by remember { mutableStateOf(25) }

    LaunchedEffect(Unit) {
        val (_, pomodoro) = FirebaseManager.loadProfile()
        pomodoroDuration = pomodoro
    }

    val activeCharacter = CharacterEngine.getCharacter(
        studyMinutes      = totalStudyMinutes,
        minutesSinceBreak = minutesSinceBreak,
        pendingTasks      = pendingTasks,
        completedTasks    = completedTasks
    )

    val navItems = listOf(
        NavItem("Inicio",       Icons.Filled.Home,     "dashboard"),
        NavItem("Tareas",       Icons.Filled.List,     "tasks"),
        NavItem("Estadísticas", Icons.Filled.Star,     "stats"),
        NavItem("Mascotas",     Icons.Filled.Favorite, "characters"),
        NavItem("Perfil",       Icons.Filled.Person,   "profile")
    )

    if (currentScreen == "session") {
        StudySessionScreen(
            onBack                 = { currentScreen = "dashboard" },
            sessionDurationMinutes = pomodoroDuration,
            onMinuteStudied        = { minutes ->
                totalStudyMinutes += minutes
                minutesSinceBreak += minutes
            },
            onBreakTaken = {
                if (minutesSinceBreak >= 1) {
                    scope.launch {
                        FirebaseManager.saveStudySession(
                            minutes   = minutesSinceBreak,
                            character = activeCharacter.name
                        )
                    }
                }
                minutesSinceBreak = 0
            }
        )
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = White,
                tonalElevation = 0.dp
            ) {
                navItems.forEach { item ->
                    val isSelected = currentScreen == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick  = { currentScreen = item.route },
                        icon = {
                            Icon(item.icon, contentDescription = item.label)
                        },
                        label = {
                            Text(item.label, fontSize = 11.sp)
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = PurplePrimary,
                            selectedTextColor   = PurplePrimary,
                            indicatorColor      = PurpleLight,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                "dashboard" -> DashboardScreen(
                    onStartSession    = { currentScreen = "session" },
                    activeCharacter   = activeCharacter,
                    totalStudyMinutes = totalStudyMinutes,
                    pendingTasks      = pendingTasks
                )
                "tasks" -> TasksScreen(
                    onBack = { currentScreen = "dashboard" },
                    onTasksChanged = { pending, completed ->
                        pendingTasks   = pending
                        completedTasks = completed
                    }
                )
                "stats" -> StatsScreen(
                    onBack            = { currentScreen = "dashboard" },
                    totalStudyMinutes = totalStudyMinutes
                )
                "characters" -> CharactersScreen(
                    totalStudyMinutes = totalStudyMinutes,
                    completedTasks    = completedTasks
                )
                "profile" -> ProfileScreen(
                    totalStudyMinutes = totalStudyMinutes,
                    completedTasks    = completedTasks,
                    onPomodoroChanged = { pomodoroDuration = it }
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    onStartSession: () -> Unit,
    activeCharacter: Character,
    totalStudyMinutes: Int,
    pendingTasks: Int
) {
    val hours   = totalStudyMinutes / 60
    val minutes = totalStudyMinutes % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "¡Hola! 💙",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Lista para lograr tus metas hoy",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        val cardColor = when (activeCharacter.color) {
            "amber" -> AmberLight
            "pink"  -> PinkLight
            else    -> PurpleLight
        }
        val textColor = when (activeCharacter.color) {
            "amber" -> AmberPrimary
            "pink"  -> PinkPrimary
            else    -> PurplePrimary
        }

        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text       = "${activeCharacter.emoji} ${activeCharacter.name}",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = textColor
                )
                Text(
                    text     = activeCharacter.role,
                    fontSize = 12.sp,
                    color    = textColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text     = activeCharacter.message,
                    fontSize = 14.sp,
                    color    = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = textColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text       = "● ${activeCharacter.role.uppercase()}",
                        fontSize   = 12.sp,
                        color      = textColor,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Estado actual", fontSize = 13.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m",
                            fontSize   = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary
                        )
                        Text("⏱ Tiempo estudiado", fontSize = 12.sp, color = TextSecondary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text       = "$pendingTasks",
                            fontSize   = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary
                        )
                        Text("📋 Tareas pendientes", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick  = onStartSession,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape    = RoundedCornerShape(16.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
        ) {
            Text(
                text       = "▶  Iniciar sesión de estudio",
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}