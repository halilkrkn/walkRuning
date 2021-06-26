package com.example.walkruning.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.walkruning.R
import com.example.walkruning.db.Running
import com.example.walkruning.other.Constants
import com.example.walkruning.other.Constants.ACTION_PAUSE_SERVICE
import com.example.walkruning.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.walkruning.other.Constants.ACTION_STOP_SERVICE
import com.example.walkruning.other.Constants.CANCEL_TRACKING_DIALOG_TAG
import com.example.walkruning.other.Constants.MAP_ZOOM
import com.example.walkruning.other.Constants.POLYLINE_COLOR
import com.example.walkruning.other.Constants.POLYLINE_WIDTH
import com.example.walkruning.other.TrackingUtility
import com.example.walkruning.service.Polyline
import com.example.walkruning.service.TrackingService
import com.example.walkruning.ui.viewmodels.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import javax.inject.Inject
import kotlin.math.round

// TODO: 20.06.2021 Tracking Fragment harita üzerinden takip izlence bölümü
@AndroidEntryPoint
class TrackingFragment:Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var map: GoogleMap? = null

    private var currentTimeMillis = 0L


    private var menu: Menu? = null

    @set:Inject
    var weight = 80f

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnToggleRun.setOnClickListener {
            toggleRun()
        }

        if (savedInstanceState != null){
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(CANCEL_TRACKING_DIALOG_TAG) as CancelTrackingDialog?

            cancelTrackingDialog?.setYesListener {
                stopRun()
            }
        }

        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunSaveToDb()
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

    // TODO: 22.06.2021 TrackingFragment de haritada kullanımınından tamamen çıkmak için cancel menu seçeneğini ekledik
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        if (currentTimeMillis > 0L){
          this.menu?.getItem(0)?.isVisible = true
        }
    }

    private fun showCancelTrackingDialog(){
        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager,CANCEL_TRACKING_DIALOG_TAG)

    }

    private fun stopRun() {
        tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runningFragment)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.miCancelTracking ->{
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)

    }

    // Haritada ki Olay geçişlerini Çalıştırmak için mesela durdurulduğunda hareket de duracak devam ettirildiğinde hareket devam edecek.
    private fun toggleRun(){
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    // Haritadan Güncel izleme durumuna göre start ve stop butonları ile tracking olaylarını yönetebilme
    private fun updateTracking(isTracking: Boolean)  {
        this.isTracking = isTracking
        if (!isTracking && currentTimeMillis > 0L){
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else if (isTracking) {
            btnToggleRun.text = "STOP"
            menu?.getItem(0)?.isVisible = true
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

    // TODO: 23.06.2021 Haritada kullanıcının tüm pozisyonlarını görmek için yaklaştırma işlemi yaptık
     private fun zoomToSeeWholeTrack(){
         val bounds = LatLngBounds.Builder()
         for (polyline in pathPoints){
             for (position in polyline){
                 bounds.include(position)
             }
         }
         map?.moveCamera(
                 CameraUpdateFactory.newLatLngBounds(
                         bounds.build(),
                         mapView.width,
                         mapView.height,
                         (mapView.height * 0.05f).toInt()
                 )
         )
     }

    // TODO: 23.06.2021 Kullanıcı Haritayı çalıştırdığında harita üzerinde çizgiler vasıtasıyla takip edilip oradaki konumu ve yürüme mesafesi üzerinden oluşan bilgileri alıp haritada finish run butonuna tıklandığında harita kullanımını bitirip oluşan bilgileri database ekledik.
    private fun endRunSaveToDb(){
        map?.snapshot { btmp ->

            var distanceMeters = 0
            for(polyline in pathPoints){
                distanceMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed = round((distanceMeters / 1000f)/ (currentTimeMillis / 1000f / 60 / 60) * 10) /10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceMeters / 1000f) * weight).toInt()
            val run = Running(btmp,dateTimestamp,avgSpeed,distanceMeters,currentTimeMillis,caloriesBurned)
            viewModel.insertRunning(run)
            Snackbar.make(
                    requireActivity().findViewById(R.id.rootView),
                    "Run saved succesfully",
                    Snackbar.LENGTH_LONG
            ).show()
            stopRun()
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