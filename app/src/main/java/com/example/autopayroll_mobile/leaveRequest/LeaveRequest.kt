package com.example.autopayroll_mobile.leaveRequest // Make sure this matches your app's package name

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.autopayroll_mobile.R
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class LeaveRequest : Fragment() {

    // Variable to hold the currently selected date.
    private lateinit var selectedDate: LocalDate

    // References to UI views
    private lateinit var monthYearTextView: TextView
    private lateinit var calendarRecyclerView: RecyclerView
    private lateinit var prevMonthButton: ImageButton
    private lateinit var nextMonthButton: ImageButton
    private lateinit var leaveProgressBar: ProgressBar
    private lateinit var remainingDaysTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leave_request, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize all views by finding them in the inflated view
        monthYearTextView = view.findViewById(R.id.monthYearTextView)
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView)
        prevMonthButton = view.findViewById(R.id.prevMonthButton)
        nextMonthButton = view.findViewById(R.id.nextMonthButton)
        leaveProgressBar = view.findViewById(R.id.leaveProgressBar)
        remainingDaysTextView = view.findViewById(R.id.remainingDaysTextView)

        // Set the initial date to the current month
        selectedDate = LocalDate.now()

        // Set up the calendar UI
        setMonthView()

        // Set up the progress bar UI
        updateLeaveProgress(remaining = 10, taken = 5)

        // Set up click listeners for the navigation buttons
        prevMonthButton.setOnClickListener {
            selectedDate = selectedDate.minusMonths(1)
            setMonthView()
        }

        nextMonthButton.setOnClickListener {
            selectedDate = selectedDate.plusMonths(1)
            setMonthView()
        }
    }

    private fun setMonthView() {
        // Set the month and year text
        monthYearTextView.text = monthYearFromDate(selectedDate)

        // Get the list of days for the month
        val daysInMonth = daysInMonthArray(selectedDate)

        // Create the adapter
        val calendarAdapter = CalendarAdapter(daysInMonth)

        // Set up the RecyclerView layout manager
        val layoutManager = GridLayoutManager(requireContext(), 7)
        calendarRecyclerView.layoutManager = layoutManager
        calendarRecyclerView.adapter = calendarAdapter
    }

    private fun updateLeaveProgress(remaining: Int, taken: Int) {
        val total = remaining + taken

        // Set the max value for the progress bar
        leaveProgressBar.max = total

        // Set the secondary progress to the max to fill the circle with orange
        leaveProgressBar.secondaryProgress = total

        // Set the main progress to the remaining leaves to draw the green arc on top
        leaveProgressBar.progress = remaining

        // Update the text in the center
        remainingDaysTextView.text = remaining.toString()
    }

    private fun daysInMonthArray(date: LocalDate): ArrayList<String> {
        val daysInMonthArray = ArrayList<String>()
        val yearMonth = YearMonth.from(date)

        val daysInMonth = yearMonth.lengthOfMonth()
        val firstOfMonth = date.withDayOfMonth(1)
        val dayOfWeek = firstOfMonth.dayOfWeek.value // Monday=1, Sunday=7

        // Add empty strings for the days before the 1st of the month
        for (i in 1 until dayOfWeek) {
            daysInMonthArray.add("")
        }

        // Add the days of the month
        for (i in 1..daysInMonth) {
            daysInMonthArray.add(i.toString())
        }
        return daysInMonthArray
    }

    private fun monthYearFromDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return date.format(formatter)
    }
}