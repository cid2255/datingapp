package com.example.datingapp.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.example.datingapp.databinding.ViewPremiumFeatureChipBinding
import com.example.datingapp.models.enums.PremiumFeature

class PremiumFeatureChip @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewPremiumFeatureChipBinding.inflate(
        LayoutInflater.from(context),
        this,
        true
    )

    fun setFeature(feature: PremiumFeature) {
        binding.apply {
            featureIcon.setImageResource(feature.icon)
            featureTitle.text = feature.title
            featureDescription.text = feature.description

            // Setup usage indicators
            featureUsageIndicator.apply {
                usage = featureUsage
                dailyLimit = feature.dailyLimit
                monthlyLimit = feature.monthlyLimit
                setupUsageIndicator()
            }

            // Setup click listener
            root.setOnClickListener {
                featureClickListener?.onFeatureClicked(feature)
            }
        }
    }

    private var featureClickListener: FeatureClickListener? = null

    fun setOnFeatureClickListener(listener: FeatureClickListener) {
        featureClickListener = listener
    }

    interface FeatureClickListener {
        fun onFeatureClicked(feature: PremiumFeature)
    }

    private fun setupUsageIndicator() {
        binding.usageIndicator.apply {
            if (dailyLimit != null) {
                dailyProgress.max = dailyLimit
                dailyProgress.progress = usage
                dailyText.text = "${usage}/${dailyLimit} daily"
            }

            if (monthlyLimit != null) {
                monthlyProgress.max = monthlyLimit
                monthlyProgress.progress = usage
                monthlyText.text = "${usage}/${monthlyLimit} monthly"
            }
        }
    }

    var usage: Int = 0
        set(value) {
            field = value
            setupUsageIndicator()
        }

    var dailyLimit: Int? = null
        set(value) {
            field = value
            setupUsageIndicator()
        }

    var monthlyLimit: Int? = null
        set(value) {
            field = value
            setupUsageIndicator()
        }
}
