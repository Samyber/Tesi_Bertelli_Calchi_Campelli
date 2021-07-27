package com.samaeli.tesi

import android.content.SharedPreferences
import android.os.Bundle
import android.os.LimitExceededException
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import java.lang.Double.parseDouble
import java.lang.NumberFormatException

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction().replace(android.R.id.content, SettingsFragment()).commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val prefLimit : EditTextPreference? = findPreference("limit_edit_text_preference")
            val prefLimitNewDriver : EditTextPreference? = findPreference("limit_new_driver_edit_text_preference")

            if(prefLimit?.text.isNullOrEmpty() || prefLimitNewDriver?.text.isNullOrEmpty()){
                PreferenceManager.setDefaultValues(activity,R.xml.root_preferences,false)
            }

            createListenerLimit(prefLimit,prefLimitNewDriver)
        }

        // Metodo eseguito se il valore inserito dall'utente come limite di tasso alcolemico non è valido
        private fun errorPrefLimit(prefLimit : EditTextPreference?){
            prefLimit?.text = "0.5"
            Toast.makeText(activity,getString(R.string.error_limit_pref),Toast.LENGTH_LONG).show()
        }

        private fun errorPrefLimitNewDriver(prefLimitNewDriver: EditTextPreference?){
            prefLimitNewDriver?.text = "0.0"
            Toast.makeText(activity,getString(R.string.error_limit_pref),Toast.LENGTH_LONG).show()
        }

        // Metodo che permette di controllare che il valore inserito dall'utente come limite di tasso alcolemico sia valido
        private fun createListenerLimit(prefLimit : EditTextPreference?, prefLimitNewDriver: EditTextPreference?){
            val listener = object : SharedPreferences.OnSharedPreferenceChangeListener{
                override fun onSharedPreferenceChanged(
                    sharedPreferences: SharedPreferences?,
                    key: String?
                ) {
                    val limitPrefValue = sharedPreferences?.getString("limit_edit_text_preference","")
                    if(limitPrefValue.isNullOrEmpty() || limitPrefValue.toString()!!.isBlank()){
                        errorPrefLimit(prefLimit)
                    }
                    try {
                        val num = parseDouble(limitPrefValue)
                        if(num < 0){
                            errorPrefLimit(prefLimit)
                        }
                    }catch (e:NumberFormatException){
                        errorPrefLimit(prefLimit)
                    }
                    val limitNewDriverPrefValue = sharedPreferences?.getString("limit_new_driver_edit_text_preference","")
                    if(limitNewDriverPrefValue.isNullOrEmpty() || limitNewDriverPrefValue.toString()!!.isBlank()){
                        errorPrefLimitNewDriver(prefLimitNewDriver)
                    }
                    try {
                        val num = parseDouble(limitNewDriverPrefValue)
                        if(num < 0){
                            errorPrefLimitNewDriver(prefLimitNewDriver)
                        }
                    }catch (e:NumberFormatException){
                        errorPrefLimitNewDriver(prefLimitNewDriver)
                    }
                }

            }
            PreferenceManager.getDefaultSharedPreferences(activity).registerOnSharedPreferenceChangeListener(listener)
        }

        /*private fun createListenerLimitNewDriver(prefLimitNewDriver : EditTextPreference?){
            val listener = object : SharedPreferences.OnSharedPreferenceChangeListener{
                override fun onSharedPreferenceChanged(
                        sharedPreferences: SharedPreferences?,
                        key: String?
                ) {
                    val limitPrefValue = sharedPreferences?.getString("limit_edit_text_preference","")
                    if(limitPrefValue.isNullOrEmpty() || limitPrefValue.toString()!!.isBlank()){
                        errorPrefLimit(prefLimit)
                    }
                    try {
                        val num = parseDouble(limitPrefValue)
                        if(num < 0){
                            errorPrefLimit(prefLimit)
                        }
                    }catch (e:NumberFormatException){
                        errorPrefLimit(prefLimit)
                    }
                }

            }
            PreferenceManager.getDefaultSharedPreferences(activity).registerOnSharedPreferenceChangeListener(listener)
        }*/
    }
}