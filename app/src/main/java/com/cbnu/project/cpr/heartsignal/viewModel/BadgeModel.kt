package com.cbnu.project.cpr.heartsignal.viewModel

class BadgeModel(var imgId: Int, var title: String)
{
    fun getImg(): Int {
        return imgId
    }

    fun getBadgeTitle(): String {
        return title
    }
}