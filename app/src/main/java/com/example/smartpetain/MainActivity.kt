package com.example.smartpetain

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartpetain.ui.theme.DashboardBlueBackground
import com.example.smartpetain.ui.theme.DashboardBlueLight
import com.example.smartpetain.ui.theme.DashboardBluePrimary
import com.example.smartpetain.ui.theme.DashboardBlueSecondary
import com.example.smartpetain.ui.theme.DashboardPinkAccent
import com.example.smartpetain.ui.theme.DashboardPinkSoft
import com.example.smartpetain.ui.theme.FredokaFont
import com.example.smartpetain.ui.theme.PurpleLight
import com.example.smartpetain.ui.theme.PurplePrimary
import com.example.smartpetain.ui.theme.SmartPetAInTheme
import com.example.smartpetain.ui.theme.TealPrimary
import com.example.smartpetain.ui.theme.TextPrimary
import com.example.smartpetain.ui.theme.TextSecondary
import com.example.smartpetain.ui.theme.White
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

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
                    AppNavigator(
                        onLogout = {
                            auth.signOut()
                            isLoggedIn = false
                        }
                    )
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
fun AppNavigator(
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf("dashboard") }
    var totalStudyMinutes by remember { mutableStateOf(0) }
    var minutesSinceBreak by remember { mutableStateOf(0) }
    var pendingTasks by remember { mutableStateOf(0) }
    var completedTasks by remember { mutableStateOf(0) }
    var pomodoroDuration by remember { mutableStateOf(25) }
    var userName by remember { mutableStateOf("Estudiante") }

    // Gamificación y Mascotas
    var equippedPet by remember { mutableStateOf("Cinnamoroll") }
    var petStatsList by remember { mutableStateOf<List<PetStats>>(emptyList()) }
    var tasksList by remember { mutableStateOf<List<Task>>(emptyList()) }
    var todayCompletedMissions by remember { mutableStateOf<List<Int>>(emptyList()) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // Flags de navegación por misiones
    var startSessionAsBreak by remember { mutableStateOf(false) }
    var initialTasksAddOpen by remember { mutableStateOf(false) }
    var initialTasksSortBy by remember { mutableStateOf("") }

    // Carga inicial y persistente
    LaunchedEffect(Unit, refreshTrigger) {
        val profile = FirebaseManager.loadProfile()
        userName = profile.name.ifBlank { "Estudiante" }
        pomodoroDuration = profile.pomodoroDuration
        equippedPet = profile.equippedPet
        
        tasksList = FirebaseManager.loadTasks()
        pendingTasks = tasksList.count { !it.isCompleted }
        completedTasks = tasksList.count { it.isCompleted }
        
        petStatsList = FirebaseManager.loadPets()
        totalStudyMinutes = FirebaseManager.loadTotalStudyMinutes()

        val weekStats = FirebaseManager.loadWeekStats()
        val today = currentDayLabel()
        todayCompletedMissions = weekStats.find { it.day == today }?.completedMissions ?: emptyList()
    }

    val activePetStats = petStatsList.find { it.name == equippedPet } ?: PetStats(equippedPet)

    val activeCharacter = CharacterEngine.getCharacter(
        equippedPetName = equippedPet,
        studyMinutes = totalStudyMinutes,
        minutesSinceBreak = minutesSinceBreak,
        pendingTasks = pendingTasks,
        completedTasks = completedTasks,
        petLevel = activePetStats.level
    )

    val navItems = listOf(
        NavItem("Inicio", Icons.Filled.Home, "dashboard"),
        NavItem("Tareas", Icons.AutoMirrored.Filled.List, "tasks"),
        NavItem("Estadísticas", Icons.Filled.Star, "stats"),
        NavItem("Mascotas", Icons.Filled.Favorite, "characters"),
        NavItem("Perfil", Icons.Filled.Person, "profile")
    )

    if (currentScreen == "session") {
        StudySessionScreen(
            onBack = { 
                currentScreen = "dashboard"
                startSessionAsBreak = false
            },
            sessionDurationMinutes = pomodoroDuration,
            startAsBreak = startSessionAsBreak,
            onPomodoroChanged = { duration ->
                pomodoroDuration = duration
                scope.launch {
                    FirebaseManager.savePomodoroDuration(duration)
                }
            },
            onMinuteStudied = { minutes ->
                totalStudyMinutes += minutes
                minutesSinceBreak += minutes
                scope.launch {
                    FirebaseManager.saveDailyStats(minutes)
                }
            },
            onSessionFinished = { minutes ->
                scope.launch {
                    FirebaseManager.saveStudySession(
                        minutes = minutes,
                        character = activeCharacter.name,
                        updateDailyStats = false
                    )
                    refreshTrigger++
                }
            },
            onBreakFinished = { minutes ->
                minutesSinceBreak = 0
                scope.launch {
                    FirebaseManager.saveBreakMinutes(minutes)
                    refreshTrigger++
                }
            }
        )
        return
    }

    Scaffold(
        bottomBar = {
            KawaiiBottomBar(
                navItems = navItems,
                currentScreen = currentScreen,
                onScreenSelected = { route -> currentScreen = route }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                "dashboard" -> DashboardScreen(
                    onStartSession = { currentScreen = "session" },
                    activeCharacter = activeCharacter,
                    totalStudyMinutes = totalStudyMinutes,
                    pendingTasks = pendingTasks,
                    userName = userName,
                    equippedPet = equippedPet
                )

                "tasks" -> TasksScreen(
                    tasks = tasksList,
                    onBack = { currentScreen = "dashboard" },
                    initialOpenAddDialog = initialTasksAddOpen,
                    initialSortBy = initialTasksSortBy,
                    onTasksChanged = { newList ->
                        tasksList = newList
                        pendingTasks = newList.count { !it.isCompleted }
                        completedTasks = newList.count { it.isCompleted }
                        initialTasksAddOpen = false
                        initialTasksSortBy = ""
                        // Forzamos refresco de XP si se completó algo
                        scope.launch {
                            refreshTrigger++
                        }
                    },
                    equippedPet = equippedPet
                )

                "stats" -> StatsScreen(
                    onBack = { currentScreen = "dashboard" },
                    totalStudyMinutes = totalStudyMinutes
                )

                "characters" -> CharactersScreen(
                    totalStudyMinutes = totalStudyMinutes,
                    completedTasks = completedTasks,
                    pendingTasks = pendingTasks,
                    petStatsList = petStatsList,
                    equippedPetName = equippedPet,
                    todayCompletedMissions = todayCompletedMissions,
                    onEquipPet = { petName ->
                        equippedPet = petName
                        scope.launch {
                            FirebaseManager.saveEquippedPet(petName)
                        }
                    },
                    onMissionAction = { action ->
                        when (action) {
                            "add_task" -> {
                                initialTasksAddOpen = true
                                currentScreen = "tasks"
                            }
                            "sort_priority" -> {
                                initialTasksSortBy = "priority"
                                currentScreen = "tasks"
                                scope.launch {
                                    FirebaseManager.completeDailyMission("Hello Kitty", 302, 10)
                                    refreshTrigger++
                                }
                            }
                            "take_break" -> {
                                startSessionAsBreak = true
                                currentScreen = "session"
                            }
                            "start_study" -> {
                                currentScreen = "session"
                            }
                        }
                    }
                )

                "profile" -> ProfileScreen(
                    totalStudyMinutes = totalStudyMinutes,
                    completedTasks = completedTasks,
                    onProfileChanged = { newName -> 
                        userName = newName 
                        refreshTrigger++
                    },
                    onLogout = {
                        currentScreen = "dashboard"
                        onLogout()
                    }
                )
            }
        }
    }
}

