package com.example.walkruning.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.example.walkruning.R
import com.example.walkruning.other.CustomMarkerView
import com.example.walkruning.other.TrackingUtility
import com.example.walkruning.ui.viewmodels.MainViewModel
import com.example.walkruning.ui.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlin.math.round

// TODO: 26.06.2021
//  Barchart Kurulumu ve Ayarlanması
//  StatisticViewModel de tanımladığımız değişkenleri StatisticsFragment göstermek için viewModel sayesinde çağırıyoruz.

@AndroidEntryPoint
class StatisticsFragment:Fragment(R.layout.fragment_statistics) {

    private val viewModel: StatisticsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Oluşturduğumuz bu fonksiyondaki yapıları StatisticsFragment de göstermek için onViewCreated a tanımladık ve UI bu değerlerin gözükmesi sağlandı.
        subscribeToObservers()
        setupBarChart()
    }


    /// TODO: 26.06.2021 *********** BarChart Setup and Settings Up - Barchart Kurulumu ve Ayarlanması *************************************
    private fun setupBarChart(){
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.RED
            textColor = Color.RED
            setDrawGridLines(false)
        }

        barChart.axisLeft.apply {
            axisLineColor = Color.RED
            textColor = Color.RED
            setDrawGridLines(false)
        }

        barChart.axisRight.apply {
            axisLineColor = Color.RED
            textColor = Color.RED
            setDrawGridLines(false)
        }

        barChart.apply {
            description.text = "Avg Speed Over Time"
            legend.isEnabled = false

        }
    }

    // StatisticViewModel de tanımladığımız değişkenleri tek bir fonksiyon üzerinden çekiyoruz ve StatisticsFragment ilgili olan yerlere tanımlıyoruz. UI bu değerler gösteriliyor.
    private fun subscribeToObservers() {

        // StatisticViewModel de tanımladığımız değişkenitotalTimeRun ı viewModel sayesinde çekip observe edip tvTotalTime MaterialTextView ına tanımlıyoruz.
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let{
               val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                tvTotalTime.text = totalTimeRun
            }
        })

        // StatisticViewModel de tanımladığımız değişkeni totalDistance ı viewModel sayesinde çekip observe edip tvTotalDistance MaterialTextView ına tanımlıyoruz.
        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let{
                val km = it * 1000f
                val totalDistance = round(km * 10f ) / 10f
                val totalDistanceString = "${totalDistance}km"
                tvTotalDistance.text = totalDistanceString
            }
        })

        // StatisticViewModel de tanımladığımız değişkeni totalAvgSpeed ı viewModel sayesinde çekip observe edip tvAverageSpeed MaterialTextView ına tanımlıyoruz.
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let{
                val avgSpeed = round(it * 10f) / 10f
                val avgSpeedString = "${avgSpeed}km/h"
                tvAverageSpeed.text = avgSpeedString
            }
        })

        // StatisticViewModel de tanımladığımız değişkeni totalCaloriesBurned ı viewModel sayesinde çekip observe edip tvTotalCalories MaterialTextView ına tanımlıyoruz.
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let{
                val caloriesBurned = "${it}kcal"
                tvTotalCalories.text = caloriesBurned
            }
        })
        viewModel.runSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let {
                val allAvgSpeeds = it.indices.map { i->
                    BarEntry(i.toFloat(),it[i].avgSpeedInKMH)
                }

                val barDataSet = BarDataSet(allAvgSpeeds,"Avg Speed Over Time").apply {
                     valueTextColor = Color.WHITE
                     color = ContextCompat.getColor(requireContext(),R.color.colorAccent)
                }
                barChart.data = BarData(barDataSet)
                barChart.marker = CustomMarkerView(it.reversed(),requireContext(),R.layout.marker_view)
                barChart.invalidate()
            }
        })
    }




}