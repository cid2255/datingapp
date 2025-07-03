package com.example.datingapp.adapters

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.datingapp.R
import com.example.datingapp.activities.ChatActivity
import com.google.android.material.snackbar.Snackbar

class MessageGestureCallback(
    private val adapter: MessageAdapter,
    private val activity: ChatActivity
) : ItemTouchHelper.Callback() {

    private val background = ColorDrawable(Color.RED)
    private val deleteIcon: Drawable
    private val replyIcon: Drawable
    private val forwardIcon: Drawable
    private val iconMargin = 16
    private val vibrator: Vibrator = activity.getSystemService()!!
    private val vibrationDuration = 50L

    init {
        deleteIcon = ContextCompat.getDrawable(activity, R.drawable.ic_delete)!!
        replyIcon = ContextCompat.getDrawable(activity, R.drawable.ic_reply)!!
        forwardIcon = ContextCompat.getDrawable(activity, R.drawable.ic_forward)!!
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // Handle drag and drop if needed
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        val message = adapter.currentList[position]
        val recyclerView = viewHolder.itemView.parent as RecyclerView

        when (direction) {
            ItemTouchHelper.END -> {
                // Delete message
                vibrator.vibrate(VibrationEffect.createOneShot(vibrationDuration, VibrationEffect.DEFAULT_AMPLITUDE))
                activity.deleteMessage(message)
                Snackbar.make(
                    recyclerView,
                    "Message deleted",
                    Snackbar.LENGTH_LONG
                ).setAction("UNDO") {
                    adapter.notifyItemChanged(position)
                }.show()
            }
            ItemTouchHelper.START -> {
                // Show options menu
                vibrator.vibrate(VibrationEffect.createOneShot(vibrationDuration, VibrationEffect.DEFAULT_AMPLITUDE))
                val popup = PopupMenu(activity, viewHolder.itemView)
                popup.menuInflater.inflate(R.menu.message_gesture_menu, popup.menu)
                
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.reply -> {
                            vibrator.vibrate(VibrationEffect.createOneShot(vibrationDuration, VibrationEffect.DEFAULT_AMPLITUDE))
                            activity.replyToMessage(message)
                            true
                        }
                        R.id.forward -> {
                            vibrator.vibrate(VibrationEffect.createOneShot(vibrationDuration, VibrationEffect.DEFAULT_AMPLITUDE))
                            activity.forwardMessage(message)
                            true
                        }
                        else -> false
                    }
                }
                
                popup.show()
            }
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20

        // Draw background
        if (dX > 0) { // Swiping right
            background.color = Color.parseColor("#FF4081")
            background.setBounds(
                itemView.left,
                itemView.top,
                dX.toInt() + backgroundCornerOffset,
                itemView.bottom
            )
            background.draw(c)

            // Draw reply icon
            replyIcon.setBounds(
                itemView.left + iconMargin,
                itemView.top + (itemView.height - replyIcon.intrinsicHeight) / 2,
                itemView.left + iconMargin + replyIcon.intrinsicWidth,
                itemView.top + (itemView.height + replyIcon.intrinsicHeight) / 2
            )
            replyIcon.draw(c)

            // Draw forward icon
            forwardIcon.setBounds(
                itemView.left + iconMargin + replyIcon.intrinsicWidth + iconMargin,
                itemView.top + (itemView.height - forwardIcon.intrinsicHeight) / 2,
                itemView.left + iconMargin + replyIcon.intrinsicWidth + iconMargin + forwardIcon.intrinsicWidth,
                itemView.top + (itemView.height + forwardIcon.intrinsicHeight) / 2
            )
            forwardIcon.draw(c)
        } else if (dX < 0) { // Swiping left
            background.color = Color.RED
            background.setBounds(
                itemView.right + dX.toInt() - backgroundCornerOffset,
                itemView.top,
                itemView.right,
                itemView.bottom
            )
            background.draw(c)

            // Draw delete icon
            deleteIcon.setBounds(
                itemView.right - deleteIcon.intrinsicWidth - iconMargin,
                itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2,
                itemView.right - iconMargin,
                itemView.top + (itemView.height + deleteIcon.intrinsicHeight) / 2
            )
            deleteIcon.draw(c)
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun isLongPressDragEnabled(): Boolean = true
    override fun isItemViewSwipeEnabled(): Boolean = true
}
