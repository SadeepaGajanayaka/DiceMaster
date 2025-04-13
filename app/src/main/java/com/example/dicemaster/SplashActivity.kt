package com.example.dicemaster

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd

class SplashActivity : AppCompatActivity() {

    private lateinit var ivDice: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var containerSplash: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        ivDice = findViewById(R.id.ivDice)
        tvTitle = findViewById(R.id.tvTitle)
        containerSplash = findViewById(R.id.containerSplash)

        // Initial setup
        ivDice.visibility = View.INVISIBLE
        tvTitle.visibility = View.INVISIBLE

        // Start animation sequence
        startSplashAnimation()
    }

    private fun startSplashAnimation() {
        // Step 1: Initial dice zoom up animation
        Handler(Looper.getMainLooper()).postDelayed({
            ivDice.visibility = View.VISIBLE
            ivDice.translationX = 0f // Reset to center initially

            // Scale from 0 to 1 with rotation
            val scaleXAnimator = ObjectAnimator.ofFloat(ivDice, View.SCALE_X, 0f, 1f)
            val scaleYAnimator = ObjectAnimator.ofFloat(ivDice, View.SCALE_Y, 0f, 1f)
            val rotateAnimator = ObjectAnimator.ofFloat(ivDice, View.ROTATION, -180f, 0f)

            val initialSet = AnimatorSet().apply {
                playTogether(scaleXAnimator, scaleYAnimator, rotateAnimator)
                duration = 800
                interpolator = OvershootInterpolator(1.2f)
            }

            initialSet.start()

            // Step 2: Move dice to left
            initialSet.doOnEnd {
                Handler(Looper.getMainLooper()).postDelayed({
                    // Move dice to the left
                    val moveLeft = ObjectAnimator.ofFloat(ivDice, View.TRANSLATION_X, 0f, -dpToPx(80f))
                    moveLeft.duration = 300
                    moveLeft.interpolator = DecelerateInterpolator()

                    moveLeft.start()

                    // Step 3: Show and animate text typing effect
                    moveLeft.doOnEnd {
                        tvTitle.visibility = View.VISIBLE
                        tvTitle.alpha = 0f

                        // Move text slightly to the right of center
                        tvTitle.translationX = dpToPx(35f)

                        val fullText = "DiceMaster"
                        animateTextTyping(fullText)

                        // Step 4: Fade out after a delay and transition to main activity
                        Handler(Looper.getMainLooper()).postDelayed({
                            fadeOutAndTransition()
                        }, 1500)
                    }
                }, 200)
            }
        }, 500)
    }

    private fun fadeOutAndTransition() {
        // Fade out text
        val fadeOutText = ObjectAnimator.ofFloat(tvTitle, View.ALPHA, 1f, 0f)
        fadeOutText.duration = 500

        // Prepare dice animations
        val moveDiceToCenter = ObjectAnimator.ofFloat(ivDice, View.TRANSLATION_X, -dpToPx(100f), 0f)
        val rotateDice = ObjectAnimator.ofFloat(ivDice, View.ROTATION, 0f, 360f)
        val fadeDiceOut = ObjectAnimator.ofFloat(ivDice, View.ALPHA, 1f, 0f)

        val diceAnimSet = AnimatorSet().apply {
            playTogether(moveDiceToCenter, rotateDice, fadeDiceOut)
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }

        // First fade out text, then animate dice
        val finalAnimSet = AnimatorSet()
        finalAnimSet.playSequentially(fadeOutText, diceAnimSet)
        finalAnimSet.start()

        // After all animations complete, transition to main activity
        finalAnimSet.doOnEnd {
            startMainActivity()
        }
    }

    private fun animateTextTyping(text: String) {
        var currentText = ""
        val valueAnimator = ValueAnimator.ofInt(0, text.length)
        valueAnimator.duration = 800 // Duration of typing animation
        valueAnimator.addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Int
            if (animatedValue > currentText.length) {
                currentText = text.substring(0, animatedValue)
                tvTitle.text = currentText
            }
        }

        // Fade in text container
        val fadeInAnimator = ObjectAnimator.ofFloat(tvTitle, View.ALPHA, 0f, 1f)
        fadeInAnimator.duration = 200

        val animSet = AnimatorSet()
        animSet.playSequentially(fadeInAnimator, valueAnimator)
        animSet.start()
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}