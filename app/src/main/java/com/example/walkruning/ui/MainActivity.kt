package com.example.walkruning.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.walkruning.R
import com.example.walkruning.db.RunningDAO
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



       bottomNavigationView.setupWithNavController(navHostFragment.findNavController())

       navHostFragment.findNavController().addOnDestinationChangedListener { controller, destination, arguments ->

           when(destination.id){
               R.id.settingsFragment, R.id.runningFragment, R.id.statisticsFragment ->
                   bottomNavigationView.visibility = View.VISIBLE
               else -> bottomNavigationView.visibility = View.GONE
           }


       }




    }
}