package com.cbnu.project.cpr.heartsignal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cbnu.project.cpr.heartsignal.R
import com.github.mikephil.charting.data.Entry

class ChartDataRecyclerViewAdapter(private val dataList: List<Entry>) :
    RecyclerView.Adapter<ChartDataRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chart_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataPoint = dataList[position]
        holder.textViewXValue.text = "X: ${dataPoint.x}"
        holder.textViewYValue.text = "Y: ${dataPoint.y}"
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewXValue: TextView = view.findViewById(R.id.textViewXValue)
        val textViewYValue: TextView = view.findViewById(R.id.textViewYValue)
    }
}
