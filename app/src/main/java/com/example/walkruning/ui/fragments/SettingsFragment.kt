package com.example.walkruning.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.walkruning.R
import com.example.walkruning.other.Constants.KEY_NAME
import com.example.walkruning.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

// TODO: 26.06.2021 SettingFragmentte kullanıcının setupFragmentte girmiş olduğu name ve weight değerlerinin güncelleyebiliyoruz.
@AndroidEntryPoint
class SettingsFragment:Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Oluşturudpumuz loadFieldsFromSharedPref fonksiyonunu Uı da çalıştırmak için çağırıyoruz.
        loadFieldsFromSharedPref()

        // GÜncelleme yapmak için Apply Changes butonuna tıklandığında olmasını istediğimiz komutları yapmasını sağlıyoruz.
        btnApplyChanges.setOnClickListener {
            val succes = applyChangesToSharedPref()
            if (succes){
                Snackbar.make(view,"Saved Changes", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(view,"Please fill out all the fields", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    // Burda nameText ve WeightText editTextlerine kullanıcı sayesinde bilgilerini güncellemek için bu değerleri tekrardan girip uygulama hafızasına kayıt edip kullanıcı bilgilerini güncelliyoruz.
    private fun applyChangesToSharedPref(): Boolean {
        val nameText = etName.text.toString()
        val weightText = etWeight.text.toString()
        if (nameText.isEmpty() || weightText.isEmpty()) {
            return false
        }
        sharedPreferences.edit()
                .putString(KEY_NAME,nameText)
                .putFloat(KEY_WEIGHT,weightText.toFloat())
                .apply()

//        val toolbarText = "Let's go, $name!"
//        requireActivity().tvToolbarTitle.text = toolbarText
        return true
    }

    // applyChangesToSharedPref de kullanıcı bilgilerini güncelledikten sonra o alandaki verileri SharedPreferences a kayıt ettiğimiz verileri çekip yüklüyoruz.
    private fun loadFieldsFromSharedPref(){
        val name = sharedPreferences.getString(KEY_NAME,"")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT,80f)
        etName.setText(name)
        etWeight.setText(weight.toString())


    }

}