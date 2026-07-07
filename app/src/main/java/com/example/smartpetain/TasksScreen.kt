package com.example.smartpetain

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartpetain.ui.theme.AmberLight
import com.example.smartpetain.ui.theme.AmberPrimary
import com.example.smartpetain.ui.theme.PinkLight
import com.example.smartpetain.ui.theme.PinkPrimary
import com.example.smartpetain.ui.theme.PurpleLight
import com.example.smartpetain.ui.theme.PurplePrimary
import com.example.smartpetain.ui.theme.TealLight
import com.example.smartpetain.ui.theme.TealPrimary
import com.example.smartpetain.ui.theme.TextSecondary
import com.example.smartpetain.ui.theme.White
import com.example.smartpetain.ui.theme.DashboardBlueBackground
import com.example.smartpetain.ui.theme.DashboardBluePrimary
import java.util.Calendar
import kotlinx.coroutines.launch

data class Task(
    val id: Int,
    val name: String,
    val subject: String,
    val priority: String,
    val emoji: String = "",
    val dueDate: String = "",
    var isCompleted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<Task>,
    onBack: () -> Unit,
    initialOpenAddDialog: Boolean = false,
    initialSortBy: String = "",
    onTasksChanged: (List<Task>) -> Unit = {},
    equippedPet: String = "Cinnamoroll"
) {
    val isDark = isSystemInDarkTheme()
    val scope = rememberCoroutineScope()

    val petThemeColor = when (equippedPet) {
        "Pompompurin" -> Color(0xFFFFF4C5)
        "Hello Kitty" -> Color(0xFFFCE4EC)
        else -> DashboardBlueBackground
    }

    val petAccentColor = when (equippedPet) {
        "Pompompurin" -> Color(0xFFE8A900)
        "Hello Kitty" -> Color(0xFFD4537E)
        else -> DashboardBluePrimary
    }

    val petTextColor = when (equippedPet) {
        "Pompompurin" -> Color(0xFF5A1C05)
        "Hello Kitty" -> Color(0xFF5A1C30)
        else -> Color(0xFF052A5A)
    }

    val petButtonColor = when (equippedPet) {
        "Pompompurin" -> Color(0xFFFFC400)
        "Hello Kitty" -> Color(0xFFE95F95)
        else -> DashboardBluePrimary
    }

    var showDialog by remember { mutableStateOf(initialOpenAddDialog) }
    var sortBy by remember { mutableStateOf(initialSortBy.ifBlank { "none" }) }
    var selectedTab by remember { mutableStateOf("pending") }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskSubject by remember { mutableStateOf("") }
    var newTaskEmoji by remember { mutableStateOf("") }

    val completedCount = tasks.count { it.isCompleted }
    val pendingCount = tasks.count { !it.isCompleted }

    val priorityOrder = mapOf("Alta" to 0, "Media" to 1, "Baja" to 2)
    val visibleTasks = tasks.filter {
        if (selectedTab == "pending") !it.isCompleted else it.isCompleted
    }
    val sortedTasks = when (sortBy) {
        "priority" -> visibleTasks.sortedBy { priorityOrder[it.priority] ?: 3 }
        "date" -> visibleTasks.sortedBy { it.dueDate.ifBlank { "9999-99-99" } }
        else -> visibleTasks
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) MaterialTheme.colorScheme.background else petThemeColor)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = if (isDark) MaterialTheme.colorScheme.onBackground else petTextColor
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Filled.MoreHoriz,
                contentDescription = null,
                tint = if (isDark) MaterialTheme.colorScheme.onBackground else petTextColor
            )
        }

        Text(
            text = "    Mis tareas  ",
            fontSize = 46.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) MaterialTheme.colorScheme.primary else petTextColor,
            modifier = Modifier.padding(start = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.StarBorder,
                contentDescription = null,
                tint = petAccentColor.copy(alpha = 0.55f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(y = 10.dp)
            )
            Image(
                painter = painterResource(id = when(equippedPet) {
                    "Pompompurin" -> R.drawable.pompompurin
                    "Hello Kitty" -> R.drawable.hello_kitty
                    else -> R.drawable.cinnamoroll
                }),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(width = 178.dp, height = 166.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-16).dp, y = 8.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 142.dp, top = 34.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Organicemos tu dia!",
                        fontSize = 16.sp,
                        color = if (isDark) MaterialTheme.colorScheme.primary else petTextColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tu puedes con todo.",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.StarBorder,
                contentDescription = null,
                tint = petAccentColor.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = 8.dp)
                    .size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TaskTabChip(
                    text = "Pendientes",
                    count = pendingCount,
                    selected = selectedTab == "pending",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = "pending" },
                    petThemeColor = petThemeColor,
                    petTextColor = petTextColor
                )
                TaskTabChip(
                    text = "Completadas",
                    count = completedCount,
                    selected = selectedTab == "completed",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = "completed" },
                    petThemeColor = petThemeColor,
                    petTextColor = petTextColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            listOf(
                "none" to "Normal",
                "priority" to "Prioridad",
                "date" to "Fecha"
            ).forEach { (key, label) ->
                val isSelected = sortBy == key
                Surface(
                    onClick = { sortBy = key },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) (if (isDark) MaterialTheme.colorScheme.primary else petAccentColor) else MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = if (isSelected) White else (if (isDark) MaterialTheme.colorScheme.onSurface else petAccentColor),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (sortedTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedTab == "pending") {
                        "Aun no tienes tareas pendientes."
                    } else {
                        "Aun no tienes tareas completadas."
                    },
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sortedTasks) { task ->
                    TaskItem(
                        task = task,
                        onToggle = { toggledTask ->
                            val updatedTasks = tasks.map {
                                if (it.id == toggledTask.id) {
                                    it.copy(isCompleted = !it.isCompleted)
                                } else {
                                    it
                                }
                            }
                            onTasksChanged(updatedTasks)
                            val updatedTask = updatedTasks.first { it.id == toggledTask.id }
                            scope.launch { 
                                FirebaseManager.saveTask(updatedTask)
                                if (updatedTask.isCompleted) {
                                    FirebaseManager.incrementDailyStat("completedTasks")
                                }
                            }
                        },
                        onDelete = { deletedTask ->
                            val updatedTasks = tasks.filter { it.id != deletedTask.id }
                            onTasksChanged(updatedTasks)
                            scope.launch { FirebaseManager.deleteTask(deletedTask.id) }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) MaterialTheme.colorScheme.primary else petButtonColor)
        ) {
            Surface(shape = RoundedCornerShape(50.dp), color = if (isDark) MaterialTheme.colorScheme.surface else White) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = if (isDark) MaterialTheme.colorScheme.primary else petTextColor,
                    modifier = Modifier.padding(8.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Agregar tarea",
                fontSize = 22.sp,
                color = if (isDark) White else petTextColor,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showDialog) {
        var selectedPriority by remember { mutableStateOf("Media") }
        val calendar = Calendar.getInstance()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = calendar.timeInMillis
        )
        var showDatePicker by remember { mutableStateOf(false) }
        var showTimePicker by remember { mutableStateOf(false) }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE)
        )

        val selectedDateText = datePickerState.selectedDateMillis?.let { millis ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = millis
            "%04d-%02d-%02d".format(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        } ?: ""

        val selectedTimeText = "%02d:%02d".format(timePickerState.hour, timePickerState.minute)
        val combinedDateTimeText = if (selectedDateText.isNotBlank()) "$selectedDateText $selectedTimeText" else "Sin fecha"

        AlertDialog(
            onDismissRequest = { showDialog = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFFFFBEE),
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nueva tarea",
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) MaterialTheme.colorScheme.onSurface else petTextColor,
                        fontSize = 26.sp
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Tarea") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTaskSubject,
                        onValueChange = { newTaskSubject = it },
                        label = { Text("Materia") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newTaskEmoji,
                        onValueChange = { newTaskEmoji = it.take(4) },
                        label = { Text("Emoji") },
                        placeholder = { Text("Ej: 📘") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = combinedDateTimeText,
                        onValueChange = { },
                        label = { Text("Fecha y Hora") },
                        readOnly = true,
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { showDatePicker = true }) {
                                    Text("Fecha", fontSize = 12.sp)
                                }
                                TextButton(onClick = { showTimePicker = true }) {
                                    Text("Hora", fontSize = 12.sp)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Prioridad",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        listOf("Alta", "Media", "Baja").forEach { priority ->
                            val isSelected = selectedPriority == priority
                            val bgColor = when (priority) {
                                "Alta" -> if (isSelected) PinkPrimary else PinkLight
                                "Media" -> if (isSelected) AmberPrimary else AmberLight
                                else -> if (isSelected) TealPrimary else TealLight
                            }
                            val textColor = when (priority) {
                                "Alta" -> if (isSelected) White else PinkPrimary
                                "Media" -> if (isSelected) White else AmberPrimary
                                else -> if (isSelected) White else TealPrimary
                            }
                            Surface(
                                onClick = { selectedPriority = priority },
                                shape = RoundedCornerShape(20.dp),
                                color = bgColor
                            ) {
                                Text(
                                    text = priority,
                                    fontSize = 13.sp,
                                    color = textColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            val newTask = Task(
                                id = (tasks.maxOfOrNull { it.id } ?: 0) + 1,
                                name = newTaskTitle.trim(),
                                subject = newTaskSubject.trim().ifBlank { "General" },
                                priority = selectedPriority,
                                emoji = newTaskEmoji.trim().ifBlank { defaultTaskEmoji(selectedPriority) },
                                dueDate = combinedDateTimeText
                            )
                            val updatedTasks = tasks + newTask
                            onTasksChanged(updatedTasks)
                            scope.launch { 
                                FirebaseManager.saveTask(newTask)
                                FirebaseManager.incrementDailyStat("createdTasks")
                            }
                            newTaskTitle = ""
                            newTaskSubject = ""
                            newTaskEmoji = ""
                            showDialog = false
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) MaterialTheme.colorScheme.primary else petButtonColor)
                ) {
                    Text("Agregar", color = if (isDark) White else petTextColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(
                        onClick = { showDatePicker = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) MaterialTheme.colorScheme.primary else petButtonColor)
                    ) {
                        Text("Confirmar", color = if (isDark) White else petTextColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showTimePicker) {
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    Button(
                        onClick = { showTimePicker = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) MaterialTheme.colorScheme.primary else petButtonColor)
                    ) {
                        Text("Confirmar", color = if (isDark) White else petTextColor)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                },
                containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFFFFBEE),
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TimePicker(state = timePickerState)
                    }
                }
            )
        }
    }
}

