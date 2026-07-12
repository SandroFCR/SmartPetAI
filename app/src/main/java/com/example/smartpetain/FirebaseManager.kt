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
    val avatarUrl: String? = null,
    val equippedPet: String = "Cinnamoroll",
    val alarmSoundUri: String? = null
)

data class StudySessionRecord(
    val minutes: Int,
    val character: String,
    val date: String,
    val timestamp: Long
)

data class PomodoroStatsSummary(
    val totalPomodoros: Int = 0,
    val totalStudyMinutes: Int = 0,
    val totalBreakMinutes: Int = 0,
    val todayPomodoros: Int = 0,
    val weekPomodoros: Int = 0,
    val monthPomodoros: Int = 0,
    val bestProductivityStreak: Int = 0,
    val consecutivePomodoroDays: Int = 0,
    val averageDailySessions: Double = 0.0
)

data class PetStats(
    val name: String = "",
    val level: Int = 1,
    val xp: Int = 0
) {
    val maxXp: Int get() = level * 100
}

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
            incrementDailyStat("completedSessions")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveTask(task: Task) {
        Log.d(TAG, "Saving task for user $userId: ${task.name}")
        try {
            val taskMap = hashMapOf(
                "id" to task.id,
                "name" to task.name,
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
            Log.d(TAG, "Task saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving task", e)
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
        Log.d(TAG, "Loading tasks for user $userId")
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("tasks")
                .get()
                .await()

            val tasks = snapshot.documents.mapNotNull { doc ->
                Task(
                    id = (doc.getLong("id") ?: 0).toInt(),
                    name = doc.getString("name") ?: doc.getString("title") ?: "",
                    subject = doc.getString("subject") ?: "",
                    priority = doc.getString("priority") ?: "Media",
                    emoji = doc.getString("emoji") ?: "",
                    dueDate = doc.getString("dueDate") ?: "",
                    isCompleted = doc.getBoolean("isCompleted") ?: false
                )
            }
            Log.d(TAG, "Loaded ${tasks.size} tasks")
            tasks
        } catch (e: Exception) {
            Log.e(TAG, "Error loading tasks", e)
            emptyList()
        }
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
            checkDailyMissions()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun incrementDailyStat(statName: String) {
        try {
            val date = getCurrentDate()
            val statsRef = db.collection("users")
                .document(userId)
                .collection("dailyStats")
                .document(date)

            db.runTransaction { transaction ->
                val current = transaction.get(statsRef)
                val currentVal = current.getLong(statName)?.toInt() ?: 0
                transaction.set(
                    statsRef,
                    hashMapOf(
                        "date" to date,
                        statName to (currentVal + 1)
                    ),
                    SetOptions.merge()
                )
            }.await()
            checkDailyMissions()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveBreakMinutes(minutes: Int) {
        if (minutes <= 0) return

        try {
            val date = getCurrentDate()
            val statsRef = db.collection("users")
                .document(userId)
                .collection("dailyStats")
                .document(date)

            db.runTransaction { transaction ->
                val current = transaction.get(statsRef)
                val currentBreakMinutes = current.getLong("breakMinutes")?.toInt() ?: 0
                val currentBreaks = current.getLong("breaks")?.toInt() ?: 0
                transaction.set(
                    statsRef,
                    hashMapOf(
                        "date" to date,
                        "breakMinutes" to (currentBreakMinutes + minutes),
                        "breaks" to (currentBreaks + 1)
                    ),
                    SetOptions.merge()
                )
            }.await()
            checkDailyMissions()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun checkDailyMissions() {
        try {
            val date = getCurrentDate()
            val statsRef = db.collection("users")
                .document(userId)
                .collection("dailyStats")
                .document(date)

            val snapshot = statsRef.get().await()
            if (!snapshot.exists()) return

            val minutes = snapshot.getLong("minutes") ?: 0
            val completedSessions = snapshot.getLong("completedSessions") ?: 0
            val createdTasks = snapshot.getLong("createdTasks") ?: 0
            val completedTasks = snapshot.getLong("completedTasks") ?: 0
            val breaks = snapshot.getLong("breaks") ?: 0
            val completedMissions = (snapshot.get("completedMissions") as? List<*>)?.map { it.toString().toInt() } ?: emptyList()

            // Cinnamoroll 1: Completa un Pomodoro (101)
            if (!completedMissions.contains(101) && completedSessions >= 1) {
                completeDailyMission("Cinnamoroll", 101, 25)
            }
            // Cinnamoroll 2: Estudia 10 min (102)
            if (!completedMissions.contains(102) && minutes >= 10) {
                completeDailyMission("Cinnamoroll", 102, 10)
            }
            // Pompompurin 1: Descansa 5 min (201)
            if (!completedMissions.contains(201) && breaks >= 1) {
                completeDailyMission("Pompompurin", 201, 15)
            }
            // Pompompurin 2: Cierra una tarea (202)
            if (!completedMissions.contains(202) && completedTasks >= 1) {
                completeDailyMission("Pompompurin", 202, 20)
            }
            // Hello Kitty 1: Agrega una tarea (301)
            if (!completedMissions.contains(301) && createdTasks >= 1) {
                completeDailyMission("Hello Kitty", 301, 10)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking missions", e)
        }
    }

    suspend fun loadWeekStats(): List<DayStats> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("dailyStats")
                .get()
                .await()

            val statsMap = snapshot.documents.associateBy { it.getString("date") ?: it.id }

            getCurrentWeekDates().map { (label, date) ->
                val doc = statsMap[date]
                DayStats(
                    day = label,
                    minutes = (doc?.getLong("minutes") ?: 0).toInt(),
                    completedSessions = (doc?.getLong("completedSessions") ?: 0).toInt(),
                    createdTasks = (doc?.getLong("createdTasks") ?: 0).toInt(),
                    completedTasks = (doc?.getLong("completedTasks") ?: 0).toInt(),
                    breaks = (doc?.getLong("breaks") ?: 0).toInt(),
                    breakMinutes = (doc?.getLong("breakMinutes") ?: 0).toInt(),
                    completedMissions = (doc?.get("completedMissions") as? List<*>)?.map { it.toString().toInt() } ?: emptyList()
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

    suspend fun loadPomodoroStatsSummary(): PomodoroStatsSummary {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("dailyStats")
                .get()
                .await()

            val today = getCurrentDate()
            val weekDates = getCurrentWeekDates().map { it.second }.toSet()
            val currentMonthPrefix = today.substring(0, 7)
            val dailyPomodoros = snapshot.documents.mapNotNull { doc ->
                val date = doc.getString("date") ?: doc.id
                if (date.length < 10) return@mapNotNull null
                DailyPomodoroTotals(
                    date = date,
                    studyMinutes = (doc.getLong("minutes") ?: 0).toInt(),
                    breakMinutes = (doc.getLong("breakMinutes") ?: 0).toInt(),
                    completedSessions = (doc.getLong("completedSessions") ?: 0).toInt()
                )
            }

            val totalPomodoros = dailyPomodoros.sumOf { it.completedSessions }
            val activeDays = dailyPomodoros.count { it.completedSessions > 0 }
            PomodoroStatsSummary(
                totalPomodoros = totalPomodoros,
                totalStudyMinutes = dailyPomodoros.sumOf { it.studyMinutes },
                totalBreakMinutes = dailyPomodoros.sumOf { it.breakMinutes },
                todayPomodoros = dailyPomodoros
                    .filter { it.date == today }
                    .sumOf { it.completedSessions },
                weekPomodoros = dailyPomodoros
                    .filter { it.date in weekDates }
                    .sumOf { it.completedSessions },
                monthPomodoros = dailyPomodoros
                    .filter { it.date.startsWith(currentMonthPrefix) }
                    .sumOf { it.completedSessions },
                bestProductivityStreak = calculateBestStreak(dailyPomodoros),
                consecutivePomodoroDays = calculateCurrentPomodoroStreak(dailyPomodoros, today),
                averageDailySessions = if (activeDays > 0) totalPomodoros.toDouble() / activeDays else 0.0
            )
        } catch (e: Exception) {
            e.printStackTrace()
            PomodoroStatsSummary()
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
        Log.d(TAG, "Saving profile for user $userId: ${profile.name}")
        try {
            db.collection("users")
                .document(userId)
                .collection("profile")
                .document("data")
                .set(
                    hashMapOf(
                        "name" to profile.name,
                        "pomodoroDuration" to profile.pomodoroDuration,
                        "avatarUrl" to profile.avatarUrl,
                        "equippedPet" to profile.equippedPet,
                        "alarmSoundUri" to profile.alarmSoundUri
                    ),
                    SetOptions.merge()
                )
                .await()
            Log.d(TAG, "Profile saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving profile", e)
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
                avatarUrl = doc.getString("avatarUrl"),
                equippedPet = doc.getString("equippedPet") ?: "Cinnamoroll",
                alarmSoundUri = doc.getString("alarmSoundUri")
            )
        } catch (e: Exception) {
            UserProfile()
        }
    }

    suspend fun loadPets(): List<PetStats> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("pets")
                .get()
                .await()

            if (snapshot.isEmpty) {
                val initialPets = listOf(
                    PetStats("Cinnamoroll"),
                    PetStats("Pompompurin"),
                    PetStats("Hello Kitty")
                )
                initialPets.forEach { pet ->
                    db.collection("users")
                        .document(userId)
                        .collection("pets")
                        .document(pet.name)
                        .set(pet)
                        .await()
                }
                initialPets
            } else {
                snapshot.documents.map { doc ->
                    PetStats(
                        name = doc.getString("name") ?: doc.id,
                        level = (doc.getLong("level") ?: 1).toInt(),
                        xp = (doc.getLong("xp") ?: 0).toInt()
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addPetXp(petName: String, amount: Int) {
        try {
            val petRef = db.collection("users")
                .document(userId)
                .collection("pets")
                .document(petName)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(petRef)
                val currentLevel = snapshot.getLong("level")?.toInt() ?: 1
                val currentXp = snapshot.getLong("xp")?.toInt() ?: 0

                var newXp = currentXp + amount
                var newLevel = currentLevel
                val maxXp = newLevel * 100

                if (newXp >= maxXp && newLevel < 5) {
                    newXp -= maxXp
                    newLevel++
                }

                transaction.update(petRef, "xp", newXp, "level", newLevel)
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveEquippedPet(petName: String) {
        try {
            db.collection("users")
                .document(userId)
                .collection("profile")
                .document("data")
                .set(hashMapOf("equippedPet" to petName), SetOptions.merge())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun completeDailyMission(petName: String, missionIndex: Int, xpReward: Int) {
        try {
            val date = getCurrentDate()
            val statsRef = db.collection("users")
                .document(userId)
                .collection("dailyStats")
                .document(date)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(statsRef)
                val completedMissions = (snapshot.get("completedMissions") as? List<*>)?.map { it.toString().toInt() }?.toMutableList() ?: mutableListOf()
                
                if (!completedMissions.contains(missionIndex)) {
                    completedMissions.add(missionIndex)
                    transaction.set(statsRef, hashMapOf("completedMissions" to completedMissions), SetOptions.merge())
                }
            }.await()
            
            addPetXp(petName, xpReward)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun generateStudyCoachRecommendation(
        weekStats: List<DayStats>,
        dailyGoalMinutes: Int,
        completedTasks: Int,
        pendingTasks: Int,
        pets: List<PetStats>,
        fallback: CoachRecommendation
    ): CoachRecommendation {
        return try {
            val payload = hashMapOf(
                "dailyGoalMinutes" to dailyGoalMinutes,
                "completedTasks" to completedTasks,
                "pendingTasks" to pendingTasks,
                "pets" to pets.map { pet ->
                    hashMapOf(
                        "name" to pet.name,
                        "level" to pet.level,
                        "xp" to pet.xp
                    )
                },
                "weekStats" to weekStats.map { day ->
                    val effective = day.minutes.coerceAtMost(dailyGoalMinutes)
                    hashMapOf(
                        "day" to day.day,
                        "minutes" to day.minutes,
                        "effectiveMinutes" to effective,
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

    private data class DailyPomodoroTotals(
        val date: String,
        val studyMinutes: Int,
        val breakMinutes: Int,
        val completedSessions: Int
    )

    private fun calculateBestStreak(days: List<DailyPomodoroTotals>): Int {
        var best = 0
        var current = 0
        var previousActiveDate: String? = null
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        days.sortedBy { it.date }.filter { it.completedSessions > 0 }.forEach { day ->
            val previous = previousActiveDate
            if (day.completedSessions > 0) {
                current = if (previous == null || isNextDay(previous, day.date, formatter)) {
                    current + 1
                } else {
                    1
                }
                previousActiveDate = day.date
                best = maxOf(best, current)
            } else {
                current = 0
            }
        }

        return best
    }

    private fun isNextDay(previousDate: String, currentDate: String, formatter: SimpleDateFormat): Boolean {
        val previous = formatter.parse(previousDate) ?: return false
        val current = formatter.parse(currentDate) ?: return false
        val cal = Calendar.getInstance()
        cal.time = previous
        cal.add(Calendar.DAY_OF_MONTH, 1)
        return formatter.format(cal.time) == formatter.format(current)
    }

    private fun calculateCurrentPomodoroStreak(days: List<DailyPomodoroTotals>, today: String): Int {
        val pomodoroDates = days
            .filter { it.completedSessions > 0 }
            .map { it.date }
            .toSet()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.time = formatter.parse(today) ?: return 0

        var streak = 0
        while (pomodoroDates.contains(formatter.format(cal.time))) {
            streak++
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }

        return streak
    }
}
