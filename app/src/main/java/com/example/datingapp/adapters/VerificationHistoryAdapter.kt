package com.example.datingapp.adapters

data class VerificationHistoryItem(
    val status: String,
    val timestamp: Date,
    val adminId: String,
    val notes: String?
)

class VerificationHistoryAdapter : RecyclerView.Adapter<VerificationHistoryAdapter.HistoryViewHolder>() {
    private var history: List<VerificationHistoryItem> = emptyList()

    fun updateHistory(newHistory: List<VerificationHistoryItem>) {
        history = newHistory
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_verification_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(history[position])
    }

    override fun getItemCount(): Int = history.size

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: VerificationHistoryItem) {
            itemView.statusTextView.text = item.status
            itemView.timestampTextView.text = item.timestamp.format()
            itemView.adminTextView.text = "Admin: ${item.adminId}"
            itemView.notesTextView.text = item.notes ?: "No notes"

            // Set status color
            when (item.status) {
                VerificationStatus.NOT_VERIFIED.name -> {
                    itemView.statusTextView.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.red)
                    )
                }
                VerificationStatus.PENDING.name -> {
                    itemView.statusTextView.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.orange)
                    )
                }
                VerificationStatus.DOCUMENT_VERIFIED.name -> {
                    itemView.statusTextView.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.blue)
                    )
                }
                VerificationStatus.FACE_VERIFIED.name -> {
                    itemView.statusTextView.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.purple)
                    )
                }
                VerificationStatus.FULLY_VERIFIED.name -> {
                    itemView.statusTextView.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.green)
                    )
                }
                VerificationStatus.REJECTED.name -> {
                    itemView.statusTextView.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.red)
                    )
                }
            }
        }
    }
}
