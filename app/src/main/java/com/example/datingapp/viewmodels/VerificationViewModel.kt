package com.example.datingapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.datingapp.models.VerificationStatus
import com.example.datingapp.models.VerificationType
import com.example.datingapp.models.VerificationLevel
import com.example.datingapp.repository.VerificationRepository
import kotlinx.coroutines.launch

class VerificationViewModel(
    private val repository: VerificationRepository
) : ViewModel() {

    private val _verificationStatus = MutableLiveData<VerificationStatus>()
    val verificationStatus: LiveData<VerificationStatus> = _verificationStatus

    private val _verificationProgress = MutableLiveData<Map<String, Any>>()
    val verificationProgress: LiveData<Map<String, Any>> = _verificationProgress

    private val _verificationHistory = MutableLiveData<List<Map<String, Any>>>()
    val verificationHistory: LiveData<List<Map<String, Any>>> = _verificationHistory

    private val _verificationDocuments = MutableLiveData<List<Map<String, Any>>>()
    val verificationDocuments: LiveData<List<Map<String, Any>>> = _verificationDocuments

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun updateVerificationStatus(status: VerificationStatus) {
        viewModelScope.launch {
            try {
                _loading.value = true
                repository.updateVerificationStatus(status)
                _verificationStatus.value = status
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun saveVerificationData(
        type: VerificationType,
        level: VerificationLevel,
        data: Map<String, Any>
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                repository.saveVerificationData(type, level, data)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun uploadVerificationDocument(
        type: VerificationType,
        fileUri: Uri,
        fileName: String
    ) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val url = repository.uploadVerificationDocument(type, fileUri, fileName)
                // Update UI with uploaded document URL
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun getVerificationStatus() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _verificationStatus.value = repository.getVerificationStatus()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun getVerificationHistory() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _verificationHistory.value = repository.getVerificationHistory()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun getVerificationDocuments() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _verificationDocuments.value = repository.getVerificationDocuments()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun verifyDocument(documentId: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                repository.verifyDocument(documentId)
                // Update UI with verification status
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun getVerificationProgress() {
        viewModelScope.launch {
            try {
                _loading.value = true
                _verificationProgress.value = repository.getVerificationProgress()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}
