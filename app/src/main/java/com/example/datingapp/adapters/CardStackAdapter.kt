package com.example.datingapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.datingapp.databinding.ItemProfileCardBinding
import com.example.datingapp.models.Profile
import com.example.datingapp.models.enums.PremiumFeature
import com.example.datingapp.models.enums.SwipeType
import com.example.datingapp.utils.loadImage

class CardStackAdapter(
    private val onLike: (Profile) -> Unit,
    private val onSuperLike: (Profile) -> Unit,
    private val onDislike: (Profile) -> Unit,
    private val onRewind: () -> Unit,
    private val onBoost: (Profile) -> Unit,
    private val onHighlight: (Profile) -> Unit,
    private val onVerifyPhoto: (Profile) -> Unit
) : ListAdapter<Profile, CardStackAdapter.CardViewHolder>(ProfileDiffCallback()) {

    private var currentProfile: Profile? = null
    private var previousProfile: Profile? = null
    private var isPremium = false

    fun setPremiumStatus(premium: Boolean) {
        isPremium = premium
        notifyItemRangeChanged(0, currentList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemProfileCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CardViewHolder(
        private val binding: ItemProfileCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(profile: Profile) {
            currentProfile = profile
            updateProfileInfo(profile)
            setupButtons(profile)
            setupPremiumFeatures(profile)
            setupAnimations(profile)
            setupStackEffects(profile)
        }

        private fun updateProfileInfo(profile: Profile) {
            binding.apply {
                profileImage.loadImage(profile.imageUrl)
                nameAgeText.text = "${profile.name}, ${profile.age}"
                locationText.text = profile.location
                jobTitleText.text = profile.jobTitle
                companyText.text = profile.company
                bioText.text = profile.bio
                setupInterests(profile.interests)
                setupEducation(profile.education)
                setupMutualInterests(profile.mutualInterests)
            }
        }

        private fun setupButtons(profile: Profile) {
            binding.apply {
                likeButton.setOnClickListener { onLike(profile) }
                superLikeButton.setOnClickListener { onSuperLike(profile) }
                dislikeButton.setOnClickListener { onDislike(profile) }
                rewindButton.setOnClickListener { onRewind() }
                boostButton.setOnClickListener { onBoost(profile) }
                highlightButton.setOnClickListener { onHighlight(profile) }
                verifyPhotoButton.setOnClickListener { onVerifyPhoto(profile) }
            }
        }

        private fun setupPremiumFeatures(profile: Profile) {
            binding.apply {
                profileImageOverlay.isVisible = profile.isPremium
                boostBadge.isVisible = profile.isBoosted
                premiumBadge.isVisible = profile.isPremium
                highlightBadge.isVisible = profile.isHighlighted
                verifiedBadge.isVisible = profile.isVerified

                premiumFeaturesChipGroup.setChips(
                    if (isPremium) {
                        PremiumFeature.values().map { feature ->
                            PremiumFeatureChip(
                                id = feature.id,
                                title = feature.title,
                                description = feature.description,
                                icon = feature.icon,
                                usage = profile.featureUsage[feature] ?: 0,
                                dailyLimit = feature.dailyLimit,
                                monthlyLimit = feature.monthlyLimit
                            )
                        }
                    } else {
                        emptyList()
                    }
                )
            }
        }

        private fun setupAnimations(profile: Profile) {
            binding.apply {
                // Setup animation layers
                likeAnimation.isVisible = false
                superLikeAnimation.isVisible = false
                dislikeAnimation.isVisible = false
                boostAnimation.isVisible = false
                highlightAnimation.isVisible = false
                verificationAnimation.isVisible = false

                // Setup animation listeners
                likeAnimation.setOnClickListener { onLike(profile) }
                superLikeAnimation.setOnClickListener { onSuperLike(profile) }
                dislikeAnimation.setOnClickListener { onDislike(profile) }
                boostAnimation.setOnClickListener { onBoost(profile) }
                highlightAnimation.setOnClickListener { onHighlight(profile) }
                verificationAnimation.setOnClickListener { onVerifyPhoto(profile) }
            }
        }

        private fun setupStackEffects(profile: Profile) {
            binding.apply {
                // Setup stack shadow
                stackShadow.alpha = 0.1f
                stackShadow.translationY = (adapterPosition * 10).toFloat()

                // Setup stack elevation
                profileCard.cardElevation = (16 - (adapterPosition * 2)).toFloat()
                profileCard.maxCardElevation = 16f

                // Setup stack scale
                val scale = 1f - (adapterPosition * 0.02f)
                profileCard.scaleX = scale
                profileCard.scaleY = scale

                // Setup stack opacity
                profileCard.alpha = 1f - (adapterPosition * 0.1f)
            }
        }

        private fun setupInterests(interests: List<String>) {
            binding.interestsRecyclerView.apply {
                adapter = InterestAdapter(interests)
                layoutManager = LinearLayoutManager(
                    binding.root.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            }
        }

        private fun setupEducation(education: List<String>) {
            binding.educationRecyclerView.apply {
                adapter = EducationAdapter(education)
                layoutManager = LinearLayoutManager(
                    binding.root.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            }
        }

        private fun setupMutualInterests(mutualInterests: List<String>) {
            binding.mutualInterestsRecyclerView.apply {
                adapter = MutualInterestAdapter(mutualInterests)
                layoutManager = LinearLayoutManager(
                    binding.root.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            }
        }
    }
}

class ProfileDiffCallback : DiffUtil.ItemCallback<Profile>() {
    override fun areItemsTheSame(oldItem: Profile, newItem: Profile): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Profile, newItem: Profile): Boolean {
        return oldItem == newItem
    }
}
