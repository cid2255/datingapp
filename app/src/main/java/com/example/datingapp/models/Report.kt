package com.example.datingapp.models

import com.google.firebase.Timestamp

data class Report(
    @DocumentId
    val id: String = "",
    val reportingUserId: String = "",
    val reportedUserId: String = "",
    val reason: ReportReason = ReportReason.OTHER,
    val description: String = "",
    val status: ReportStatus = ReportStatus.PENDING,
    val evidence: List<String> = listOf(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    
    enum class ReportReason {
        FAKE_PROFILE,
        INAPPROPRIATE_CONTENT,
        HARASSMENT,
        FRAUD,
        SPAM,
        OTHER
    }
    
    enum class ReportStatus {
        PENDING,
        APPROVED,
        REJECTED,
        ACTION_TAKEN,
        CLOSED
    }
}
