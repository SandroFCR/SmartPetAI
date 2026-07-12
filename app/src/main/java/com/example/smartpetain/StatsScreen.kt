package com.example.smartpetain

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartpetain.ui.theme.PurpleLight
import com.example.smartpetain.ui.theme.PurplePrimary
import com.example.smartpetain.ui.theme.TealPrimary
import com.example.smartpetain.ui.theme.TextPrimary
import com.example.smartpetain.ui.theme.TextSecondary
import com.example.smartpetain.ui.theme.White
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

data class DayStats(
    val day: String,
    val minutes: Int,
    val completedSessions: Int = 0,
    val createdTasks: Int = 0,
    val completedTasks: Int = 0,
    val breaks: Int = 0,
    val breakMinutes: Int = 0,
    val completedMissions: List<Int> = emptyList()
)

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    totalStudyMinutes: Int = 0,
    equippedPetName: String = "Cinnamoroll"
) {
    val scope = rememberCoroutineScope()
    var weekStats by remember { mutableStateOf(emptyWeekStats()) }
    var weeklyGoalMinutes by remember { mutableStateOf(1050) }
    var summary by remember { mutableStateOf(PomodoroStatsSummary()) }
    var isLoading by remember { mutableStateOf(true) }
    var showInHours by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val profile = FirebaseManager.loadProfile()
        weeklyGoalMinutes = profile.weeklyGoalMinutes
        weekStats = FirebaseManager.loadWeekStats()
        summary = FirebaseManager.loadPomodoroStatsSummary()
        isLoading = false
    }

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

    val maxMinutes = weekStats.maxOf { it.minutes }.coerceAtLeast(1)
    val totalWeekMinutes = weekStats.sumOf { it.minutes }
    val totalTrackedStudy = summary.totalStudyMinutes.takeIf { it > 0 } ?: totalStudyMinutes
    
    // Calculate productivity based on the fixed weekly goal
    val effectiveMinutes = weekStats.sumOf { it.minutes }
    val productivity = ((effectiveMinutes / weeklyGoalMinutes.toFloat()) * 100).toInt().coerceIn(0, 100)
    
    val productivityLabel = when {
        productivity >= 80 -> "Excelente"
        productivity >= 50 -> "Buen avance"
        productivity > 0 -> "En progreso"
        else -> "Sin datos"
    }
    val todayLabel = currentDayLabel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(themeBg)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onBack) {
                Text("<- Volver", color = themeColor, fontSize = 16.sp)
            }
        }

        PomodoroStatsHero(themeColor)

        Spacer(modifier = Modifier.height(16.dp))

        StudyTimeCard(
            totalTrackedStudy = totalTrackedStudy,
            weekStats = weekStats,
            maxMinutes = maxMinutes,
            todayLabel = todayLabel,
            isLoading = isLoading,
            themeColor = themeColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Updated Weekly Goal Card with decoupled logic and Unit Switch
        WeeklyGoalEditorCard(
            effectiveMinutes = effectiveMinutes,
            weeklyGoalMinutes = weeklyGoalMinutes,
            productivity = productivity,
            productivityLabel = productivityLabel,
            themeColor = themeColor,
            showInHours = showInHours,
            onUnitToggle = { showInHours = !showInHours },
            onGoalChange = { newGoal ->
                weeklyGoalMinutes = newGoal
                scope.launch {
                    FirebaseManager.saveWeeklyGoal(newGoal)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        MetricRow {
            MetricCard(
                imageRes = R.drawable.sesiones,
                value = "${summary.totalPomodoros}",
                title = "Pomodoros totales",
                subtitle = "completados",
                modifier = Modifier.weight(1f),
                themeColor = themeColor
            )
            MetricCard(
                imageRes = R.drawable.pompompurin,
                value = formatMinutes(summary.totalBreakMinutes),
                title = "Descanso total",
                subtitle = "pausas completadas",
                modifier = Modifier.weight(1f),
                themeColor = themeColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        MetricRow {
            MetricCard(
                imageRes = R.drawable.cinnamoroll,
                value = "${summary.todayPomodoros}",
                title = "Pomodoros hoy",
                subtitle = "registrados hoy",
                modifier = Modifier.weight(1f),
                themeColor = themeColor
            )
            MetricCard(
                imageRes = R.drawable.productividad,
                value = "${summary.weekPomodoros}",
                title = "Esta semana",
                subtitle = "Pomodoros completos",
                modifier = Modifier.weight(1f),
                themeColor = themeColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Análisis de Productividad",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = weeklyStudyMessage(totalWeekMinutes, weeklyGoalMinutes),
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Progreso de meta: $productivity% ($productivityLabel)",
                    fontSize = 13.sp,
                    color = themeColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PomodoroStatsHero(themeColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Estadísticas de Estudio",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Seguimiento de tus metas semanales personalizadas.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            Box(
                modifier = Modifier.size(94.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cinnamoroll),
                    contentDescription = "Cinnamoroll",
                    modifier = Modifier.size(76.dp)
                )
            }
        }
    }
}

@Composable
private fun StudyTimeCard(
    totalTrackedStudy: Int,
    weekStats: List<DayStats>,
    maxMinutes: Int,
    todayLabel: String,
    isLoading: Boolean,
    themeColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Tiempo total estudiado",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Text(
                text = formatMinutes(totalTrackedStudy),
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Minutos por día",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = themeColor)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().height(138.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weekStats.forEach { day ->
                        val barHeight = if (day.minutes > 0) {
                            (day.minutes.toFloat() / maxMinutes * 78).dp
                        } else {
                            4.dp
                        }
                        val isToday = day.day == todayLabel

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.height(138.dp)
                        ) {
                            Box(
                                modifier = Modifier.height(104.dp).width(32.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (day.minutes > 0) {
                                        Text(text = "${day.minutes}", fontSize = 9.sp, color = TextSecondary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                    Box(
                                        modifier = Modifier.width(28.dp).height(barHeight)
                                            .background(
                                                color = if (isToday) themeColor else themeColor.copy(alpha = 0.3f),
                                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                            )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = day.day,
                                fontSize = 11.sp,
                                color = if (isToday) themeColor else TextSecondary,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyGoalEditorCard(
    effectiveMinutes: Int,
    weeklyGoalMinutes: Int,
    productivity: Int,
    productivityLabel: String,
    themeColor: Color,
    showInHours: Boolean,
    onUnitToggle: () -> Unit,
    onGoalChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Meta Semanal",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Surface(
                    onClick = onUnitToggle,
                    shape = RoundedCornerShape(12.dp),
                    color = themeColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (showInHours) "Ver en Minutos" else "Ver en Horas",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { if (weeklyGoalMinutes > 60) onGoalChange(weeklyGoalMinutes - 60) }) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = themeColor)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (showInHours) {
                            String.format(Locale.getDefault(), "%.1f / %.0f h", effectiveMinutes / 60f, weeklyGoalMinutes / 60f)
                        } else {
                            "$effectiveMinutes / $weeklyGoalMinutes min"
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                IconButton(onClick = { onGoalChange(weeklyGoalMinutes + 60) }) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = themeColor)
                }
            }

            LinearProgressIndicator(
                progress = { productivity / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).padding(top = 8.dp),
                color = themeColor,
                trackColor = themeColor.copy(alpha = 0.1f)
            )
            
            Text(
                text = "Estado: $productivityLabel",
                fontSize = 12.sp,
                color = themeColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun MetricRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

@Composable
private fun MetricCard(
    imageRes: Int,
    value: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    themeColor: Color
) {
    Card(
        modifier = modifier.heightIn(min = 132.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.align(Alignment.TopEnd).size(46.dp)
            )

            Column(modifier = Modifier.fillMaxWidth().padding(top = 28.dp)) {
                Text(
                    text = value,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = title, fontSize = 10.sp, color = TextSecondary, maxLines = 2)
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = themeColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
            }
        }
    }
}

private fun emptyWeekStats(): List<DayStats> {
    return listOf(
        DayStats("Lun", 0), DayStats("Mar", 0), DayStats("Mie", 0),
        DayStats("Jue", 0), DayStats("Vie", 0), DayStats("Sab", 0), DayStats("Dom", 0)
    )
}

fun currentDayLabel(): String {
    return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Lun"
        Calendar.TUESDAY -> "Mar"
        Calendar.WEDNESDAY -> "Mie"
        Calendar.THURSDAY -> "Jue"
        Calendar.FRIDAY -> "Vie"
        Calendar.SATURDAY -> "Sab"
        else -> "Dom"
    }
}

private fun formatMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

private fun weeklyStudyMessage(totalWeekMinutes: Int, weeklyGoalMinutes: Int): String {
    return when {
        totalWeekMinutes == 0 -> "Comienza una sesión de estudio para ver tu progreso real."
        totalWeekMinutes >= weeklyGoalMinutes -> "¡Increíble! Has superado tu meta semanal de estudio. ✨"
        else -> "Llevas un buen ritmo. Te faltan ${formatMinutes(weeklyGoalMinutes - totalWeekMinutes)} para alcanzar tu objetivo semanal."
    }
}
