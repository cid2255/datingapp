package com.example.datingapp.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.adapters.MessageAdapter
import com.example.datingapp.models.User
import com.example.datingapp.models.chat.ChatMessage
import com.example.datingapp.models.chat.MessageType
import com.example.datingapp.repositories.ChatRepository
import com.example.datingapp.repositories.UserRepository
import com.example.datingapp.utils.FirebaseStructure
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private var isLoading = false
    private var hasMoreMessages = true
    private var lastVisible: DocumentSnapshot? = null

    private fun showLoading(show: Boolean) {
        binding.loadingProgress.visibility = if (show) View.VISIBLE else View.GONE
        binding.loadingPlaceholder.visibility = if (show) View.VISIBLE else View.GONE
        binding.messagesRecyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    @Inject
    lateinit var chatRepository: ChatRepository
    @Inject
    lateinit var userRepository: UserRepository
    private lateinit var typingStatusService: TypingStatusService
    private lateinit var messageStatusService: MessageStatusService
    private lateinit var blockService: BlockService
    private var isBlocked = false

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var adapter: MessageAdapter
    private lateinit var replyBar: LinearLayout
    private lateinit var repliedMessageText: TextView
    private lateinit var cancelReplyButton: ImageButton
    private var keyboardManager: InputMethodManager? = null
    private var currentChatId: String? = null
    private var typingTimer: Timer? = null
    private var typingMessage: String = ""
    private var typingStatusJob: Job? = null
    private var currentChatId: String = ""
    private var otherUserId: String = ""
    private var otherUser: User? = null
    private var currentRecording: File? = null
    private var isRecording = false
    private var selectedMessage: ChatMessage? = null
    private val messageContextMenu = MessageContextMenu()

    private inner class MessageContextMenu {
        fun show(message: ChatMessage) {
            val popup = PopupMenu(this@ChatActivity, findViewById(R.id.messageRecyclerView))
            popup.menuInflater.inflate(R.menu.message_context_menu, popup.menu)
            
            // Disable forward option for forwarded messages
            val forwardItem = popup.menu.findItem(R.id.forward)
            forwardItem?.isVisible = !message.isForwarded
            
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.reply -> {
                        replyToMessage(message)
                        true
                    }
                    R.id.forward -> {
                        forwardMessage(message)
                        true
                    }
                    R.id.copy -> {
                        copyMessage(message)
                        true
                    }
                    R.id.delete -> {
                        deleteMessage(message)
                        true
                    }
                    else -> false
                }
            }
            
            popup.show()
        }
    }

    private fun forwardMessage(message: ChatMessage) {
        // Create forward intent
        val intent = Intent(this, ForwardMessageActivity::class.java)
        intent.putExtra("message", message)
        
        // Start forward activity with animation
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun replyToMessage(message: ChatMessage) {
        selectedMessage = message
        
        // Update reply bar with slide animation
        replyBar.visibility = View.VISIBLE
        val slideIn = AnimatorInflater.loadAnimator(this, R.animator.reply_bar_slide_in)
        slideIn.setTarget(replyBar)
        slideIn.start()
        
        repliedMessageText.text = message.text
        
        // Show reply options menu
        val popup = PopupMenu(this, replyBar)
        popup.menuInflater.inflate(R.menu.reply_options_menu, popup.menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.text_reply -> {
                    // Show text input
                    messageInput.hint = "Reply to: ${message.text}"
                    messageInput.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        ContextCompat.getDrawable(this, R.drawable.ic_reply_indicator),
                        null
                    )
                    messageInput.requestFocus()
                    true
                }
                R.id.voice_reply -> {
                    // Start voice reply activity
                    val intent = Intent(this, VoiceReplyActivity::class.java)
                    intent.putExtra("message", message)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }

    private fun cancelReply() {
        selectedMessage = null
        
        // Hide reply bar with slide animation
        val slideOut = AnimatorInflater.loadAnimator(this, R.animator.reply_bar_slide_out)
        slideOut.setTarget(replyBar)
        slideOut.start()
        
        // Reset input field
        messageInput.hint = getString(R.string.type_a_message)
        messageInput.text.clear()
        
        // Remove reply indicator
        messageInput.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            null,
            null
        )
        
        // Clear keyboard shortcut
        messageInput.setOnKeyListener(null)
        
        // Reset focus
        messageInput.clearFocus()
        
        // Reset input field state
        messageInput.background = ContextCompat.getDrawable(this, R.drawable.input_background)
        
        // Reset hint color
        messageInput.setHintTextColor(ContextCompat.getColor(this, R.color.hint))
        
        // Reset input type
        messageInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        
        // Reset cursor position
        messageInput.setSelection(0)
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            sendMediaMessage(cameraImageUri, MessageType.IMAGE)
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { sendMediaMessage(it, MessageType.IMAGE) }
    }

    private val audioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                sendMediaMessage(uri, MessageType.AUDIO)
            }
        }
    }

    private val audioRecordLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            currentRecording?.let { file ->
                sendMediaMessage(Uri.fromFile(file), MessageType.AUDIO)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        
        // Initialize reply bar components
        replyBar = findViewById(R.id.replyBar)
        repliedMessageText = findViewById(R.id.repliedMessageText)
        cancelReplyButton = findViewById(R.id.cancelReplyButton)
        
        // Initialize keyboard manager
        keyboardManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        typingStatusService = TypingStatusService.getInstance(this)
        messageStatusService = MessageStatusService.getInstance(this)

        currentChatId = intent.getStringExtra("chatId")
        if (currentChatId == null) {
            Toast.makeText(this, "Invalid chat", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews()
        setupRecyclerView()

        // Load messages and update status
        loadMessages()
        
        // Check if user is blocked
        checkIfBlocked()

        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()
            if (messageText.isEmpty()) return@setOnClickListener

            val message = ChatMessage(
                senderId = auth.currentUser?.uid ?: "",
                receiverId = receiverId,
                text = messageText,
                timestamp = System.currentTimeMillis(),
                type = MessageType.TEXT,
                replyTo = selectedMessage?.id,
                replyContent = selectedMessage?.text,
                replyType = selectedMessage?.type
            )

            chatRepository.sendMessage(message)
            messageInput.text.clear()
            
            // Reset reply state with animation
            val slideOut = AnimatorInflater.loadAnimator(this, R.animator.reply_bar_slide_out)
            slideOut.setTarget(replyBar)
            slideOut.start()
            
            // Reset input field state
            messageInput.hint = getString(R.string.type_a_message)
            messageInput.background = ContextCompat.getDrawable(this, R.drawable.input_background)
            messageInput.setHintTextColor(ContextCompat.getColor(this, R.color.hint))
            messageInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            messageInput.setSelection(0)
            
            // Clear focus and keyboard
            messageInput.clearFocus()
            hideKeyboard()
            
            // Reset reply state
            selectedMessage = null
            messageInput.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                null,
                null
            )
            messageInput.setOnKeyListener(null)
        }

        attachButton.setOnClickListener {
            showAttachmentMenu()
        }

        locationButton.setOnClickListener {
            checkLocationPermission()
        }

        voiceButton.setOnClickListener {
            checkVoicePermission()
        }

        videoButton.setOnClickListener {
            checkVideoPermission()
        }

        fileButton.setOnClickListener {
            checkFilePermission()
        }

        callButton.setOnClickListener {
            checkCallPermission()
        }

        cameraButton.setOnClickListener {
            checkCameraPermission()
        }

        galleryButton.setOnClickListener {
            checkGalleryPermission()
        }

        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.isNotEmpty() == true) {
                    updateTypingStatus(currentChatId ?: return@onTextChanged, TypingStatus.TYPING)
                    startTypingTimer()
                } else {
                    clearTypingStatus(currentChatId ?: return@onTextChanged)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // No-op
            }
        })

        // Start listening to typing status
        startTypingStatusListener()

        lifecycleScope.launch {
            try {
                updateMessageStatus(currentChatId ?: return@launch, "Started chat")
                updateCurrentChat(currentChatId ?: return@launch)
                updateActivityStatus("Chatting")
                updateCurrentActivity("ChatActivity")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupRecyclerView() {
        messageRecyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(this, { message ->
            // Handle message click
        })
        messageRecyclerView.adapter = messageAdapter
        
        // Attach gesture support
        messageAdapter.attachToRecyclerView(messageRecyclerView)
        
        // Handle scroll to bottom
        messageRecyclerView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            if (messageRecyclerView.adapter?.itemCount == 0) return@addOnLayoutChangeListener
            messageRecyclerView.scrollToPosition(messageRecyclerView.adapter?.itemCount?.minus(1) ?: 0)
        }
        
        // Load more messages when scrolled to top
        messageRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                
                if (firstVisibleItem <= 2 && hasMoreMessages) {
                    loadMessages()
                }
            }
        })
    }

    private fun sendMessage(message: String) {
        if (auth.currentUser == null) return
        if (isBlocked) {
            Toast.makeText(this@ChatActivity, "You cannot send messages to blocked users", Toast.LENGTH_SHORT).show()
            return
        }

        coroutineScope.launch {
            try {
                val messageId = UUID.randomUUID().toString()
                val messageData = hashMapOf(
                    "id" to messageId,
                    "senderId" to auth.currentUser?.uid,
                    "receiverId" to currentChatId,
                    "content" to message,
                    "type" to MessageType.TEXT.name,
                    "timestamp" to Timestamp.now(),
                    "status" to MessageStatus.SENT.name,
                    "sentTime" to Timestamp.now()
                )

                db.collection("messages").document(messageId).set(messageData).await()

                // Update local message list
                val message = ChatMessage(
                    id = messageId,
                    senderId = auth.currentUser?.uid ?: "",
                    receiverId = currentChatId ?: "",
                    content = message,
                    type = MessageType.TEXT,
                    timestamp = Timestamp.now(),
                    status = MessageStatus.SENT,
                    sentTime = Timestamp.now()
                )
                messageAdapter.addMessage(message)
                messageRecyclerView.smoothScrollToPosition(messageAdapter.itemCount - 1)

                // Update unread count
                updateUnreadCount(currentChatId ?: return@launch, 0)

                // Listen for status updates
                lifecycleScope.launch {
                    messageStatusService.listenToMessageStatus(messageId)
                        .collect { status ->
                            updateMessageStatus(messageId, status)
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun sendMediaMessage(uri: Uri, type: MessageType) {
        if (isBlocked) {
            Toast.makeText(this@ChatActivity, "You cannot send messages to blocked users", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val mediaUrl = chatRepository.uploadMedia(uri, type)
                val chatMessage = ChatMessage(
                    chatId = currentChatId,
                    senderId = auth.currentUser?.uid ?: "",
                    receiverId = intent.getStringExtra("receiverId") ?: "",
                    content = "",
                    mediaUrl = mediaUrl,
                    type = type
                )
                chatRepository.sendMessage(chatMessage)
                messageAdapter.addMessage(chatMessage)
                messageRecyclerView.smoothScrollToPosition(messageAdapter.itemCount - 1)
            } catch (e: Exception) {
                Toast.makeText(this@ChatActivity, R.string.error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMessages() {
        if (isLoading) return
        isLoading = true
        showLoading(true)
        
        val chatId = intent.getStringExtra("chatId") ?: return
        val otherUserId = intent.getStringExtra("otherUserId") ?: return
        
        val messagesRef = db.collection("messages").document(chatId)
        val query = messagesRef.collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)

        if (lastVisible != null) {
            query = query.startAfter(lastVisible)
        }

        query.get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val messages = documents.map { it.toObject(Message::class.java) }
                    lastVisible = documents.documents.last()
                    
                    if (messages.size < 20) {
                        hasMoreMessages = false
                    }
                    
                    messageAdapter.submitList(messages.reversed())
                } else {
                    hasMoreMessages = false
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading messages: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                isLoading = false
                showLoading(false)
            }
    }

    private fun startTypingTimer() {
        typingTimer?.cancel()
        typingTimer = Timer()
        typingTimer?.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    if (messageEditText.text.toString().isNotEmpty()) {
                        updateTypingStatus(currentChatId ?: return@run, TypingStatus.TYPING)
                    } else {
                        clearTypingStatus(currentChatId ?: return@run)
                    }
                }
            }
        }, 5000, 5000)
    }

    override fun onDestroy() {
        super.onDestroy()
        messageAdapter?.detachFromRecyclerView()
        messageAdapter = null
        typingTimer?.cancel()
        typingTimer = null
        typingStatusJob?.cancel()
        typingStatusJob = null
        
        // Update status
        updateActivityStatus("Offline")
        updateCurrentActivity(null)
        updateCurrentChat(null)
        clearTypingStatus(currentChatId ?: return)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_chat -> {
                lifecycleScope.launch {
                    try {
                        chatRepository.clearChat(currentChatId ?: return@launch)
                        adapter.submitList(emptyList())
                    } catch (e: Exception) {
                        Toast.makeText(this@ChatActivity, R.string.error, Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }
            R.id.action_block -> {
                showBlockStatusDialog()
                true
            }
            R.id.action_report -> {
                reportUser()
                true
            }
            R.id.action_blocked_users -> {
                startActivity(Intent(this, BlockedUsersActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadMessages() {
    private fun clearChat() {
        lifecycleScope.launch {
            try {
                chatRepository.clearChat(currentChatId ?: return@launch)
                chatRepository.clearChat(currentChatId)
                adapter.submitList(emptyList())
            } catch (e: Exception) {
                Toast.makeText(this@ChatActivity, R.string.error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun blockUser() {
        val currentUserId = auth.currentUser?.uid ?: return
        val otherUserId = intent.getStringExtra("receiverId") ?: return

        // Show block status dialog
        showBlockStatusDialog()
    }
    }

    private fun reportUser() {
        val currentUserId = auth.currentUser?.uid ?: return
        val otherUserId = intent.getStringExtra("receiverId") ?: return

        val reportDialog = ReportDialogFragment.newInstance(otherUserId)
        reportDialog.show(supportFragmentManager, "report_dialog")
    }

    private fun viewProfile() {
        // TODO: Navigate to user profile
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val STORAGE_PERMISSION_CODE = 101
        private const val LOCATION_PERMISSION_CODE = 102
        private const val AUDIO_PERMISSION_CODE = 103
        private lateinit var cameraImageUri: Uri
    }
}
