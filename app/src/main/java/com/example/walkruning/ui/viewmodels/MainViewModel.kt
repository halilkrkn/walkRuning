package com.example.walkruning.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.walkruning.repositories.MainRepository
import javax.inject.Inject

class MainViewModel @ViewModelInject constructor(
        val mainRepository: MainRepository
): ViewModel() {
}
