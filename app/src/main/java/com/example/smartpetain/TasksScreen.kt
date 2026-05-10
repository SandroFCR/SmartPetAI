package com.example.smartpetain

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartpetain.ui.theme.*
import java.util.Calendar
import kotlinx.coroutines.launch

data class Task(
    val id: Int,
    val title: String,
    val subject: String,
    val priority: String,
    val dueDate: String = "",
    var isCompleted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onBack: () -> Unit,
    onTasksChanged: (Int, Int) -> Unit = { _, _ -> }
) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }

    var tasks by remember {
        mutableStateOf(
            listOf(
                Task(1, "Resolver ejercicios de integrales", "Matemáticas", "Alta", "2026-05-10"),
                Task(2, "Leer capítulo 4: Revolución Industrial", "Historia", "Media", "2026-05-12"),
                Task(3, "Proyecto en Flutter", "Programación", "Alta", "2026-05-15"),
                Task(4, "Practicar tiempos verbales", "Inglés", "Baja", "2026-05-20")
            )
        )
    }

    // Cargar tareas desde Firebase al inicio
    LaunchedEffect(Unit) {
        val firebaseTasks = FirebaseManager.loadTasks()
        if (firebaseTasks.isNotEmpty()) {
            tasks = firebaseTasks
        }
        isLoading = false
    }

    var showDialog by remember { mutableStateOf(false) }
    var sortBy by remember { mutableStateOf("none") }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskSubject by remember { mutableStateOf("") }

    val completedCount = tasks.count { it.isCompleted }
    val pendingCount = tasks.count { !it.isCompleted }

    val priorityOrder = mapOf("Alta" to 0, "Media" to 1, "Baja" to 2)
    val sortedTasks = when (sortBy) {
        "priority" -> tasks.sortedBy { priorityOrder[it.priority] ?: 3 }
        "date" -> tasks.sortedBy { it.dueDate }
        else -> tasks
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(Background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PurplePrimary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Volver", color = PurplePrimary, fontSize = 16.sp)
            }
        }

        Text(
            text = "Mis tareas",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(20.dp), color = PurpleLight) {
                Text(
                    text = "📋 $pendingCount pendientes",
                    fontSize = 13.sp,
                    color = PurplePrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Surface(shape = RoundedCornerShape(20.dp), color = TealLight) {
                Text(
                    text = "✅ $completedCount completadas",
                    fontSize = 13.sp,
                    color = TealPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "none" to "📋 Normal",
                "priority" to "🔥 Prioridad",
                "date" to "📅 Fecha"
            ).forEach { (key, label) ->
                val isSelected = sortBy == key
                Surface(
                    onClick = { sortBy = key },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) PurplePrimary else PurpleLight
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = if (isSelected) White else PurplePrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sortedTasks) { task ->
                TaskItem(
                    task = task,
                    onToggle = { toggledTask ->
                        tasks = tasks.map {
                            if (it.id == toggledTask.id)
                                it.copy(isCompleted = !it.isCompleted)
                            else it
                        }
                        // Guardar en Firebase
                        val updatedTask = tasks.first { it.id == toggledTask.id }
                        scope.launch { FirebaseManager.saveTask(updatedTask) }
                        onTasksChanged(
                            tasks.count { !it.isCompleted },
                            tasks.count { it.isCompleted }
                        )
                    },
                    onDelete = { deletedTask ->
                        tasks = tasks.filter { it.id != deletedTask.id }
                        onTasksChanged(
                            tasks.count { !it.isCompleted },
                            tasks.count { it.isCompleted }
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
        ) {
            Text(text = "+ Agregar tarea", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }

    if (showDialog) {
        var selectedPriority by remember { mutableStateOf("Media") }
        val calendar = Calendar.getInstance()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = calendar.timeInMillis
        )
        var showDatePicker by remember { mutableStateOf(false) }

        val selectedDateText = datePickerState.selectedDateMillis?.let { millis ->
            val cal = Calendar.getInstance()
            cal.timeInMillis = millis
            "%04d-%02d-%02d".format(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        } ?: "Sin fecha"

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(text = "Nueva tarea", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        value = selectedDateText,
                        onValueChange = { },
                        label = { Text("Fecha") },
                        readOnly = true,
                        trailingIcon = {
                            TextButton(onClick = { showDatePicker = true }) {
                                Text("📅", fontSize = 18.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(text = "Prioridad", fontSize = 13.sp, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            val newTask = Task(
                                id = tasks.size + 1,
                                title = newTaskTitle,
                                subject = newTaskSubject.ifBlank { "General" },
                                priority = selectedPriority,
                                dueDate = selectedDateText
                            )
                            tasks = tasks + newTask
                            // Guardar en Firebase
                            scope.launch { FirebaseManager.saveTask(newTask) }
                            newTaskTitle = ""
                            newTaskSubject = ""
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Agregar")
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
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) { Text("Confirmar") }
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
    }
}

@Composable
fun TaskItem(task: Task, onToggle: (Task) -> Unit, onDelete: (Task) -> Unit) {
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) Background else White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle(task) },
                colors = CheckboxDefaults.colors(checkedColor = PurplePrimary)
            )
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (task.isCompleted) TextSecondary else TextPrimary,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = task.subject, fontSize = 12.sp, color = TextSecondary)
                    Surface(shape = RoundedCornerShape(10.dp), color = priorityBg) {
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
                        text = "📅 ${task.dueDate}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            TextButton(onClick = { onDelete(task) }) {
                Text("✕", color = TextSecondary, fontSize = 16.sp)
            }
        }
    }
}