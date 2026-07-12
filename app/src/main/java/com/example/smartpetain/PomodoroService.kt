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
                currentSoundUri = soundUriStr?.let { Uri.parse(it) }
                startTimer(minutes, phaseStr)
            }
            "PAUSE" -> pauseTimer()
            "RESUME" -> resumeTimer()
            "STOP" -> stopTimer()
            "STOP_ALARM" -> SmartPetNotificationManager.stopSound()
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
        timer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = millisUntilFinished
                updateNotification(millisUntilFinished)
            }

            override fun onFinish() {
                _timeLeft.value = 0
                _isRunning.value = false
                handleTimerFinished()
            }
        }.start()
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

    private fun stopTimer() {
        timer?.cancel()
        _isRunning.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
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
