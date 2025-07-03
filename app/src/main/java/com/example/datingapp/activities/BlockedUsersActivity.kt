package com.example.datingapp.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.datingapp.R
import com.example.datingapp.adapters.BlockedUsersAdapter
import com.example.datingapp.databinding.ActivityBlockedUsersBinding
import com.example.datingapp.models.Block
import com.example.datingapp.viewmodels.BlockedUsersViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BlockedUsersActivity : BaseActivity() {
    private lateinit var binding: ActivityBlockedUsersBinding
    private val viewModel: BlockedUsersViewModel by viewModels()
    private lateinit var adapter: BlockedUsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Blocked Users"

        setupRecyclerView()
        observeBlockedUsers()
    }

    private fun setupRecyclerView() {
        adapter = BlockedUsersAdapter { block ->
            showUnblockDialog(block)
        }
        binding.blockedUsersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@BlockedUsersActivity)
            adapter = this@BlockedUsersActivity.adapter
        }
    }

    private fun observeBlockedUsers() {
        lifecycleScope.launch {
            viewModel.blockedUsers.collect { blocks ->
                adapter.submitList(blocks)
            }
        }
    }

    private fun showUnblockDialog(block: Block) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Unblock User")
            .setMessage("Are you sure you want to unblock ${block.blockedUserDisplayName}?\n\n" +
                    "- You will be able to chat with them\n" +
                    "- They will be able to see your profile\n" +
                    "- They will be able to send you messages")
            .setPositiveButton("Unblock") { _, _ ->
                lifecycleScope.launch {
                    try {
                        viewModel.unblockUser(block.blockedUserId)
                        Snackbar.make(binding.root, "User unblocked successfully", Snackbar.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Snackbar.make(binding.root, "Failed to unblock user: ${e.message}", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.blocked_users_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
