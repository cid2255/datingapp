package com.example.datingapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.datingapp.R
import com.example.datingapp.databinding.DialogBlockStatusBinding
import com.example.datingapp.models.Block
import com.example.datingapp.services.BlockService
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BlockStatusDialogFragment : DialogFragment() {
    private var _binding: DialogBlockStatusBinding? = null
    private val binding get() = _binding!!
    private var onBlockStatusChanged: ((Boolean) -> Unit)? = null
    private var blockService: BlockService? = null
    private var currentUserId: String? = null
    private var otherUserId: String? = null

    companion object {
        const val ARG_CURRENT_USER_ID = "current_user_id"
        const val ARG_OTHER_USER_ID = "other_user_id"

        fun newInstance(currentUserId: String, otherUserId: String): BlockStatusDialogFragment {
            return BlockStatusDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CURRENT_USER_ID, currentUserId)
                    putString(ARG_OTHER_USER_ID, otherUserId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentUserId = arguments?.getString(ARG_CURRENT_USER_ID)
        otherUserId = arguments?.getString(ARG_OTHER_USER_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogBlockStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        blockService = BlockService.getInstance(requireContext())
        
        // Set initial block status
        updateBlockStatus()

        // Set up unblock button
        binding.unblockButton.setOnClickListener {
            unblockUser()
        }
    }

    private fun updateBlockStatus() {
        lifecycleScope.launch {
            try {
                val isBlocked = blockService?.isBlocked(currentUserId ?: return@launch, otherUserId ?: return@launch) ?: false
                binding.blockStatusText.text = if (isBlocked) {
                    "This user has blocked you"
                } else {
                    "You can now message this user"
                }
                binding.unblockButton.text = if (isBlocked) {
                    "Unblock User"
                } else {
                    "Block User"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun unblockUser() {
        val currentUserId = this.currentUserId ?: return
        val otherUserId = this.otherUserId ?: return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Unblock User")
            .setMessage("Are you sure you want to unblock this user?\n\n" +
                    "- You will be able to chat with them\n" +
                    "- They will be able to see your profile\n" +
                    "- They will be able to send you messages")
            .setPositiveButton("Unblock") { _, _ ->
                lifecycleScope.launch {
                    try {
                        blockService?.unblockUser(currentUserId, otherUserId)
                        onBlockStatusChanged?.invoke(false)
                        dismiss()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun setOnBlockStatusChangedListener(listener: (Boolean) -> Unit) {
        onBlockStatusChanged = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
