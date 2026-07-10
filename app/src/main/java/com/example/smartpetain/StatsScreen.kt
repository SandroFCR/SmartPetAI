package com.example.smartpetain

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartpetain.ui.theme.Background
import com.example.smartpetain.ui.theme.PurpleLight
import com.example.smartpetain.ui.theme.PurplePrimary
import com.example.smartpetain.ui.theme.TealPrimary
import com.example.smartpetain.ui.theme.TextPrimary
import com.example.smartpetain.ui.theme.TextSecondary
import com.example.smartpetain.ui.theme.White
import java.util.Calendar
import java.util.Locale

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
    var weekStats by remember { mutableStateOf(emptyWeekStats()) }
    var dailyGoalMinutes by remember { mutableStateOf(25) }
    var summary by remember { mutableStateOf(PomodoroStatsSummary()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        dailyGoalMinutes = FirebaseManager.loadProfile().pomodoroDuration
        weekStats = FirebaseManager.loadWeekStats()
        summary = FirebaseManager.loadPomodoroStatsSummary()
        isLoading = false
    }

    // Theme based on mascot
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
    val totalWeek = weekStats.sumOf { it.minutes }
    val totalTrackedStudy = summary.totalStudyMinutes.takeIf { it > 0 } ?: totalStudyMinutes
    val avgMinutes = totalWeek / 7
    val weeklyGoalMinutes = (dailyGoalMinutes * 7).coerceAtLeast(1)
    val effectiveMinutes = weekStats.sumOf { it.minutes.coerceAtMost(dailyGoalMinutes) }
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

        WeeklyGoalCard(
            effectiveMinutes = effectiveMinutes,
            weeklyGoalMinutes = weeklyGoalMinutes,
            dailyGoalMinutes = dailyGoalMinutes,
            productivity = productivity,
            productivityLabel = productivityLabel,
            themeColor = themeColor
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

        Spacer(modifier = Modifier.height(12.dp))

        MetricRow {
            MetricCard(
                imageRes = R.drawable.mejor_dia,
                value = "${summary.monthPomodoros}",
                title = "Este mes",
                subtitle = "Pomodoros completos",
                modifier = Modifier.weight(1f),
                themeColor = themeColor
            )
            MetricCard(
                imageRes = R.drawable.racha,
                value = "${summary.bestProductivityStreak}",
                title = "Mejor racha",
                subtitle = "dias productivos",
                modifier = Modifier.weight(1f),
                themeColor = themeColor
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = themeColor.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Analisis Pomodoro",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = weeklyStudyMessage(totalWeek, avgMinutes, dailyGoalMinutes),
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Productividad semanal: $productivity% ($productivityLabel)",
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
                    text = "Estadisticas Pomodoro",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Tus sesiones y descansos se registran al completar el temporizador.",
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
                    painter = painterResource(id = R.drawable.pompompurin),
                    contentDescription = "Pompompurin",
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(50.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.cinnamoroll),
                    contentDescription = "Cinnamoroll",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(76.dp)
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
            Text(
                text = if (totalTrackedStudy > 0) "Actualizado automaticamente desde el Pomodoro" else "Completa un Pomodoro para empezar",
                fontSize = 13.sp,
                color = themeColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Minutos por dia",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = themeColor)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(138.dp),
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
                                modifier = Modifier
                                    .height(104.dp)
                                    .width(32.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (day.minutes > 0) {
                                        Text(
                                            text = "${day.minutes}",
                                            fontSize = 9.sp,
                                            color = TextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(28.dp)
                                            .height(barHeight)
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
private fun WeeklyGoalCard(
    effectiveMinutes: Int,
    weeklyGoalMinutes: Int,
    dailyGoalMinutes: Int,
    productivity: Int,
    productivityLabel: String,
    themeColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Meta semanal",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Text(
                text = "${effectiveMinutes.coerceAtMost(weeklyGoalMinutes)} / $weeklyGoalMinutes min",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
            LinearProgressIndicator(
                progress = { productivity / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .padding(top = 8.dp),
                color = themeColor,
                trackColor = themeColor.copy(alpha = 0.1f)
            )
            Text(
                text = "Objetivo diario: $dailyGoalMinutes min - $productivityLabel",
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
        modifier = modifier
            .heightIn(min = 132.dp)
            .padding(0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(46.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp)
            ) {
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
        DayStats("Lun", 0),
        DayStats("Mar", 0),
        DayStats("Mie", 0),
        DayStats("Jue", 0),
        DayStats("Vie", 0),
        DayStats("Sab", 0),
        DayStats("Dom", 0)
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

private fun formatAverage(value: Double): String {
    return if (value == 0.0) "0" else String.format(Locale.getDefault(), "%.1f", value)
}

private fun weeklyStudyMessage(totalWeek: Int, avgMinutes: Int, dailyGoalMinutes: Int): String {
    return when {
        totalWeek == 0 -> "Completa una sesion Pomodoro para empezar a ver tus estadisticas reales."
        avgMinutes >= dailyGoalMinutes -> "Buen ritmo: esta semana estas sosteniendo tu meta diaria de $dailyGoalMinutes minutos en promedio."
        else -> "Ya empezaste. Intenta completar una sesion de $dailyGoalMinutes minutos para mejorar tu productividad semanal."
    }
}
