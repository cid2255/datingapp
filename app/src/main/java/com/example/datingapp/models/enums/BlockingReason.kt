package com.example.datingapp.models.enums

enum class BlockingReason(
    val id: String,
    val title: String,
    val description: String
) {
    HARASSMENT(
        "harassment",
        "Harassment",
        "The user is sending harassing messages or engaging in abusive behavior"
    ),
    FAKE_PROFILE(
        "fake_profile",
        "Fake Profile",
        "The user's profile appears to be fake or fraudulent"
    ),
    SPAM(
        "spam",
        "Spam",
        "The user is sending spam messages or trying to promote something"
    ),
    INAPPROPRIATE(
        "inappropriate",
        "Inappropriate Content",
        "The user is sharing inappropriate or offensive content"
    ),
    SCAM(
        "scam",
        "Scam",
        "The user is attempting to scam or defraud other users"
    ),
    PRIVACY(
        "privacy",
        "Privacy Violation",
        "The user is violating my privacy or sharing personal information"
    ),
    OTHER(
        "other",
        "Other",
        "Other reason not listed above"
    );

    companion object {
        fun fromId(id: String): BlockingReason? {
            return values().firstOrNull { it.id == id }
        }
    }
}
