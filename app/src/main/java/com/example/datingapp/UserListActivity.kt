package com.example.datingapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_user_list.*
import kotlinx.android.synthetic.main.item_user.*

private const val PAGE_SIZE = 10

class UserListActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: UserAdapter
    private var lastDocument: Query.DocumentSnapshot? = null
    private var isLoading = false
    private var isLastPage = false
    private var currentFilter: String = ""
    private var currentGender: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set up RecyclerView
        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = UserAdapter()
        usersRecyclerView.adapter = adapter

        // Set up search
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currentFilter = s.toString()
                loadUsers()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

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
            currentFilter = "18-25"
            loadUsers()
        }

        age26_35Chip.setOnClickListener {
            currentFilter = "26-35"
            loadUsers()
        }

        age36_45Chip.setOnClickListener {
            currentFilter = "36-45"
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

        // Load initial users
        loadUsers()
    }

    private fun loadUsers() {
        isLoading = true
        showLoading()
        adapter.clear()
        lastDocument = null
        isLastPage = false

        val query = buildQuery()
        query.get().addOnSuccessListener { result ->
            val users = result.toObjects(User::class.java)
            adapter.addAll(users)
            lastDocument = result.documents.lastOrNull()
            isLoading = false
            hideLoading()
        }.addOnFailureListener { e ->
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            isLoading = false
            hideLoading()
        }
    }

    private fun loadMoreUsers() {
        if (isLoading || isLastPage) return

        isLoading = true
        val query = buildQuery()
        query.get().addOnSuccessListener { result ->
            val users = result.toObjects(User::class.java)
            adapter.addAll(users)
            lastDocument = result.documents.lastOrNull()
            isLastPage = users.isEmpty()
            isLoading = false
        }.addOnFailureListener { e ->
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            isLoading = false
        }
    }

    private fun buildQuery(): Query {
        var query = db.collection("users")
            .whereNotEqualTo("id", auth.currentUser?.uid)
            .orderBy("username")
            .limit(PAGE_SIZE.toLong())

        if (lastDocument != null) {
            query = query.startAfter(lastDocument)
        }

        if (currentFilter.isNotEmpty()) {
            query = query.whereGreaterThanOrEqualTo("username", currentFilter)
                .whereLessThan("username", currentFilter + "\uf8ff")
        }

        if (currentGender.isNotEmpty()) {
            query = query.whereEqualTo("gender", currentGender)
        }

        return query
    }

    private fun showLoading() {
        loadingIndicator.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.GONE
    }

    private fun hideLoading() {
        loadingIndicator.visibility = View.GONE
        if (adapter.itemCount == 0) {
            emptyStateLayout.visibility = View.VISIBLE
        }
    }

    inner class UserAdapter : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
        private val users = mutableListOf<User>()

        fun clear() {
            users.clear()
            notifyDataSetChanged()
        }

        fun addAll(newUsers: List<User>) {
            val startPosition = users.size
            users.addAll(newUsers)
            notifyItemRangeInserted(startPosition, newUsers.size)
        }

        override fun onCreateViewHolder(parent: View, viewType: Int): UserViewHolder {
            val view = layoutInflater.inflate(R.layout.item_user, parent, false)
            return UserViewHolder(view)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            holder.bind(users[position])
        }

        override fun getItemCount() = users.size

        inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            fun bind(user: User) {
                // Set profile image
                Glide.with(itemView.context)
                    .load(user.profileImageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.ic_person)
                    .into(profileImageView)

                // Set user info
                usernameTextView.text = user.username
                ageTextView.text = "${user.age}"
                locationTextView.text = user.location
                aboutTextView.text = user.about

                // Set like button state
                likeButton.text = if (user.liked) {
                    getString(R.string.liked)
                } else {
                    getString(R.string.like)
                }

                // Handle like button click
                likeButton.setOnClickListener {
                    handleLike(user)
                }

                // Handle item click
                itemView.setOnClickListener {
                    // Navigate to user profile
                }
            }
        }
    }

    private fun handleLike(user: User) {
        val userId = auth.currentUser?.uid ?: return
        val userData = hashMapOf(
            "liked" to !user.liked,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("users")
            .document(userId)
            .update(userData)
            .addOnSuccessListener {
                // Update UI
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            }
    }
}
