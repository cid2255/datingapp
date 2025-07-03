package com.example.datingapp.viewholders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.datingapp.models.Match
import com.example.datingapp.models.MatchStatus
import com.example.datingapp.models.PremiumFeature
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val profileImage: CircleImageView = itemView.findViewById(R.id.profileImage)
    private val premiumBadge: BadgeDrawable = itemView.findViewById(R.id.premiumBadge)
    private val nameText: TextView = itemView.findViewById(R.id.nameText)
    private val statusIndicator: Chip = itemView.findViewById(R.id.statusIndicator)
    private val lastMessageText: TextView = itemView.findViewById(R.id.lastMessageText)
    private val unreadCount: Chip = itemView.findViewById(R.id.unreadCount)
    private val distanceText: TextView = itemView.findViewById(R.id.distanceText)
    private val ageText: TextView = itemView.findViewById(R.id.ageText)
    private val lastActiveText: TextView = itemView.findViewById(R.id.lastActiveText)
    private val callButton: MaterialButton = itemView.findViewById(R.id.callButton)
    private val premiumFeaturesGroup: ChipGroup = itemView.findViewById(R.id.premiumFeaturesGroup)
    private val loadingIndicator: CircularProgressIndicator = itemView.findViewById(R.id.loadingIndicator)
    private val errorText: TextView = itemView.findViewById(R.id.errorText)

    private val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun bind(match: Match, onMatchClick: (Match) -> Unit, onCallClick: (Match) -> Unit) {
        // Set loading state
        loadingIndicator.visibility = View.GONE
        errorText.visibility = View.GONE

        try {
            // Load profile image
            Glide.with(itemView.context)
                .load(match.user2Photo)
                .circleCrop()
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_error)
                .into(profileImage)

            // Set name
            nameText.text = match.user2Name

            // Set status
            setStatus(match.status)

            // Set last message
            lastMessageText.text = match.lastMessage

            // Set unread count
            unreadCount.text = match.unreadCount.toString()
            unreadCount.visibility = if (match.unreadCount > 0) View.VISIBLE else View.GONE

            // Set distance
            distanceText.text = if (match.distance != null) {
                "%.1f km away".format(match.distance)
            } else {
                "Nearby"
            }

            // Set age
            ageText.text = if (match.user2Age != null) {
                "${match.user2Age} years"
            } else {
                ""
            }

            // Set last active
            val timestamp = match.lastMessageTime ?: match.timestamp
            val date = Date(timestamp)
            val now = Date()
            val calendar = Calendar.getInstance()
            calendar.time = date

            if (calendar.get(Calendar.DATE) == now.get(Calendar.DATE)) {
                lastActiveText.text = "Today at ${timeFormat.format(date)}"
            } else if (calendar.get(Calendar.DATE) == now.get(Calendar.DATE) - 1) {
                lastActiveText.text = "Yesterday at ${timeFormat.format(date)}"
            } else {
                lastActiveText.text = "${dateFormat.format(date)} at ${timeFormat.format(date)}"
            }

            // Set premium features
            setPremiumFeatures(match.premiumFeatures)

            // Set premium badge
            premiumBadge.visibility = if (match.premiumFeatures.isNotEmpty()) View.VISIBLE else View.GONE

            // Set click listeners
            itemView.setOnClickListener { onMatchClick(match) }
            callButton.setOnClickListener { onCallClick(match) }

        } catch (e: Exception) {
            // Show error state
            loadingIndicator.visibility = View.GONE
            errorText.text = "Error loading match"
            errorText.visibility = View.VISIBLE
        }
    }

    private fun setStatus(status: MatchStatus) {
        statusIndicator.text = when (status) {
            MatchStatus.MATCHED -> "Active"
            MatchStatus.BLOCKED -> "Blocked"
            MatchStatus.REPORTED -> "Reported"
            MatchStatus.ARCHIVED -> "Archived"
            MatchStatus.DELETED -> "Deleted"
        }

        statusIndicator.chipBackgroundColor = when (status) {
            MatchStatus.MATCHED -> itemView.context.getColorStateList(R.color.status_active)
            MatchStatus.BLOCKED -> itemView.context.getColorStateList(R.color.status_blocked)
            MatchStatus.REPORTED -> itemView.context.getColorStateList(R.color.status_reported)
            MatchStatus.ARCHIVED -> itemView.context.getColorStateList(R.color.status_archived)
            MatchStatus.DELETED -> itemView.context.getColorStateList(R.color.status_deleted)
        }
    }

    private fun setPremiumFeatures(features: List<PremiumFeature>) {
        premiumFeaturesGroup.removeAllViews()
        
        features.forEach { feature ->
            val chip = Chip(itemView.context).apply {
                text = feature.name
                isCheckable = false
                chipBackgroundColor = itemView.context.getColorStateList(R.color.premium_background)
                chipStrokeWidth = 1f
                chipStrokeColor = itemView.context.getColorStateList(R.color.premium_border)
            }
            premiumFeaturesGroup.addView(chip)
        }

        premiumFeaturesGroup.visibility = if (features.isNotEmpty()) View.VISIBLE else View.GONE
    }
}
