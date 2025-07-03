package com.example.datingapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    companion object {
        const val CHANNEL_ID_MESSAGES = "messages_channel"
        const val CHANNEL_ID_CALLS = "calls_channel"
        const val CHANNEL_ID_MATCHES = "matches_channel"
        const val CHANNEL_ID_PREMIUM = "premium_channel"
        const val CHANNEL_ID_SYSTEM = "system_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createMessageChannel()
            createCallChannel()
            createMatchChannel()
            createPremiumChannel()
            createSystemChannel()
        }
    }

    private fun createMessageChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_MESSAGES,
            "Messages",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for new messages"
            enableLights(true)
            enableVibration(true)
            setSound(null, null) // Use default sound
            setShowBadge(true)
        }

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createCallChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_CALLS,
            "Calls",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for incoming calls"
            enableLights(true)
            enableVibration(true)
            setShowBadge(false)
            setBypassDnd(true) // Bypass Do Not Disturb
            setSound(null, null) // Use default sound
        }

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createMatchChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_MATCHES,
            "Matches",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for new matches"
            enableLights(true)
            enableVibration(true)
            setShowBadge(true)
            setSound(null, null) // Use default sound
        }

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createPremiumChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_PREMIUM,
            "Premium",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for premium features"
            enableLights(true)
            enableVibration(true)
            setShowBadge(true)
            setSound(null, null) // Use default sound
        }

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createSystemChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID_SYSTEM,
            "System",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "System notifications"
            enableLights(false)
            enableVibration(false)
            setShowBadge(false)
            setSound(null, null) // Use default sound
        }

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
