package com.example.datingapp.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class FireworksView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val particles = mutableListOf<Particle>()
    private val maxParticles = 100
    private var isAnimating = false

    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var color: Int,
        var alpha: Int = 255,
        var size: Float
    )

    fun startAnimation() {
        particles.clear()
        isAnimating = true
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isAnimating) {
            // Create new particles
            if (particles.size < maxParticles) {
                val centerX = width / 2f
                val centerY = height / 2f
                val radius = Math.min(width, height) / 3f

                val angle = Random.nextDouble(0, 2 * Math.PI).toFloat()
                val speed = Random.nextFloat() * 10 + 5

                particles.add(
                    Particle(
                        x = centerX,
                        y = centerY,
                        vx = Math.cos(angle) * speed,
                        vy = Math.sin(angle) * speed,
                        color = Color.argb(255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)),
                        size = Random.nextFloat() * 10 + 5
                    )
                )
            }

            // Update and draw particles
            particles.forEach { particle ->
                particle.x += particle.vx
                particle.y += particle.vy
                particle.alpha = (particle.alpha * 0.95).toInt()
                particle.size *= 0.95f

                paint.color = particle.color
                paint.alpha = particle.alpha
                paint.strokeWidth = particle.size

                canvas.drawPoint(particle.x, particle.y, paint)
            }

            // Remove particles that have faded out
            particles.removeAll { it.alpha < 10 }

            // Stop animation when all particles have faded
            if (particles.isEmpty()) {
                isAnimating = false
            } else {
                invalidate()
            }
        }
    }
}
