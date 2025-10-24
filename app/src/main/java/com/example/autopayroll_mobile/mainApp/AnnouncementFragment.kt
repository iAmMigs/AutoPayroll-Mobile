package com.example.autopayroll_mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout

class AnnouncementFragment : Fragment() {

    private lateinit var announcementAdapter: AnnouncementAdapter

    //TODO: Announcements taken from API?
    private val allAnnouncements = listOf(
        AnnouncementItem(R.drawable.ic_payroll, "Salary Disbursement", "Your payroll for August 1-15, has been processed...", "Aug. 15, 2025", AnnouncementCategory.Payroll),
        AnnouncementItem(R.drawable.ic_admin, "Follow-Up", "Hi Nicho, natapos mo na ba lampasuhin yang third floor?", "Aug. 14, 2025", AnnouncementCategory.Admin),
        AnnouncementItem(R.drawable.ic_memo, "Memo: Holiday Schedule", "Please be advised of the upcoming holiday schedule for...", "Aug. 12, 2025", AnnouncementCategory.Memo),
        AnnouncementItem(R.drawable.ic_payroll, "Salary Disbursement", "Your payroll for July 16-31, has been processed...", "July 31, 2025", AnnouncementCategory.Payroll)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_announcement, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Views
        val tabLayout: TabLayout = view.findViewById(R.id.tabLayout)
        val recyclerView: RecyclerView = view.findViewById(R.id.announcementRecyclerView)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        announcementAdapter = AnnouncementAdapter(allAnnouncements) { clickedItem ->
            // Handle item click: navigate to details
            val detailsFragment = AnnouncementDetailsFragment.newInstance(
                clickedItem.title,
                clickedItem.date,
                clickedItem.message
            )
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.nav_host_fragment, detailsFragment) // Use your container ID
                addToBackStack(null)
                commit()
            }
        }
        recyclerView.adapter = announcementAdapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val filteredList = when (tab?.position) {
                    1 -> allAnnouncements.filter { it.category == AnnouncementCategory.Payroll }
                    2 -> allAnnouncements.filter { it.category == AnnouncementCategory.Admin }
                    3 -> allAnnouncements.filter { it.category == AnnouncementCategory.Memo }
                    else -> allAnnouncements // Position 0 is "All"
                }
                announcementAdapter.filterList(filteredList)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}