package com.example.datingapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.datingapp.models.Match
import com.example.datingapp.viewholders.MatchViewHolder
import com.example.datingapp.R
import com.example.datingapp.viewmodels.MatchesViewModel
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class MatchAdapter @Inject constructor(
    private val viewModel: MatchesViewModel
) : ListAdapter<Match, MatchViewHolder>(MatchDiffCallback()) {

    private var onMatchClickListener: ((Match) -> Unit)? = null
    private var onCallClickListener: ((Match) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val match = getItem(position)
        holder.bind(match, onMatchClick = { selectedMatch ->
            onMatchClickListener?.invoke(selectedMatch)
        }, onCallClick = { selectedMatch ->
            onCallClickListener?.invoke(selectedMatch)
        })
    }

    fun setOnMatchClickListener(listener: (Match) -> Unit) {
        onMatchClickListener = listener
    }

    fun setOnCallClickListener(listener: (Match) -> Unit) {
        onCallClickListener = listener
    }

    fun updateMatches(matches: List<Match>) {
        submitList(matches)
    }

    fun clear() {
        submitList(emptyList())
    }

    companion object {
        private class MatchDiffCallback : DiffUtil.ItemCallback<Match>() {
            override fun areItemsTheSame(oldItem: Match, newItem: Match): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Match, newItem: Match): Boolean {
                return oldItem == newItem
            }
        }
    }
}
