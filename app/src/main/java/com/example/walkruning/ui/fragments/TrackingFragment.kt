package com.example.walkruning.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.walkruning.R
import com.example.walkruning.other.Constants
import com.example.walkruning.other.Constants.ACTION_PAUSE_SERVICE
import com.example.walkruning.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.walkruning.other.Constants.MAP_ZOOM
import com.example.walkruning.other.Constants.POLYLINE_COLOR
import com.example.walkruning.other.Constants.POLYLINE_WIDTH
import com.example.walkruning.other.TrackingUtility
import com.example.walkruning.service.Polyline
import com.example.walkruning.service.TrackingService
import com.example.walkruning.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*

// TODO: 20.06.2021 Tracking Fragment harita üzerinden takip izlence bölümü
@AndroidEntryPoint
class TrackingFragment:Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var map: GoogleMap? = null

    private var currentTimeMillis = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync {
            map = it
            addAllPolylines() // tüm çizgilerin haritada asenkron bir şekilde çalışması için buraya eklendi
        }

        subscribeToObservers()
    }

    private fun subscribeToObservers() {

        // TODO: 20.06.2021  Kullanıcıyı Haritada İzleme ve Takip Etme İşlemleri
        // Kullanıcıları gözlemleme, harita daki kullanıcnı konum biligileri için yazılan fonksiyonların ve Tracking Service in bu fonk da çalıştırılma
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })
        // TODO: 20.06.2021  Kullanıcıyı Haritada İzleme ve Takip Etme İşlemleri
        // Kullanıcıları gözlemleme, harita daki kullanıcnı konum biligileri için yazılan fonksiyonların ve Tracking Service in bu fonk da çalıştırılmasısı
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        // TODO: 21.06.2021 Koronometre İşlemleri
        // Kronometre İşlemleri İçin TrackingService den gerekli işlemleri UI göstermek için
        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeMillis,true)
            tvTimer.text = formattedTime
        })
    }

    // Haritada ki Olay geçişlerini Çalıştırmak için mesela durdurulduğunda hareket de duracak devam ettirildiğinde hareket devam edecek.
    private fun toggleRun(){
        if (isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    // Haritadan Güncel izleme durumuna göre start ve stop butonları ile tracking olaylarını yönetebilme
    private fun updateTracking(isTracking: Boolean)  {
        this.isTracking = isTracking
        if (!isTracking){
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else {
            btnToggleRun.text = "STOP"
            btnFinishRun.visibility = View.GONE

        }
    }

    // Harita üzerindeki konum bilgilerinin çizgilerini  harita kamerası ile takip etme
    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            pathPoints.last().last(),
                            MAP_ZOOM
                    )
            )
        }
    }


    // Tüm çoklu çizgileri kullanıcnıın haritadaki pozisyonlarına göre konum bilgilerini alıp çizdirme.
    private fun addAllPolylines() {
        for(polyline in pathPoints){
            val polylineOptions = PolylineOptions()
                    .color(POLYLINE_COLOR)
                    .width(POLYLINE_WIDTH)
                    .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    // Kullanıcının son harita konum bilgileri için izleme çizgisinin önce ve sonraki konum bilgilerini alıp çizim eklemesi için
    private fun addLatestPolyline(){
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1){
            val preLastLatLng = pathPoints.last()[pathPoints.last().size -2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                    .color(POLYLINE_COLOR)
                    .width(POLYLINE_WIDTH)
                    .add(preLastLatLng)
                    .add(lastLatLng)
            map?.addPolyline(polylineOptions)

        }
    }

//    Oluşturulan Tracking Service sınıfından service'i çekiyoruz ve o service e komut gönderiyoruz. Böylelikle Uygulamada başlama bekleme devam ettirme durdurma işlemleri buradan yönlendiriliyor.
    private fun sendCommandToService(action:String){
        Intent(requireContext(),TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}