package com.example.autopayroll_mobile.leaveRequest // Use your package name

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.autopayroll_mobile.R

class LeaveDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_leave_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize all views
        val backButton: ImageView = view.findViewById(R.id.backButton)
        val detailRequestId: TextView = view.findViewById(R.id.detailRequestId)
        val detailLeaveType: TextView = view.findViewById(R.id.detailLeaveType)
        val detailStatus: TextView = view.findViewById(R.id.detailStatus)
        val detailRemarks: TextView = view.findViewById(R.id.detailRemarks)

        // Retrieve the full LeaveRequestItem object from the arguments
        val leaveRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_LEAVE_REQUEST, LeaveRequestItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_LEAVE_REQUEST)
        }

        // Populate the views with data
        if (leaveRequest != null) {
            detailRequestId.text = "Leave Request Id: ${leaveRequest.id}"
            detailLeaveType.text = "Leave Type: ${leaveRequest.leaveType}\nLeave Date: ${leaveRequest.date}\nLeave Duration: ${leaveRequest.leaveDuration}"
            detailStatus.text = "Status: ${leaveRequest.status.name}"
            detailRemarks.text = leaveRequest.remarks
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    companion object {
        private const val ARG_LEAVE_REQUEST = "arg_leave_request"

        // Updated newInstance to accept the entire object
        fun newInstance(leaveRequest: LeaveRequestItem): LeaveDetailsFragment {
            val fragment = LeaveDetailsFragment()
            val args = Bundle()
            args.putParcelable(ARG_LEAVE_REQUEST, leaveRequest)
            fragment.arguments = args
            return fragment
        }
    }
}