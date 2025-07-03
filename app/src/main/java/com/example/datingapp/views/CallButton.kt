package com.example.datingapp.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.datingapp.databinding.ViewCallButtonBinding
import com.example.datingapp.models.enums.PremiumFeature

class CallButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewCallButtonBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    private var onCallClickListener: (() -> Unit)? = null
    private var onPremiumClickListener: (() -> Unit)? = null
    private var isPremium = false
    private var isLoading = false

    init {
        setupClickListeners()
    }

    fun setOnCallClickListener(listener: () -> Unit) {
        onCallClickListener = listener
    }

    fun setOnPremiumClickListener(listener: () -> Unit) {
        onPremiumClickListener = listener
    }

    fun setPremiumStatus(enabled: Boolean) {
        isPremium = enabled
        updateUI()
    }

    fun setLoading(loading: Boolean) {
        isLoading = loading
        updateUI()
    }

    private fun setupClickListeners() {
        binding.root.setOnClickListener {
            if (isLoading) return@setOnClickListener
            if (!isPremium) {
                onPremiumClickListener?.invoke()
            } else {
                onCallClickListener?.invoke()
            }
        }
    }

    private fun updateUI() {
        binding.apply {
            callProgress.isVisible = isLoading
            callText.isVisible = !isLoading
            callIcon.isVisible = !isLoading

            if (!isPremium) {
                callText.text = "Upgrade for Video Call"
                callIcon.setImageResource(R.drawable.ic_upgrade)
            } else {
                callText.text = "Video Call"
                callIcon.setImageResource(R.drawable.ic_video_call)
            }
        }
    }
}
