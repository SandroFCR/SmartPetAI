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
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
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
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var sortBy by remember { mutableStateOf(initialSortBy.ifBlank { "none" }) }
    var selectedTab by remember { mutableStateOf("pending") }
    var newTaskTitle by remember(taskToEdit) { mutableStateOf(taskToEdit?.name ?: "") }
    var newTaskSubject by remember(taskToEdit) { mutableStateOf(taskToEdit?.subject ?: "") }
    var newTaskEmoji by remember(taskToEdit) { mutableStateOf(taskToEdit?.emoji ?: "") }
    var selectedPriority by remember(taskToEdit) { mutableStateOf(taskToEdit?.priority ?: "Media") }

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
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

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
            text = "Mis tareas",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) MaterialTheme.colorScheme.primary else petTextColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Compact Mascot Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp) // Reduced height from 170
        ) {
            Image(
                painter = painterResource(id = when(equippedPet) {
                    "Pompompurin" -> R.drawable.pompompurin
                    "Hello Kitty" -> R.drawable.hello_kitty
                    else -> R.drawable.cinnamoroll
                }),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(width = 120.dp, height = 110.dp) // Reduced size
                    .align(Alignment.BottomStart)
                    .offset(x = (-8).dp, y = 4.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 125.dp, top = 24.dp), // Increased start padding to not cover ear
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Organicemos tu día!",
                        fontSize = 14.sp,
                        color = if (isDark) MaterialTheme.colorScheme.primary else petTextColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tu puedes con todo.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Compact Tabs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TaskTabChip(
                    text = "Pendientes",
                    count = pendingCount,
                    selected = selectedTab == "pending",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = "pending" },
                    petThemeColor = petThemeColor,
                    petTextColor = petTextColor,
                    petAccentColor = petAccentColor
                )
                TaskTabChip(
                    text = "Completadas",
                    count = completedCount,
                    selected = selectedTab == "completed",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = "completed" },
                    petThemeColor = petThemeColor,
                    petTextColor = petTextColor,
                    petAccentColor = petAccentColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Compact Sorting Chips
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
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) (if (isDark) MaterialTheme.colorScheme.primary else petAccentColor) else MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = if (isSelected) White else (if (isDark) MaterialTheme.colorScheme.onSurface else petAccentColor),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                verticalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing
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
                        },
                        onEdit = { task ->
                            taskToEdit = task
                            showDialog = true
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { 
                taskToEdit = null
                showDialog = true 
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), // Reduced height from 64
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) MaterialTheme.colorScheme.primary else petButtonColor)
        ) {
            Surface(shape = RoundedCornerShape(50.dp), color = if (isDark) MaterialTheme.colorScheme.surface else White) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = if (isDark) MaterialTheme.colorScheme.primary else petTextColor,
                    modifier = Modifier.padding(6.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Agregar tarea",
                fontSize = 18.sp,
                color = if (isDark) White else petTextColor,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showDialog) {
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
        val combinedDateTimeText = if (selectedDateText.isNotBlank()) "$selectedDateText $selectedTimeText" else (taskToEdit?.dueDate ?: "Sin fecha")

        AlertDialog(
            onDismissRequest = { 
                showDialog = false 
                taskToEdit = null
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFFFFBEE),
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (equippedPet == "Pompompurin") {
                            Image(
                                painter = painterResource(id = R.drawable.pompompurin), // Assuming this is the new image ID or similar
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(bottom = 8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        Text(
                            text = if (taskToEdit != null) "Editar tarea" else "Nueva tarea",
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) MaterialTheme.colorScheme.onSurface else petTextColor,
                            fontSize = 24.sp
                        )
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
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
                                    Text("Fecha", fontSize = 11.sp)
                                }
                                TextButton(onClick = { showTimePicker = true }) {
                                    Text("Hora", fontSize = 11.sp)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Prioridad",
                        fontSize = 13.sp,
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
                                    fontSize = 12.sp,
                                    color = textColor,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            val updatedTask = if (taskToEdit != null) {
                                taskToEdit!!.copy(
                                    name = newTaskTitle.trim(),
                                    subject = newTaskSubject.trim(),
                                    priority = selectedPriority,
                                    emoji = newTaskEmoji.trim().ifBlank { taskToEdit!!.emoji },
                                    dueDate = combinedDateTimeText
                                )
                            } else {
                                Task(
                                    id = (tasks.maxOfOrNull { it.id } ?: 0) + 1,
                                    name = newTaskTitle.trim(),
                                    subject = newTaskSubject.trim().ifBlank { "General" },
                                    priority = selectedPriority,
                                    emoji = newTaskEmoji.trim().ifBlank { defaultTaskEmoji(selectedPriority) },
                                    dueDate = combinedDateTimeText
                                )
                            }
                            
                            val updatedTasks = if (taskToEdit != null) {
                                tasks.map { if (it.id == updatedTask.id) updatedTask else it }
                            } else {
                                tasks + updatedTask
                            }
                            
                            onTasksChanged(updatedTasks)
                            scope.launch { 
                                FirebaseManager.saveTask(updatedTask)
                                if (taskToEdit == null) {
                                    FirebaseManager.incrementDailyStat("createdTasks")
                                }
                            }
                            newTaskTitle = ""
                            newTaskSubject = ""
                            newTaskEmoji = ""
                            showDialog = false
                            taskToEdit = null
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (equippedPet == "Pompompurin") Color(0xFFFFC400) else if (isDark) MaterialTheme.colorScheme.primary else petButtonColor
                    )
                ) {
                    Text(
                        if (taskToEdit != null) "Guardar" else "Agregar", 
                        color = if (equippedPet == "Pompompurin") Color(0xFF5A1C05) else if (isDark) White else petTextColor, 
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDialog = false 
                    taskToEdit = null
                }) {
                    Text("Cancelar", color = if (equippedPet == "Pompompurin") Color(0xFFE8A900) else TextSecondary)
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
    petTextColor: Color,
    petAccentColor: Color
) {
    val isDark = isSystemInDarkTheme()
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp), // More compact
        color = if (selected) (if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else petThemeColor.copy(alpha = 0.8f)) else Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (text == "Pendientes") "📋 $text" else "✓ $text",
                fontSize = 13.sp, // Reduced font
                color = if (selected) (if (isDark) MaterialTheme.colorScheme.primary else petTextColor) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = if (selected) petAccentColor else Color.Gray.copy(alpha = 0.3f)
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 11.sp, // Reduced font
                    color = White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onToggle: (Task) -> Unit, onDelete: (Task) -> Unit, onEdit: (Task) -> Unit) {
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
        shape = RoundedCornerShape(18.dp), // More compact corners
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) (if (isDark) Color(0xFF1B2B1B) else Color(0xFFF5FFF0)) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp), // Reduced padding from 18
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle(task) },
                colors = CheckboxDefaults.colors(
                    checkedColor = TealPrimary,
                    uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                ),
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = task.name,
                    fontSize = 16.sp, // Reduced from 18
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) TealPrimary else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = task.subject, 
                        fontSize = 11.sp, 
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(shape = RoundedCornerShape(8.dp), color = if (isDark) priorityBg.copy(alpha = 0.2f) else priorityBg) {
                        Text(
                            text = task.priority,
                            fontSize = 10.sp,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
                if (task.dueDate.isNotBlank()) {
                    Text(
                        text = task.dueDate,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) priorityBg.copy(alpha = 0.2f) else priorityBg,
                modifier = Modifier.size(44.dp) // Reduced from 58
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = task.emoji.ifBlank { defaultTaskEmoji(task.priority) },
                        fontSize = 20.sp // Reduced from 28
                    )
                }
            }
            IconButton(
                onClick = { onEdit(task) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = { onDelete(task) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
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
