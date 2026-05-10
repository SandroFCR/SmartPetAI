package com.example.smartpetain

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.smartpetain.ui.theme.*

// Tipo de imagen: local o url
sealed class ImageSource {
    data class Local(val resId: Int) : ImageSource()
    data class Remote(val url: String) : ImageSource()
}

data class CharacterInfo(
    val name: String,
    val emoji: String,
    val imageSource: ImageSource,
    val role: String,
    val description: String,
    val trigger: String,
    val level: Int,
    val xp: Int,
    val maxXp: Int,
    val color: String,
    val isActive: Boolean = false
)

@Composable
fun CharactersScreen(
    totalStudyMinutes: Int = 0,
    completedTasks: Int = 0
) {
    val characters = listOf(
        CharacterInfo(
            name = "Cinnamoroll",
            emoji = "🐶",
            imageSource = ImageSource.Local(R.drawable.cinnamoroll),
            role = "Motivación y enfoque",
            description = "Aparece cuando estudias bien y necesitas mantenerte motivado. Te anima con cada sesión completada.",
            trigger = "Aparece al estudiar 25+ min seguidos",
            level = 3,
            xp = totalStudyMinutes.coerceAtMost(150),
            maxXp = 150,
            color = "purple",
            isActive = true
        ),
        CharacterInfo(
            name = "Pompompurin",
            emoji = "🍮",
            imageSource = ImageSource.Local(resId = R.drawable.pompompurin),
            role = "Descanso y equilibrio",
            description = "Detecta cuando estás saturado y te recomienda tomar pausas inteligentes para rendir mejor.",
            trigger = "Aparece tras 50+ min sin descanso",
            level = 2,
            xp = (completedTasks * 20).coerceAtMost(100),
            maxXp = 100,
            color = "amber"
        ),
        CharacterInfo(
            name = "Hello Kitty",
            emoji = "🎀",
            imageSource = ImageSource.Remote("https://static.wikia.nocookie.net/hellokitty/images/5/52/Sanrio_Characters_Hello_Kitty_Image026.png/revision/latest?cb=20250110105831"),
            role = "Organización",
            description = "Te ayuda a planificar tareas y horarios. Te recuerda lo importante para que no se te escape nada.",
            trigger = "Aparece con 3+ tareas pendientes",
            level = 1,
            xp = (completedTasks * 10).coerceAtMost(50),
            maxXp = 50,
            color = "pink"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Mis personajes",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Cada uno te ayuda a su manera 🌟",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        characters.forEach { character ->
            CharacterCard(character = character)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CharacterCard(character: CharacterInfo) {
    val cardColor = when (character.color) {
        "amber" -> AmberLight
        "pink" -> PinkLight
        else -> PurpleLight
    }
    val accentColor = when (character.color) {
        "amber" -> AmberPrimary
        "pink" -> PinkPrimary
        else -> PurplePrimary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardColor,
                    modifier = Modifier.size(72.dp)
                ) {
                    // Aquí está la diferencia — local vs remota
                    when (val source = character.imageSource) {
                        is ImageSource.Local -> {
                            Image(
                                painter = painterResource(id = source.resId),
                                contentDescription = character.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            )
                        }
                        is ImageSource.Remote -> {
                            AsyncImage(
                                model = source.url,
                                contentDescription = character.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }
                }

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

                if (character.isActive) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = accentColor
                    ) {
                        Text(
                            text = "Activo",
                            fontSize = 11.sp,
                            color = White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = cardColor
                    ) {
                        Text(
                            text = "Nivel ${character.level}",
                            fontSize = 11.sp,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
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
                    text = "⚡ ${character.trigger}",
                    fontSize = 12.sp,
                    color = accentColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                progress = { character.xp.toFloat() / character.maxXp },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = accentColor,
                trackColor = cardColor
            )
        }
    }
}