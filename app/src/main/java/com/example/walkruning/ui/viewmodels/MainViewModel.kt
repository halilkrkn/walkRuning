package com.example.walkruning.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.walkruning.db.Running
import com.example.walkruning.repositories.MainRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @ViewModelInject constructor(
        val mainRepository: MainRepository
): ViewModel() {

    // TODO: 23.06.2021 TrackingFragment daki harita bilgilerini database eklemek mainRepositoryden eklemek içinv gerekli fonksiyonu çağırdık ve viewModel sayesinde TrackingFragmentte bu fonksiyonu ekleyip kayıt işlemlerini yaptık.
    fun insertRunning(run: Running) = viewModelScope.launch {
        mainRepository.insertRunning(run)
    }


    // TODO: 23.06.2021 Bu kısımda runningFragment ta database kayıt ettiğimiz bilgileri belli bir sıralamaya göre göstermek için mainViewModel de tanımladık. 
    val runningSortedByDate = mainRepository.getAllRunsSortedByDate()



}
