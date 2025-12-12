package com.bignerbranch.android.zd7_v5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bignerbranch.android.zd7_v5.Room.Bus

class BusAdapter(private val buses: List<Bus>) : RecyclerView.Adapter<BusAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val busId: TextView = view.findViewById(R.id.busIdTextView)
        val busNumber: TextView = view.findViewById(R.id.busNumberTextView)
        val busCondition: TextView = view.findViewById(R.id.busConditionTextView)
        val busStatus: TextView = view.findViewById(R.id.busStatusTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bus, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bus = buses[position]
        holder.busId.text = "ID: ${bus.busId}"
        holder.busNumber.text = bus.number
        holder.busCondition.text = "Состояние: ${bus.condition}"
        holder.busStatus.text = if (bus.busy) "Занят" else "Свободен"
        holder.busStatus.setTextColor(
            if (bus.busy) holder.itemView.context.getColor(android.R.color.holo_red_dark)
            else holder.itemView.context.getColor(android.R.color.holo_green_dark)
        )
    }

    override fun getItemCount(): Int = buses.size
}