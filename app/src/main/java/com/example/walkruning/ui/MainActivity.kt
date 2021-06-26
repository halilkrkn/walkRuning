package com.example.walkruning.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.walkruning.R
import com.example.walkruning.db.RunningDAO
import com.example.walkruning.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //TrackingFragment UI ine gitmesi için intent i fonksiyona gönderdik.
        navigateToTrackingFragmentIfNeeded(intent)

       bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
       bottomNavigationView.setOnNavigationItemReselectedListener { /* NO-OP*/ }

       navHostFragment.findNavController().addOnDestinationChangedListener { controller, destination, arguments ->

           when(destination.id){
               R.id.settingsFragment, R.id.runningFragment, R.id.statisticsFragment ->
                   bottomNavigationView.visibility = View.VISIBLE
               else -> bottomNavigationView.visibility = View.GONE
           }


       }
    }

    // TODO: 20.06.2021 Notification İşlemleri için
    //Uygulama kapatıldığında yine arkaplan çalışacak ve bildirim çubuğundan aktif olarak gözükecek.
    // Bildirim çubupundaki app uygulamasını tıklandığında uygulama tekrar çalışacak yani TrackingFragment UI ına gidecek
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    // Gerektiğinde Tracking Fragmentine Gitmek için
    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?){
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT){
            navHostFragment.findNavController().navigate(R.id.action_global_trackingFragment)
        }
    }
}