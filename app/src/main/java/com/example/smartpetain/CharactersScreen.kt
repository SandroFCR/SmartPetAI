package com.example.smartpetain

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.smartpetain.ui.theme.AmberLight
import com.example.smartpetain.ui.theme.AmberPrimary
import com.example.smartpetain.ui.theme.Background
import com.example.smartpetain.ui.theme.PinkLight
import com.example.smartpetain.ui.theme.PinkPrimary
import com.example.smartpetain.ui.theme.PurpleLight
import com.example.smartpetain.ui.theme.PurplePrimary
import com.example.smartpetain.ui.theme.TealLight
import com.example.smartpetain.ui.theme.TealPrimary
import com.example.smartpetain.ui.theme.TextPrimary
import com.example.smartpetain.ui.theme.TextSecondary
import com.example.smartpetain.ui.theme.White

sealed class ImageSource {
    data class Local(val resId: Int) : ImageSource()
    data class Remote(val url: String) : ImageSource()
}

data class CharacterTip(
    val title: String,
    val description: String,
    val points: Int,
    val action: String = "",
    val index: Int = 0,
    val isCompleted: Boolean = false
)

data class CharacterInfo(
    val name: String,
    val imageSource: ImageSource,
    val role: String,
    val description: String,
    val trigger: String,
    val level: Int,
    val xp: Int,
    val maxXp: Int,
    val color: String,
    val detailMessage: String,
    val tips: List<CharacterTip>,
    val isActive: Boolean = false
)

data class CoachRecommendation(
    val characterName: String,
    val title: String,
    val message: String,
    val reason: String,
    val weeklyProgress: String,
    val source: String = "local"
)

