package com.example.datingapp.models.enums

enum class ReportCategory(
    val id: String,
    val title: String,
    val description: String,
    val evidenceRequired: Boolean
) {
    HARASSMENT(
        "harassment",
        "Harassment",
        "The user is sending harassing messages or engaging in abusive behavior",
        true
    ),
    FAKE_PROFILE(
        "fake_profile",
        "Fake Profile",
        "The user's profile appears to be fake or fraudulent",
        false
    ),
    SPAM(
        "spam",
        "Spam",
        "The user is sending spam messages or trying to promote something",
        true
    ),
    INAPPROPRIATE(
        "inappropriate",
        "Inappropriate Content",
        "The user is sharing inappropriate or offensive content",
        true
    ),
    SCAM(
        "scam",
        "Scam",
        "The user is attempting to scam or defraud other users",
        true
    ),
    PRIVACY(
        "privacy",
        "Privacy Violation",
        "The user is violating my privacy or sharing personal information",
        true
    ),
    AGE_MISMATCH(
        "age_mismatch",
        "Age Mismatch",
        "The user's age does not match their profile",
        false
    ),
    LOCATION_MISMATCH(
        "location_mismatch",
        "Location Mismatch",
        "The user's location does not match their profile",
        false
    ),
    OTHER(
        "other",
        "Other",
        "Other reason not listed above",
        true
    );

    companion object {
        fun fromId(id: String): ReportCategory? {
            return values().firstOrNull { it.id == id }
        }
    }
}
