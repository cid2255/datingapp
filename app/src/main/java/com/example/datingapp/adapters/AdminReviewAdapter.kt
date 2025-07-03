package com.example.datingapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.models.User
import com.example.datingapp.models.VerificationStatus
import kotlinx.android.synthetic.main.item_user_review.view.*

class AdminReviewAdapter(
    private var users: List<User>,
    private val onAction: (User, Action) -> Unit
) : RecyclerView.Adapter<AdminReviewAdapter.UserViewHolder>() {

    enum class Action {
        VIEW_ID, APPROVE, REJECT
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(user: User, onAction: (User, Action) -> Unit) {
            // Set profile image
            Glide.with(itemView.context)
                .load(user.profileImageUrl)
                .into(itemView.profileImageView)

            // Set username
            itemView.usernameTextView.text = user.username

            // Set verification status
            itemView.verificationStatusTextView.text = user.verificationStatus.getVerificationText()
            itemView.verificationStatusTextView.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    user.verificationStatus.getVerificationColor()
                )
            )

            // Set ID proof status
            itemView.idProofTextView.text = if (user.idProofUrl != null) {
                "ID Proof Available"
            } else {
                "No ID Proof"
            }

            // Set click listeners
            itemView.viewIdButton.setOnClickListener {
                onAction(user, Action.VIEW_ID)
            }

            itemView.approveButton.setOnClickListener {
                onAction(user, Action.APPROVE)
            }

            itemView.rejectButton.setOnClickListener {
                onAction(user, Action.REJECT)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_review, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position], onAction)
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
