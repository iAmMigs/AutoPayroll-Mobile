package com.example.autopayroll_mobile // Use your package name

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class AnnouncementDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_announcement_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find views
        val backButton: ImageView = view.findViewById(R.id.backButton)
        val detailTitle: TextView = view.findViewById(R.id.detailTitle)
        val detailDate: TextView = view.findViewById(R.id.detailDate)
        val detailMessage: TextView = view.findViewById(R.id.detailMessage)

        // Retrieve the data passed from the list fragment
        detailTitle.text = arguments?.getString(ARG_TITLE)
        detailDate.text = arguments?.getString(ARG_DATE)
        detailMessage.text = arguments?.getString(ARG_MESSAGE)

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_DATE = "arg_date"
        private const val ARG_MESSAGE = "arg_message"

        fun newInstance(title: String, date: String, message: String): AnnouncementDetailsFragment {
            val fragment = AnnouncementDetailsFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_DATE, date)
            args.putString(ARG_MESSAGE, message)
            fragment.arguments = args
            return fragment
        }
    }
}