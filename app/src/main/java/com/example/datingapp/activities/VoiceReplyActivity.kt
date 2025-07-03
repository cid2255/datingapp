package com.example.datingapp.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.datingapp.R
import com.example.datingapp.models.chat.ChatMessage
import com.example.datingapp.utils.FirebaseStructure
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_voice_reply.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class VoiceReplyActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedMessage: ChatMessage
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false
    private var recordingTime = 0L
    private var maxRecordingDuration = 30000L // 30 seconds
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_reply)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        selectedMessage = intent.getParcelableExtra("message") ?: return

        // Request permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }

        setupUI()
    }

    private fun setupUI() {
        recordButton.setOnClickListener { toggleRecording() }
        cancelButton.setOnClickListener { finish() }
        sendButton.setOnClickListener { sendVoiceMessage() }
    }

    private fun toggleRecording() {
        if (!isRecording) {
            startRecording()
        } else {
            stopRecording()
        }
    }

    private fun startRecording() {
        try {
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                audioFile = File.createTempFile("audio", ".m4a", cacheDir)
                setOutputFile(audioFile?.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            recordButton.setImageResource(R.drawable.ic_stop)
            timer = object : CountDownTimer(maxRecordingDuration, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    recordingTime += 1000
                    timerText.text = "${recordingTime / 1000}s"
                }

                override fun onFinish() {
                    stopRecording()
                }
            }.start()
        } catch (e: Exception) {
            e.printStackTrace()
            showErrorMessage("Failed to start recording")
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            recordButton.setImageResource(R.drawable.ic_mic)
            timer?.cancel()
            recordButton.visibility = View.GONE
            sendButton.visibility = View.VISIBLE
            timerText.text = "${recordingTime / 1000}s"
        } catch (e: Exception) {
            e.printStackTrace()
            showErrorMessage("Failed to stop recording")
        }
    }

    private fun sendVoiceMessage() {
        audioFile?.let { file ->
            showLoading(true)
            
            lifecycleScope.launch {
                try {
                    val result = voiceMessageRepository.uploadVoiceMessage(file, currentChatId ?: "")
                    
                    when (result) {
                        is Result.Success -> {
                            // Create message with uploaded URL
                            val message = ChatMessage(
                                senderId = auth.currentUser?.uid ?: "",
                                receiverId = selectedMessage.senderId,
                                text = "Voice message",
                                timestamp = System.currentTimeMillis(),
                                type = MessageType.VOICE,
                                mediaUrl = result.data,
                                duration = recordingTime,
                                replyTo = selectedMessage.id,
                                replyContent = selectedMessage.text,
                                replyType = selectedMessage.type
                            )

                            // Send message
                            val chatRepository = ChatRepository()
                            chatRepository.sendMessage(message)
                            
                            finish()
                        }
                        is Result.Failure -> {
                            showErrorMessage("Failed to upload voice message: ${result.exception.message}")
                            showLoading(false)
                        }
                    }
                } catch (e: Exception) {
                    showErrorMessage("Error uploading voice message: ${e.message}")
                    showLoading(false)
                }
            }
        }
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        timer?.cancel()
    }
}
