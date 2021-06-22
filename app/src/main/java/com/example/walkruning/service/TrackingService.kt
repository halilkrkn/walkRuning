package com.example.walkruning.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.walkruning.R
import com.example.walkruning.other.Constants.ACTION_PAUSE_SERVICE
import com.example.walkruning.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.walkruning.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.walkruning.other.Constants.ACTION_STOP_SERVICE
import com.example.walkruning.other.Constants.FASTEST_LOCATION_INTERVAL
import com.example.walkruning.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.walkruning.other.Constants.NOTIFICATION_CHANEL_ID
import com.example.walkruning.other.Constants.NOTIFICATION_CHANEL_NAME
import com.example.walkruning.other.Constants.NOTIFICATION_ID
import com.example.walkruning.other.Constants.TIMER_UPDATE_INTERVAL
import com.example.walkruning.other.TrackingUtility
import com.example.walkruning.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService: LifecycleService() {

    var isFirstRun = true
    var serviceKilled = false

    // Konum Sağlayıcısı
    @Inject
    lateinit var  fusedLocationProviderClient: FusedLocationProviderClient

    // TODO: 22.06.2021   Notification İşlemleri için
    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder
    lateinit var currentNotificationBuilder: NotificationCompat.Builder


    private val timeRunInSeconds = MutableLiveData<Long>()

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    // Haritada kullanıcının ilk harita bilgilerini göndermek için gerekli değerleri tanımlıyoruz.
    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }


    // Location Sağlayıcı biligilerini Bilgileri ve TrackingFragment de çalışması için
    override fun onCreate() {
        super.onCreate()
        currentNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })


    }

    // TODO: 22.06.2021 Harita kullanımından tamamen çıkıp tüm service işlemlerinden çıkmak için
    private fun killService() {
        serviceKilled = true
        isTimerEnabled = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }


    //    Tracking service i başlatma komutu. Başlatma, Devam Ettirilme, Bekletme, Durdurma işlemleri intent sayesinde uı da yönlendirmeler yapılıyor.
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
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused Service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped Service")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    // TODO: 21.06.2021 ****** KRONOMETRE ISLEMLERI **********
    // TrackingUtility de ise Kronometre kurulum işlemleri yapıldı.
    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L
    
    // Kronometreyi Başlattıldığında süreyi saydırmak için.
    private fun startTimer(){

        addEmptyPolyLine()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
                // time difference between now and timeStarted - şimdi ile timeStarted arasındaki zaman farkı
                lapTime = System.currentTimeMillis() - timeStarted
                // post the new lapTime - yeni tur zamanını gönder
                timeRunInMillis.postValue(timeRun + lapTime)

                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }


// TODO: 20.06.2021  ************************** NOTIFICATION İŞLEMLERİ ****************************************

    // TODO: 22.06.2021  Notification (Bildirim) Çubuğunda kronometre sayacının aktifleştirilmesi ve bildirim çubuğu üzerinden koronomtrenin kontrolü yapıldı.
    private fun updateNotificationTrackingState(isTracking: Boolean){
        val notificationActionText = if (isTracking) "Pause" else "Resume"

        val pendingIntent = if (isTracking){
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this,1,pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this,2,resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotificationBuilder,ArrayList<NotificationCompat.Action>())
        }

        if (!serviceKilled){
            currentNotificationBuilder = baseNotificationBuilder
                    .addAction(R.drawable.ic_baseline_motion_photos_paused_24, notificationActionText,pendingIntent)
            notificationManager.notify(NOTIFICATION_ID,currentNotificationBuilder.build())
        }
    }


    //    Ön Baslatma Hizmeti yani Başlata tıklandığında app bildirim çubuğunda da gözükecek ve yönetilecek.
//    Başlata tıklandığında harita da kullanıcının durumuna göre haritada izleyecek
    private fun startForegroundService(){

        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

    startForeground(NOTIFICATION_ID,baseNotificationBuilder.build())


    // TODO: 22.06.2021 uygulamada tracking işlemi başlatıldığında notficationda e çalışıp sürecinin akması için
    timeRunInSeconds.observe(this, Observer {

        if (!serviceKilled) {
            val notification = currentNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
            notificationManager.notify(NOTIFICATION_ID, notification.build())
        }
    })
    }


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


// TODO: 20.06.2021  ************************** LOCATION İŞLEMLERİ ****************************************

    // Location bilgilerini güncel olarak izlemek için yapıldı.
    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean){

        if (isTracking){
            if (TrackingUtility.hasLocationPermissions(this)){
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                        request,
                        locationCallback,
                        Looper.getMainLooper()
                )
            }
        }else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }



    // KUllanıcının Konum Bilgilerini geri çağırıp(alıp) anlık olarak addPathPoint e ekliyoruz.
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)

            if (isTracking.value!!){
                result?.locations?.let { locations ->
                    for (location in locations){
                        addPathPoint(location)
                        Timber.d("NEW LOCATION:${location.latitude}, ${location.longitude}")
                    }

                }
            }
        }
    }

    // kullanıcının position ını alıp harita da konum bilgsini alıyoruz
    private fun addPathPoint(location: Location?){

        location?.let {
            val position = LatLng(location.latitude,location.longitude)
            pathPoints.value?.apply {
                last().add(position)
                pathPoints.postValue(this)
            }
        }


    }
    // İLk önce boş bir çoklu çizgi ekliyoruz.
    private fun addEmptyPolyLine() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))


}