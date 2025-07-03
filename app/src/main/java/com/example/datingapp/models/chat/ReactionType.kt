package com.example.datingapp.models.chat

enum class ReactionType(
    val emoji: String,
    val color: Int
) {
    LIKE("👍", 0xFF4CAF50.toInt()),
    LOVE("❤️", 0xFFE91E63.toInt()),
    LAUGH("😂", 0xFFFFC107.toInt()),
    SURPRISE("😲", 0xFFFF9800.toInt()),
    SAD("😢", 0xFF2196F3.toInt()),
    ANGRY("😠", 0xFFF44336.toInt()),
    WINK("😉", 0xFF9C27B0.toInt()),
    COOL("😎", 0xFF00BCD4.toInt()),
    THINK("🤔", 0xFF795548.toInt()),
    CONFUSED("😕", 0xFF607D8B.toInt()),
    WAVE("👋", 0xFFE040FB.toInt()),
    CLAP("👏", 0xFFFF4081.toInt()),
    PARTY("🎉", 0xFFFFEB3B.toInt()),
    FIRE("🔥", 0xFFFF5722.toInt()),
    EYES("👀", 0xFF00BCD4.toInt()),
    HEART_EYES("😍", 0xFFE91E63.toInt()),
    THUMBS_UP("👍", 0xFF4CAF50.toInt()),
    THUMBS_DOWN("👎", 0xFFF44336.toInt()),
    OK_HAND("👌", 0xFF00BCD4.toInt()),
    PRAY("🙏", 0xFF9C27B0.toInt()),
    FLEX("💪", 0xFF4CAF50.toInt()),
    DANCE("💃", 0xFFFF4081.toInt()),
    SLEEP("😴", 0xFF607D8B.toInt()),
    SWEAT("💦", 0xFF9C27B0.toInt()),
    CRY("😭", 0xFF2196F3.toInt()),
    ZZZ("💤", 0xFF795548.toInt()),
    FIREWORKS("🎆", 0xFFFFEB3B.toInt()),
    BALLOON("🎈", 0xFFFF9800.toInt()),
    ROCKET("🚀", 0xFF00BCD4.toInt()),
    TADA("🎉", 0xFFFF4081.toInt()),
    CONFETTI("🎊", 0xFFFFEB3B.toInt()),
    TROPHY("🏆", 0xFF4CAF50.toInt()),
    MEDAL("🏅", 0xFFE91E63.toInt()),
    FLAG("🏁", 0xFFFF9800.toInt());

    companion object {
        fun fromEmoji(emoji: String): ReactionType? {
            return values().firstOrNull { it.emoji == emoji }
        }
    }
}
