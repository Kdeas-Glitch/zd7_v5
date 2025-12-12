package com.bignerbranch.android.zd7_v5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bignerbranch.android.zd7_v5.Room.RouteBusLink

class DriverRoutesAdapter(
    private val routes: List<RouteBusLink>,
    private val onRemoveClick: (RouteBusLink) -> Unit
) : RecyclerView.Adapter<DriverRoutesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val routeId: TextView = view.findViewById(R.id.routeIdTextView)
        val busId: TextView = view.findViewById(R.id.busIdTextView)
        val status: TextView = view.findViewById(R.id.statusTextView)
        val removeButton: Button = view.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_driver_route, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val route = routes[position]
        holder.routeId.text = "Маршрут ID: ${route.routesId}"
        holder.busId.text = "Автобус ID: ${route.busId}"
        holder.status.text = if (route.isActive) "Активен" else "Неактивен"

        // Устанавливаем цвет статуса
        val context = holder.itemView.context
        holder.status.setTextColor(
            if (route.isActive) context.getColor(android.R.color.holo_green_dark)
            else context.getColor(android.R.color.holo_red_dark)
        )

        // Настройка фона статуса
        holder.status.setBackgroundResource(
            if (route.isActive) R.drawable.bg_status_active
            else R.drawable.bg_status_inactive
        )

        // Кнопка удаления
        holder.removeButton.setOnClickListener {
            onRemoveClick(route)
        }

        // Показываем кнопку удаления только для активных маршрутов
        holder.removeButton.visibility = if (route.isActive) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int = routes.size
}