@Composable
private fun KawaiiBottomBar(
    navItems: List<NavItem>,
    currentScreen: String,
    onScreenSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp), clip = false),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    val isSelected = currentScreen == item.route
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(22.dp))
                            .clickable { onScreenSelected(item.route) }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) DashboardBlueLight else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) DashboardBluePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(if (isSelected) 22.dp else 19.dp)
                            )
                        }
                        Text(
                            text = item.label,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) DashboardBluePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .size(if (isSelected) 6.dp else 3.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) DashboardBluePrimary else Color.Transparent)
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DashboardScreen(
    onStartSession: () -> Unit,
    activeCharacter: Character,
    totalStudyMinutes: Int,
    pendingTasks: Int,
    userName: String,
    equippedPet: String
) {
    val hours = totalStudyMinutes / 60
    val minutes = totalStudyMinutes % 60
    val studiedText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DashboardBlueBackground,
                        DashboardBlueLight,
                        White.copy(alpha = 0.92f)
                    )
                )
            )
    ) {
        val compact = maxHeight < 620.dp
        val horizontalPadding = if (compact) 20.dp else 24.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            GreetingHeader(userName = userName, compact = compact)
            Spacer(modifier = Modifier.height(if (compact) 4.dp else 6.dp))
            MascotHero(compact = compact, equippedPet = equippedPet)
            Spacer(modifier = Modifier.height(if (compact) 4.dp else 6.dp))
            MotivationCard(message = activeCharacter.message, compact = compact)
            Spacer(modifier = Modifier.height(if (compact) 6.dp else 10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 14.dp)
            ) {
                DashboardMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Schedule,
                    iconTint = DashboardBluePrimary,
                    iconBackground = DashboardBlueLight,
                    label = "Tiempo estudiado hoy",
                    value = studiedText,
                    footer = "estudiado hoy",
                    compact = compact
                )
                DashboardMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    iconTint = DashboardPinkAccent,
                    iconBackground = DashboardPinkSoft,
                    label = "Tareas pendientes",
                    value = "$pendingTasks",
                    footer = "totales",
                    compact = compact
                )
            }
            Spacer(modifier = Modifier.height(if (compact) 8.dp else 12.dp))
            StartStudyButton(onStartSession = onStartSession, compact = compact)
        }
    }
}

