package com.example.datingapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.models.User
import com.example.datingapp.utils.FirebaseStructure
import kotlinx.android.synthetic.main.item_user.view.*
import java.text.SimpleDateFormat
import java.util.*

class UserAdapter(
    private val onUserClick: (User) -> Unit,
    private val onLikeClick: (User) -> Unit
) : ListAdapter<User, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(user: User) {
            // Set profile image with placeholder
            Glide.with(itemView.context)
                .load(user.profileImageUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(itemView.profileImageView)

            // Set user info
            itemView.usernameTextView.text = user.username
            itemView.ageTextView.text = "${user.age}"
            itemView.locationTextView.text = user.location
            itemView.aboutTextView.text = user.about

            // Format interests
            val interests = user.interests.take(3).joinToString(", ") { it }
            itemView.interestsTextView.text = interests

            // Format last online time
            itemView.lastOnlineTextView.text = "Last seen: ${formatLastOnline(user.lastOnline)}"

            // Set like button state
            itemView.likeButton.apply {
                text = if (user.liked) {
                    itemView.context.getString(R.string.liked)
                } else {
                    itemView.context.getString(R.string.like)
                }
                setOnClickListener { onLikeClick(user) }
            }

            // Handle item click
            itemView.setOnClickListener { onUserClick(user) }
        }

        private fun formatLastOnline(timestamp: com.google.firebase.Timestamp): String {
            val date = timestamp.toDate()
            val now = Date()
            val diff = now.time - date.time
            
            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000} minutes ago"
                diff < 86400000 -> "${diff / 3600000} hours ago"
                else -> dateFormat.format(date)
            }
        }
    }

    private class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
