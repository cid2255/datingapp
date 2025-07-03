package com.example.datingapp.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.datingapp.R
import com.example.datingapp.adapters.MatchAdapter
import com.example.datingapp.databinding.ActivityMatchesBinding
import com.example.datingapp.viewmodels.MatchesViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MatchesActivity : AppCompatActivity() {

    @Inject
    lateinit var matchAdapter: MatchAdapter

    private val viewModel: MatchesViewModel by viewModels()
    private lateinit var binding: ActivityMatchesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFilters()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerView() {
        binding.matchesRecyclerView.apply {
            adapter = matchAdapter
            setHasFixedSize(true)
        }

        matchAdapter.setOnMatchClickListener { match ->
            // Handle match click
            viewModel.selectMatch(match)
        }

        matchAdapter.setOnCallClickListener { match ->
            // Handle call click
            viewModel.makeCall(match)
        }
    }

    private fun setupFilters() {
        binding.filterTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    viewModel.setFilter(FilterOptions(status = listOf(it.text.toString().toMatchStatus())))
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })

        binding.filterButton.setOnClickListener {
            binding.filtersDrawer.open()
        }

        binding.refreshButton.setOnClickListener {
            viewModel.refreshMatches()
        }
    }

    private fun setupListeners() {
        binding.filterButton.setOnClickListener {
            binding.filtersDrawer.open()
        }

        binding.refreshButton.setOnClickListener {
            viewModel.refreshMatches()
        }

        binding.filtersDrawer.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.showPremiumSwitch -> {
                    viewModel.setFilter(viewModel.filters.value.copy(showPremiumOnly = true))
                }
                R.id.showUnreadSwitch -> {
                    viewModel.setFilter(viewModel.filters.value.copy(showUnreadOnly = true))
                }
                R.id.showNearbySwitch -> {
                    viewModel.setFilter(viewModel.filters.value.copy(showNearbyOnly = true))
                }
                R.id.clearFiltersButton -> {
                    viewModel.clearFilters()
                }
            }
            false
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.matches.collectLatest { matches ->
                        matchAdapter.updateMatches(matches)
                        binding.emptyView.isVisible = matches.isEmpty()
                    }
                }

                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        binding.loadingIndicator.isVisible = isLoading
                    }
                }

                launch {
                    viewModel.error.collectLatest { error ->
                        error?.let {
                            MaterialAlertDialogBuilder(this@MatchesActivity)
                                .setTitle("Error")
                                .setMessage(error)
                                .setPositiveButton("OK") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .show()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_matches, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_clear_all -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Clear Matches")
                    .setMessage("Are you sure you want to clear all matches?")
                    .setPositiveButton("Clear") { _, _ ->
                        viewModel.clearAllMatches()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun String.toMatchStatus(): MatchStatus {
        return when (this) {
            "All" -> MatchStatus.MATCHED
            "Active" -> MatchStatus.MATCHED
            "Blocked" -> MatchStatus.BLOCKED
            "Archived" -> MatchStatus.ARCHIVED
            "Premium" -> MatchStatus.MATCHED
            "Unread" -> MatchStatus.MATCHED
            "Nearby" -> MatchStatus.MATCHED
            "Recent" -> MatchStatus.MATCHED
            else -> MatchStatus.MATCHED
        }
    }
}
