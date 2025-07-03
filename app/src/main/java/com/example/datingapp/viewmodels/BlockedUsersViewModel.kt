package com.example.datingapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datingapp.models.Block
import com.example.datingapp.services.BlockService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BlockedUsersViewModel : ViewModel() {
    private val _blockedUsers = MutableStateFlow<List<Block>>(emptyList())
    val blockedUsers: StateFlow<List<Block>> = _blockedUsers
    private val blockService = BlockService.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadBlockedUsers()
    }

    private fun loadBlockedUsers() {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val blocks = blockService.getBlocks(currentUserId)
                _blockedUsers.value = blocks
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unblockUser(blockedUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                blockService.unblockUser(currentUserId, blockedUserId)
                loadBlockedUsers()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearAllBlocks() {
        val currentUserId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                blockService.clearAllBlocks(currentUserId)
                loadBlockedUsers()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
