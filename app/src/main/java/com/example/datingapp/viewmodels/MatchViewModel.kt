package com.example.datingapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datingapp.models.Profile
import com.example.datingapp.models.enums.PremiumFeature
import com.example.datingapp.models.enums.SwipeType
import com.example.datingapp.repositories.MatchRepository
import com.example.datingapp.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _matchEvents = MutableStateFlow<MatchEvent?>(null)
    val matchEvents: StateFlow<MatchEvent?> = _matchEvents.asStateFlow()

    private var profiles: List<Profile> = emptyList()
    private var currentProfileIndex = 0
    private var undoStack: MutableList<Profile> = mutableListOf()
    private var superLikeCount = 0
    private var boostCount = 0
    private var highlightCount = 0
    private var photoVerificationCount = 0
    private val featureUsage = mutableMapOf<PremiumFeature, Int>()
    private val dailyUsage = mutableMapOf<PremiumFeature, Int>()
    private val monthlyUsage = mutableMapOf<PremiumFeature, Int>()

    init {
        loadProfiles()
        loadUserState()
        initializeFeatureUsage()
    }

    private fun initializeFeatureUsage() {
        PremiumFeature.values().forEach { feature ->
            featureUsage[feature] = 0
            dailyUsage[feature] = 0
            monthlyUsage[feature] = 0
        }
    }

    init {
        loadProfiles()
        loadUserState()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            profiles = matchRepository.getPotentialMatches()
            currentProfileIndex = 0
            updateUiState()
        }
    }

    private fun loadUserState() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            _uiState.value = _uiState.value.copy(
                isPremium = user.isPremium,
                canSuperLike = user.isPremium && superLikeCount < 5,
                canRewind = user.isPremium && undoStack.isNotEmpty()
            )
        }
    }

    fun likeProfile(profile: Profile) {
        viewModelScope.launch {
            matchRepository.likeProfile(profile)
            _matchEvents.value = MatchEvent.Like(profile)
            trackFeatureUsage(PremiumFeature.UNLIMITED_LIKES)
            moveToNextProfile()
        }
    }

    fun superLikeProfile(profile: Profile) {
        viewModelScope.launch {
            if (canUseFeature(PremiumFeature.SUPER_LIKES)) {
                matchRepository.superLikeProfile(profile)
                _matchEvents.value = MatchEvent.SuperLike(profile)
                trackFeatureUsage(PremiumFeature.SUPER_LIKES)
                superLikeCount++
                updateUiState()
                moveToNextProfile()
            }
        }
    }

    fun boostProfile(profile: Profile) {
        viewModelScope.launch {
            if (canUseFeature(PremiumFeature.BOOST)) {
                matchRepository.boostProfile(profile)
                _matchEvents.value = MatchEvent.Boost(profile)
                trackFeatureUsage(PremiumFeature.BOOST)
                boostCount++
                updateUiState()
            }
        }
    }

    fun highlightProfile(profile: Profile) {
        viewModelScope.launch {
            if (canUseFeature(PremiumFeature.PROFILE_HIGHLIGHT)) {
                matchRepository.highlightProfile(profile)
                _matchEvents.value = MatchEvent.Highlight(profile)
                trackFeatureUsage(PremiumFeature.PROFILE_HIGHLIGHT)
                highlightCount++
                updateUiState()
            }
        }
    }

    fun verifyPhoto(profile: Profile) {
        viewModelScope.launch {
            if (canUseFeature(PremiumFeature.PHOTO_VERIFICATION)) {
                matchRepository.verifyPhoto(profile)
                _matchEvents.value = MatchEvent.PhotoVerified(profile)
                trackFeatureUsage(PremiumFeature.PHOTO_VERIFICATION)
                photoVerificationCount++
                updateUiState()
            }
        }
    }

    private fun canUseFeature(feature: PremiumFeature): Boolean {
        if (!_uiState.value.isPremium) return false

        return when {
            feature.dailyLimit != null && dailyUsage[feature]!! >= feature.dailyLimit -> false
            feature.monthlyLimit != null && monthlyUsage[feature]!! >= feature.monthlyLimit -> false
            else -> true
        }
    }

    private fun trackFeatureUsage(feature: PremiumFeature) {
        featureUsage[feature] = (featureUsage[feature] ?: 0) + 1
        dailyUsage[feature] = (dailyUsage[feature] ?: 0) + 1
        monthlyUsage[feature] = (monthlyUsage[feature] ?: 0) + 1
    }

    fun dislikeProfile(profile: Profile) {
        viewModelScope.launch {
            matchRepository.dislikeProfile(profile)
            _matchEvents.value = MatchEvent.Dislike(profile)
            moveToNextProfile()
        }
    }

    fun rewind() {
        viewModelScope.launch {
            if (_uiState.value.canRewind) {
                val lastProfile = undoStack.removeLast()
                currentProfileIndex--
                _matchEvents.value = MatchEvent.Rewind(lastProfile)
                updateUiState()
            }
        }
    }

    private fun moveToNextProfile() {
        if (currentProfileIndex < profiles.size - 1) {
            currentProfileIndex++
            undoStack.add(profiles[currentProfileIndex - 1])
            updateUiState()
        } else {
            _uiState.value = _uiState.value.copy(hasProfiles = false)
        }
    }

    private fun updateUiState() {
        _uiState.value = UiState(
            hasProfiles = currentProfileIndex < profiles.size,
            profileCount = profiles.size - currentProfileIndex,
            currentProfile = profiles.getOrNull(currentProfileIndex),
            isPremium = _uiState.value.isPremium,
            canSuperLike = _uiState.value.canSuperLike,
            canRewind = _uiState.value.canRewind
        )
    }

    data class UiState(
        val hasProfiles: Boolean = true,
        val profileCount: Int = 0,
        val currentProfile: Profile? = null,
        val isPremium: Boolean = false,
        val canSuperLike: Boolean = false,
        val canRewind: Boolean = false,
        val canBoost: Boolean = false,
        val canHighlight: Boolean = false,
        val canVerifyPhoto: Boolean = false,
        val featureUsage: Map<PremiumFeature, Int> = emptyMap(),
        val dailyLimits: Map<PremiumFeature, Int> = emptyMap(),
        val monthlyLimits: Map<PremiumFeature, Int> = emptyMap()
    )

    sealed class MatchEvent {
        data class Like(val profile: Profile) : MatchEvent()
        data class SuperLike(val profile: Profile) : MatchEvent()
        data class Dislike(val profile: Profile) : MatchEvent()
        data class Match(val match: Match) : MatchEvent()
        data class Rewind(val profile: Profile) : MatchEvent()
        data class Boost(val profile: Profile) : MatchEvent()
        data class Highlight(val profile: Profile) : MatchEvent()
        data class PhotoVerified(val profile: Profile) : MatchEvent()
        data class FeatureLimitReached(val feature: PremiumFeature) : MatchEvent()
        data class ProfileBoosted(val profile: Profile) : MatchEvent()
        data class ProfileHighlighted(val profile: Profile) : MatchEvent()
    }
}
