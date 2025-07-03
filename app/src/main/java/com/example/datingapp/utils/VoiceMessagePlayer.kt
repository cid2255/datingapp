package com.example.datingapp.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.widget.ImageButton
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL

class VoiceMessagePlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingMessage: String? = null
    private var currentJob: Job? = null
    private var progressUpdateListener: ((Float) -> Unit)? = null
    
    fun play(message: Message, playButton: ImageButton) {
        stop()
        
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(message.mediaUrl)
                prepareAsync()
                setOnPreparedListener {
                    currentPlayingMessage = message.id
                    playButton.setImageResource(R.drawable.ic_pause)
                    start()
                    
                    // Start progress update coroutine
                    currentJob = (context as LifecycleOwner).lifecycleScope.launch {
                        while (isPlaying) {
                            val progress = currentPosition.toFloat() / duration.toFloat()
                            progressUpdateListener?.invoke(progress)
                            delay(100)
                        }
                    }
                }
                setOnCompletionListener {
                    stop()
                    playButton.setImageResource(R.drawable.ic_play)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stop()
        }
    }
    
    fun stop() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
        currentPlayingMessage = null
        currentJob?.cancel()
        currentJob = null
    }
    
    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
    
    fun setProgressUpdateListener(listener: (Float) -> Unit) {
        progressUpdateListener = listener
    }
    
    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }
    
    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }
    
    fun getDuration(): Int {
        return mediaPlayer?.duration ?: 0
    }
}
