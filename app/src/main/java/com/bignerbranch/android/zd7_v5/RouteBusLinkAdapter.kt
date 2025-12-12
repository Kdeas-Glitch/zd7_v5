package com.bignerbranch.android.zd7_v5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bignerbranch.android.zd7_v5.Room.RouteBusLink

class RouteBusLinkAdapter(private val links: List<RouteBusLink>) : RecyclerView.Adapter<RouteBusLinkAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val linkId: TextView = view.findViewById(R.id.linkIdTextView)
        val linkStatus: TextView = view.findViewById(R.id.linkStatusTextView)
        val linkRouteId: TextView = view.findViewById(R.id.linkRouteIdTextView)
        val linkBusId: TextView = view.findViewById(R.id.linkBusIdTextView)
        val linkDriverId: TextView = view.findViewById(R.id.linkDriverIdTextView)
        val linkNotes: TextView = view.findViewById(R.id.linkNotesTextView)
        val notesLayout: View = linkNotes.parent as View
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_route_bus_link, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val link = links[position]
        holder.linkId.text = "Связь #${link.linkId}"
        holder.linkStatus.text = if (link.isActive) "АКТИВНА" else "НЕАКТИВНА"

        // Простой способ установки цвета фона
        if (link.isActive) {
            holder.linkStatus.setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
        } else {
            holder.linkStatus.setBackgroundColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
        }
        holder.linkStatus.setTextColor(holder.itemView.context.getColor(android.R.color.white))

        holder.linkRouteId.text = link.routesId.toString()
        holder.linkBusId.text = link.busId.toString()
        holder.linkDriverId.text = link.driverId?.toString() ?: "—"

        if (!link.notes.isNullOrEmpty()) {
            holder.notesLayout.visibility = View.VISIBLE
            holder.linkNotes.text = link.notes
        } else {
            holder.notesLayout.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = links.size
}