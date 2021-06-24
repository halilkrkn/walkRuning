package com.example.walkruning.ui.fragments

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.walkruning.R
import com.example.walkruning.adapters.RunningAdapter
import com.example.walkruning.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.walkruning.other.SortType
import com.example.walkruning.other.TrackingUtility
import com.example.walkruning.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_running.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunningFragment:Fragment(R.layout.fragment_running), EasyPermissions.PermissionCallbacks {

    private val viewModel: MainViewModel by viewModels()

    private  lateinit var runningAdapter: RunningAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        floating_btn.setOnClickListener {
            findNavController().navigate(R.id.action_runningFragment_to_trackingFragment)
        }

        requestPermissions()

        // TODO: 24.06.2021 spFilter filtreleme Çubuğunda sıralama işlemlerini ekleyip UI da gösterdik.
        when(viewModel.sortType){
            SortType.DATE -> spFilter.setSelection(0)
            SortType.RUNNING_TIME -> spFilter.setSelection(1)
            SortType.DISTANCE -> spFilter.setSelection(2)
            SortType.AVG_SPEED -> spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> spFilter.setSelection(4)
        }

        // TODO: 24.06.2021  UI da filtreleme yaptıktan sonra eklenilen ögelere göre sıralama için seçme işlemi yapıldı.
        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
              when(position){
                  0 -> viewModel.sortRuns(SortType.DATE)
                  1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                  2 -> viewModel.sortRuns(SortType.DISTANCE)
                  3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                  4 -> viewModel.sortRuns(SortType.CALORIES_BURNED)

              }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }

        // TODO: 23.06.2021
        //  RecyclerView adapter oluşturup RunningFragmentta çalışması için kurulumunu yaptık
        setupRecyclerView()
        // mainViewModel aldığımız runningSortedByDate daki database eklenmiş olan bilgileri runningFragment Uı da listelemek için
        // run diye bir değişken oluşturup tek bir değişken üzerinden değerleri uı de listeledik.
        viewModel.runs.observe(viewLifecycleOwner, Observer {
            runningAdapter.submitList(it)
        })
    }

    // TODO: 23.06.2021 - RecyclerView adapter oluşturup RunningFragmentta çalışması için kurulumunu yaptık.
    private fun setupRecyclerView() = rvRuns.apply {
        runningAdapter = RunningAdapter()
        adapter = runningAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    // Lokasyon izinlerini için
    private fun requestPermissions(){
        if(TrackingUtility.hasLocationPermissions(requireContext())){
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            EasyPermissions.requestPermissions(
                   this,
                    "You need to accept location permissions to use this app",
                    REQUEST_CODE_LOCATION_PERMISSION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    "You need to accept location permissions to use this app",
                    REQUEST_CODE_LOCATION_PERMISSION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        //Bazı izinleri kalıcı olarak reddetmek için
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms)){

            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {}
//    Kullanıcı izin verdiğinde
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }
}