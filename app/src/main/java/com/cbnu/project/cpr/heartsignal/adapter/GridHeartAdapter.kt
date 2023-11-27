package com.cbnu.project.cpr.heartsignal.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.viewModel.BadgeHeartModel

class GridHeartAdapter(context: Context, courseModelArrayList: MutableList<BadgeHeartModel>) :
    ArrayAdapter<BadgeHeartModel?>(context, 0, courseModelArrayList!! as List<BadgeHeartModel?>) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var listitemView = convertView
        if (listitemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listitemView = LayoutInflater.from(context).inflate(R.layout.item_chart_data, parent, false)
        }

        val courseModel: BadgeHeartModel? = getItem(position)

        val icon = listitemView?.findViewById<ImageView>(R.id.badge_icon)
        val name = listitemView?.findViewById<TextView>(R.id.badge_title)
        icon?.setImageResource(courseModel!!.getImg())
        name?.text = courseModel!!.getBadgeTitle()
        return listitemView!!
    }
}