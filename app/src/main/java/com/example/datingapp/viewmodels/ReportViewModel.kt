package com.example.datingapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datingapp.models.Report
import com.example.datingapp.services.BlockService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportViewModel : ViewModel() {
    private val _reportStatus = MutableStateFlow<ReportStatus>(ReportStatus.Idle)
    val reportStatus: StateFlow<ReportStatus> = _reportStatus
    private val blockService = BlockService.getInstance()
    private val auth = FirebaseAuth.getInstance()

    sealed class ReportStatus {
        object Idle : ReportStatus()
        object Loading : ReportStatus()
        data class Success(val message: String) : ReportStatus()
        data class Error(val message: String) : ReportStatus()
    }

    fun submitReport(
        reportedUserId: String,
        reason: String,
        description: String
    ) {
        _reportStatus.value = ReportStatus.Loading
        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
                val report = Report(
                    reporterId = currentUserId,
                    reportedUserId = reportedUserId,
                    reason = reason,
                    description = description,
                    timestamp = System.currentTimeMillis(),
                    status = "pending"
                )
                blockService.reportUser(report)
                _reportStatus.value = ReportStatus.Success("Report submitted successfully. Our team will review it.")
            } catch (e: Exception) {
                _reportStatus.value = ReportStatus.Error("Failed to submit report: ${e.message}")
            }
        }
    }
}