@Composable
private fun GreetingHeader(userName: String, compact: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hola, $userName",
                fontSize = if (compact) 40.sp else 42.sp,
                fontFamily = FredokaFont,
                fontWeight = FontWeight.ExtraBold,
                color = DashboardBluePrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lineHeight = if (compact) 31.sp else 35.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = White,
                        offset = Offset(0f, 3f),
                        blurRadius = 7f
                    )
                )
            )
            Text(
                text = "¿Lista para lograr tus metas hoy?",
                fontSize = if (compact) 15.sp else 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FredokaFont,
                color = DashboardBluePrimary.copy(alpha = 0.68f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Text(
            text = "✦",
            color = DashboardBlueSecondary,
            fontSize = if (compact) 22.sp else 25.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun MascotHero(compact: Boolean, equippedPet: String) {
    val heroHeight = if (compact) 180.dp else 210.dp
    val circleSize = if (compact) 150.dp else 176.dp
    val imageHeight = if (compact) 170.dp else 198.dp

    val petImage = when (equippedPet) {
        "Pompompurin" -> R.drawable.pompompurin
        "Hello Kitty" -> R.drawable.hello_kitty
        else -> R.drawable.cinnamoroll
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heroHeight),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(circleSize)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            White.copy(alpha = 0.84f),
                            DashboardBlueSecondary.copy(alpha = 0.32f),
                            Color.Transparent
                        )
                    )
                )
        )
        DecorativeCloud(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 0.dp, bottom = if (compact) 18.dp else 26.dp),
            alpha = 0.72f
        )
        DecorativeCloud(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 0.dp, bottom = if (compact) 20.dp else 28.dp),
            alpha = 0.44f,
            blueTint = true
        )
        Text(
            text = "✦",
            color = DashboardPinkAccent.copy(alpha = 0.28f),
            fontSize = if (compact) 15.sp else 18.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = if (compact) 74.dp else 82.dp, top = 8.dp)
        )
        Text(
            text = "✦",
            color = DashboardBluePrimary.copy(alpha = 0.34f),
            fontSize = if (compact) 16.sp else 20.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = if (compact) 58.dp else 70.dp, top = 12.dp)
        )
        Text(
            text = "★",
            color = Color(0xFFFFD96F).copy(alpha = 0.88f),
            fontSize = if (compact) 21.sp else 25.sp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp)
        )
        Text(
            text = "✦",
            color = White.copy(alpha = 0.9f),
            fontSize = if (compact) 20.sp else 24.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 68.dp, bottom = if (compact) 48.dp else 62.dp)
        )
        Image(
            painter = painterResource(id = petImage),
            contentDescription = equippedPet,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(imageHeight)
        )
    }
}

