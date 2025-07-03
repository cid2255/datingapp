package com.example.datingapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datingapp.models.Match
import com.example.datingapp.models.MatchStatus
import com.example.datingapp.repositories.MatchRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchesViewModel @Inject constructor(
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _filters = MutableStateFlow(FilterOptions())
    val filters: StateFlow<FilterOptions> = _filters

    private var matchesListener: EventListener<List<DocumentSnapshot>>? = null

    init {
        loadMatches()
    }

    fun loadMatches() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                val matches = matchRepository.getMatches().collect { matches ->
                    _matches.value = applyFilters(matches)
                }
                
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setFilter(filter: FilterOptions) {
        _filters.value = filter
        applyCurrentFilters()
    }

    private fun applyCurrentFilters() {
        val currentMatches = _matches.value
        _matches.value = applyFilters(currentMatches)
    }

    private fun applyFilters(matches: List<Match>): List<Match> {
        return matches.filter { match ->
            val filter = _filters.value
            
            // Apply status filter
            if (filter.status.isNotEmpty() && !filter.status.contains(match.status)) {
                return@filter false
            }
            
            // Apply premium filter
            if (filter.showPremiumOnly && match.premiumFeatures.isEmpty()) {
                return@filter false
            }
            
            // Apply unread filter
            if (filter.showUnreadOnly && match.unreadCount == 0) {
                return@filter false
            }
            
            // Apply distance filter
            if (filter.showNearbyOnly && match.distance == null) {
                return@filter false
            }
            if (filter.showNearbyOnly && match.distance != null && match.distance > filter.maxDistance) {
                return@filter false
            }
            
            // Apply age filter
            if (filter.minAge > 0 && match.user2Age != null && match.user2Age < filter.minAge) {
                return@filter false
            }
            if (filter.maxAge < 100 && match.user2Age != null && match.user2Age > filter.maxAge) {
                return@filter false
            }
            
            true
        }
    }

    fun refreshMatches() {
        loadMatches()
    }

    fun clearFilters() {
        _filters.value = FilterOptions()
        applyCurrentFilters()
    }

    override fun onCleared() {
        super.onCleared()
        matchesListener?.let { matchRepository.removeMatchesListener(it) }
    }

    data class FilterOptions(
        val status: List<MatchStatus> = listOf(MatchStatus.MATCHED),
        val showPremiumOnly: Boolean = false,
        val showUnreadOnly: Boolean = false,
        val showNearbyOnly: Boolean = false,
        val minAge: Int = 18,
        val maxAge: Int = 100,
        val maxDistance: Double = 50.0
    )
}
