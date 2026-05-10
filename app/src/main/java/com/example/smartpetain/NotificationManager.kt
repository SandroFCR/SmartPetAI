package com.example.smartpetain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object SmartPetNotificationManager {

    private const val CHANNEL_BREAK  = "smartpet_break"
    private const val NOTIF_BREAK_ID = 1001

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_BREAK,
                "Hora de descansar",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisa cuando termina una sesión Pomodoro"
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    fun sendBreakNotification(context: Context) {
        val notif = NotificationCompat.Builder(context, CHANNEL_BREAK)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡Sesión completada! 🎉")
            .setContentText("Cinnamoroll dice: ¡Descansa 5 minutos, te lo mereces! 🐶")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            if (areNotificationsEnabled()) notify(NOTIF_BREAK_ID, notif)
        }
    }
}