package com.example.datingapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.datingapp.R
import com.example.datingapp.databinding.DialogReportBinding
import com.example.datingapp.models.ReportReason
import com.example.datingapp.viewmodels.ReportViewModel

class ReportDialogFragment : DialogFragment() {
    private var _binding: DialogReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ReportViewModel
    private var reportedUserId: String = ""

    companion object {
        const val ARG_REPORTED_USER_ID = "reported_user_id"

        fun newInstance(reportedUserId: String): ReportDialogFragment {
            return ReportDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_REPORTED_USER_ID, reportedUserId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reportedUserId = requireArguments().getString(ARG_REPORTED_USER_ID) ?: ""
        viewModel = ViewModelProvider(this)[ReportViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up reason spinner
        val reasons = ReportReason.values().map { it.description }
        binding.reasonSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            reasons
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Observe report status
        viewModel.reportStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is ReportViewModel.ReportStatus.Loading -> {
                    binding.submitButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                }
                is ReportViewModel.ReportStatus.Success -> {
                    binding.submitButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    dismiss()
                    Toast.makeText(requireContext(), status.message, Toast.LENGTH_SHORT).show()
                }
                is ReportViewModel.ReportStatus.Error -> {
                    binding.submitButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), status.message, Toast.LENGTH_SHORT).show()
                }
                ReportViewModel.ReportStatus.Idle -> {
                    binding.submitButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

        // Set up submit button
        binding.submitButton.setOnClickListener {
            val reason = ReportReason.values()[binding.reasonSpinner.selectedItemPosition]
            val description = binding.descriptionEditText.text.toString().trim()
            
            if (description.isEmpty()) {
                binding.descriptionEditText.error = "Please enter a description"
                return@setOnClickListener
            }

            viewModel.submitReport(
                reportedUserId = reportedUserId,
                reason = reason.description,
                description = description
            )
        }

        // Set up cancel button
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
}
