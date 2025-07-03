package com.example.datingapp.models

enum class ReportReason(val description: String) {
    SPAM("Spam or unwanted messages"),
    HARASSMENT("Harassment or bullying"),
    FAKE_PROFILE("Fake profile"),
    INAPPROPRIATE_CONTENT("Inappropriate content"),
    OTHER("Other")
}
