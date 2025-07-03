package com.example.datingapp.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.datingapp.R
import com.example.datingapp.models.User
import com.example.datingapp.models.VerificationStatus
import com.example.datingapp.adapters.AdminReviewAdapter
import com.example.datingapp.adapters.VerificationHistoryAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_admin_review.*
import kotlinx.android.synthetic.main.dialog_verification_history.*
import kotlinx.android.synthetic.main.item_verification_history.*
import java.text.SimpleDateFormat
import java.util.*

class AdminReviewActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: AdminReviewAdapter
    private var users: MutableList<User> = mutableListOf()
    private var selectedUsers: MutableList<User> = mutableListOf()
    private var isBatchMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_review)
        setSupportActionBar(toolbar)

        db = FirebaseFirestore.getInstance()
        setupRecyclerView()
        loadUsers()
        setupRefreshButton()
        setupBatchMode()
    }

    private fun setupRecyclerView() {
        adapter = AdminReviewAdapter(users) { user, action ->
            when (action) {
                AdminReviewAdapter.Action.VIEW_ID -> viewIdProof(user)
                AdminReviewAdapter.Action.VIEW_HISTORY -> viewVerificationHistory(user)
                AdminReviewAdapter.Action.APPROVE -> approveUser(user)
                AdminReviewAdapter.Action.REJECT -> rejectUser(user)
                AdminReviewAdapter.Action.ADD_NOTE -> addVerificationNote(user)
                AdminReviewAdapter.Action.SELECT -> toggleSelection(user)
            }
        }
        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersRecyclerView.adapter = adapter
    }

    private fun loadUsers() {
        loadingProgressBar.visibility = View.VISIBLE
        db.collection("users")
            .orderBy("verificationStatus", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                users.clear()
                documents.forEach { document ->
                    val user = document.toObject(User::class.java)
                    user.id = document.id
                    users.add(user)
                }
                adapter.notifyDataSetChanged()
                loadingProgressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading users: ${e.message}", Toast.LENGTH_SHORT).show()
                loadingProgressBar.visibility = View.GONE
            }
    }

    private fun setupRefreshButton() {
        refreshButton.setOnClickListener {
            loadUsers()
        }
    }

    private fun setupBatchMode() {
        val batchModeButton = findViewById<Button>(R.id.batchModeButton)
        batchModeButton.setOnClickListener {
            isBatchMode = !isBatchMode
            adapter.setBatchMode(isBatchMode)
            batchModeButton.text = if (isBatchMode) "Exit Batch Mode" else "Batch Mode"
            if (!isBatchMode) {
                selectedUsers.clear()
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun toggleSelection(user: User) {
        if (isBatchMode) {
            if (selectedUsers.contains(user)) {
                selectedUsers.remove(user)
            } else {
                selectedUsers.add(user)
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun batchApprove() {
        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, "No users selected", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Batch Approve")
            .setMessage("Approve ${selectedUsers.size} users?")
            .setPositiveButton("Approve") { _, _ ->
                selectedUsers.forEach { user ->
                    approveUser(user)
                }
                selectedUsers.clear()
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun batchReject() {
        if (selectedUsers.isEmpty()) {
            Toast.makeText(this, "No users selected", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Batch Reject")
            .setMessage("Reject ${selectedUsers.size} users?")
            .setPositiveButton("Reject") { _, _ ->
                selectedUsers.forEach { user ->
                    rejectUser(user)
                }
                selectedUsers.clear()
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun viewIdProof(user: User) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_id_proof)
        dialog.setCancelable(true)

        val imageView = dialog.findViewById<ImageView>(R.id.idProofImageView)
        val url = user.idProofUrl ?: return
        
        Glide.with(this)
            .load(url)
            .into(imageView)

        dialog.show()
    }

    private fun viewVerificationHistory(user: User) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_verification_history)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val historyAdapter = VerificationHistoryAdapter()
        verificationHistoryRecyclerView.layoutManager = LinearLayoutManager(this)
        verificationHistoryRecyclerView.adapter = historyAdapter

        db.collection("users")
            .document(user.id)
            .collection("verification")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val history = documents.map { document ->
                    val data = document.data
                    VerificationHistoryItem(
                        status = data["status"] as String,
                        timestamp = (data["timestamp"] as com.google.firebase.Timestamp).toDate(),
                        adminId = data["adminId"] as String,
                        notes = data["notes"] as String?
                    )
                }
                historyAdapter.updateHistory(history)
            }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addVerificationNote(user: User) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Verification Note")
            .setView(R.layout.dialog_add_note)
            .setPositiveButton("Save") { _, _ ->
                val note = findViewById<EditText>(R.id.noteEditText).text.toString()
                if (note.isNotEmpty()) {
                    saveVerificationNote(user, note)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun saveVerificationNote(user: User, note: String) {
        val userRef = db.collection("users").document(user.id)
        val verificationRef = userRef.collection("verification")

        val noteData = hashMapOf(
            "status" to user.verificationStatus.name,
            "timestamp" to com.google.firebase.Timestamp.now(),
            "adminId" to getCurrentAdminId(),
            "notes" to note
        )

        verificationRef.document("${user.id}_${System.currentTimeMillis()}_note")
            .set(noteData)
            .addOnSuccessListener {
                Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show()
                loadUsers()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving note: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun approveUser(user: User) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Approve User")
            .setView(R.layout.dialog_add_note)
            .setPositiveButton("Approve") { _, _ ->
                val note = findViewById<EditText>(R.id.noteEditText).text.toString()
                
                val userRef = db.collection("users").document(user.id)
                val verificationRef = userRef.collection("verification")

                // Update verification status
                userRef.update("verificationStatus", VerificationStatus.FULLY_VERIFIED)
                    .addOnSuccessListener {
                        // Save verification record with note
                        val verificationData = hashMapOf(
                            "status" to VerificationStatus.FULLY_VERIFIED.name,
                            "timestamp" to com.google.firebase.Timestamp.now(),
                            "adminId" to getCurrentAdminId(),
                            "notes" to "Approved by admin: ${note.ifEmpty { "No note provided" }}"
                        )

                        verificationRef.document("${user.id}_${System.currentTimeMillis()}")
                            .set(verificationData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "User approved successfully", Toast.LENGTH_SHORT).show()
                                loadUsers()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error saving verification: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error approving user: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun rejectUser(user: User) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Reject User")
            .setView(R.layout.dialog_add_note)
            .setPositiveButton("Reject") { _, _ ->
                val note = findViewById<EditText>(R.id.noteEditText).text.toString()
                
                val userRef = db.collection("users").document(user.id)
                val verificationRef = userRef.collection("verification")

                // Update verification status
                userRef.update("verificationStatus", VerificationStatus.REJECTED)
                    .addOnSuccessListener {
                        // Save verification record with note
                        val verificationData = hashMapOf(
                            "status" to VerificationStatus.REJECTED.name,
                            "timestamp" to com.google.firebase.Timestamp.now(),
                            "adminId" to getCurrentAdminId(),
                            "notes" to "Rejected by admin: ${note.ifEmpty { "No note provided" }}"
                        )

                        verificationRef.document("${user.id}_${System.currentTimeMillis()}")
                            .set(verificationData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "User rejected successfully", Toast.LENGTH_SHORT).show()
                                loadUsers()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error saving verification: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error rejecting user: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun getCurrentAdminId(): String {
        // Get current admin ID from shared preferences or auth
        return "admin_${System.currentTimeMillis()}" // Replace with actual admin ID
    }

    private fun VerificationStatus.getVerificationText(): String {
        return when (this) {
            VerificationStatus.NOT_VERIFIED -> "Not Verified"
            VerificationStatus.PENDING -> "Pending Verification"
            VerificationStatus.DOCUMENT_VERIFIED -> "Document Verified"
            VerificationStatus.FACE_VERIFIED -> "Face Verified"
            VerificationStatus.FULLY_VERIFIED -> "Fully Verified"
            VerificationStatus.REJECTED -> "Rejected"
        }
    }

    private fun VerificationStatus.getVerificationColor(): Int {
        return when (this) {
            VerificationStatus.NOT_VERIFIED -> R.color.red
            VerificationStatus.PENDING -> R.color.orange
            VerificationStatus.DOCUMENT_VERIFIED -> R.color.blue
            VerificationStatus.FACE_VERIFIED -> R.color.purple
            VerificationStatus.FULLY_VERIFIED -> R.color.green
            VerificationStatus.REJECTED -> R.color.red
        }
    }

    private fun Date.format(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(this)
    }
}
