package com.example.datingapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.databinding.ItemMessageBinding
import com.example.datingapp.databinding.ItemMessageReactionsBinding
import com.example.datingapp.databinding.ItemVoiceMessageBinding
import com.example.datingapp.models.chat.*
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val onMessageClick: (ChatMessage) -> Unit,
    private val onReactionClick: (ReactionType) -> Unit,
    private val onVoicePlayClick: (ChatMessage) -> Unit,
    private val onVoicePauseClick: (ChatMessage) -> Unit
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCallback()) {

    private class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return when (message.type) {
            MessageType.TEXT -> R.layout.item_message
            MessageType.VOICE, MessageType.VOICE_NOTE -> R.layout.item_voice_message
            else -> R.layout.item_message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_message -> MessageViewHolder(
                ItemMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            R.layout.item_voice_message -> VoiceMessageViewHolder(
                ItemVoiceMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> MessageViewHolder(
                ItemMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is MessageViewHolder -> holder.bind(message)
            is VoiceMessageViewHolder -> holder.bind(message)
        }
    }

    inner class MessageViewHolder(
        private val binding: ItemMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener { onMessageClick(getItem(adapterPosition)) }
            binding.reactionsButton.setOnClickListener {
                showReactionsMenu(binding.reactionsButton)
            }
        }

        fun bind(message: ChatMessage) {
            // Set message content based on type
            when (message.type) {
                MessageType.TEXT -> {
                    binding.messageText.text = message.content
                }
                MessageType.IMAGE -> {
                    Glide.with(binding.root)
                        .load(message.mediaUrl)
                        .into(binding.messageImage)
                }
                MessageType.LOCATION -> {
                    binding.messageText.text = "Location shared"
                }
                MessageType.FILE -> {
                    binding.messageText.text = "File: ${message.fileName}"
                }
                MessageType.CONTACT -> {
                    binding.messageText.text = "Contact shared"
                }
                MessageType.VIDEO -> {
                    binding.messageVideo.setVideoURI(message.mediaUrl)
                }
                else -> {
                    binding.messageText.text = message.content
                }
            }

            // Set status chip
            when (message.status) {
                MessageStatus.SENT -> {
                    binding.messageStatusChip.text = ""
                    binding.messageStatusChip.setIconResource(R.drawable.ic_clock)
                    binding.messageStatusChip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.sent))
                }
                MessageStatus.DELIVERED -> {
                    binding.messageStatusChip.text = ""
                    binding.messageStatusChip.setIconResource(R.drawable.ic_double_check)
                    binding.messageStatusChip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.delivered))
                }
                MessageStatus.SEEN -> {
                    binding.messageStatusChip.text = ""
                    binding.messageStatusChip.setIconResource(R.drawable.ic_double_check)
                    binding.messageStatusChip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.seen))
                }
                MessageStatus.FAILED -> {
                    binding.messageStatusChip.text = "Failed"
                    binding.messageStatusChip.setIconResource(R.drawable.ic_error)
                    binding.messageStatusChip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.error))
                }
            }

            // Format and set timestamp
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            binding.timestampText.text = dateFormat.format(message.timestamp)

            // Set background based on sender
            if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
                binding.root.setBackgroundResource(R.drawable.message_background_sent)
                binding.messageStatusChip.visibility = View.VISIBLE
            } else {
                binding.root.setBackgroundResource(R.drawable.message_background_received)
                binding.messageStatusChip.visibility = View.INVISIBLE
            }

            // Set message alignment
            binding.root.apply {
                if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
                    layoutParams = layoutParams.apply {
                        gravity = android.view.Gravity.END
                    }
                    binding.messageCard.strokeColor = context.getColor(R.color.primary)
                } else {
                    layoutParams = layoutParams.apply {
                        gravity = android.view.Gravity.START
                    }
                    binding.messageCard.strokeColor = context.getColor(R.color.secondary)
                }
            }

            // Set reactions
            if (message.reactions.isNotEmpty()) {
                binding.reactionsButton.visibility = View.VISIBLE
                binding.reactionsButton.text = "${message.reactions.size}"
            } else {
                binding.reactionsButton.visibility = View.GONE
            }
        }

        private fun showReactionsMenu(anchor: View) {
            val reactions = ReactionType.values()
            val menu = PopupMenu(binding.root.context, anchor)
            reactions.forEach { reaction ->
                menu.menu.add(reaction.emoji)
            }
            menu.setOnMenuItemClickListener { item ->
                onReactionClick(ReactionType.fromEmoji(item.title.toString())!!)
                true
            }
            menu.show()
        }

        private fun formatTimestamp(timestamp: Date): String {
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(timestamp)
        }
    }

    inner class VoiceMessageViewHolder(
        private val binding: ItemVoiceMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.playButton.setOnClickListener {
                val message = getItem(adapterPosition)
                if (currentPlayingMessage == message) {
                    onVoicePauseClick(message)
                } else {
                    onVoicePlayClick(message)
                }
            }
            binding.downloadButton.setOnClickListener {
                // Handle download
            }
        }

        fun bind(message: ChatMessage) {
            // Set message alignment
            binding.root.apply {
                if (message.senderId == FirebaseAuth.getInstance().currentUser?.uid) {
                    layoutParams = layoutParams.apply {
                        gravity = android.view.Gravity.END
                    }
                } else {
                    layoutParams = layoutParams.apply {
                        gravity = android.view.Gravity.START
                    }
                }
            }

            // Set duration
            binding.durationTextView.text = formatDuration(message.duration)

            // Set seek bar progress
            binding.seekBar.progress = message.progress

            // Update play button icon
            binding.playButton.setImageResource(
                if (currentPlayingMessage == message) R.drawable.ic_pause
                else R.drawable.ic_play_arrow
            )
        }

        private fun formatDuration(duration: Long): String {
            val minutes = duration / 60
            val seconds = duration % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun updateMessage(message: ChatMessage) {
        val position = currentList.indexOfFirst { it.id == message.id }
        if (position != -1) {
            currentList[position] = message
            notifyItemChanged(position)
        }
    }

    fun addReaction(messageId: String, reaction: Reaction) {
        val position = currentList.indexOfFirst { it.id == messageId }
        if (position != -1) {
            val message = currentList[position]
            val updatedMessage = message.copy(
                reactions = message.reactions.toMutableList().apply {
                    add(reaction)
                }
            )
            currentList[position] = updatedMessage
            notifyItemChanged(position)
        }
    }

    fun removeReaction(messageId: String, userId: String) {
        val position = currentList.indexOfFirst { it.id == messageId }
        if (position != -1) {
            val message = currentList[position]
            val updatedMessage = message.copy(
                reactions = message.reactions.filter { it.userId != userId }
            )
            currentList[position] = updatedMessage
            notifyItemChanged(position)
        }
    }
}
