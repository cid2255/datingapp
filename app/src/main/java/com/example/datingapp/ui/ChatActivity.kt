package com.example.datingapp.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.datingapp.databinding.ActivityChatBinding
import com.example.datingapp.models.Message
import com.example.datingapp.utils.CallUtils
import com.example.datingapp.utils.loadImage
import com.example.datingapp.viewmodels.ChatViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModel: ChatViewModel

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MessageAdapter
    private var otherUserId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        otherUserId = intent.getStringExtra("userId") ?: return
        setupViews()
        setupObservers()
    }

    private fun setupViews() {
        // Setup RecyclerView
        adapter = MessageAdapter(this)
        binding.messagesRecyclerView.adapter = adapter

        // Setup message input
        binding.sendButton.setOnClickListener { sendMessage() }
        binding.voiceButton.setOnClickListener { startVoiceRecording() }
        binding.imageButton.setOnClickListener { pickImage() }
        binding.locationButton.setOnClickListener { shareLocation() }

        // Setup call button
        binding.callButton.setOnCallClickListener {
            val intent = Intent(this, VideoCallActivity::class.java).apply {
                putExtra("receiverUid", otherUserId)
            }
            startActivity(intent)
        }

        // Setup premium status
        viewModel.isPremium.observe(this) { isPremium ->
            binding.callButton.setPremiumStatus(isPremium)
        }
    }

    private fun setupObservers() {
        viewModel.messages.observe(this) { messages ->
            adapter.submitList(messages)
            binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
        }

        viewModel.messageStatus.observe(this) { status ->
            when (status) {
                is MessageStatus.Sending -> {
                    binding.sendButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                }
                is MessageStatus.Success -> {
                    binding.sendButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    binding.messageInput.text?.clear()
                }
                is MessageStatus.Error -> {
                    binding.sendButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    showErrorDialog(status.error)
                }
            }
        }
    }

    private fun sendMessage() {
        val message = binding.messageInput.text.toString().trim()
        if (message.isNotEmpty()) {
            lifecycleScope.launch {
                viewModel.sendMessage(message, otherUserId)
            }
        }
    }

    private fun startVoiceRecording() {
        // TODO: Implement voice recording
    }

    private fun shareLocation() {
        if (!checkLocationPermissions()) {
            requestLocationPermissions()
            return
        }

        // Get current location
        viewModel.getCurrentLocation { location ->
            if (location != null) {
                // Create location message
                val message = Message.createLocationMessage(
                    senderId = viewModel.currentUserId,
                    receiverId = otherUserId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    address = viewModel.getAddressFromLocation(location)
                )

                // Send location message
                viewModel.sendMessage(message)
            } else {
                showLocationError()
            }
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun showLocationError() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Location Error")
            .setMessage("Unable to get your current location. Please make sure location services are enabled.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun pickImage() {
        // TODO: Implement image picking
    }

    private fun showErrorDialog(error: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Error")
            .setMessage(error)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CallUtils.PERMISSION_REQUEST_CODE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Permissions granted, can proceed with call
                    CallUtils.launchVideoCall(this, otherUserId)
                } else {
                    // Handle permission denied
                    showPermissionDeniedDialog()
                }
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permissions Required")
            .setMessage("Camera and microphone permissions are required for video calls.")
            .setPositiveButton("Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
}
