package com.example.datingapp.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.datingapp.R
import com.example.datingapp.adapters.UserAdapter
import com.example.datingapp.models.User
import com.example.datingapp.repositories.UserRepository
import com.example.datingapp.utils.FirebaseStructure
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_user_list.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserListActivity : AppCompatActivity() {
    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: UserAdapter
    private var lastDocument: com.google.firebase.firestore.QuerySnapshot? = null
    private var isLoading = false
    private var isLastPage = false
    private var currentFilter: String = ""
    private var currentGender: String = ""
    private var currentAgeRange: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize adapter
        adapter = UserAdapter(
            onUserClick = { user ->
                // Navigate to user profile
                navigateToUserProfile(user)
            },
            onLikeClick = { user ->
                handleLike(user)
            }
        )

        // Set up RecyclerView
        usersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@UserListActivity)
            adapter = this@UserListActivity.adapter
            setHasFixedSize(true)
        }

        // Set up search
        searchTextInputLayout.setEndIconOnClickListener {
            currentFilter = searchEditText.text.toString()
            loadUsers()
        }

        // Set up gender filters
        maleChip.setOnClickListener {
            currentGender = "male"
            loadUsers()
        }

        femaleChip.setOnClickListener {
            currentGender = "female"
            loadUsers()
        }

        // Set up age filters
        age18_25Chip.setOnClickListener {
            currentAgeRange = "18-25"
            loadUsers()
        }

        age26_35Chip.setOnClickListener {
            currentAgeRange = "26-35"
            loadUsers()
        }

        age36_45Chip.setOnClickListener {
            currentAgeRange = "36-45"
            loadUsers()
        }

        // Set up scroll listener for pagination
        usersRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && !isLoading && !isLastPage) {
                    loadMoreUsers()
                }
            }
        })

        // Set up swipe refresh
        swipeRefreshLayout.setOnRefreshListener {
            loadUsers()
        }

        // Load initial users
        loadUsers()
    }

    private fun loadUsers() {
        if (isLoading) return

        isLoading = true
        showLoading()
        adapter.submitList(emptyList())
        lastDocument = null
        isLastPage = false

        lifecycleScope.launch {
            try {
                val users = userRepository.searchUsers(currentFilter)
                adapter.submitList(users)
                lastDocument = users.lastOrNull()
                isLoading = false
                hideLoading()
            } catch (e: Exception) {
                Toast.makeText(this@UserListActivity, R.string.error, Toast.LENGTH_SHORT).show()
                isLoading = false
                hideLoading()
            }
        }
    }

    private fun loadMoreUsers() {
        if (isLoading || isLastPage) return

        isLoading = true
        lifecycleScope.launch {
            try {
                val users = userRepository.getNearbyUsers(currentLocation, currentRadius)
                adapter.submitList(adapter.currentList + users)
                lastDocument = users.lastOrNull()
                isLastPage = users.isEmpty()
                isLoading = false
            } catch (e: Exception) {
                Toast.makeText(this@UserListActivity, R.string.error, Toast.LENGTH_SHORT).show()
                isLoading = false
            }
        }
    }

    private fun handleLike(user: User) {
        lifecycleScope.launch {
            try {
                userRepository.updateLikeStatus(user.id, !user.liked)
                // Check if it's a match
                if (user.liked) {
                    userRepository.createMatch(user.id)
                }
            } catch (e: Exception) {
                Toast.makeText(this@UserListActivity, R.string.error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToUserProfile(user: User) {
        // TODO: Implement navigation to user profile
    }

    private fun showLoading() {
        swipeRefreshLayout.isRefreshing = true
        emptyStateLayout.visibility = View.GONE
        loadingIndicator.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        swipeRefreshLayout.isRefreshing = false
        loadingIndicator.visibility = View.GONE
        if (adapter.currentList.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
        }
    }
}
