package com.bignerbranch.android.zd7_v5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bignerbranch.android.zd7_v5.Room.Driver

class DriverAdapter(private val drivers: List<Driver>) : RecyclerView.Adapter<DriverAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val driverName: TextView = view.findViewById(R.id.driverNameTextView)
        val driverId: TextView = view.findViewById(R.id.driverIdTextView)
        val driverBusId: TextView = view.findViewById(R.id.driverBusIdTextView)
        val driverSalary: TextView = view.findViewById(R.id.driverSalaryTextView)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_driver, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val driver = drivers[position]
        holder.driverName.text = driver.name
        holder.driverId.text = "ID: ${driver.drivId}"
        holder.driverBusId.text = "Автобус: ${driver.busId}"

        // Показываем маршрут (String)
        val routeText = if (driver.routesId.isEmpty()) "Нет маршрута" else "Маршрут: ${driver.routesId}"
        // Можно добавить в существующее поле или создать новое
        holder.driverSalary.text = "ЗП: ${driver.baseSalary} ₽ | $routeText"
    }

    override fun getItemCount(): Int = drivers.size
}