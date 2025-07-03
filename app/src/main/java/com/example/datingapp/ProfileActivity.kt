package com.example.datingapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.item_interest.view.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null
    private var userId: String? = null

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
                uploadProfileImage(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        userId = auth.currentUser?.uid

        // Set up RecyclerView
        interestsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        interestsRecyclerView.adapter = InterestAdapter(getInterests())

        // Set up FAB
        addPhotoButton.setOnClickListener {
            showImagePickerOptions()
        }

        // Set up edit button
        editProfileButton.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        // Set up swipe refresh
        swipeRefreshLayout.setOnRefreshListener {
            loadUserData()
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

    private fun uploadProfileImage(uri: Uri) {
        userId?.let { uid ->
            val imageRef = storage.reference.child("profile_images/$uid.jpg")

            imageRef.putFile(uri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        updateProfileImageUrl(downloadUrl.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false
                }
        }
    }

    private fun updateProfileImageUrl(imageUrl: String) {
        userId?.let { uid ->
            val userRef = db.collection("users").document(uid)

            userRef.update("profileImageUrl", imageUrl)
                .addOnSuccessListener {
                    Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false
                }
                .addOnFailureListener {
                    Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false
                }
        }
    }

    private fun loadUserData() {
        userId?.let { uid ->
            val userRef = db.collection("users").document(uid)

            swipeRefreshLayout.isRefreshing = true
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userData = document.data
                        userData?.let {
                            usernameTextView.text = it["username"] as? String ?: ""
                            ageTextView.text = "${it["age"]}"
                            aboutMeTextView.text = it["about"] as? String ?: ""
                            
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
                    swipeRefreshLayout.isRefreshing = false
                }
                .addOnFailureListener {
                    Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
                    swipeRefreshLayout.isRefreshing = false
                }
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
                itemView.interestChip.setOnClickListener {
                    // Handle interest click
                }
            }
        }
    }

    private fun showLoading() {
        swipeRefreshLayout.isRefreshing = true
    }

    private fun hideLoading() {
        swipeRefreshLayout.isRefreshing = false
    }
}
