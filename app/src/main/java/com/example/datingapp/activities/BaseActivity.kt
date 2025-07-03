package com.example.datingapp.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.datingapp.services.OnlineStatusService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BaseActivity : AppCompatActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val onlineStatusService = OnlineStatusService.getInstance(this)
    private val auth = FirebaseAuth.getInstance()

    private var isAppInForeground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                // No-op
            }

            override fun onActivityStarted(activity: Activity) {
                isAppInForeground = true
                updateOnlineStatus(true)
            }

            override fun onActivityResumed(activity: Activity) {
                // No-op
            }

            override fun onActivityPaused(activity: Activity) {
                // No-op
            }

            override fun onActivityStopped(activity: Activity) {
                isAppInForeground = false
                updateOnlineStatus(false)
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                // No-op
            }

            override fun onActivityDestroyed(activity: Activity) {
                // No-op
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser != null) {
            updateOnlineStatus(true)
        }
    }

    override fun onPause() {
        super.onPause()
        if (auth.currentUser != null) {
            updateOnlineStatus(false)
        }
    }

    private fun updateOnlineStatus(isOnline: Boolean) {
        if (auth.currentUser == null) return

        coroutineScope.launch {
            try {
                onlineStatusService.updateOnlineStatus(isOnline)
                if (!isOnline) {
                    onlineStatusService.updateLastSeen()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        coroutineScope.cancel()
        super.onDestroy()
    }
}
