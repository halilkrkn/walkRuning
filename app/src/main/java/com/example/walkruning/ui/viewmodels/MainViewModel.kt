package com.example.walkruning.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walkruning.db.Running
import com.example.walkruning.other.SortType
import com.example.walkruning.repositories.MainRepository
import kotlinx.coroutines.launch
import javax.annotation.meta.When
import javax.inject.Inject

class MainViewModel @ViewModelInject constructor(
        val mainRepository: MainRepository
): ViewModel() {

    // TODO: 23.06.2021 TrackingFragment daki harita bilgilerini database eklemek mainRepositoryden eklemek içinv gerekli fonksiyonu çağırdık ve viewModel sayesinde TrackingFragmentte bu fonksiyonu ekleyip kayıt işlemlerini yaptık.
    fun insertRunning(run: Running) = viewModelScope.launch {
        mainRepository.insertRunning(run)
    }


    // TODO: 23.06.2021 Bu kısımda runningFragment ta database kayıt ettiğimiz bilgileri belli bir sıralamaya göre göstermek için mainViewModel de tanımladık. 
    private val runningSortedByDate = mainRepository.getAllRunsSortedByDate()

    // TODO: 24.06.2021 *************** Sorting Runnings - Sıralama İşlemleri ***********
    private val runningSortedByDistanceInMeters = mainRepository.getAllRunsSortedByDistanceInMeters()
    private val runningSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()
    private val runningSortedByAvgSpeed = mainRepository.getAllRunsSortedByAvgSpeed()
    private val runningSortedByTimeInMillis = mainRepository.getAllRunsSortedByTimeInMillis()


    val runs = MediatorLiveData<List<Running>>()

    var sortType = SortType.DATE

    // init bloğu içerisinde ilk önce runningDao da database eklemek yapmak için oluşturulan sıralamalı değişkenleri mainRepository de tanımladık ve sonra ordan çekip RunningFragmentte göstermek için o değişkenleri değerlerini runs değişkenine atadık.
    init {
        runs.addSource(runningSortedByDate) { result ->
            if (sortType == SortType.DATE){
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runningSortedByDistanceInMeters) { result ->
            if (sortType == SortType.DISTANCE){
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runningSortedByCaloriesBurned) { result ->
            if (sortType == SortType.CALORIES_BURNED){
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runningSortedByAvgSpeed) { result ->
            if (sortType == SortType.AVG_SPEED){
                result?.let {
                    runs.value = it
                }
            }
        }

        runs.addSource(runningSortedByTimeInMillis) { result ->
            if (sortType == SortType.RUNNING_TIME){
                result?.let {
                    runs.value = it
                }
            }
        }
    }

    // SortType adında enum class oluşturup bir sıralama koşulu oluşturuldu.
    fun sortRuns(sortType: SortType){
        when(sortType) {
            SortType.DATE -> runningSortedByDate.value?.let {runs.value = it}
            SortType.RUNNING_TIME -> runningSortedByTimeInMillis.value?.let {runs.value = it}
            SortType.AVG_SPEED -> runningSortedByAvgSpeed.value?.let {runs.value = it}
            SortType.DISTANCE -> runningSortedByDistanceInMeters.value?.let {runs.value = it}
            SortType.CALORIES_BURNED -> runningSortedByCaloriesBurned.value?.let {runs.value = it}
        }.also {
            this.sortType = sortType
        }
    }



}
