package com.example.walkruning.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.walkruning.R
import com.example.walkruning.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.walkruning.other.Constants.KEY_NAME
import com.example.walkruning.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

// TODO: 24.06.2021 Kullanıcı İsmi ve Ağırlığının kullanıcı tarafından alınması ile başka bir fragmentta yönlendirilmesi
@AndroidEntryPoint
class SetupFragment:Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPref : SharedPreferences

    @set:Inject
    var isFirstAppOpen = true



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Uygulama ilk Açıldığında UI lar yani fragmentlar arasında gönderme yapılıyor.
        if (!isFirstAppOpen){
            val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.setupFragment,true)
                    .build()
            findNavController().navigate(
                    R.id.action_setupFragment_to_runningFragment,
                    savedInstanceState,
                    navOptions
            )
        }


        // Uygulama içersinde setup fragmenttan runningFragmenta yönlendirme işlemi yapılıyor ve editText lerde kullanıcının bilgilerinin girilmesi isteniyor.
        tvContinue.setOnClickListener {
            val success = writePersonalDataToSharedPref()
            if (success){
                findNavController().navigate(R.id.action_setupFragment_to_runningFragment)
            } else {
                Snackbar.make(requireView(),"Please enter all the fields", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    // BU kısımda ise kullanıcının setupfragmentta yani uygulama açıldığındaki ilk Uı daki gerekli kısımların doldurulması ve girilen bilgilerin sharedPreferences vasıtasıyla uygulama hafızasına eklenmesi işlemleri.
    private fun writePersonalDataToSharedPref():Boolean{
        val name = etName.text.toString()
        val weight = etWeight.text.toString()
        if (name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPref.edit()
                .putString(KEY_NAME,name)
                .putFloat(KEY_WEIGHT,weight.toFloat())
                .putBoolean(KEY_FIRST_TIME_TOGGLE,false)
                .apply()
//        val toolbarText = "Let's go, $name!"
//        requireActivity().tvToolbarTitle.text = toolbarText
        return true
    }
}