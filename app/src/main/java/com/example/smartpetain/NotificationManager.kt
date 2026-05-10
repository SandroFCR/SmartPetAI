package com.example.smartpetain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_ALL
import androidx.core.app.NotificationManagerCompat

object SmartPetNotificationManager {

    private const val CHANNEL_POMODORO = "smartpet_pomodoro"
    private const val NOTIF_BREAK_ID = 1001
    private const val NOTIF_STUDY_ID = 1002

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

    fun sendBreakNotification(context: Context) {
        val notif = NotificationCompat.Builder(context, CHANNEL_POMODORO)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Sesion completada")
            .setContentText("Descansa 5 minutos. Te lo ganaste.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            if (areNotificationsEnabled()) notify(NOTIF_BREAK_ID, notif)
        }
    }

    fun sendStudyNotification(context: Context) {
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
    }
}
