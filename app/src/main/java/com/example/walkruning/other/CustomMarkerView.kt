package com.example.walkruning.other

import android.content.Context
import com.example.walkruning.db.Running
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.marker_view.view.*
import java.text.SimpleDateFormat
import java.util.*

// TODO: 26.06.2021  BarChart taki barlara tıklandığında her bir bar herbir yürüyüş sonundaki verileri pop-up şeklinde gösteriyoır.
class CustomMarkerView(
        val runs: List<Running>,
        context: Context,
        layoutId:Int
): MarkerView(context, layoutId){


    override fun getOffset(): MPPointF {
        return MPPointF(-width/2f, -height.toFloat())

    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)

        if(e == null){
            return
        }

        val currentRunId = e.x.toInt()
        val run = runs[currentRunId]

        val calender = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }

        val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        tvDate.text = dateFormat.format(calender.time)

        val avgSpeed = "${run.avgSpeedInKMH}km/h"
        tvAvgSpeed.text = avgSpeed

        val distanceInKm = "${run.distanceInMeters / 1000f}km"
        tvDistance.text = distanceInKm

        val caloriesBurned = "${run.caloriesBurned}kcal"
        tvCaloriesBurned.text = caloriesBurned

        tvDuration.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)
    }

}