@Composable
fun CharactersScreen(
    totalStudyMinutes: Int = 0,
    completedTasks: Int = 0,
    pendingTasks: Int = 0,
    petStatsList: List<PetStats> = emptyList(),
    equippedPetName: String = "Cinnamoroll",
    todayCompletedMissions: List<Int> = emptyList(),
    onEquipPet: (String) -> Unit = {},
    onMissionAction: (String) -> Unit = {}
) {
    val characters = remember(petStatsList, equippedPetName, todayCompletedMissions) {
        buildCharactersFromStats(petStatsList, equippedPetName, todayCompletedMissions)
    }
    var selectedCharacter by remember { mutableStateOf<CharacterInfo?>(null) }
    var weekStats by remember { mutableStateOf(emptyCharacterWeekStats()) }
    var dailyGoalMinutes by remember { mutableStateOf(25) }
    var isCoachLoading by remember { mutableStateOf(true) }
    var coachRecommendation by remember {
        mutableStateOf(
            buildCoachRecommendation(
                weekStats = weekStats,
                dailyGoalMinutes = dailyGoalMinutes,
                completedTasks = completedTasks,
                pendingTasks = pendingTasks,
                characters = characters
            )
        )
    }

    LaunchedEffect(petStatsList, characters) {
        isCoachLoading = true
        val profile = FirebaseManager.loadProfile()
        val loadedDailyGoalMinutes = profile.pomodoroDuration
        val loadedWeekStats = FirebaseManager.loadWeekStats()
        
        dailyGoalMinutes = loadedDailyGoalMinutes
        weekStats = loadedWeekStats
        
        val fallbackRecommendation = buildCoachRecommendation(
            weekStats = loadedWeekStats,
            dailyGoalMinutes = loadedDailyGoalMinutes,
            completedTasks = completedTasks,
            pendingTasks = pendingTasks,
            characters = characters
        )
        coachRecommendation = fallbackRecommendation
        
        coachRecommendation = FirebaseManager.generateStudyCoachRecommendation(
            weekStats = loadedWeekStats,
            dailyGoalMinutes = loadedDailyGoalMinutes,
            completedTasks = completedTasks,
            pendingTasks = pendingTasks,
            pets = petStatsList,
            fallback = fallbackRecommendation
        )
        isCoachLoading = false
    }

    selectedCharacter?.let { character ->
        CharacterDetailScreen(
            character = character,
            onBack = { selectedCharacter = null },
            onEquip = { 
                onEquipPet(character.name)
                selectedCharacter = null
            },
            onMissionClick = { action ->
                onMissionAction(action)
                selectedCharacter = null
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Mis mascotas",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Mascota equipada: $equippedPetName",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        CoachRecommendationCard(
            recommendation = coachRecommendation,
            isLoading = isCoachLoading,
            characters = characters
        )

        Spacer(modifier = Modifier.height(16.dp))

        characters.forEach { character ->
            CharacterCard(
                character = character,
                onClick = { selectedCharacter = character }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun buildCoachRecommendation(
    weekStats: List<DayStats>,
    dailyGoalMinutes: Int,
    completedTasks: Int,
    pendingTasks: Int,
    characters: List<CharacterInfo>
): CoachRecommendation {
    val safeDailyGoal = dailyGoalMinutes.coerceAtLeast(1)
    val weeklyGoal = safeDailyGoal * 7
    val effectiveMinutes = weekStats.sumOf { it.minutes.coerceAtMost(safeDailyGoal) }
    val productivity = ((effectiveMinutes / weeklyGoal.toFloat()) * 100).toInt().coerceIn(0, 100)
    val remainingWeeklyMinutes = (weeklyGoal - effectiveMinutes).coerceAtLeast(0)
    val todayStats = weekStats.getOrNull(currentWeekIndex())
    val todayMinutes = todayStats?.minutes ?: 0
    val remainingTodayMinutes = (safeDailyGoal - todayMinutes).coerceAtLeast(0)

    val selectedCharacterName = when {
        pendingTasks >= 3 -> "Hello Kitty"
        remainingTodayMinutes > 0 -> "Cinnamoroll"
        completedTasks > 0 && productivity >= 60 -> "Pompompurin"
        else -> characters.maxByOrNull { it.xp }?.name ?: "Cinnamoroll"
    }

    val title = when (selectedCharacterName) {
        "Hello Kitty" -> "Ordena tus pendientes"
        "Pompompurin" -> "Buen ritmo, cuida tu energia"
        else -> "Sube tu enfoque de hoy"
    }

    val message = when {
        remainingTodayMinutes > 0 -> {
            "Estudia $remainingTodayMinutes min ahora para cumplir tu meta diaria y subir XP con $selectedCharacterName."
        }
        pendingTasks > 0 -> {
            "Ya cumpliste tu meta de estudio. Completa una tarea pendiente para seguir ganando progreso."
        }
        else -> {
            "Vas al dia. Haz una sesion corta de repaso para mantener tu racha activa."
        }
    }

    val reason = when (selectedCharacterName) {
        "Hello Kitty" -> "Te conviene porque tienes $pendingTasks tareas pendientes y esta mascota mejora con organizacion."
        "Pompompurin" -> "Te conviene porque ya avanzaste y ahora toca sostener el equilibrio entre estudio y descanso."
        else -> "Te conviene porque Cinnamoroll crece con tus minutos de enfoque y sesiones Pomodoro."
    }

    return CoachRecommendation(
        characterName = selectedCharacterName,
        title = title,
        message = message,
        reason = reason,
        weeklyProgress = "$effectiveMinutes/$weeklyGoal min esta semana - $productivity%. Faltan $remainingWeeklyMinutes min."
    )
}

private fun buildCharactersFromStats(
    petStatsList: List<PetStats>,
    equippedPetName: String,
    todayCompletedMissions: List<Int>
): List<CharacterInfo> {
    return listOf("Cinnamoroll", "Pompompurin", "Hello Kitty").map { petName ->
        val stats = petStatsList.find { it.name == petName } ?: PetStats(petName)
        val level = stats.level
        
        when (petName) {
            "Cinnamoroll" -> CharacterInfo(
                name = "Cinnamoroll",
                imageSource = ImageSource.Local(R.drawable.cinnamoroll),
                role = "Motivacion y enfoque",
                description = "Sube cuando estudias y completas sesiones Pomodoro.",
                trigger = "Aparece al estudiar 25+ min seguidos",
                level = level,
                xp = stats.xp,
                maxXp = stats.maxXp,
                color = "purple",
                detailMessage = "Cinnamoroll crece con tus minutos de estudio. Mientras mas constante seas, mas rapido sube de nivel.",
                tips = listOf(
                    CharacterTip("Completa un Pomodoro", "Estudia hasta terminar el timer.", 20 + (level * 5), "start_study", 101),
                    CharacterTip("Estudia 10 min", "Minutos extra para tu nivel.", 10, "start_study", 102)
                ).map { it.copy(isCompleted = todayCompletedMissions.contains(it.index)) },
                isActive = equippedPetName == "Cinnamoroll"
            )
            "Pompompurin" -> CharacterInfo(
                name = "Pompompurin",
                imageSource = ImageSource.Local(R.drawable.pompompurin),
                role = "Descanso y equilibrio",
                description = "Sube cuando mantienes buenos habitos y descansas.",
                trigger = "Aparece tras 50+ min sin descanso",
                level = level,
                xp = stats.xp,
                maxXp = stats.maxXp,
                color = "amber",
                detailMessage = "Pompompurin te ayuda a descansar a tiempo. Mejora cuando equilibras avance con pausas sanas.",
                tips = listOf(
                    CharacterTip("Descansa 5 min", "Toma una pausa real.", 15 + (level * 2), "take_break", 201),
                    CharacterTip("Cierra una tarea", "Reduce tu carga mental.", 20, "add_task", 202)
                ).map { it.copy(isCompleted = todayCompletedMissions.contains(it.index)) },
                isActive = equippedPetName == "Pompompurin"
            )
            else -> CharacterInfo(
                name = "Hello Kitty",
                imageSource = ImageSource.Local(R.drawable.hello_kitty),
                role = "Organizacion",
                description = "Sube cuando organizas tus pendientes.",
                trigger = "Aparece con 3+ tareas pendientes",
                level = level,
                xp = stats.xp,
                maxXp = stats.maxXp,
                color = "pink",
                detailMessage = "Hello Kitty mejora cuando tus tareas estan claras. Registra y ordena tus pendientes.",
                tips = listOf(
                    CharacterTip("Agrega una tarea", "Registra lo que debes hacer.", 10 + (level * 3), "add_task", 301),
                    CharacterTip("Ordena por prioridad", "Empieza por lo urgente.", 10, "sort_priority", 302)
                ).map { it.copy(isCompleted = todayCompletedMissions.contains(it.index)) },
                isActive = equippedPetName == "Hello Kitty"
            )
        }
    }
}

@Composable
private fun CoachRecommendationCard(
    recommendation: CoachRecommendation,
    isLoading: Boolean,
    characters: List<CharacterInfo>
) {
    val character = characters.find { it.name == recommendation.characterName } ?: characters.first()
    val cardColor = characterLightColor(character.color)
    val accentColor = characterAccentColor(character.color)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CharacterImage(
                    source = character.imageSource,
                    name = character.name,
                    backgroundColorName = character.color,
                    size = 54
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (recommendation.source == "ai") "Coach IA activa" else "Coach local",
                        fontSize = 12.sp,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = recommendation.title,
                        fontSize = 18.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = accentColor,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Analizando tu progreso...",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            } else {
                Text(
                    text = recommendation.message,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = recommendation.reason,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = White.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = recommendation.weeklyProgress,
                        fontSize = 12.sp,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CharacterCard(
    character: CharacterInfo,
    onClick: () -> Unit
) {
    val cardColor = characterLightColor(character.color)
    val accentColor = characterAccentColor(character.color)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CharacterImage(
                    source = character.imageSource,
                    name = character.name,
                    backgroundColorName = character.color,
                    size = 72
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = character.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Text(
                        text = character.role,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (character.isActive) accentColor else cardColor
                ) {
                    Text(
                        text = if (character.isActive) "Equipado" else "Nivel ${character.level}",
                        fontSize = 11.sp,
                        color = if (character.isActive) White else accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CharacterProgress(character = character)
        }
    }
}

@Composable
private fun CharacterDetailScreen(
    character: CharacterInfo,
    onBack: () -> Unit,
    onEquip: () -> Unit,
    onMissionClick: (String) -> Unit
) {
    val cardColor = characterLightColor(character.color)
    val accentColor = characterAccentColor(character.color)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cardColor)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        TextButton(onClick = onBack) {
            Text("<- Mascotas", color = accentColor, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = character.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            
            Spacer(modifier = Modifier.height(18.dp))

            CharacterImage(
                source = character.imageSource,
                name = character.name,
                backgroundColorName = character.color,
                size = 170
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!character.isActive) {
                Button(
                    onClick = onEquip,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Equipar mascota", fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = White.copy(alpha = 0.5f)
                ) {
                    Text(
                        "Equipada actualmente",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = character.detailMessage,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                CharacterProgress(character = character)
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = "Misiones diarias:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        character.tips.forEach { tip ->
            TipCard(
                tip = tip,
                colorName = character.color,
                onClick = { if (!tip.isCompleted) onMissionClick(tip.action) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CharacterProgress(character: CharacterInfo) {
    val cardColor = characterLightColor(character.color)
    val accentColor = characterAccentColor(character.color)
    val progress = (character.xp / character.maxXp.toFloat()).coerceIn(0f, 1f)
    val remainingXp = (character.maxXp - character.xp).coerceAtLeast(0)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Nivel ${character.level}",
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${character.xp}/${character.maxXp} XP",
            fontSize = 12.sp,
            color = TextSecondary
        )
    }

    Spacer(modifier = Modifier.height(6.dp))

    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp),
        color = accentColor,
        trackColor = cardColor
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = if (remainingXp == 0) "Nivel maximo alcanzado" else "Faltan $remainingXp XP para subir de nivel",
        fontSize = 12.sp,
        color = TextSecondary
    )
}

@Composable
private fun TipCard(
    tip: CharacterTip,
    colorName: String,
    onClick: () -> Unit
) {
    val cardColor = characterLightColor(colorName)
    val accentColor = characterAccentColor(colorName)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !tip.isCompleted, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (tip.isCompleted) White.copy(alpha = 0.6f) else White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (tip.isCompleted) TealPrimary.copy(alpha = 0.2f) else cardColor, 
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (tip.isCompleted) "✓" else "+${tip.points}",
                    color = if (tip.isCompleted) TealPrimary else accentColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tip.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (tip.isCompleted) TextSecondary else TextPrimary
                )
                Text(
                    text = if (tip.isCompleted) "¡Mision cumplida por hoy!" else tip.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            if (!tip.isCompleted) {
                Text(
                    text = ">",
                    fontSize = 22.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun CharacterImage(
    source: ImageSource,
    name: String,
    backgroundColorName: String,
    size: Int
) {
    Surface(
        shape = RoundedCornerShape((size / 4).dp),
        color = characterLightColor(backgroundColorName),
        modifier = Modifier.size(size.dp)
    ) {
        when (source) {
            is ImageSource.Local -> {
                Image(
                    painter = painterResource(id = source.resId),
                    contentDescription = name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding((size / 14).dp)
                )
            }
            is ImageSource.Remote -> {
                AsyncImage(
                    model = source.url,
                    contentDescription = name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding((size / 14).dp)
                        .clip(RoundedCornerShape((size / 5).dp))
                )
            }
        }
    }
}

private fun characterLightColor(colorName: String) = when (colorName) {
    "amber" -> AmberLight
    "pink" -> PinkLight
    "teal" -> TealLight
    else -> PurpleLight
}

private fun characterAccentColor(colorName: String) = when (colorName) {
    "amber" -> AmberPrimary
    "pink" -> PinkPrimary
    "teal" -> TealPrimary
    else -> PurplePrimary
}

private fun currentWeekIndex(): Int {
    val day = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)
    return when (day) {
        java.util.Calendar.MONDAY -> 0
        java.util.Calendar.TUESDAY -> 1
        java.util.Calendar.WEDNESDAY -> 2
        java.util.Calendar.THURSDAY -> 3
        java.util.Calendar.FRIDAY -> 4
        java.util.Calendar.SATURDAY -> 5
        else -> 6
    }
}

private fun emptyCharacterWeekStats(): List<DayStats> {
    return listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom").map { day ->
        DayStats(day = day, minutes = 0)
    }
}
