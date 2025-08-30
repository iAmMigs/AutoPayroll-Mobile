package com.example.autopayroll_mobile.creditAdjustment // Or your correct package name

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
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ArrayRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.autopayroll_mobile.R
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.SimpleDateFormat
import java.util.*

class RequestFilingFragment : Fragment() {

    // --- Views ---
    private lateinit var backButton: ImageView
    private lateinit var adjustmentTypeRadioGroup: RadioGroup
    private lateinit var subTypeAutoComplete: AutoCompleteTextView
    private lateinit var startDateEditText: TextInputEditText
    private lateinit var endDateEditText: TextInputEditText
    private lateinit var attachmentLayout: LinearLayout
    private lateinit var attachmentTextView: TextView
    private lateinit var sendButton: Button

    // ** NEW: Add references to the TextInputLayouts for error messages **
    private lateinit var subTypeLayout: TextInputLayout
    private lateinit var startDateLayout: TextInputLayout
    private lateinit var endDateLayout: TextInputLayout

    // --- Activity Result Launcher for File Picking ---
    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val uri = data?.data
            if (uri != null) {
                attachmentTextView.text = "File attached"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_request_filing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Initialize Views ---
        backButton = view.findViewById(R.id.backButton)
        adjustmentTypeRadioGroup = view.findViewById(R.id.adjustmentTypeRadioGroup)
        subTypeAutoComplete = view.findViewById(R.id.subTypeAutoComplete)
        startDateEditText = view.findViewById(R.id.startDateEditText)
        endDateEditText = view.findViewById(R.id.endDateEditText)
        attachmentLayout = view.findViewById(R.id.attachmentLayout)
        attachmentTextView = view.findViewById(R.id.attachmentTextView)
        sendButton = view.findViewById(R.id.sendButton)

        // ** NEW: Initialize TextInputLayouts **
        subTypeLayout = view.findViewById(R.id.subTypeLayout)
        startDateLayout = view.findViewById(R.id.startDateLayout)
        endDateLayout = view.findViewById(R.id.endDateLayout)


        // --- Setup Listeners ---
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        adjustmentTypeRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            updateSubTypeAdapter(checkedId)
        }

        startDateEditText.setOnClickListener {
            showDatePicker(startDateEditText)
        }

        endDateEditText.setOnClickListener {
            showDatePicker(endDateEditText)
        }

        attachmentLayout.setOnClickListener {
            openFilePicker()
        }

        // ** MODIFIED: Updated Send Button Listener **
        sendButton.setOnClickListener {
            if (validateInputs()) {
                showConfirmationDialog()
            }
        }

        // Set a default state for the dropdown when the view first loads
        updateSubTypeAdapter(R.id.radioLeave)
        adjustmentTypeRadioGroup.check(R.id.radioLeave)
    }

    // ** NEW: Function to validate required fields **
    private fun validateInputs(): Boolean {
        var isValid = true

        // 1. Validate Sub Type
        if (subTypeAutoComplete.text.isNullOrEmpty()) {
            subTypeLayout.error = "Please select a sub type"
            isValid = false
        } else {
            subTypeLayout.error = null // Clear error
        }

        // 2. Validate Start Date
        if (startDateEditText.text.isNullOrEmpty()) {
            startDateLayout.error = "Please select a start date"
            isValid = false
        } else {
            startDateLayout.error = null // Clear error
        }

        // 3. Validate End Date
        if (endDateEditText.text.isNullOrEmpty()) {
            endDateLayout.error = "Please select an end date"
            isValid = false
        } else {
            endDateLayout.error = null // Clear error
        }

        return isValid
    }

    private fun updateSubTypeAdapter(checkedId: Int) {
        @ArrayRes val arrayResId = when (checkedId) {
            R.id.radioLeave -> R.array.leave_types
            R.id.radioAttendance -> R.array.attendance_sub_types
            R.id.radioHoliday -> R.array.other_sub_types
            else -> R.array.leave_types
        }

        val subTypes = resources.getStringArray(arrayResId)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, subTypes)
        subTypeAutoComplete.setAdapter(adapter)
        subTypeAutoComplete.text.clear()
    }

    private fun showDatePicker(editText: TextInputEditText) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select date")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            editText.setText(format.format(calendar.time))
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
        pickFileLauncher.launch(intent)
    }

    private fun showConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmation")
            .setMessage("Are all details correct?")
            .setPositiveButton("Yes") { dialog, _ ->
                // TODO: Handle the 'Yes' action
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}