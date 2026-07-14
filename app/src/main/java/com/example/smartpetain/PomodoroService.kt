package com.example.smartpetain

import android.app.*
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PomodoroService : Service() {

    private val binder = PomodoroBinder()
    
    private var timer: CountDownTimer? = null
    
    private val _timeLeft = MutableStateFlow(0L)
    val timeLeft: StateFlow<Long> = _timeLeft
    
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning
    
    private val _phase = MutableStateFlow("STUDY")
    val phase: StateFlow<String> = _phase

    private var currentSoundUri: Uri? = null
    private var blockedPackages: Set<String> = emptySet()
    private var isBlockerActive: Boolean = false

    inner class PomodoroBinder : Binder() {
        fun getService(): PomodoroService = this@PomodoroService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "START" -> {
                val minutes = intent.getIntExtra("MINUTES", 25)
                val phaseStr = intent.getStringExtra("PHASE") ?: "STUDY"
                val soundUriStr = intent.getStringExtra("SOUND_URI")
                val blocked = intent.getStringArrayExtra("BLOCKED_APPS")?.toSet() ?: emptySet()
                
                currentSoundUri = soundUriStr?.let { Uri.parse(it) }
                blockedPackages = blocked
                isBlockerActive = blocked.isNotEmpty() && phaseStr == "STUDY"
                
                startTimer(minutes, phaseStr)
            }
            "PAUSE" -> pauseTimer()
            "RESUME" -> resumeTimer()
            "STOP" -> {
                isBlockerActive = false
                blockedPackages = emptySet()
                pauseTimer()
                stopForeground(STOP_FOREGROUND_REMOVE)
gitt                val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                nm.cancel(2001) // Explicitly cancel the timer notification
                stopSelf()
            }
            "STOP_ALARM" -> {
                isBlockerActive = false
                SmartPetNotificationManager.stopSound()
            }
        }
        return START_STICKY
    }

    private fun startTimer(minutes: Int, phaseStr: String) {
        _phase.value = phaseStr
        val millis = minutes * 60 * 1000L
        _timeLeft.value = millis
        _isRunning.value = true
        
        createNotification(millis)
        runTimer(millis)
    }

    private fun runTimer(millis: Long) {
        timer?.cancel()
        timer = object : CountDownTimer(millis, 500) { // Check every 500ms
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = millisUntilFinished
                updateNotification(millisUntilFinished)
                checkForegroundApp()
            }

            override fun onFinish() {
                _timeLeft.value = 0
                _isRunning.value = false
                isBlockerActive = false
                handleTimerFinished()
            }
        }.start()
    }

    private fun checkForegroundApp() {
        if (!isBlockerActive || _phase.value != "STUDY") return
        
        val foregroundApp = AppBlockerManager.getForegroundApp(this)
        val myPackage = packageName
        
        // Only pull back if:
        // 1. We detected a foreground app
        // 2. It's NOT our own app
        // 3. It IS in the blocked list
        if (foregroundApp != null && foregroundApp != myPackage && blockedPackages.contains(foregroundApp)) {
            // Blocked app detected! Bring our app to front
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                putExtra("BLOCKED_TRIGGER", true)
            }
            startActivity(intent)
        }
    }

    private fun handleTimerFinished() {
        if (_phase.value == "STUDY") {
            SmartPetNotificationManager.sendBreakNotification(this, currentSoundUri)
        } else {
            SmartPetNotificationManager.sendStudyNotification(this, currentSoundUri)
        }
        stopForeground(STOP_FOREGROUND_DETACH)
    }

    private fun pauseTimer() {
        timer?.cancel()
        _isRunning.value = false
    }

    private fun resumeTimer() {
        _isRunning.value = true
        runTimer(_timeLeft.value)
    }

    private fun createNotification(millis: Long) {
        val notification = NotificationCompat.Builder(this, "smartpet_timer_silent")
            .setContentTitle("Pomodoro en curso")
            .setContentText(formatTime(millis))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        startForeground(2001, notification)
    }

    private fun updateNotification(millis: Long) {
        val notification = NotificationCompat.Builder(this, "smartpet_timer_silent")
            .setContentTitle("Pomodoro en curso")
            .setContentText(formatTime(millis))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(2001, notification)
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
