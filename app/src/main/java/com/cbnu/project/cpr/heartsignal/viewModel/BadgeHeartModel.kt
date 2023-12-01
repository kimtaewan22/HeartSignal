package com.cbnu.project.cpr.heartsignal.viewModel


class BadgeHeartModel(var imgId: Int, var title: String) {
    fun getImg(): Int {
        return imgId
    }

    fun getBadgeTitle(): String {
        return title
    }
}