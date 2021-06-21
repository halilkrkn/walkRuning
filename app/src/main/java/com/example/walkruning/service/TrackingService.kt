package com.example.walkruning.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>
class TrackingService: LifecycleService() {

    var isFirstRun = true

    // Konum Sağlayıcısı
    lateinit var  fusedLocationProviderClient: FusedLocationProviderClient

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
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })


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
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
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

//    Ön Baslatma Hizmeti yani Başlata tıklandığında app bildirim çubuğunda da gözükecek ve yönetilecek.
//    Başlata tıklandığında harita da kullanıcının durumuna göre haritada izleyecek
    private fun startForegroundService(){

        startTimer()
        isTracking.postValue(true)

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