package com.example.autopayroll_mobile.leaveRequest // Make sure this matches

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.autopayroll_mobile.R
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class FileLeave : Fragment() {

    // --- Views ---
    private lateinit var backButton: ImageView
    private lateinit var leaveTypeAutoComplete: AutoCompleteTextView
    private lateinit var fromDateEditText: TextInputEditText
    private lateinit var toDateEditText: TextInputEditText
    private lateinit var attachmentLayout: LinearLayout
    private lateinit var attachmentTextView: TextView
    private lateinit var sendButton: Button

    // --- Activity Result Launcher for Image Picking ---
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val uri = data?.data
            if (uri != null) {
                attachmentTextView.text = "File attached" // <-- Change this line
                attachmentTextView.setTextColor(resources.getColor(android.R.color.black, null))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_file_leave, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Initialize Views ---
        backButton = view.findViewById(R.id.backButton)
        leaveTypeAutoComplete = view.findViewById(R.id.leaveTypeAutoComplete)
        fromDateEditText = view.findViewById(R.id.fromDateEditText)
        toDateEditText = view.findViewById(R.id.toDateEditText)
        attachmentLayout = view.findViewById(R.id.attachmentLayout)
        attachmentTextView = view.findViewById(R.id.attachmentTextView)
        sendButton = view.findViewById(R.id.sendButton)

        // --- Setup Dropdown Menu ---
        setupLeaveTypeDropdown()

        // --- Setup Listeners ---
        backButton.setOnClickListener {
            // Navigate back to the previous fragment
            parentFragmentManager.popBackStack()
        }

        fromDateEditText.setOnClickListener {
            showDatePicker(true)
        }

        toDateEditText.setOnClickListener {
            showDatePicker(false)
        }

        attachmentLayout.setOnClickListener {
            openImagePicker()
        }

        sendButton.setOnClickListener {
            showConfirmationDialog()
        }
    }

    private fun setupLeaveTypeDropdown() {
        val leaveTypes = resources.getStringArray(R.array.leave_types)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, leaveTypes)
        leaveTypeAutoComplete.setAdapter(adapter)
    }

    private fun showDatePicker(isFromDate: Boolean) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            // The selection is a Long (timestamp). Convert it to a formatted date string.
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val formattedDate = format.format(calendar.time)

            if (isFromDate) {
                fromDateEditText.setText(formattedDate)
            } else {
                toDateEditText.setText(formattedDate)
            }
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*" // <-- Change this line from "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmation")
            .setMessage("Are all details correct?")
            .setPositiveButton("Yes") { dialog, which ->
                // TODO: Handle the 'Yes' action (e.g., send data to server)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
}