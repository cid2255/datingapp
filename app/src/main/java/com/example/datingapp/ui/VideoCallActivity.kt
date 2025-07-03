package com.example.datingapp.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.datingapp.databinding.ActivityVideoCallBinding
import com.example.datingapp.utils.formatDuration
import com.example.datingapp.utils.getDeviceId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.jitsi.meet.sdk.*
import com.example.datingapp.managers.CallManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import java.util.*

@AndroidEntryPoint
class VideoCallActivity : AppCompatActivity(), JitsiMeetActivityDelegate {

    private lateinit var binding: ActivityVideoCallBinding
    private lateinit var conferenceOptions: JitsiMeetConferenceOptions
    private var isMuted = false
    private var isVideoOff = false
    private var callStartTime: Long = 0
    private var currentUserId: String = ""
    private var receiverUserId: String = ""
    private var callId: String = ""
    
    @Inject
    lateinit var callManager: CallManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupConference()
        setupControls()
        startCallDurationTimer()
    }

    private fun setupConference() {
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        receiverUserId = intent.getStringExtra("receiverUid") ?: return
        
        // Generate unique call ID
        callId = "${currentUserId}_${receiverUserId}_${System.currentTimeMillis()}"
        
        val roomName = "call_${callId}"
        val serverUrl = "https://meet.jit.si"
        val userInfo = JitsiMeetUserInfo().apply {
            displayName = FirebaseAuth.getInstance().currentUser?.displayName
            email = FirebaseAuth.getInstance().currentUser?.email
            avatar = FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()
        }

        // Update call status using CallManager
        callManager.endCall(callId) // End any existing calls
        callManager.showIncomingCallNotification(receiverUserId, callId)

        // Update call status in Firebase
        updateCallStatus(true)

        conferenceOptions = JitsiMeetConferenceOptions.Builder()
            .setServerURL(serverUrl)
            .setRoom(roomName)
            .setUserInfo(userInfo)
            .setFeatureFlag("invite.enabled", false)
            .setFeatureFlag("calendar.enabled", false)
            .setFeatureFlag("recording.enabled", false)
            .setFeatureFlag("live-streaming.enabled", false)
            .setFeatureFlag("raise-hand.enabled", false)
            .setFeatureFlag("pip.enabled", true)
            .setFeatureFlag("filmstrip.enabled", true)
            .setFeatureFlag("toolbar.buttons", arrayOf("microphone", "camera"))
            .build()

        JitsiMeetView.setDefaultConferenceOptions(conferenceOptions)
        binding.jitsiConferenceView.setJitsiMeetViewDelegate(this)
        binding.jitsiConferenceView.join(conferenceOptions)

        // Update call status in Firebase
        updateCallStatus(roomName, true)
    }

    private fun setupControls() {
        binding.muteButton.setOnClickListener {
            isMuted = !isMuted
            binding.jitsiConferenceView.setAudioMuted(isMuted)
            updateButtonIcon(binding.muteButton, isMuted, R.drawable.ic_mic_off, R.drawable.ic_mic)
        }

        binding.videoButton.setOnClickListener {
            isVideoOff = !isVideoOff
            binding.jitsiConferenceView.setVideoMuted(isVideoOff)
            updateButtonIcon(binding.videoButton, isVideoOff, R.drawable.ic_video_off, R.drawable.ic_video)
        }

        binding.endCallButton.setOnClickListener {
            endCall()
        }

        binding.participantsButton.setOnClickListener {
            binding.jitsiConferenceView.toggleFilmstrip()
        }

        binding.settingsButton.setOnClickListener {
            binding.jitsiConferenceView.showSettings()
        }
    }

    private fun updateButtonIcon(button: View, isDisabled: Boolean, disabledIcon: Int, enabledIcon: Int) {
        (button as MaterialButton).icon = getDrawable(disabledIcon.takeIf { isDisabled } ?: enabledIcon)
    }

    private fun startCallDurationTimer() {
        callStartTime = System.currentTimeMillis()
        binding.callDuration.post(object : Runnable {
            override fun run() {
                val duration = System.currentTimeMillis() - callStartTime
                binding.callDuration.text = formatDuration(duration)
                binding.callDuration.postDelayed(this, 1000)
            }
        })
    }

    private fun updateCallStatus(isOngoing: Boolean) {
        val deviceId = getDeviceId()

        // Update caller's status
        val callerRef = FirebaseDatabase.getInstance().getReference("calls/$currentUserId/$deviceId")
        callerRef.setValue(mapOf(
            "callId" to callId,
            "receiverUid" to receiverUserId,
            "isOngoing" to isOngoing,
            "startTime" to System.currentTimeMillis()
        ))

        // Update receiver's status
        val receiverRef = FirebaseDatabase.getInstance().getReference("calls/$receiverUserId/$deviceId")
        receiverRef.setValue(mapOf(
            "callId" to callId,
            "callerUid" to currentUserId,
            "isOngoing" to isOngoing,
            "startTime" to System.currentTimeMillis()
        ))
    }

    private fun endCall() {
        binding.jitsiConferenceView.leave()
        updateCallStatus("", false)
        finish()
    }

    override fun onConferenceWillJoin(p0: JitsiMeetConferenceOptions?) {
        // Called when conference is about to be joined
    }

    override fun onConferenceJoined(p0: JitsiMeetConferenceOptions?) {
        // Called when conference is joined
    }

    override fun onConferenceTerminated(p0: JitsiMeetConferenceOptions?, p1: String?) {
        updateCallStatus(false)
        finish()

        // Clean up call references
        val deviceId = getDeviceId()
        val callerRef = FirebaseDatabase.getInstance().getReference("calls/$currentUserId/$deviceId")
        val receiverRef = FirebaseDatabase.getInstance().getReference("calls/$receiverUserId/$deviceId")
        
        callerRef.removeValue()
        receiverRef.removeValue()
    }
    }

    override fun onDestroy() {
        super.onDestroy()
        endCall()
        callManager.endCall(callId)
    }
    }
}
