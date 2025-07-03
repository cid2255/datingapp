package com.example.datingapp.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.datingapp.R
import com.example.datingapp.adapters.ProfileAdapter
import com.example.datingapp.databinding.ActivityMatchBinding
import com.example.datingapp.models.enums.PremiumFeature
import com.example.datingapp.models.enums.SwipeType
import com.example.datingapp.viewmodels.MatchViewModel
import com.github.florent37.swipe.Swipe
import com.github.florent37.swipe.SwipeListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MatchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatchBinding
    private val viewModel: MatchViewModel by viewModels()
    private lateinit var profileAdapter: ProfileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSwipeStack()
        setupObservers()
        setupPremiumFeatures()
    }

    private fun setupRecyclerView() {
        profileAdapter = ProfileAdapter(
            onLike = { profile -> viewModel.likeProfile(profile) },
            onSuperLike = { profile -> viewModel.superLikeProfile(profile) },
            onDislike = { profile -> viewModel.dislikeProfile(profile) },
            onRewind = { viewModel.rewind() }
        )

        binding.profileRecyclerView.apply {
            adapter = profileAdapter
            layoutManager = LinearLayoutManager(this@MatchActivity)
        }
    }

    private fun setupSwipeStack() {
        binding.swipeStackView.setListener(object : SwipeListener {
            override fun onCardSwiped(direction: Swipe.Direction?) {
                when (direction) {
                    Swipe.Direction.RIGHT -> viewModel.likeProfile(profileAdapter.currentProfile)
                    Swipe.Direction.LEFT -> viewModel.dislikeProfile(profileAdapter.currentProfile)
                    Swipe.Direction.UP -> viewModel.superLikeProfile(profileAdapter.currentProfile)
                    else -> {}
                }
            }

            override fun onCardDragged(percentX: Float, percentY: Float) {
                // Handle drag animations
            }

            override fun getLikeDirection(): Swipe.Direction = Swipe.Direction.RIGHT
            override fun getDislikeDirection(): Swipe.Direction = Swipe.Direction.LEFT
            override fun getSuperLikeDirection(): Swipe.Direction = Swipe.Direction.UP
        })
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                handleUiState(state)
            }
        }

        lifecycleScope.launch {
            viewModel.matchEvents.collectLatest { event ->
                handleMatchEvent(event)
            }
        }
    }

    private fun handleUiState(state: MatchViewModel.UiState) {
        binding.apply {
            swipeStackView.isVisible = state.hasProfiles
            emptyStateLayout.isVisible = !state.hasProfiles
            stackCounter.text = state.profileCount.toString()
            superLikeButton.isEnabled = state.canSuperLike
            rewindButton.isEnabled = state.canRewind
        }
    }

    private fun handleMatchEvent(event: MatchViewModel.MatchEvent) {
        when (event) {
            is MatchViewModel.MatchEvent.Like -> showLikeAnimation(event.profile)
            is MatchViewModel.MatchEvent.SuperLike -> showSuperLikeAnimation(event.profile)
            is MatchViewModel.MatchEvent.Dislike -> showDislikeAnimation(event.profile)
            is MatchViewModel.MatchEvent.Match -> showMatchAnimation(event.match)
            is MatchViewModel.MatchEvent.Rewind -> showRewindAnimation(event.profile)
            is MatchViewModel.MatchEvent.Boost -> showBoostAnimation(event.profile)
            is MatchViewModel.MatchEvent.Highlight -> showHighlightAnimation(event.profile)
            is MatchViewModel.MatchEvent.PhotoVerified -> showPhotoVerificationAnimation(event.profile)
            is MatchViewModel.MatchEvent.FeatureLimitReached -> showFeatureLimitDialog(event.feature)
            is MatchViewModel.MatchEvent.ProfileBoosted -> showProfileBoostedDialog(event.profile)
            is MatchViewModel.MatchEvent.ProfileHighlighted -> showProfileHighlightedDialog(event.profile)
        }
    }

    private fun showLikeAnimation(profile: Profile) {
        binding.likeAnimation.isVisible = true
        binding.likeAnimation.startAnimation(
            R.anim.slide_in_right,
            onEnd = { 
                binding.likeAnimation.isVisible = false
                playSound(R.raw.like_sound)
            }
        )
    }

    private fun showSuperLikeAnimation(profile: Profile) {
        binding.superLikeAnimation.isVisible = true
        binding.superLikeFireworks.startAnimation()
        binding.superLikeAnimation.startAnimation(
            R.anim.slide_in_up,
            onEnd = {
                binding.superLikeAnimation.isVisible = false
                binding.superLikeFireworks.stopAnimation()
                playSound(R.raw.super_like_sound)
                vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        )
    }

    private fun showDislikeAnimation(profile: Profile) {
        binding.dislikeAnimation.isVisible = true
        binding.dislikeConfetti.startAnimation()
        binding.dislikeAnimation.startAnimation(
            R.anim.slide_in_left,
            onEnd = {
                binding.dislikeAnimation.isVisible = false
                binding.dislikeConfetti.stopAnimation()
                playSound(R.raw.dislike_sound)
            }
        )
    }

    private fun showBoostAnimation(profile: Profile) {
        binding.boostAnimation.isVisible = true
        binding.boostAnimation.startAnimation(
            R.anim.scale_up,
            onEnd = { 
                binding.boostAnimation.isVisible = false
                playSound(R.raw.boost_sound)
                showBoostOverlay(profile)
            }
        )
    }

    private fun showHighlightAnimation(profile: Profile) {
        binding.highlightAnimation.isVisible = true
        binding.highlightAnimation.startAnimation(
            R.anim.shine_effect,
            onEnd = { 
                binding.highlightAnimation.isVisible = false
                playSound(R.raw.highlight_sound)
                showHighlightOverlay(profile)
            }
        )
    }

    private fun showPhotoVerificationAnimation(profile: Profile) {
        binding.verificationAnimation.isVisible = true
        binding.verificationAnimation.startAnimation(
            R.anim.glow_effect,
            onEnd = { 
                binding.verificationAnimation.isVisible = false
                playSound(R.raw.verification_sound)
                showVerificationOverlay(profile)
            }
        )
    }

    private fun showFeatureLimitDialog(feature: PremiumFeature) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Feature Limit Reached")
            .setMessage("You've reached your ${feature.title} limit.\n\nDaily Limit: ${feature.dailyLimit}\nMonthly Limit: ${feature.monthlyLimit}")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    private fun showProfileBoostedDialog(profile: Profile) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Profile Boosted")
            .setMessage("Your profile has been boosted and will be shown to more people!")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    private fun showProfileHighlightedDialog(profile: Profile) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Profile Highlighted")
            .setMessage("Your profile has been highlighted and will stand out more!")
            .setPositiveButton("OK") { _, _ -> }
            .show()
    }

    private fun showBoostOverlay(profile: Profile) {
        binding.boostOverlay.isVisible = true
        binding.boostOverlay.startAnimation(
            R.anim.fade_in,
            onEnd = { 
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.boostOverlay.isVisible = false
                }, 3000)
            }
        )
    }

    private fun showHighlightOverlay(profile: Profile) {
        binding.highlightOverlay.isVisible = true
        binding.highlightOverlay.startAnimation(
            R.anim.fade_in,
            onEnd = { 
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.highlightOverlay.isVisible = false
                }, 3000)
            }
        )
    }

    private fun showVerificationOverlay(profile: Profile) {
        binding.verificationOverlay.isVisible = true
        binding.verificationOverlay.startAnimation(
            R.anim.fade_in,
            onEnd = { 
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.verificationOverlay.isVisible = false
                }, 3000)
            }
        )
    }

    private fun showSuperLikeAnimation(profile: Profile) {
        binding.superLikeAnimation.isVisible = true
        binding.superLikeFireworks.startAnimation()
        binding.superLikeAnimation.startAnimation(
            R.anim.slide_in_up,
            onEnd = {
                binding.superLikeAnimation.isVisible = false
                binding.superLikeFireworks.stopAnimation()
            }
        )
    }

    private fun showDislikeAnimation(profile: Profile) {
        binding.dislikeAnimation.isVisible = true
        binding.dislikeConfetti.startAnimation()
        binding.dislikeAnimation.startAnimation(
            R.anim.slide_in_left,
            onEnd = {
                binding.dislikeAnimation.isVisible = false
                binding.dislikeConfetti.stopAnimation()
            }
        )
    }

    private fun showMatchAnimation(match: Match) {
        binding.matchAnimation.isVisible = true
        binding.matchAnimation.startAnimation(
            R.anim.fade_in,
            onEnd = { binding.matchAnimation.isVisible = false }
        )
    }

    private fun showRewindAnimation(profile: Profile) {
        binding.rewindAnimation.isVisible = true
        binding.rewindAnimation.startAnimation(
            R.anim.rotate,
            onEnd = { binding.rewindAnimation.isVisible = false }
        )
    }

    private fun setupPremiumFeatures() {
        viewModel.uiState.collectLatest { state ->
            binding.apply {
                premiumFeaturesLayout.isVisible = state.isPremium
                boostButton.isVisible = state.canBoost
                superLikeButton.isVisible = state.canSuperLike
                rewindButton.isVisible = state.canRewind
                highlightButton.isVisible = state.canHighlight
                verifyPhotoButton.isVisible = state.canVerifyPhoto

                // Update feature usage indicators
                featureUsageIndicator.setUsage(
                    state.featureUsage,
                    state.dailyLimits,
                    state.monthlyLimits
                )

                // Update premium features chip group
                premiumFeaturesChipGroup.setChips(
                    PremiumFeature.values().map { feature ->
                        PremiumFeatureChip(
                            id = feature.id,
                            title = feature.title,
                            description = feature.description,
                            icon = feature.icon,
                            usage = state.featureUsage[feature] ?: 0,
                            dailyLimit = feature.dailyLimit,
                            monthlyLimit = feature.monthlyLimit
                        )
                    }
                )

                // Update feature status indicators
                boostStatusIndicator.updateStatus(state.canBoost)
                superLikeStatusIndicator.updateStatus(state.canSuperLike)
                highlightStatusIndicator.updateStatus(state.canHighlight)
                verifyPhotoStatusIndicator.updateStatus(state.canVerifyPhoto)
            }
        }
    }

    private fun View.startAnimation(
        @AnimRes animRes: Int,
        onEnd: () -> Unit = {}
    ) {
        val animation = animationInflater.inflate(animRes)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) = onEnd()
            override fun onAnimationRepeat(animation: Animation) {}
        })
        startAnimation(animation)
    }
}
