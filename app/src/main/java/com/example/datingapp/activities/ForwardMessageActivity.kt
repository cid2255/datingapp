package com.example.datingapp.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.datingapp.R
import com.example.datingapp.adapters.ForwardContactsAdapter
import com.example.datingapp.models.User
import com.example.datingapp.repositories.UserRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_forward_message.*

class ForwardMessageActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository
    private lateinit var adapter: ForwardContactsAdapter
    private var selectedMessage: ChatMessage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forward_message)

        auth = FirebaseAuth.getInstance()
        userRepository = UserRepository()

        // Get selected message from intent
        selectedMessage = intent.getParcelableExtra("message")

        // Setup RecyclerView
        adapter = ForwardContactsAdapter(
            onContactSelected = { user ->
                forwardMessage(user)
            }
        )
        contactsRecyclerView.layoutManager = LinearLayoutManager(this)
        contactsRecyclerView.adapter = adapter

        // Load contacts
        loadContacts()
    }

    private fun loadContacts() {
        userRepository.getUsers().observe(this) { users ->
            val filteredUsers = users.filter { it.id != auth.currentUser?.uid }
            adapter.submitList(filteredUsers)
        }
    }

    private fun forwardMessage(user: User) {
        val message = selectedMessage?.copy(
            senderId = auth.currentUser?.uid ?: "",
            receiverId = user.id,
            timestamp = System.currentTimeMillis(),
            type = MessageType.TEXT,
            isForwarded = true
        )

        if (message != null) {
            val chatRepository = ChatRepository()
            chatRepository.sendMessage(message)
            
            MaterialAlertDialogBuilder(this)
                .setTitle("Message Forwarded")
                .setMessage("Message has been forwarded to ${user.name}")
                .setPositiveButton("OK") { _, _ ->
                    finish()
                }
                .show()
        }
    }

    private fun onBackClick(view: View) {
        finish()
    }
}
