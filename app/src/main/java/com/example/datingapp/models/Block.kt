package com.example.datingapp.models

import com.google.firebase.Timestamp

data class Block(
    val uid: String = "",
    val blockedId: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
