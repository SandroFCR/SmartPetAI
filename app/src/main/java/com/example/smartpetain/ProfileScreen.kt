package com.example.smartpetain

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.smartpetain.ui.theme.Background
import com.example.smartpetain.ui.theme.PurpleLight
import com.example.smartpetain.ui.theme.PurplePrimary
import com.example.smartpetain.ui.theme.TextPrimary
import com.example.smartpetain.ui.theme.TextSecondary
import com.example.smartpetain.ui.theme.White
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    totalStudyMinutes: Int = 0,
    completedTasks: Int = 0,
    equippedPetName: String = "Cinnamoroll",
    onProfileChanged: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var profile by remember { mutableStateOf(UserProfile(name = "Cargando...")) }
    var editingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    var localAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var isUploadingAvatar by remember { mutableStateOf(false) }
    var avatarMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        profile = FirebaseManager.loadProfile()
        tempName = profile.name
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

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            localAvatarUri = uri
            avatarMessage = null
            isUploadingAvatar = true
            scope.launch {
                val avatarUrl = FirebaseManager.uploadProfileImage(uri)
                if (avatarUrl != null) {
                    profile = profile.copy(avatarUrl = avatarUrl)
                    FirebaseManager.saveProfile(profile)
                    localAvatarUri = null
                } else {
                    avatarMessage = "No se pudo guardar la foto en Firebase Storage."
                }
                isUploadingAvatar = false
            }
        }
    }

    val hours = totalStudyMinutes / 60
    val minutes = totalStudyMinutes % 60

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        themeBg,
                        themeBg.copy(alpha = 0.82f),
                        White
                    )
                )
            )
    ) {
        ProfileBackgroundDecorations(themeColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Perfil",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = themeColor,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        val frameResource = when (equippedPetName) {
            "Pompompurin" -> R.drawable.profile_frame_pompompurin_transparent
            "Hello Kitty" -> R.drawable.profile_frame_hello_kitty_transparent
            else -> R.drawable.profile_frame_cinnamoroll_transparent
        }

        Box(
            modifier = Modifier
                .width(220.dp)
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = frameResource),
                contentDescription = "Marco de $equippedPetName",
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.TopCenter)
                    .offset(x = 2.dp),
                contentScale = ContentScale.FillBounds
            )

            Box(contentAlignment = Alignment.BottomEnd) {
                val avatarModifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(2.dp, themeColor, CircleShape)
                    .clickable(enabled = !isUploadingAvatar) { imagePicker.launch("image/*") }

                val avatarModel = localAvatarUri ?: profile.avatarUrl

                if (avatarModel != null) {
                    AsyncImage(
                        model = avatarModel,
                        contentDescription = "Foto de perfil",
                        modifier = avatarModifier,
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = avatarModifier.background(themeColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = themeColor
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(themeColor)
                        .border(2.dp, White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUploadingAvatar) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Cambiar foto",
                            modifier = Modifier.size(16.dp),
                            tint = White
                        )
                    }
                }
            }
        }

        if (avatarMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = avatarMessage.orEmpty(),
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (editingName) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = {
                        val newName = tempName.trim().ifEmpty { "Estudiante" }
                        val updatedProfile = profile.copy(name = newName)
                        profile = updatedProfile
                        editingName = false
                        onProfileChanged(newName)
                        scope.launch {
                            FirebaseManager.saveProfile(updatedProfile)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK")
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = profile.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = themeColor,
                    modifier = Modifier.weight(1f, fill = false),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = {
                    tempName = profile.name
                    editingName = true
                }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar nombre",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                    Text("Tiempo estudiado", fontSize = 11.sp, color = TextSecondary)
                }
                HorizontalDivider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = themeColor.copy(alpha = 0.2f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$completedTasks",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                    Text("Tareas completadas", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Configuracion",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Pomodoro", fontSize = 15.sp, color = TextPrimary)
                        Text(
                            "Se configura al iniciar una sesion",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        "${profile.pomodoroDuration} min",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = themeColor.copy(alpha = 0.2f),
                contentColor = themeColor
            )
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Cerrar sesion",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileBackgroundDecorations(accentColor: Color) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "✦",
            color = accentColor.copy(alpha = 0.22f),
            fontSize = 42.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 42.dp, end = 26.dp)
        )
        Text(
            text = "★",
            color = Color(0xFFFFD96F).copy(alpha = 0.58f),
            fontSize = 18.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 116.dp, start = 20.dp)
        )
        Text(
            text = "✦",
            color = accentColor.copy(alpha = 0.18f),
            fontSize = 24.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 176.dp, end = 18.dp)
        )
        Text(
            text = "✦",
            color = White.copy(alpha = 0.86f),
            fontSize = 30.sp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp)
        )
        Text(
            text = "♥",
            color = Color(0xFFFF91AD).copy(alpha = 0.42f),
            fontSize = 22.sp,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp, top = 96.dp)
        )
        Text(
            text = "★",
            color = White.copy(alpha = 0.82f),
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 42.dp, top = 150.dp)
        )
        Text(
            text = "♥",
            color = accentColor.copy(alpha = 0.18f),
            fontSize = 34.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 26.dp, bottom = 92.dp)
        )
        Text(
            text = "✦",
            color = accentColor.copy(alpha = 0.14f),
            fontSize = 56.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 18.dp, bottom = 156.dp)
        )
    }
}
