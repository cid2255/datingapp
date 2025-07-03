package com.example.datingapp.adapters

import android.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.datingapp.models.VerificationOption
import com.example.datingapp.R as AppR

class VerificationOptionAdapter(
    private val options: List<VerificationOption>,
    private val onOptionSelected: (VerificationOption) -> Unit
) : RecyclerView.Adapter<VerificationOptionAdapter.VerificationOptionViewHolder>() {

    class VerificationOptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconImageView = itemView.findViewById<ImageView>(AppR.id.iconImageView)
        val titleTextView = itemView.findViewById<TextView>(AppR.id.titleTextView)
        val arrowImageView = itemView.findViewById<ImageView>(AppR.id.arrowImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerificationOptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(AppR.layout.item_verification_option, parent, false)
        return VerificationOptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: VerificationOptionViewHolder, position: Int) {
        val option = options[position]
        
        holder.iconImageView.setImageResource(option.icon)
        holder.titleTextView.text = option.title
        
        holder.itemView.setOnClickListener {
            onOptionSelected(option)
        }
    }

    override fun getItemCount(): Int = options.size
}
