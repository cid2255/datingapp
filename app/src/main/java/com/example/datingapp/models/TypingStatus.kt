package com.example.datingapp.models

enum class TypingStatus(val message: String) {
    TYPING("Typing...") {
        override fun getAnimation(): String = "typing"
    },
    VOICE_RECORDING("Recording voice message...") {
        override fun getAnimation(): String = "recording"
    },
    VIDEO_RECORDING("Recording video message...") {
        override fun getAnimation(): String = "video"
    },
    LOCATION_SHARING("Sharing location...") {
        override fun getAnimation(): String = "location"
    },
    FILE_UPLOADING("Uploading file...") {
        override fun getAnimation(): String = "uploading"
    },
    CAMERA_SHOOTING("Taking photo...") {
        override fun getAnimation(): String = "camera"
    },
    GALLERY_PICKING("Selecting from gallery...") {
        override fun getAnimation(): String = "gallery"
    },
    TYPING_DONE("Done typing") {
        override fun getAnimation(): String = ""
    };

    abstract fun getAnimation(): String
}
