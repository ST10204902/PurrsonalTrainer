package za.co.varsitycollege.st10204902.purrsonaltrainer.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import za.co.varsitycollege.st10204902.purrsonaltrainer.R

class NotificationService : Service() {

    private lateinit var notificationManager: NotificationManager
    private val channelId = "purrsonal_trainer_channel"
    private val channelName = "Purrsonal Trainer Notifications"

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Static method to show one-off notification
    companion object {
        fun showOneOffNotification(context: Context, title: String, message: String) {
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(context, "purrsonal_trainer_channel")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            notificationManager.notify(1, notification)
        }
    }

    // Type 2 Notification
    fun showContinuousNotification(exerciseDuration: Long) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Exercising")
            .setContentText("Time spent exercising: ${formatDuration(exerciseDuration)}")
            .setOngoing(true) // Makes the notification ongoing
            .build()

        notificationManager.notify(2, notification)
    }

    private fun formatDuration(duration: Long): String {
        val hours = duration / 3600
        val minutes = (duration % 3600) / 60
        val seconds = duration % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}

