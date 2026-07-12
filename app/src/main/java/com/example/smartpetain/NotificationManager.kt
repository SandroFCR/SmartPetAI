package com.example.smartpetain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_ALL
import androidx.core.app.NotificationManagerCompat

object SmartPetNotificationManager {

    private const val CHANNEL_POMODORO = "smartpet_pomodoro"
    private const val CHANNEL_SILENT = "smartpet_timer_silent"
    private const val NOTIF_BREAK_ID = 1001
    private const val NOTIF_STUDY_ID = 1002
    private var mediaPlayer: MediaPlayer? = null

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_POMODORO,
                "Alarmas Pomodoro",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisa cuando termina una sesión Pomodoro o un descanso"
                enableVibration(true)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)

            val silentChannel = NotificationChannel(
                CHANNEL_SILENT,
                "Temporizador en curso",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Muestra el tiempo restante sin emitir sonidos"
                setShowBadge(false)
            }
            nm.createNotificationChannel(silentChannel)
        }
    }

    fun sendBreakNotification(context: Context, soundUri: Uri? = null) {
        val notif = NotificationCompat.Builder(context, CHANNEL_POMODORO)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡Sesión completada!")
            .setContentText("Es momento de descansar. Toca para detener la alarma.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(DEFAULT_ALL)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIF_BREAK_ID, notif)
        }
        playSound(context, soundUri, loop = true)
    }

    fun sendStudyNotification(context: Context, soundUri: Uri? = null) {
        val notif = NotificationCompat.Builder(context, CHANNEL_POMODORO)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡Descanso terminado!")
            .setContentText("Hora de volver a enfocarse.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(DEFAULT_ALL)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIF_STUDY_ID, notif)
        }
        playSound(context, soundUri, loop = true)
    }

    private fun playSound(context: Context, soundUri: Uri?, loop: Boolean) {
        try {
            stopSound()
            
            val uri = soundUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                isLooping = loop
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopSound() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
