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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val points: Int
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

@Composable
fun CharactersScreen(
    totalStudyMinutes: Int = 0,
    completedTasks: Int = 0,
    pendingTasks: Int = 0
) {
    val characters = remember(totalStudyMinutes, completedTasks, pendingTasks) {
        buildCharacters(totalStudyMinutes, completedTasks, pendingTasks)
    }
    var selectedCharacter by remember { mutableStateOf<CharacterInfo?>(null) }

    selectedCharacter?.let { character ->
        CharacterDetailScreen(
            character = character,
            onBack = { selectedCharacter = null }
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
            text = "Toca una mascota para ver su progreso.",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        characters.forEach { character ->
            CharacterCard(
                character = character,
                onClick = { selectedCharacter = character }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun buildCharacters(
    totalStudyMinutes: Int,
    completedTasks: Int,
    pendingTasks: Int
): List<CharacterInfo> {
    val cinnamorollXp = totalStudyMinutes.coerceAtMost(150)
    val pompompurinXp = (completedTasks * 20).coerceAtMost(100)
    val helloKittyXp = ((completedTasks * 12) + (pendingTasks.coerceAtMost(5) * 4)).coerceAtMost(80)

    return listOf(
        CharacterInfo(
            name = "Cinnamoroll",
            imageSource = ImageSource.Local(R.drawable.cinnamoroll),
            role = "Motivacion y enfoque",
            description = "Sube cuando estudias y completas sesiones Pomodoro.",
            trigger = "Aparece al estudiar 25+ min seguidos",
            level = calculateLevel(cinnamorollXp, 50),
            xp = cinnamorollXp,
            maxXp = 150,
            color = "purple",
            detailMessage = "Cinnamoroll crece con tus minutos de estudio. Mientras mas constante seas, mas rapido sube de nivel.",
            tips = listOf(
                CharacterTip("Completa una sesion Pomodoro", "Estudia sin pausar hasta terminar el temporizador.", 25),
                CharacterTip("Estudia 10 minutos extra", "Suma minutos de enfoque para acercarte al siguiente nivel.", 10),
                CharacterTip("Revisa tus estadisticas", "Mira tu progreso semanal para mantener la rutina.", 5)
            ),
            isActive = true
        ),
        CharacterInfo(
            name = "Pompompurin",
            imageSource = ImageSource.Local(R.drawable.pompompurin),
            role = "Descanso y equilibrio",
            description = "Sube cuando mantienes buenos habitos y completas tareas sin saturarte.",
            trigger = "Aparece tras 50+ min sin descanso",
            level = calculateLevel(pompompurinXp, 40),
            xp = pompompurinXp,
            maxXp = 100,
            color = "amber",
            detailMessage = "Pompompurin te ayuda a descansar a tiempo. Mejora cuando equilibras avance con pausas sanas.",
            tips = listOf(
                CharacterTip("Descansa 5 minutos", "Haz una pausa real despues de una sesion intensa.", 10),
                CharacterTip("Completa una tarea pendiente", "Cerrar tareas reduce carga mental y sube su progreso.", 20),
                CharacterTip("Evita estudiar demasiado seguido", "Cuando el timer pida descanso, tomalo antes de seguir.", 10)
            )
        ),
        CharacterInfo(
            name = "Hello Kitty",
            imageSource = ImageSource.Remote("https://static.wikia.nocookie.net/hellokitty/images/5/52/Sanrio_Characters_Hello_Kitty_Image026.png/revision/latest?cb=20250110105831"),
            role = "Organizacion",
            description = "Sube cuando organizas tus pendientes y conviertes tareas en avance real.",
            trigger = "Aparece con 3+ tareas pendientes",
            level = calculateLevel(helloKittyXp, 30),
            xp = helloKittyXp,
            maxXp = 80,
            color = "pink",
            detailMessage = "Hello Kitty mejora cuando tus tareas estan claras. Agrega pendientes, ordenalos y completa los mas importantes.",
            tips = listOf(
                CharacterTip("Agrega tus tareas reales", "Registra lo que debes hacer para que la app pueda ayudarte.", 8),
                CharacterTip("Completa una tarea", "Marca una tarea como completada para ganar progreso.", 12),
                CharacterTip("Ordena por prioridad", "Empieza por lo mas urgente y deja menos ruido en tu lista.", 6)
            )
        )
    )
}

private fun calculateLevel(xp: Int, xpPerLevel: Int): Int {
    return (xp / xpPerLevel + 1).coerceIn(1, 5)
}

@Composable
fun CharacterCard(
    character: CharacterInfo,
    onClick: () -> Unit
) {
    val cardColor = characterLightColor(character.color)
    val accentColor = characterAccentColor(character.color)
    val progress = (character.xp / character.maxXp.toFloat()).coerceIn(0f, 1f)

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
                        text = if (character.isActive) "Activo" else "Nivel ${character.level}",
                        fontSize = 11.sp,
                        color = if (character.isActive) White else accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = character.description,
                fontSize = 14.sp,
                color = TextPrimary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = cardColor
            ) {
                Text(
                    text = character.trigger,
                    fontSize = 12.sp,
                    color = accentColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CharacterProgress(character = character)
        }
    }
}

@Composable
private fun CharacterDetailScreen(
    character: CharacterInfo,
    onBack: () -> Unit
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
            Text(
                text = character.role,
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            CharacterImage(
                source = character.imageSource,
                name = character.name,
                backgroundColorName = character.color,
                size = 170
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

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
            text = "Para mejorar:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        character.tips.forEach { tip ->
            TipCard(
                tip = tip,
                colorName = character.color
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = White.copy(alpha = 0.72f)
        ) {
            Text(
                text = "Pequenos avances todos los dias tambien suben tus niveles.",
                fontSize = 14.sp,
                color = TextPrimary,
                lineHeight = 20.sp,
                modifier = Modifier.padding(18.dp)
            )
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
        text = if (remainingXp == 0) "Nivel maximo alcanzado" else "Faltan $remainingXp XP para completar esta barra",
        fontSize = 12.sp,
        color = TextSecondary
    )
}

@Composable
private fun TipCard(
    tip: CharacterTip,
    colorName: String
) {
    val cardColor = characterLightColor(colorName)
    val accentColor = characterAccentColor(colorName)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
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
                    .background(cardColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${tip.points}",
                    color = accentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tip.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = tip.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }

            Text(
                text = ">",
                fontSize = 22.sp,
                color = TextSecondary
            )
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
