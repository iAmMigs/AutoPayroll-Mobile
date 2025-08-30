package com.example.autopayroll_mobile // Use your package name

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AnnouncementAdapter(
    private var announcements: List<AnnouncementItem>,
    private val onItemClicked: (AnnouncementItem) -> Unit
) : RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder>() {

    class AnnouncementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.announcementIcon)
        val title: TextView = itemView.findViewById(R.id.announcementTitle)
        val message: TextView = itemView.findViewById(R.id.announcementMessage)
        val date: TextView = itemView.findViewById(R.id.announcementDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.announcement_item, parent, false)
        return AnnouncementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        val announcement = announcements[position]
        holder.icon.setImageResource(announcement.iconResId)
        holder.title.text = announcement.title
        holder.message.text = announcement.message
        holder.date.text = announcement.date
        holder.itemView.setOnClickListener { onItemClicked(announcement) }
    }

    override fun getItemCount() = announcements.size

    fun filterList(filteredList: List<AnnouncementItem>) {
        announcements = filteredList
        notifyDataSetChanged()
    }
}