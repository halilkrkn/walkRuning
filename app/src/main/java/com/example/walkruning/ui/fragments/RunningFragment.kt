package com.example.walkruning.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.walkruning.R
import com.example.walkruning.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunningFragment:Fragment(R.layout.fragment_running) {

    private val viewModel: MainViewModel by viewModels()
}