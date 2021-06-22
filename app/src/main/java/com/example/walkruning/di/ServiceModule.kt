package com.example.walkruning.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.walkruning.R
import com.example.walkruning.other.Constants
import com.example.walkruning.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped


// TODO: 22.06.2021 Notification (Bildirim) Çubuğunda kronometre sayacının aktifleştirilmesi, bildirim çubuğu üzerinden kronometrenin kullanımı ve Location İşlemleri için Service Modulü oluşturuldu.
@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(
            @ApplicationContext app: Context
    ) = FusedLocationProviderClient(app)



    // app içerisinde tracking fragment içerisindeki harita çalıştırıldığında main activite bunu alıcak sonrasında bildirim çubuğunda gözükmesi sağlanacak
    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
            @ApplicationContext app: Context
    ) =  PendingIntent.getActivity(
            app,
            0,
            Intent(app, MainActivity::class.java).also {
                it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
            },
            PendingIntent.FLAG_UPDATE_CURRENT
    )


    //Notification İşlemleri
    // App içerisindeki haritayı çalıştırdıktan sonra Cihazın bildirim çubuğunda  oluşan uygulama işlemleri kısmı
    // Yani bildirim çubuğunda olması gereken işlemleri inşaa ettik.
    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
            @ApplicationContext app: Context,
            pendingIntent: PendingIntent
    ) =  NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_baseline_directions_run_24)
            .setContentTitle("walkRunning")
            .setContentText("00:00:00")
            .setContentIntent(pendingIntent)


}