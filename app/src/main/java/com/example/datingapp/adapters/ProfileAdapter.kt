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

class ProfileAdapter(
    private val onLike: (Profile) -> Unit,
    private val onSuperLike: (Profile) -> Unit,
    private val onDislike: (Profile) -> Unit,
    private val onRewind: () -> Unit
) : ListAdapter<Profile, ProfileAdapter.ProfileViewHolder>(ProfileDiffCallback()) {

    private var currentProfile: Profile? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val binding = ItemProfileCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProfileViewHolder(
        private val binding: ItemProfileCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(profile: Profile) {
            currentProfile = profile
            updateProfileInfo(profile)
            setupButtons(profile)
            setupPremiumFeatures(profile)
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
            }
        }

        private fun setupPremiumFeatures(profile: Profile) {
            binding.apply {
                profileImageOverlay.isVisible = profile.isPremium
                boostBadge.isVisible = profile.isBoosted
                premiumBadge.isVisible = profile.isPremium

                premiumFeaturesChipGroup.setChips(
                    if (profile.isPremium) {
                        PremiumFeature.values().map { feature ->
                            PremiumFeatureChip(
                                id = feature.id,
                                title = feature.title,
                                description = feature.description,
                                icon = feature.icon
                            )
                        }
                    } else {
                        emptyList()
                    }
                )
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
