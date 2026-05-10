package com.example.smartpetain

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartpetain.ui.theme.*

data class DayStats(
    val day: String,
    val minutes: Int
)

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    totalStudyMinutes: Int = 0
) {
    // Datos de ejemplo por ahora
    val weekStats = remember {
        listOf(
            DayStats("Lun", 45),
            DayStats("Mar", 90),
            DayStats("Mié", 30),
            DayStats("Jue", 120),
            DayStats("Vie", 75),
            DayStats("Sáb", 60),
            DayStats("Dom", 0)
        )
    }

    val maxMinutes = weekStats.maxOf { it.minutes }.coerceAtLeast(1)
    val totalWeek = weekStats.sumOf { it.minutes }
    val totalHours = totalWeek / 60
    val totalMins = totalWeek % 60
    val avgMinutes = totalWeek / 7
    val completedSessions = weekStats.count { it.minutes >= 25 }

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
                Text("← Volver", color = PurplePrimary, fontSize = 16.sp)
            }
        }

        Text(
            text = "Estadísticas",
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

        // Card tiempo total
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
                    text = "+15% vs semana pasada",
                    fontSize = 13.sp,
                    color = TealPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Gráfica de barras manual
                Text(
                    text = "Minutos por día",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weekStats.forEach { day ->
                        val barHeight = if (maxMinutes > 0)
                            (day.minutes.toFloat() / maxMinutes * 100).dp
                        else 0.dp

                        val isToday = day.day == "Jue"

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
                                    .height(barHeight.coerceAtLeast(4.dp))
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

        Spacer(modifier = Modifier.height(16.dp))

        // Cards de métricas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sesiones completadas
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🎯",
                        fontSize = 24.sp
                    )
                    Text(
                        text = "$completedSessions",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Sesiones completadas",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "+12%",
                        fontSize = 12.sp,
                        color = TealPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Productividad
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "⚡",
                        fontSize = 24.sp
                    )
                    Text(
                        text = "85%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Productividad",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Excelente",
                        fontSize = 12.sp,
                        color = TealPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card análisis IA
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PurpleLight),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🧠 Análisis de IA",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurplePrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Eres más productivo en las tardes (2:00pm - 6:00pm). ¡Intenta mantener ese patrón! 🌟",
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