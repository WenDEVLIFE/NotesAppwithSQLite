package com.example.notesappwithsqlite

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val title = intent?.getStringExtra("title") ?: "Reminder"
        val message = intent?.getStringExtra("message") ?: "Don't forget your note!"

        val channelId = "note_reminder_channel"

        // Create Notification Channel (For Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Note Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Create Notification
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure you have a valid icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        // Show Notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}
