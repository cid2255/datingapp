package com.example.datingapp.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.datingapp.R
import com.example.datingapp.models.User
import com.example.datingapp.models.VerificationStatus
import com.example.datingapp.models.VerificationStyle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile.*
import java.util.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var currentUserId: String
    private lateinit var blockService: BlockService
    private var selectedImageUri: Uri? = null
    private var selectedVerificationStatus: VerificationStatus = VerificationStatus.NOT_VERIFIED
    private var selectedVerificationStyle: VerificationStyle = VerificationStyle.NONE

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            handleImageUpload()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            handleImageUpload()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        
        // Initialize block service
        blockService = BlockService.getInstance(db)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        setupClickListeners()
        setupProfileActions()
        loadUserProfile()
    }

    private fun setupClickListeners() {
        cameraButton.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            }
        }

        galleryButton.setOnClickListener {
            if (checkGalleryPermission()) {
                openGallery()
            }
        }

        verificationBadgeContainer.setOnClickListener {
            showVerificationOptions()
        }
    }

    private fun checkCameraPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                100
            )
            return false
        }
        return true
    }

    private fun checkGalleryPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                101
            )
            return false
        }
        return true
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryLauncher.launch(intent)
    }

    private fun handleImageUpload() {
        selectedImageUri?.let { uri ->
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val imageRef = storage.reference.child("profile_images/$currentUserId/$fileName")

            imageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { downloadUri ->
                        updateProfileImage(downloadUri.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error uploading image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateProfileImage(imageUrl: String) {
        val userRef = db.collection("users").document(currentUserId)
        userRef.update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile image updated successfully", Toast.LENGTH_SHORT).show()
                loadUserProfile()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error updating profile: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfile() {
        val userRef = db.collection("users").document(currentUserId)
        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        setupUserProfile(it)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading profile: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupUserProfile(user: User) {
        // Set profile image
        profileImageView.loadImage(user.profileImageUrl)
        
        // Set verification badge
        selectedVerificationStatus = user.verificationStatus
        selectedVerificationStyle = user.verificationStatus.getVerificationStyle()
        updateVerificationBadge()
        
        // Set other profile info
        usernameTextView.text = user.username
        ageTextView.text = "${user.age} years"
        locationTextView.text = user.location?.city ?: ""
        aboutTextView.text = user.about
    }

    private fun updateVerificationBadge() {
        verificationBadge.setImageResource(selectedVerificationStatus.getVerificationBadge())
        verificationBadge.visibility = if (selectedVerificationStatus != VerificationStatus.NOT_VERIFIED) View.VISIBLE else View.GONE
        verificationStatusText.text = selectedVerificationStatus.getVerificationText()
        verificationStatusText.setTextColor(ContextCompat.getColor(this, selectedVerificationStatus.getVerificationColor()))
        verificationStatusText.visibility = if (selectedVerificationStatus != VerificationStatus.NOT_VERIFIED) View.VISIBLE else View.GONE
    }

    private fun showVerificationOptions() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_verification_options)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.optionsRecyclerView)
        val closeButton = dialog.findViewById<Button>(R.id.closeButton)

        // Initialize verification options
        val options = listOf(
            VerificationOption(
                "email",
                "Verify Email",
                "Verify your email address",
                R.drawable.ic_email,
                VerificationType.EMAIL,
                VerificationLevel.BASIC,
                listOf("email_confirmation")
            ),
            VerificationOption(
                "phone",
                "Verify Phone",
                "Verify your phone number",
                R.drawable.ic_phone,
                VerificationType.PHONE,
                VerificationLevel.BASIC,
                listOf("phone_confirmation")
            ),
            VerificationOption(
                "document",
                "Verify Document",
                "Upload ID document",
                R.drawable.ic_document,
                VerificationType.DOCUMENT,
                VerificationLevel.PREMIUM,
                listOf("id_document", "selfie_with_id")
            ),
            VerificationOption(
                "face",
                "Face Verification",
                "Verify your face",
                R.drawable.ic_face,
                VerificationType.FACE,
                VerificationLevel.PREMIUM,
                listOf("face_scan")
            ),
            VerificationOption(
                "premium",
                "Premium Verification",
                "Complete all verification steps",
                R.drawable.ic_premium,
                VerificationType.PREMIUM,
                VerificationLevel.PREMIUM,
                listOf("all_documents")
            ),
            VerificationOption(
                "enterprise",
                "Enterprise Verification",
                "Enterprise level verification",
                R.drawable.ic_enterprise,
                VerificationType.ENTERPRISE,
                VerificationLevel.ENTERPRISE,
                listOf("enterprise_documents")
            )
        )

        // Set up adapter
        val adapter = VerificationOptionAdapter(options) { option ->
            handleVerificationOption(option)
            dialog.dismiss()
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Close button
        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun handleVerificationOption(option: VerificationOption) {
        when (option.verificationType) {
            VerificationType.EMAIL -> verifyEmail()
            VerificationType.PHONE -> verifyPhone()
            VerificationType.DOCUMENT -> verifyDocument()
            VerificationType.FACE -> verifyFace()
            VerificationType.PREMIUM -> verifyPremium()
            VerificationType.ENTERPRISE -> verifyEnterprise()
        }
    }

    private fun verifyEmail() {
        val flow = VerificationFlow(this, selectedVerificationOption)
        flow.start()
    }

    private fun verifyPhone() {
        val flow = VerificationFlow(this, selectedVerificationOption)
        flow.start()
    }

    private fun verifyDocument() {
        val flow = VerificationFlow(this, selectedVerificationOption)
        flow.start()
    }

    private fun verifyFace() {
        val flow = VerificationFlow(this, selectedVerificationOption)
        flow.start()
    }

    private fun verifyPremium() {
        val flow = VerificationFlow(this, selectedVerificationOption)
        flow.start()
    }

    private fun verifyEnterprise() {
        val flow = VerificationFlow(this, selectedVerificationOption)
        flow.start()
    }

    private var selectedVerificationOption: VerificationOption? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == VerificationFlow.DOCUMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedVerificationOption?.requiredDocuments?.forEach { documentType ->
                    DocumentUploadHandler(this).handleDocumentUpload(uri, documentType)
                }
            }
        }
    }

    fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun updateVerificationBadge(status: VerificationStatus) {
        selectedVerificationStatus = status
        updateVerificationBadge()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            101 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
