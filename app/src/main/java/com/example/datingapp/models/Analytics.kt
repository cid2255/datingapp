package com.example.datingapp.models

import com.google.firebase.Timestamp

data class Analytics(
    @DocumentId
    val id: String = "",
    val type: AnalyticsType = AnalyticsType.USER,
    val userId: String = "",
    val action: String = "",
    val category: String = "",
    val label: String? = null,
    val value: Int = 0,
    val timestamp: Timestamp = Timestamp.now(),
    val metadata: Map<String, Any> = mapOf()
) {
    
    enum class AnalyticsType {
        USER,
        APP,
        FEATURE,
        PERFORMANCE,
        ERROR
    }
}
