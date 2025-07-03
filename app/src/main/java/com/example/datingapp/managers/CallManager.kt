package com.example.datingapp.managers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.datingapp.R
import com.example.datingapp.ui.VideoCallActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val callChannelId = "call_channel"
    private val callNotificationId = 1

    init {
        createNotificationChannel()
        setupCallListeners()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                callChannelId,
                "Call Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming calls"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupCallListeners() {
        val userId = auth.currentUser?.uid ?: return
        val callRef = database.getReference("calls/$userId")

        callRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val activeCalls = snapshot.children.filter { it.child("isOngoing").getValue(Boolean::class.java) == true }
                
                if (activeCalls.isNotEmpty()) {
                    val callData = activeCalls.first().getValue(Map::class.java) ?: return
                    val callerUid = callData["callerUid"] as? String ?: return
                    val callId = callData["callId"] as? String ?: return

                    // Check if this is an incoming call
                    if (callerUid != userId) {
                        showIncomingCallNotification(callerUid, callId)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun showIncomingCallNotification(callerUid: String, callId: String) {
        val intent = Intent(context, VideoCallActivity::class.java).apply {
            putExtra("receiverUid", callerUid)
            putExtra("callId", callId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, callChannelId)
            .setContentTitle("Incoming Call")
            .setContentText("Someone is calling you")
            .setSmallIcon(R.drawable.ic_call)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(callNotificationId, notification)
    }

    fun endCall(callId: String) {
        val userId = auth.currentUser?.uid ?: return
        val callRef = database.getReference("calls/$userId")
        
        callRef.orderByChild("callId").equalTo(callId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { child ->
                    child.ref.child("isOngoing").setValue(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun getActiveCall(): String? {
        val userId = auth.currentUser?.uid ?: return null
        val callRef = database.getReference("calls/$userId")
        
        return callRef.orderByChild("isOngoing").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val activeCall = snapshot.children.firstOrNull()
                        ?.getValue(Map::class.java)
                        ?.get("callId") as? String
                    
                    if (activeCall != null) {
                        return activeCall
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        return null
    }
}
