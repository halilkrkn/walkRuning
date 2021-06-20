package com.example.walkruning.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.example.walkruning.R
import com.example.walkruning.other.Constants.ACTION_PAUSE_SERVICE
import com.example.walkruning.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.walkruning.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.walkruning.other.Constants.ACTION_STOP_SERVICE
import com.example.walkruning.other.Constants.NOTIFICATION_CHANEL_ID
import com.example.walkruning.other.Constants.NOTIFICATION_CHANEL_NAME
import com.example.walkruning.other.Constants.NOTIFICATION_ID
import com.example.walkruning.ui.MainActivity
import timber.log.Timber

class TrackingService: LifecycleService() {

    var isFirstRun = true

//    Tracking service i başlatma komutu
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                // Uygulama başladığında ve devam ettirildiğinde
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun){
                        startForegroundService()
                        isFirstRun = false
                    } else{
                        Timber.d("Resuming Service")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused Service")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped Service")
                }
            }
        }


        return super.onStartCommand(intent, flags, startId)
    }

//    Ön Baslatma Hizmeti yani Başlata tıklandığında app bildirim çubuğunda da gözükecek ve yönetilecek.
    private fun startForegroundService(){
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

    //Notification İşlemleri
    // App içerisindeki haritayı çalıştırdıktan sonra Cihazın bildirim çubuğunda  oluşan uygulama işlemleri kısmı
    // Yani bildirim çubuğunda olması gereken işlemleri inşaa ettik.
    val notificationBuilder =  NotificationCompat.Builder(this, NOTIFICATION_CHANEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_directions_run_24)
            .setContentTitle("walkRunning")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())


    startForeground(NOTIFICATION_ID,notificationBuilder.build())
    }


    // app içerisinde tracking fragment içerisindeki harita çalıştırıldığında main activite bunu alıcak sonrasında bildirim çubuğunda gözükmesi sağlanacak
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).also {
                it.action = ACTION_SHOW_TRACKING_FRAGMENT
            },
            FLAG_UPDATE_CURRENT
    )



    //Notification (Bildirim Çubuğu) Oluşturuyoruz.
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
                NOTIFICATION_CHANEL_ID,
                NOTIFICATION_CHANEL_NAME,
                IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}