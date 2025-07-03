package com.example.datingapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.models.chat.Message
import com.example.datingapp.models.chat.MessageType
import com.example.datingapp.utils.FirebaseStructure
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.item_message.view.*
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val context: Context,
    private val onMessageLongClick: (Message) -> Unit
) : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    private var itemTouchHelper: ItemTouchHelper? = null
    private val voiceMessagePlayer = VoiceMessagePlayer(context)
    private var currentPlayingMessage: Message? = null
    private var currentPlayingViewHolder: MessageViewHolder? = null

    fun attachToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.adapter = this
        itemTouchHelper = ItemTouchHelper(MessageGestureCallback(this, context as ChatActivity))
        itemTouchHelper?.attachToRecyclerView(recyclerView)
    }

    fun detachFromRecyclerView() {
        voiceMessagePlayer.stop()
        itemTouchHelper?.attachToRecyclerView(null)
    }

    private fun playVoiceMessage(message: Message, holder: MessageViewHolder) {
        voiceMessagePlayer.setProgressUpdateListener { progress ->
            holder.messageDuration.text = "${(progress * 100).toInt()}%"
        }
        
        voiceMessagePlayer.play(message, holder.playButton)
        currentPlayingMessage = message
        currentPlayingViewHolder = holder
    }

    private fun stopVoiceMessage() {
        voiceMessagePlayer.stop()
        currentPlayingMessage = null
        currentPlayingViewHolder?.playButton?.setImageResource(R.drawable.ic_play)
        currentPlayingViewHolder = null
    }

    private var messages: List<Message> = emptyList()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    companion object {
        private const val VIEW_TYPE_TEXT = 1
        private const val VIEW_TYPE_IMAGE = 2
        private const val VIEW_TYPE_VOICE = 3
        private const val VIEW_TYPE_LOCATION = 4
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TEXT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_text, parent, false)
                TextMessageViewHolder(view)
            }
            VIEW_TYPE_IMAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_image, parent, false)
                ImageMessageViewHolder(view)
            }
            VIEW_TYPE_VOICE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_voice, parent, false)
                VoiceMessageViewHolder(view)
            }
            VIEW_TYPE_LOCATION -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_location, parent, false)
                LocationMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    private inner class BaseMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMessageLongClick.invoke(messages[position])
                    true
                } else {
                    false
                }
            }
        }
    }

    private inner class MessageViewHolder(itemView: View) : BaseMessageViewHolder(itemView) {
        private val messageText = itemView.message_text
        private val messageTime = itemView.message_time
        private val messageStatus = itemView.message_status
        private val replyIndicator = itemView.replyIndicator
        private val replyContent = itemView.replyContent
        private val messageDuration = itemView.message_duration
        private val playButton = itemView.play_button
        private val replyType = itemView.replyType
        private val replyContent = itemView.replyContent
        
        init {
            // Set up touch listener for progress scrubbing
            itemView.setOnTouchListener { _, event ->
                if (currentPlayingMessage?.id == getItem(bindingAdapterPosition)?.id) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            val x = event.x
                            val width = itemView.width
                            val progress = x / width
                            val position = (progress * voiceMessagePlayer.getDuration()).toInt()
                            voiceMessagePlayer.seekTo(position)
                        }
                    }
                }
                false
            }
        }

        private fun bindMessage(message: Message) {
            itemView.setBackgroundColor(
                when (message.type) {
                    MessageType.VOICE -> Color.parseColor("#FF4081")
                    else -> Color.parseColor("#FFC107")
                }
            )

            when (message.type) {
                MessageType.TEXT -> {
                    messageText.visibility = View.VISIBLE
                    messageText.text = message.text
                    messageDuration.visibility = View.GONE
                    playButton.visibility = View.GONE
                }
                MessageType.VOICE -> {
                    messageText.visibility = View.GONE
                    messageDuration.visibility = View.VISIBLE
                    messageDuration.text = "${message.duration / 1000}s"
                    playButton.visibility = View.VISIBLE
                    
                    // Set up voice message player
                    playButton.setOnClickListener {
                        if (currentPlayingMessage?.id == message.id) {
                            stopVoiceMessage()
                        } else {
                            playVoiceMessage(message, this)
                        }
                    }
                    
                    // Update play button state
                    playButton.setImageResource(
                        if (currentPlayingMessage?.id == message.id) R.drawable.ic_pause
                        else R.drawable.ic_play
                    )
                }
                else -> {
                    messageText.visibility = View.VISIBLE
                    messageText.text = message.text
                    messageDuration.visibility = View.GONE
                    playButton.visibility = View.GONE
                }
            }

            messageTime.text = dateFormat.format(message.timestamp)
            messageStatus.setImageResource(
                when (message.status) {
                    Message.Status.SENT -> R.drawable.ic_sent
                    Message.Status.DELIVERED -> R.drawable.ic_delivered
                    Message.Status.READ -> R.drawable.ic_read
                    else -> R.drawable.ic_pending
                }
            )

            // Show reply information if this is a reply message
            if (message.replyTo != null) {
                replyIndicator.visibility = View.VISIBLE
                replyContent.visibility = View.VISIBLE
                // Set reply type with icon
                replyType.text = when (message.replyType) {
                    MessageType.TEXT -> "Text"
                    MessageType.IMAGE -> "Image"
                    MessageType.VOICE -> "Voice"
                    MessageType.LOCATION -> "Location"
                    else -> "Message"
                }

                // Set reply content with ellipsis if too long
                replyContent.text = message.replyContent

                // Set reply sender
                val sender = if (message.senderId == userId) {
                    "You"
                } else {
                    "Someone"
                }
                replySender.text = "-$sender"

                // Setup reply options menu
                replyOptions.setOnClickListener {
                    val popup = PopupMenu(context, replyOptions)
                    popup.menuInflater.inflate(R.menu.reply_options_menu, popup.menu)

                    popup.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.copy_reply -> {
                                // copyToClipboard(message.replyContent)
                                true
                            }
                            R.id.reply_to_reply -> {
                                // replyToMessage(message)
                                true
                            }
                            R.id.delete_reply -> {
                                // deleteReply(message)
                                true
                            }
                            else -> false
                        }
                    }

                    popup.show()
                }
            } else {
                replyCard.visibility = View.GONE
            }
        }

        fun bind(message: Message) {
            bindTextMessage(message)
            itemView.message_time.text = dateFormat.format(message.timestamp)
        }
    }

    private inner class ImageMessageViewHolder(itemView: View) : BaseMessageViewHolder(itemView) {
        private val messageImage = itemView.message_image
        private val messageTime = itemView.message_time
        private val messageStatus = itemView.message_status

        fun bind(message: Message) {
            Glide.with(context)
                .load(message.imageUrl)
                .into(messageImage)
            messageTime.text = dateFormat.format(message.timestamp)
            
            // Set message status
            when (message.status) {
                Message.Status.SENT -> messageStatus.setImageResource(R.drawable.ic_sent)
                Message.Status.DELIVERED -> messageStatus.setImageResource(R.drawable.ic_delivered)
                Message.Status.READ -> messageStatus.setImageResource(R.drawable.ic_read)
                else -> messageStatus.setImageResource(R.drawable.ic_pending)
            }
        }
    }

    private inner class VoiceMessageViewHolder(itemView: View) : BaseMessageViewHolder(itemView) {
        private val messageDuration = itemView.message_duration
        private val messageTime = itemView.message_time
        private val messageStatus = itemView.message_status

        fun bind(message: Message) {
            messageDuration.text = message.duration
            messageTime.text = dateFormat.format(message.timestamp)
            
            // Set message status
            when (message.status) {
                Message.Status.SENT -> messageStatus.setImageResource(R.drawable.ic_sent)
                Message.Status.DELIVERED -> messageStatus.setImageResource(R.drawable.ic_delivered)
                Message.Status.READ -> messageStatus.setImageResource(R.drawable.ic_read)
                else -> messageStatus.setImageResource(R.drawable.ic_pending)
            }
        }
    }

    private inner class LocationMessageViewHolder(itemView: View) : BaseMessageViewHolder(itemView) {
        private val messageLocation = itemView.message_location
        private val messageTime = itemView.message_time
        private val messageStatus = itemView.message_status

        fun bind(message: Message) {
            messageLocation.text = message.location
            messageTime.text = dateFormat.format(message.timestamp)
            
            // Set message status
            when (message.status) {
                Message.Status.SENT -> messageStatus.setImageResource(R.drawable.ic_sent)
                Message.Status.DELIVERED -> messageStatus.setImageResource(R.drawable.ic_delivered)
                Message.Status.READ -> messageStatus.setImageResource(R.drawable.ic_read)
                else -> messageStatus.setImageResource(R.drawable.ic_pending)
            }
        }
    }
        return when (viewType) {
            VIEW_TYPE_TEXT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_text, parent, false)
                TextMessageViewHolder(view)
            }
            VIEW_TYPE_IMAGE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_image, parent, false)
                ImageMessageViewHolder(view)
            }
            VIEW_TYPE_VOICE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_voice, parent, false)
                VoiceMessageViewHolder(view)
            }
            VIEW_TYPE_LOCATION -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_location, parent, false)
                LocationMessageViewHolder(view)
            }
            else -> {
                throw IllegalArgumentException("Unknown view type: $viewType")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        
        when (holder) {
            is TextMessageViewHolder -> {
                holder.bind(message)
            }
            is ImageMessageViewHolder -> {
                holder.bind(message)
            }
            is VoiceMessageViewHolder -> {
                holder.bind(message)
            }
            is LocationMessageViewHolder -> {
                holder.bind(message)
            }
        }
    }

    private fun bindMessageViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageContentTextView.text = message.content
        holder.messageTimeTextView.text = formatTime(message.timestamp)

        // Set message status
        when (message.status) {
            MessageStatus.SENT -> {
                holder.messageStatusChip.text = ""
                holder.messageStatusChip.setIconResource(R.drawable.ic_clock)
                holder.messageStatusChip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.sent))
            }
            MessageStatus.DELIVERED -> {
                holder.messageStatusChip.text = ""
                holder.messageStatusChip.setIconResource(R.drawable.ic_double_check)
                holder.messageStatusChip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.delivered))
            }
            MessageStatus.SEEN -> {
                holder.messageStatusChip.text = ""
                holder.messageStatusChip.setIconResource(R.drawable.ic_double_check)
                holder.messageStatusChip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.seen))
            }
        }

        if (message.senderId == auth?.uid) {
            holder.itemView.setBackgroundResource(R.drawable.message_background_sent)
            holder.messageStatusChip.visibility = View.VISIBLE
        } else {
            holder.itemView.setBackgroundResource(R.drawable.message_background_received)
            holder.messageStatusChip.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when (message.type) {
            MessageType.TEXT -> VIEW_TYPE_TEXT
            MessageType.IMAGE -> VIEW_TYPE_IMAGE
            MessageType.VOICE -> VIEW_TYPE_VOICE
            MessageType.LOCATION -> VIEW_TYPE_LOCATION
            else -> VIEW_TYPE_TEXT
        }
    }

    fun submitMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    fun addMessage(message: Message) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

        fun bind(message: Message) {
            messageText.text = message.content
            messageTime.text = message.timestamp.toDate().format(DateTimeFormatter.ofPattern("hh:mm a"))
            
            // Set message status
            when (message.status) {
                MessageStatus.SENT -> messageStatus.setImageResource(R.drawable.ic_sent)
                MessageStatus.DELIVERED -> messageStatus.setImageResource(R.drawable.ic_delivered)
                MessageStatus.SEEN -> messageStatus.setImageResource(R.drawable.ic_seen)
                MessageStatus.FAILED -> messageStatus.setImageResource(R.drawable.ic_failed)
            }
        }
    }

    class ImageMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageImage: ImageView = itemView.findViewById(R.id.messageImage)
        private val messageTime: TextView = itemView.findViewById(R.id.messageTime)

        fun bind(message: Message) {
            Glide.with(itemView.context)
                .load(message.imageUrl)
                .into(messageImage)
            messageTime.text = message.timestamp.toDate().format(DateTimeFormatter.ofPattern("hh:mm a"))
        }
    }

    class VoiceMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageAudio: ImageView = itemView.findViewById(R.id.messageAudio)
        private val messageDuration: TextView = itemView.findViewById(R.id.messageDuration)
        private val messageTime: TextView = itemView.findViewById(R.id.messageTime)

        fun bind(message: Message) {
            messageDuration.text = String.format("%02d:%02d", 
                message.duration?.div(1000)?.div(60) ?: 0,
                message.duration?.div(1000)?.rem(60) ?: 0)
            messageTime.text = message.timestamp.toDate().format(DateTimeFormatter.ofPattern("hh:mm a"))
        }
    }

    class LocationMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val locationAddress: TextView = itemView.findViewById(R.id.locationAddress)
        private val locationMapPreview: ImageView = itemView.findViewById(R.id.locationMapPreview)
        private val locationLatitude: TextView = itemView.findViewById(R.id.locationLatitude)
        private val locationLongitude: TextView = itemView.findViewById(R.id.locationLongitude)
        private val openMapButton: MaterialButton = itemView.findViewById(R.id.openMapButton)
        private val messageTime: TextView = itemView.findViewById(R.id.messageTime)

        fun bind(message: Message) {
            val location = message.location ?: return
            
            // Set address
            locationAddress.text = location.address
            
            // Set coordinates
            locationLatitude.text = String.format("Latitude: %.6f", location.latitude)
            locationLongitude.text = String.format("Longitude: %.6f", location.longitude)
            
            // Set timestamp
            messageTime.text = message.timestamp.toDate().format(DateTimeFormatter.ofPattern("hh:mm a"))
            
            // Set map preview (can be implemented with a map snapshot API)
            locationMapPreview.setImageResource(R.drawable.ic_map_preview)
            
            // Open in Maps click handler
            openMapButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(
                    "geo:${location.latitude},${location.longitude}"))
                itemView.context.startActivity(intent)
            }
        }
    }

    private class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}
