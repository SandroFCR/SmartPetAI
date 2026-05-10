package com.example.smartpetain

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.smartpetain.ui.theme.*
import kotlinx.coroutines.launch
@Composable
fun ProfileScreen(
    totalStudyMinutes: Int = 0,
    completedTasks: Int = 0,
    onPomodoroChanged: (Int) -> Unit = {}

) {
    var name by remember { mutableStateOf("Cargando...") }
    var editingName by remember { mutableStateOf(false) }
    var tempName by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var pomodoroDuration by remember { mutableStateOf(25) }
    var darkMode by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

// Cargar perfil al iniciar
    LaunchedEffect(Unit) {
        val (loadedName, loadedPomodoro) = FirebaseManager.loadProfile()
        name = loadedName
        tempName = loadedName
        pomodoroDuration = loadedPomodoro
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) avatarUri = uri }

    val hours = totalStudyMinutes / 60
    val minutes = totalStudyMinutes % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Perfil",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Avatar ──
        Box(contentAlignment = Alignment.BottomEnd) {
            if (avatarUri != null) {
                AsyncImage(
                    model = avatarUri,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(2.dp, PurplePrimary, CircleShape)
                        .clickable { imagePicker.launch("image/*") },
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(PurpleLight)
                        .border(2.dp, PurplePrimary, CircleShape)
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = PurplePrimary
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(PurplePrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Cambiar foto",
                    modifier = Modifier.size(16.dp),
                    tint = White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Nombre ──
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
                        name = tempName.trim().ifEmpty { "Valeria" }
                        editingName = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("✓")
                }
                Button(
                    onClick = {
                        name = tempName.trim().ifEmpty { "Estudiante" }
                        editingName = false
                        scope.launch {
                            FirebaseManager.saveProfile(name, pomodoroDuration)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("✓")
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = {
                    tempName = name
                    editingName = true
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar nombre",
                        tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = {
                    if (pomodoroDuration > 5) {
                        pomodoroDuration -= 5
                        scope.launch { FirebaseManager.saveProfile(name, pomodoroDuration) }
                    }
                }) {
                    Text("−", fontSize = 20.sp, color = PurplePrimary, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = {
                    if (pomodoroDuration < 60) {
                        pomodoroDuration += 5
                        scope.launch { FirebaseManager.saveProfile(name, pomodoroDuration) }
                    }
                }) {
                    Text("+", fontSize = 20.sp, color = PurplePrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Stats rápidas ──
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
                        color = PurplePrimary
                    )
                    Text("Tiempo estudiado", fontSize = 11.sp, color = TextSecondary)
                }
                Divider(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp),
                    color = PurpleLight
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$completedTasks",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PurplePrimary
                    )
                    Text("Tareas completadas", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Configuración ──
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Configuración",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Duración Pomodoro
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Duración Pomodoro", fontSize = 15.sp, color = TextPrimary)
                        Text("$pomodoroDuration minutos", fontSize = 12.sp, color = TextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            if (pomodoroDuration > 5) pomodoroDuration -= 5
                        }) {
                            Text("−", fontSize = 20.sp, color = PurplePrimary,
                                fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "$pomodoroDuration",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        IconButton(onClick = {
                            if (pomodoroDuration < 60) pomodoroDuration += 5
                        }) {
                            Text("+", fontSize = 20.sp, color = PurplePrimary,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                    pomodoroDuration -= 5
                    onPomodoroChanged(pomodoroDuration)

                    pomodoroDuration += 5
                    onPomodoroChanged(pomodoroDuration)

                }

                Divider(color = PurpleLight, thickness = 0.5.dp,
                    modifier = Modifier.padding(vertical = 12.dp))

                // Dark mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Modo oscuro", fontSize = 15.sp, color = TextPrimary)
                    Switch(
                        checked = darkMode,
                        onCheckedChange = { darkMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PurplePrimary,
                            checkedTrackColor = PurpleLight
                        )
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(32.dp))
    }
}