@Composable
private fun TaskTabChip(
    text: String,
    count: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    petThemeColor: Color,
    petTextColor: Color
) {
    val isDark = isSystemInDarkTheme()
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) (if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else petThemeColor.copy(alpha = 0.8f)) else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (text == "Pendientes") "📋 $text" else "✓ $text",
                fontSize = 15.sp,
                color = if (selected) (if (isDark) MaterialTheme.colorScheme.primary else petTextColor) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = if (text == "Pendientes") PinkPrimary else TealPrimary
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 13.sp,
                    color = White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onToggle: (Task) -> Unit, onDelete: (Task) -> Unit) {
    val isDark = isSystemInDarkTheme()
    val priorityColor = when (task.priority) {
        "Alta" -> PinkPrimary
        "Media" -> AmberPrimary
        else -> TealPrimary
    }
    val priorityBg = when (task.priority) {
        "Alta" -> PinkLight
        "Media" -> AmberLight
        else -> TealLight
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) (if (isDark) Color(0xFF1B2B1B) else Color(0xFFF5FFF0)) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle(task) },
                colors = CheckboxDefaults.colors(
                    checkedColor = TealPrimary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = task.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) TealPrimary else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = task.subject, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Surface(shape = RoundedCornerShape(10.dp), color = if (isDark) priorityBg.copy(alpha = 0.2f) else priorityBg) {
                        Text(
                            text = task.priority,
                            fontSize = 11.sp,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                if (task.dueDate.isNotBlank()) {
                    Text(
                        text = task.dueDate,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isDark) priorityBg.copy(alpha = 0.2f) else priorityBg,
                modifier = Modifier.size(58.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = task.emoji.ifBlank { defaultTaskEmoji(task.priority) },
                        fontSize = 28.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = { onDelete(task) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

private fun defaultTaskEmoji(priority: String): String {
    return when (priority) {
        "Alta" -> "💻"
        "Media" -> "📘"
        else -> "📒"
    }
}
