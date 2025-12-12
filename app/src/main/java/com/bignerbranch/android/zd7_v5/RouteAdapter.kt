package com.bignerbranch.android.zd7_v5

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bignerbranch.android.zd7_v5.Room.Routes

class RouteAdapter(private val routes: List<Routes>) : RecyclerView.Adapter<RouteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val routeId: TextView = view.findViewById(R.id.routeIdTextView)
        val routeMap: TextView = view.findViewById(R.id.routeMapTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_route, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val route = routes[position]
        holder.routeId.text = "Маршрут #${route.routesId}"
        holder.routeMap.text = route.map
    }

    override fun getItemCount(): Int = routes.size
}