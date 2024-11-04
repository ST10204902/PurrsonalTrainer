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
import android.util.Log
import androidx.core.app.NotificationCompat
import za.co.varsitycollege.st10204902.purrsonaltrainer.R

class NotificationService : Service() {

    private lateinit var notificationManager: NotificationManager
    private val channelId = "purrsonal_trainer_channel"
    private val channelName = "Purrsonal Trainer Notifications"

    private val continuousNotificationId = 2
    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var isServiceRunning = false

    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationService", "------------------------------------onCreate called")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setSound(null, null)
                enableLights(false)
                enableVibration(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {

        const val ACTION_STOP_SERVICE = "za.co.varsitycollege.st10204902.purrsonaltrainer.ACTION_STOP_SERVICE"

        fun showOneOffNotification(context: Context, title: String, message: String) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = NotificationCompat.Builder(context, "purrsonal_trainer_channel")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))  // Use BigTextStyle
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(1, notification)
        }

        fun showContinuousNotification(context: Context, startTimeMillis: Long) {
            try {
                val intent = Intent(context, NotificationService::class.java).apply {
                    putExtra("startTimeMillis", startTimeMillis)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }

                Log.d("NotificationService", "------------HERE-------------------Showing continuous notification from ${context}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun dismissContinuousNotification(context: Context) {
            try {
                Log.d("NotificationService", "------------------------------------Attempting to stop service directly")
                // Stop the service directly
                context.stopService(Intent(context, NotificationService::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private fun startContinuousNotification(startTimeMillis: Long) {
        if (isServiceRunning) {
            Log.d("NotificationService", "-------------------------Service already running, not starting again")
            return
        }

        isServiceRunning = true
        Log.d("NotificationService", "------------------------------Starting continuous notification")
        val notification = NotificationCompat.Builder(this@NotificationService, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Exercising")
            .setContentText("Starting workout tracking...")
            .setOngoing(false)
            .setAutoCancel(false)
            .build()

        startForeground(continuousNotificationId, notification)

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                if (!isServiceRunning) return

                val elapsedTime = System.currentTimeMillis() - startTimeMillis
                val formattedTime = formatDuration(elapsedTime / 1000)

                val updatedNotification = NotificationCompat.Builder(this@NotificationService, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("Exercising")
                    .setContentText("Time spent exercising: $formattedTime")
                    .setOngoing(true)
                    .setAutoCancel(false)
                    .build()

                notificationManager.notify(continuousNotificationId, updatedNotification)
                handler?.postDelayed(this, 1000)
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
        Log.d("NotificationService", "onStartCommand called with intent: $intent")

        if (intent == null) {
            // Service is being restarted by the system, do not restart the notification
            Log.d("NotificationService", "Intent is null. Service restarted by system. Stopping service.")
            stopSelf()
            return START_NOT_STICKY
        }

        val startTimeMillis = intent.getLongExtra("startTimeMillis", System.currentTimeMillis())
        startContinuousNotification(startTimeMillis)
        return START_NOT_STICKY
    }


    override fun onDestroy() {
        Log.d("NotificationService", "onDestroy called")
        try {
            handler?.removeCallbacksAndMessages(null)
            handler = null
            runnable = null
            isServiceRunning = false
            stopForeground(true)
            notificationManager.cancel(continuousNotificationId)
            super.onDestroy()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onBind(intent: Intent?): IBinder? = null
}
