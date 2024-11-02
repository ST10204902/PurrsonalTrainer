package za.co.varsitycollege.st10204902.purrsonaltrainer.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import za.co.varsitycollege.st10204902.purrsonaltrainer.R

class NotificationService : Service() {

    private lateinit var notificationManager: NotificationManager
    private val channelId = "purrsonal_trainer_channel"
    private val channelName = "Purrsonal Trainer Notifications"

    private val continuousNotificationId = 2
    private var handler: Handler? = null
    private var runnable: Runnable? = null

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

    companion object {
        // Static method to show one-off notification
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

        // Method to start continuous notification with start time
        fun showContinuousNotification(context: Context, startTimeMillis: Long) {
            val intent = Intent(context, NotificationService::class.java)
            intent.putExtra("startTimeMillis", startTimeMillis)
            context.startForegroundService(intent)
        }

        // Method to dismiss the continuous notification
        fun dismissContinuousNotification(context: Context) {
            val intent = Intent(context, NotificationService::class.java)
            context.stopService(intent)
        }
    }

    private fun startContinuousNotification(startTimeMillis: Long) {
        val notification = NotificationCompat.Builder(this@NotificationService, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Exercising")
            .setContentText("Starting workout tracking...")
            .setOngoing(true)
            .build()

        startForeground(continuousNotificationId, notification) // Start service in foreground

        // Now start updating the notification
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val elapsedTime = System.currentTimeMillis() - startTimeMillis
                val formattedTime = formatDuration(elapsedTime / 1000) // Convert to seconds

                val updatedNotification = NotificationCompat.Builder(this@NotificationService, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("Exercising")
                    .setContentText("Time spent exercising: $formattedTime")
                    .setOngoing(true)
                    .build()

                notificationManager.notify(continuousNotificationId, updatedNotification)

                handler?.postDelayed(this, 1000) // Update every second
            }
        }
        handler?.post(runnable!!)
    }

    private fun formatDuration(duration: Long): String {
        val hours = duration / 3600
        val minutes = (duration % 3600) / 60
        val seconds = duration % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val startTimeMillis = intent?.getLongExtra("startTimeMillis", System.currentTimeMillis()) ?: System.currentTimeMillis()
        startContinuousNotification(startTimeMillis)
        return START_STICKY // Keeps the service running
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop handler from updating the notification
        handler?.removeCallbacks(runnable!!)
        handler = null
        runnable = null

        // Stop foreground service and remove notification
        stopForeground(true)
        notificationManager.cancel(continuousNotificationId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
