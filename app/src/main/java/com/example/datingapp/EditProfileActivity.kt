package com.example.datingapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.item_interest.view.*

class EditProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null

    // Activity result launcher for image selection
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            selectedImageUri = data?.data
            selectedImageUri?.let { uri ->
                Glide.with(this)
                    .load(uri)
                    .circleCrop()
                    .into(profileImageView)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Set up RecyclerView
        interestsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        interestsRecyclerView.adapter = InterestAdapter(getInterests())

        // Set up profile picture change
        changePhotoTextView.setOnClickListener {
            showImagePickerOptions()
        }

        // Set up save button
        saveButton.setOnClickListener {
            saveProfile()
        }

        // Load user data
        loadUserData()
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Camera", "Gallery", "Cancel")
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(R.string.select_photo)
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
                2 -> {}
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imagePickerLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun saveProfile() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        val username = usernameEditText.text.toString()
        val age = ageEditText.text.toString().toIntOrNull()
        val location = locationEditText.text.toString()
        val about = aboutEditText.text.toString()

        if (username.isEmpty() || age == null || location.isEmpty()) {
            Toast.makeText(this, R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show()
            return
        }

        val userData = hashMapOf(
            "username" to username,
            "age" to age,
            "location" to location,
            "about" to about
        )

        userRef.update(userData as Map<String, Any>)
            .addOnSuccessListener {
                if (selectedImageUri != null) {
                    uploadProfileImage(selectedImageUri!!)
                } else {
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadProfileImage(uri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val imageRef = storage.reference.child("profile_images/$userId.jpg")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    db.collection("users").document(userId)
                        .update("profileImageUrl", downloadUrl.toString())
                        .addOnSuccessListener {
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid ?: return
        val userRef = db.collection("users").document(userId)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userData = document.data
                    userData?.let {
                        usernameEditText.setText(it["username"] as? String)
                        ageEditText.setText((it["age"] as? Int)?.toString())
                        locationEditText.setText(it["location"] as? String)
                        aboutEditText.setText(it["about"] as? String)
                        
                        val profileImageUrl = it["profileImageUrl"] as? String
                        profileImageUrl?.let { url ->
                            Glide.with(this)
                                .load(url)
                                .circleCrop()
                                .placeholder(R.drawable.ic_person)
                                .into(profileImageView)
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            }
    }

    private fun getInterests(): List<String> {
        // This should be loaded from Firestore
        return listOf(
            "Sports",
            "Music",
            "Travel",
            "Reading",
            "Cooking",
            "Movies",
            "Gaming",
            "Photography",
            "Hiking",
            "Dancing"
        )
    }

    private inner class InterestAdapter(private val interests: List<String>) :
        androidx.recyclerview.widget.RecyclerView.Adapter<InterestAdapter.InterestViewHolder>() {

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): InterestViewHolder {
            val view = layoutInflater.inflate(R.layout.item_interest, parent, false)
            return InterestViewHolder(view)
        }

        override fun onBindViewHolder(holder: InterestViewHolder, position: Int) {
            holder.bind(interests[position])
        }

        override fun getItemCount() = interests.size

        inner class InterestViewHolder(itemView: android.view.View) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            fun bind(interest: String) {
                itemView.interestChip.text = interest
                itemView.interestChip.isChecked = false // Initially unchecked
                itemView.interestChip.setOnClickListener {
                    // Handle interest selection
                }
            }
        }
    }
}
