package com.example.smartpetain

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
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

data class DayStats(
    val day: String,
    val minutes: Int
)

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    totalStudyMinutes: Int = 0
) {
    var weekStats by remember { mutableStateOf(emptyWeekStats()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        weekStats = FirebaseManager.loadWeekStats()
        isLoading = false
    }

    val maxMinutes = weekStats.maxOf { it.minutes }.coerceAtLeast(1)
    val totalWeek = weekStats.sumOf { it.minutes }
    val totalHours = totalWeek / 60
    val totalMins = totalWeek % 60
    val avgMinutes = totalWeek / 7
    val completedSessions = weekStats.count { it.minutes >= 25 }
    val productivity = ((totalWeek / (7f * 25f)) * 100).toInt().coerceIn(0, 100)
    val productivityLabel = when {
        productivity >= 80 -> "Excelente"
        productivity >= 50 -> "Buen avance"
        productivity > 0 -> "En progreso"
        else -> "Sin datos"
    }
    val todayLabel = currentDayLabel()
    val hasStudyData = totalWeek > 0 || totalStudyMinutes > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onBack) {
                Text("<- Volver", color = PurplePrimary, fontSize = 16.sp)
            }
        }

        Text(
            text = "Estadisticas",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Esta semana",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Tiempo de estudio",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    text = "${totalHours}h ${totalMins}m",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = if (hasStudyData) "Datos reales de Firebase" else "Aun no hay sesiones esta semana",
                    fontSize = 13.sp,
                    color = TealPrimary,
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
                        CircularProgressIndicator(color = PurplePrimary)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weekStats.forEach { day ->
                            val barHeight = if (day.minutes > 0) {
                                (day.minutes.toFloat() / maxMinutes * 100).dp
                            } else {
                                4.dp
                            }
                            val isToday = day.day == todayLabel

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.height(120.dp)
                            ) {
                                if (day.minutes > 0) {
                                    Text(
                                        text = "${day.minutes}",
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .height(barHeight)
                                        .background(
                                            color = if (isToday) PurplePrimary else PurpleLight,
                                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = day.day,
                                    fontSize = 11.sp,
                                    color = if (isToday) PurplePrimary else TextSecondary,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                icon = "O",
                value = "$completedSessions",
                title = "Sesiones completadas",
                subtitle = if (completedSessions > 0) "25 min o mas" else "Sin sesiones",
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                icon = "*",
                value = "$productivity%",
                title = "Productividad",
                subtitle = productivityLabel,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PurpleLight),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Analisis de estudio",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = weeklyStudyMessage(totalWeek, avgMinutes),
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Promedio diario: ${avgMinutes}min",
                    fontSize = 13.sp,
                    color = PurplePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MetricCard(
    icon: String,
    value: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(104.dp)
            .padding(0.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = icon, fontSize = 20.sp, color = PurplePrimary)
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(text = title, fontSize = 11.sp, color = TextSecondary)
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TealPrimary,
                fontWeight = FontWeight.Bold
            )
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

private fun currentDayLabel(): String {
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

private fun weeklyStudyMessage(totalWeek: Int, avgMinutes: Int): String {
    return when {
        totalWeek == 0 -> "Completa una sesion Pomodoro para empezar a ver tus estadisticas reales."
        avgMinutes >= 25 -> "Buen ritmo: esta semana estas sosteniendo al menos un Pomodoro diario en promedio."
        else -> "Ya empezaste. Intenta completar una sesion de 25 minutos para mejorar tu promedio semanal."
    }
}
