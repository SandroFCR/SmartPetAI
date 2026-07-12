package com.example.smartpetain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_ALL
import androidx.core.app.NotificationManagerCompat

object SmartPetNotificationManager {

    private const val CHANNEL_POMODORO = "smartpet_pomodoro"
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
                description = "Avisa cuando termina una sesion Pomodoro o un descanso"
                enableVibration(true)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun sendBreakNotification(context: Context, soundUri: Uri? = null) {
        val notif = NotificationCompat.Builder(context, CHANNEL_POMODORO)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Sesión completada")
            .setContentText("Descansa 5 minutos. Te lo ganaste.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            if (areNotificationsEnabled()) notify(NOTIF_BREAK_ID, notif)
        }
        playSound(context, soundUri)
    }

    fun sendStudyNotification(context: Context, soundUri: Uri? = null) {
        val notif = NotificationCompat.Builder(context, CHANNEL_POMODORO)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Descanso terminado")
            .setContentText("Hora de volver a estudiar.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            if (areNotificationsEnabled()) notify(NOTIF_STUDY_ID, notif)
        }
        playSound(context, soundUri)
    }

    private fun playSound(context: Context, soundUri: Uri?) {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            
            val uri = soundUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            mediaPlayer = MediaPlayer.create(context, uri)
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener { 
                it.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
