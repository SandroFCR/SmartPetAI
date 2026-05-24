package com.example.smartpetain

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class UserProfile(
    val name: String = "Estudiante",
    val pomodoroDuration: Int = 25,
    val avatarUrl: String? = null
)

data class StudySessionRecord(
    val minutes: Int,
    val character: String,
    val date: String,
    val timestamp: Long
)

object FirebaseManager {

    private const val TAG = "FirebaseManager"

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val functions = FirebaseFunctions.getInstance()
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "anonimo"

    suspend fun saveStudySession(
        minutes: Int,
        character: String = "Cinnamoroll",
        updateDailyStats: Boolean = true
    ) {
        if (minutes <= 0) return

        try {
            val session = hashMapOf(
                "minutes" to minutes,
                "character" to character,
                "timestamp" to System.currentTimeMillis(),
                "date" to getCurrentDate()
            )
            db.collection("users")
                .document(userId)
                .collection("sessions")
                .add(session)
                .await()

            if (updateDailyStats) {
                saveDailyStats(minutes)
            }
            incrementDailyCompletedSessions()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveTask(task: Task) {
        try {
            val taskMap = hashMapOf(
                "id" to task.id,
                "title" to task.title,
                "subject" to task.subject,
                "priority" to task.priority,
                "emoji" to task.emoji,
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

    suspend fun deleteTask(taskId: Int) {
        try {
            db.collection("users")
                .document(userId)
                .collection("tasks")
                .document(taskId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadTasks(): List<Task> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("tasks")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val task = Task(
                    id = (doc.getLong("id") ?: 0).toInt(),
                    title = doc.getString("title") ?: "",
                    subject = doc.getString("subject") ?: "",
                    priority = doc.getString("priority") ?: "Media",
                    emoji = doc.getString("emoji") ?: "",
                    dueDate = doc.getString("dueDate") ?: "",
                    isCompleted = doc.getBoolean("isCompleted") ?: false
                )
                if (isSampleTask(task)) {
                    doc.reference.delete().await()
                    null
                } else {
                    task
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun isSampleTask(task: Task): Boolean {
        return task.title == "Resolver ejercicios de integrales" ||
            task.title.startsWith("Leer cap") ||
            task.title == "Proyecto en Flutter" ||
            task.title == "Practicar tiempos verbales"
    }

    suspend fun saveDailyStats(minutes: Int) {
        if (minutes <= 0) return

        try {
            val date = getCurrentDate()
            val statsRef = db.collection("users")
                .document(userId)
                .collection("dailyStats")
                .document(date)

            db.runTransaction { transaction ->
                val current = transaction.get(statsRef)
                val currentMinutes = current.getLong("minutes")?.toInt() ?: 0
                transaction.set(
                    statsRef,
                    hashMapOf(
                        "date" to date,
                        "minutes" to (currentMinutes + minutes)
                    ),
                    SetOptions.merge()
                )
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun incrementDailyCompletedSessions() {
        try {
            val date = getCurrentDate()
            val statsRef = db.collection("users")
                .document(userId)
                .collection("dailyStats")
                .document(date)

            db.runTransaction { transaction ->
                val current = transaction.get(statsRef)
                val currentSessions = current.getLong("completedSessions")?.toInt() ?: 0
                transaction.set(
                    statsRef,
                    hashMapOf(
                        "date" to date,
                        "completedSessions" to (currentSessions + 1)
                    ),
                    SetOptions.merge()
                )
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loadWeekStats(): List<DayStats> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("dailyStats")
                .get()
                .await()

            val minutesByDate = snapshot.documents.associate { doc ->
                val date = doc.getString("date") ?: doc.id
                val minutes = (doc.getLong("minutes") ?: 0).toInt()
                date to minutes
            }
            val completedByDate = snapshot.documents.associate { doc ->
                val date = doc.getString("date") ?: doc.id
                val completedSessions = (doc.getLong("completedSessions") ?: 0).toInt()
                date to completedSessions
            }

            getCurrentWeekDates().map { (label, date) ->
                DayStats(
                    day = label,
                    minutes = minutesByDate[date] ?: 0,
                    completedSessions = completedByDate[date] ?: 0
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            getCurrentWeekDates().map { (label, _) ->
                DayStats(day = label, minutes = 0)
            }
        }
    }

    suspend fun loadTotalStudyMinutes(): Int {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("dailyStats")
                .get()
                .await()

            snapshot.documents.sumOf { (it.getLong("minutes") ?: 0).toInt() }
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }

    suspend fun loadRecentStudySessions(limit: Long = 5): List<StudySessionRecord> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("sessions")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            snapshot.documents.map { doc ->
                StudySessionRecord(
                    minutes = (doc.getLong("minutes") ?: 0).toInt(),
                    character = doc.getString("character") ?: "Cinnamoroll",
                    date = doc.getString("date") ?: "",
                    timestamp = doc.getLong("timestamp") ?: 0L
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun saveProfile(profile: UserProfile) {
        try {
            db.collection("users")
                .document(userId)
                .collection("profile")
                .document("data")
                .set(
                    hashMapOf(
                        "name" to profile.name,
                        "pomodoroDuration" to profile.pomodoroDuration,
                        "avatarUrl" to profile.avatarUrl
                    ),
                    SetOptions.merge()
                )
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveProfile(name: String, pomodoroDuration: Int) {
        val current = loadProfile()
        saveProfile(
            current.copy(
                name = name,
                pomodoroDuration = pomodoroDuration
            )
        )
    }

    suspend fun savePomodoroDuration(pomodoroDuration: Int) {
        try {
            db.collection("users")
                .document(userId)
                .collection("profile")
                .document("data")
                .set(
                    hashMapOf("pomodoroDuration" to pomodoroDuration),
                    SetOptions.merge()
                )
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun uploadProfileImage(uri: Uri): String? {
        return try {
            val ref = storage.reference
                .child("users")
                .child(userId)
                .child("profile.jpg")

            ref.putFile(uri).await()
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun loadProfile(): UserProfile {
        return try {
            val doc = db.collection("users")
                .document(userId)
                .collection("profile")
                .document("data")
                .get()
                .await()

            UserProfile(
                name = doc.getString("name") ?: "Estudiante",
                pomodoroDuration = doc.getLong("pomodoroDuration")?.toInt() ?: 25,
                avatarUrl = doc.getString("avatarUrl")
            )
        } catch (e: Exception) {
            UserProfile()
        }
    }

    suspend fun generateStudyCoachRecommendation(
        weekStats: List<DayStats>,
        dailyGoalMinutes: Int,
        completedTasks: Int,
        pendingTasks: Int,
        fallback: CoachRecommendation
    ): CoachRecommendation {
        return try {
            val payload = hashMapOf(
                "dailyGoalMinutes" to dailyGoalMinutes,
                "completedTasks" to completedTasks,
                "pendingTasks" to pendingTasks,
                "weekStats" to weekStats.map { day ->
                    hashMapOf(
                        "day" to day.day,
                        "minutes" to day.minutes,
                        "completedSessions" to day.completedSessions
                    )
                }
            )

            val result = functions
                .getHttpsCallable("generateStudyCoach")
                .call(payload)
                .await()
            val data = result.data as? Map<*, *> ?: return fallback

            CoachRecommendation(
                characterName = data["characterName"] as? String ?: fallback.characterName,
                title = data["title"] as? String ?: fallback.title,
                message = data["message"] as? String ?: fallback.message,
                reason = data["reason"] as? String ?: fallback.reason,
                weeklyProgress = data["weeklyProgress"] as? String ?: fallback.weeklyProgress,
                source = "ai"
            )
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo generar recomendacion con IA", e)
            fallback
        }
    }

    private fun getCurrentWeekDates(): List<Pair<String, String>> {
        val labels = listOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom")
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        return labels.map { label ->
            val date = formatter.format(cal.time)
            cal.add(Calendar.DAY_OF_MONTH, 1)
            label to date
        }
    }

    private fun getCurrentDate(): String {
        val cal = Calendar.getInstance()
        return "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }
}