@Composable
private fun DecorativeCloud(
    modifier: Modifier = Modifier,
    alpha: Float,
    blueTint: Boolean = false
) {
    val cloudBrush = if (blueTint) {
        Brush.horizontalGradient(
            colors = listOf(
                DashboardBlueSecondary.copy(alpha = alpha * 0.58f),
                DashboardBluePrimary.copy(alpha = alpha * 0.34f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                White.copy(alpha = alpha),
                White.copy(alpha = alpha * 0.72f)
            )
        )
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(cloudBrush)
        )
        Box(
            modifier = Modifier
                .padding(start = 0.dp)
                .size(50.dp, 20.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(cloudBrush)
        )
    }
}

@Composable
private fun MotivationCard(message: String, compact: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 62.dp else 72.dp)
            .shadow(9.dp, RoundedCornerShape(22.dp), clip = false),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Box(
                modifier = Modifier.size(if (compact) 38.dp else 44.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "★",
                    color = Color(0xFFFFDE72),
                    fontSize = if (compact) 36.sp else 42.sp,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xFFFFEFB1),
                            offset = Offset(0f, 2f),
                            blurRadius = 5f
                        )
                    )
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¡Vas muy bien!",
                    fontSize = if (compact) 16.sp else 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF6B72E8),
                    lineHeight = if (compact) 18.sp else 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = shortMotivation(message),
                    fontSize = if (compact) 11.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    label: String,
    value: String,
    footer: String,
    compact: Boolean
) {
    Card(
        modifier = modifier
            .height(if (compact) 120.dp else 136.dp)
            .shadow(9.dp, RoundedCornerShape(22.dp), clip = false),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "✦",
                color = iconTint.copy(alpha = 0.18f),
                fontSize = 24.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 18.dp, top = 20.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = if (compact) 10.dp else 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(if (compact) 36.dp else 44.dp)
                        .clip(CircleShape)
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(if (compact) 22.dp else 27.dp)
                    )
                }
                Text(
                    text = label,
                    fontSize = if (compact) 10.sp else 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                    textAlign = TextAlign.Center,
                    lineHeight = if (compact) 12.sp else 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    fontSize = if (compact) 28.sp else 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = iconTint,
                    lineHeight = if (compact) 29.sp else 33.sp
                )
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = iconBackground.copy(alpha = 0.88f)
                ) {
                    Text(
                        text = footer,
                        fontSize = if (compact) 10.sp else 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = iconTint,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StartStudyButton(onStartSession: () -> Unit, compact: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 52.dp else 58.dp)
            .shadow(10.dp, RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF58C8F8),
                        DashboardBluePrimary,
                        DashboardBlueSecondary
                    )
                )
            )
            .clickable(onClick = onStartSession)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "✦",
            color = White.copy(alpha = 0.35f),
            fontSize = if (compact) 22.sp else 26.sp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.size(if (compact) 36.dp else 42.dp))
            Text(
                text = "Iniciar sesión de estudio",
                fontSize = if (compact) 14.sp else 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Box(
                modifier = Modifier
                    .size(if (compact) 38.dp else 44.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.94f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = DashboardBluePrimary,
                    modifier = Modifier.size(if (compact) 25.dp else 29.dp)
                )
            }
        }
    }
}

private fun shortMotivation(message: String): String {
    return when {
        message.contains("hora", ignoreCase = true) -> "Más de una hora de enfoque cuenta ✨"
        message.contains("Pomodoro", ignoreCase = true) -> "Sigue así, cada minuto cuenta ✨"
        message.contains("tareas", ignoreCase = true) -> "Ordena tus pendientes paso a paso ✨"
        message.contains("pausa", ignoreCase = true) ||
            message.contains("descans", ignoreCase = true) -> "Descansar también te ayuda a avanzar ✨"

        else -> "Sigue así, cada minuto cuenta ✨"
    }
}
