package com.example.smartpetain

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth
object FirebaseManager {

    private val db = FirebaseFirestore.getInstance()
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "anonimo"

    // Guardar tiempo de estudio
    suspend fun saveStudySession(minutes: Int, character: String = "Cinnamoroll") {
        try {
            val session = hashMapOf(
                "minutes"   to minutes,
                "character" to character,
                "timestamp" to System.currentTimeMillis(),
                "date"      to getCurrentDate()
            )
            db.collection("users")
                .document(userId)
                .collection("sessions")
                .add(session)
                .await()

            // Actualizar estadísticas del día automáticamente
            saveDailyStats(minutes)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Guardar tarea
    suspend fun saveTask(task: Task) {
        try {
            val taskMap = hashMapOf(
                "id" to task.id,
                "title" to task.title,
                "subject" to task.subject,
                "priority" to task.priority,
                "dueDate" to task.dueDate,
                "isCompleted" to task.isCompleted
            )
            db.collection("users")
                .document(userId)
                .collection("tasks")
                .document(task.id.toString())
                .set(taskMap)
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Cargar tareas
    suspend fun loadTasks(): List<Task> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("tasks")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                Task(
                    id = (doc.getLong("id") ?: 0).toInt(),
                    title = doc.getString("title") ?: "",
                    subject = doc.getString("subject") ?: "",
                    priority = doc.getString("priority") ?: "Media",
                    dueDate = doc.getString("dueDate") ?: "",
                    isCompleted = doc.getBoolean("isCompleted") ?: false
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Guardar estadísticas del día
    suspend fun saveDailyStats(minutes: Int) {
        try {
            val date = getCurrentDate()
            val statsRef = db.collection("users")
                .document(userId)
                .collection("dailyStats")
                .document(date)

            val current = statsRef.get().await()
            val currentMinutes = current.getLong("minutes")?.toInt() ?: 0

            statsRef.set(hashMapOf(
                "date" to date,
                "minutes" to (currentMinutes + minutes)
            )).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Cargar estadísticas de la semana
    suspend fun loadWeekStats(): List<DayStats> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("dailyStats")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                DayStats(
                    day = doc.getString("date")?.takeLast(5) ?: "",
                    minutes = (doc.getLong("minutes") ?: 0).toInt()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun getCurrentDate(): String {
        val cal = java.util.Calendar.getInstance()
        return "%04d-%02d-%02d".format(
            cal.get(java.util.Calendar.YEAR),
            cal.get(java.util.Calendar.MONTH) + 1,
            cal.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }
    suspend fun saveProfile(name: String, pomodoroDuration: Int) {
        try {
            db.collection("users")
                .document(userId)
                .collection("profile")
                .document("data")
                .set(hashMapOf(
                    "name" to name,
                    "pomodoroDuration" to pomodoroDuration
                ))
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadProfile(): Pair<String, Int> {
        return try {
            val doc = db.collection("users")
                .document(userId)
                .collection("profile")
                .document("data")
                .get()
                .await()
            val name = doc.getString("name") ?: "Estudiante"
            val pomodoro = doc.getLong("pomodoroDuration")?.toInt() ?: 25
            Pair(name, pomodoro)
        } catch (e: Exception) {
            Pair("Estudiante", 25)
        }
    }}