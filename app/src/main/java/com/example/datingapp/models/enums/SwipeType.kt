package com.example.datingapp.models.enums

enum class SwipeType(
    val id: String,
    val direction: String,
    val animation: String,
    val buttonId: Int,
    val animationId: Int
) {
    LIKE(
        "like",
        "right",
        "LIKE",
        R.id.likeButton,
        R.anim.slide_in_right
    ),
    SUPER_LIKE(
        "super_like",
        "up",
        "SUPER LIKE",
        R.id.superLikeButton,
        R.anim.slide_in_up
    ),
    DISLIKE(
        "dislike",
        "left",
        "NOPE",
        R.id.dislikeButton,
        R.anim.slide_in_left
    ),
    UNDO(
        "undo",
        "none",
        "UNDO",
        R.id.undoButton,
        R.anim.fade_in
    );

    companion object {
        fun fromDirection(direction: String): SwipeType? {
            return values().firstOrNull { it.direction == direction }
        }
    }
}
