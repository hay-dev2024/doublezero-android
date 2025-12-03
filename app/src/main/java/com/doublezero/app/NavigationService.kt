package com.doublezero.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.doublezero.data.network.RiskUpdateEvent
import com.doublezero.data.repository.NavigationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NavigationService : Service() {

    @Inject
    lateinit var navigationRepository: NavigationRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var sessionId: String? = null

    companion object {
        private const val TAG = "NavigationService"
        private const val CHANNEL_ID = "navigation_channel"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_SESSION_ID = "EXTRA_SESSION_ID"
        const val EXTRA_TOKEN = "EXTRA_TOKEN"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "NavigationService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val newSessionId = intent.getStringExtra(EXTRA_SESSION_ID)
                val token = intent.getStringExtra(EXTRA_TOKEN)

                if (newSessionId != null && token != null) {
                    sessionId = newSessionId
                    startForeground(NOTIFICATION_ID, createNotification("Monitoring road risks..."))
                    startRiskMonitoring(newSessionId, token)
                    Log.d(TAG, "Started monitoring session: $newSessionId")
                }
            }
            ACTION_STOP -> {
                Log.d(TAG, "Stopping service")
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startRiskMonitoring(sessionId: String, token: String) {
        // ViewModel에서 SSE를 관리하므로 여기서는 초기 알림만 표시
        Log.d(TAG, "Risk monitoring started - SSE handled by ViewModel")
        updateNotification("Monitoring road risks...")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Navigation Risk Monitoring",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Real-time road risk monitoring during navigation"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DoubleZero Navigation")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(message: String) {
        val notification = createNotification(message)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "NavigationService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

