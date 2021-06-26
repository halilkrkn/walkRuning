package com.example.walkruning.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.walkruning.repositories.MainRepository
import javax.inject.Inject

// TODO: 26.06.2021 StatisticFragmentte istenilen değişkenlerin değerlerini göstermek için mainRepository üzerinden Databaseden çekiyoruz.
class StatisticsViewModel @ViewModelInject constructor(
        val mainRepository: MainRepository
): ViewModel() {

    // Bu kısımda StatisticFragmentte tanımlamaların gösterilmesi için gerekli değişkenlerin  mainRepositoryden Çekiyoruz.
    val totalTimeRun = mainRepository.getTotalTimeInMillis()
    val totalDistance = mainRepository.getTotalDistanceInMeters()
    val totalCaloriesBurned = mainRepository.getTotalCaloriesBurned()
    val totalAvgSpeed = mainRepository.getTotalAvgSpeed()

    val runSortedByDate = mainRepository.getAllRunsSortedByDate()

}
