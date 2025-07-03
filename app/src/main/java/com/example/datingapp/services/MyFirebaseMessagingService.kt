package com.example.datingapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.datingapp.R
import com.example.datingapp.ui.ChatActivity
import com.example.datingapp.ui.MatchesActivity
import com.example.datingapp.ui.VideoCallActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManager: NotificationManager

    companion object {
        private const val CHANNEL_ID_MESSAGES = "messages_channel"
        private const val CHANNEL_ID_CALLS = "calls_channel"
        private const val CHANNEL_ID_MATCHES = "matches_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        when (remoteMessage.data["type"]) {
            "message" -> handleMessageNotification(remoteMessage)
            "call" -> handleCallNotification(remoteMessage)
            "match" -> handleMatchNotification(remoteMessage)
            else -> handleDefaultNotification(remoteMessage)
        }
    }

    private fun handleMessageNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "New Message"
        val message = remoteMessage.data["message"] ?: ""
        val senderId = remoteMessage.data["senderId"] ?: ""
        val senderName = remoteMessage.data["senderName"] ?: ""

        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("receiverUid", senderId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        showNotification(
            title = title,
            message = message,
            intent = pendingIntent,
            channel = CHANNEL_ID_MESSAGES,
            icon = R.drawable.ic_message
        )
    }

    private fun handleCallNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "Incoming Call"
        val message = remoteMessage.data["message"] ?: ""
        val callerId = remoteMessage.data["callerId"] ?: ""
        val callerName = remoteMessage.data["callerName"] ?: ""

        val intent = Intent(this, VideoCallActivity::class.java).apply {
            putExtra("receiverUid", callerId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        showNotification(
            title = title,
            message = message,
            intent = pendingIntent,
            channel = CHANNEL_ID_CALLS,
            icon = R.drawable.ic_call
        )
    }

    private fun handleMatchNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.data["title"] ?: "New Match"
        val message = remoteMessage.data["message"] ?: ""
        val matchId = remoteMessage.data["matchId"] ?: ""

        val intent = Intent(this, MatchesActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        showNotification(
            title = title,
            message = message,
            intent = pendingIntent,
            channel = CHANNEL_ID_MATCHES,
            icon = R.drawable.ic_heart
        )
    }

    private fun handleDefaultNotification(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "New Notification"
        val message = remoteMessage.notification?.body ?: ""

        val intent = Intent(this, MatchesActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        showNotification(
            title = title,
            message = message,
            intent = pendingIntent,
            channel = CHANNEL_ID_MESSAGES,
            icon = R.drawable.ic_notification
        )
    }

    private fun showNotification(
        title: String,
        message: String,
        intent: PendingIntent,
        channel: String,
        icon: Int
    ) {
        createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channel)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(intent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = when (channelId) {
                CHANNEL_ID_MESSAGES -> "Messages"
                CHANNEL_ID_CALLS -> "Calls"
                CHANNEL_ID_MATCHES -> "Matches"
                else -> "Notifications"
            }

            val description = when (channelId) {
                CHANNEL_ID_MESSAGES -> "Message notifications"
                CHANNEL_ID_CALLS -> "Call notifications"
                CHANNEL_ID_MATCHES -> "Match notifications"
                else -> "App notifications"
            }

            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                this.description = description
                enableLights(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to your server or update it in your database
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        // TODO: Implement this method to send token to your server
    }
}
