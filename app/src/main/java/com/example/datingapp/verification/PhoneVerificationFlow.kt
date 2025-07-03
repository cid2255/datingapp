package com.example.datingapp.verification

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.datingapp.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import java.util.concurrent.TimeUnit

class PhoneVerificationFlow(
    private val activity: Activity,
    private val phoneNumber: String
) : DialogFragment() {

    private lateinit var callbacks: OnVerificationStateChangedCallbacks
    private var verificationId: String? = null
    private var resendToken: ForceResendingToken? = null
    private var timer: CountDownTimer? = null
    private var isVerificationInProgress = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_phone_verification, container, false)
        setupViews(view)
        return view
    }

    private fun setupViews(view: View) {
        val phoneNumberTextView = view.findViewById<TextView>(R.id.phoneNumberTextView)
        val resendButton = view.findViewById<Button>(R.id.resendButton)
        val verifyButton = view.findViewById<Button>(R.id.verifyButton)
        val otpEditText = view.findViewById<EditText>(R.id.otpEditText)

        phoneNumberTextView.text = phoneNumber
        resendButton.setOnClickListener { resendVerificationCode() }
        verifyButton.setOnClickListener { verifyCode(otpEditText.text.toString()) }

        // Start verification
        startPhoneNumberVerification()
    }

    private fun startPhoneNumberVerification() {
        if (isVerificationInProgress) return

        isVerificationInProgress = true
        showLoading(true)

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,        // Phone number to verify
            60,                 // Timeout duration
            TimeUnit.SECONDS,   // Unit of timeout
            activity,           // Activity (for callback binding)
            callbacks
        )
    }

    private fun resendVerificationCode() {
        if (resendToken == null) {
            activity.showToast("Please verify your phone number first")
            return
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            activity,
            callbacks,
            resendToken
        )
    }

    private fun verifyCode(code: String) {
        if (verificationId == null) {
            activity.showToast("Please verify your phone number first")
            return
        }

        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    activity.showToast("Phone number verified successfully")
                    updateVerificationStatus(VerificationStatus.PHONE_VERIFIED)
                    dismiss()
                } else {
                    activity.showToast("Verification failed: ${task.exception?.message}")
                }
            }
    }

    private fun setupCallbacks() {
        callbacks = object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                isVerificationInProgress = false
                showLoading(false)
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                isVerificationInProgress = false
                showLoading(false)
                activity.showToast("Verification failed: ${e.message}")
            }

            override fun onCodeSent(
                verificationId: String,
                token: ForceResendingToken
            ) {
                isVerificationInProgress = false
                showLoading(false)
                this@PhoneVerificationFlow.verificationId = verificationId
                resendToken = token
                startTimer()
            }
        }
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update timer UI
            }

            override fun onFinish() {
                // Timer finished
            }
        }.start()
    }

    private fun showLoading(show: Boolean) {
        // Update loading UI
    }

    private fun updateVerificationStatus(status: VerificationStatus) {
        val userRef = FirebaseFirestore.getInstance().collection("users").document(currentUserId)
        userRef.update("verificationStatus", status)
            .addOnSuccessListener {
                activity.showToast("Verification status updated")
            }
            .addOnFailureListener { exception ->
                activity.showToast("Failed to update verification status: ${exception.message}")
            }
    }
}
