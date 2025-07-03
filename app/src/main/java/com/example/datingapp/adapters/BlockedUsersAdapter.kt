package com.example.datingapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.datingapp.databinding.ItemBlockedUserBinding
import com.example.datingapp.models.Block
import com.example.datingapp.utils.formatTimestamp

class BlockedUsersAdapter(
    private val onUnblockClick: (Block) -> Unit
) : ListAdapter<Block, BlockedUsersAdapter.ViewHolder>(BlockDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBlockedUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemBlockedUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(block: Block) {
            binding.apply {
                blockedUserText.text = block.blockedUserDisplayName
                blockedTimeText.text = "Blocked on: ${block.timestamp.formatTimestamp()}"
                blockReasonText.text = block.reason ?: "No reason specified"
                
                unblockButton.setOnClickListener {
                    onUnblockClick(block)
                }
            }
        }
    }

    private class BlockDiffCallback : DiffUtil.ItemCallback<Block>() {
        override fun areItemsTheSame(oldItem: Block, newItem: Block): Boolean {
            return oldItem.blockedUserId == newItem.blockedUserId
        }

        override fun areContentsTheSame(oldItem: Block, newItem: Block): Boolean {
            return oldItem == newItem
        }
    }
}
