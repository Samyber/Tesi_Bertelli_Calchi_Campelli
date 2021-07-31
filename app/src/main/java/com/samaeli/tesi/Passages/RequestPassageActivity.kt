package com.samaeli.tesi.Passages

import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.samaeli.tesi.R
import com.samaeli.tesi.calculationBloodAlcohol.DrinkActivity
import com.samaeli.tesi.databinding.ActivityRequestPassageBinding
import com.samaeli.tesi.databinding.ActivityResultCalculationBinding
import com.samaeli.tesi.models.Passage
import java.io.IOException
import java.util.*

class RequestPassageActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRequestPassageBinding

    companion object{
        val TAG = "Request Passage Activity"
    }

    private var error : Boolean = false
    
    private var hour : Int? = null
    private var minute : Int? = null
    private var departureAddress : String? = null
    private var departureCity : String? = null
    private var arrivalAddress : String? = null
    private var arrivalCity : String? = null
    private var numberPerson : Int? = null
    private var departureLatitude : Double? = null
    private var departureLongitude : Double? = null
    private var arrivalLatitude : Double? = null
    private var arrivalLongitude : Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_request_passage)

        binding = ActivityRequestPassageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        changeIconColor()

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.insert_ride_informations)
        }

        binding.hourEditRequestPassage.inputType = InputType.TYPE_NULL
        binding.hourEditRequestPassage.setOnClickListener {
            Log.d(DrinkActivity.TAG, "Try to show timePicker")
            val picker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setTitleText(getString(R.string.time_taken_drink))
                    .build()

            picker.show(supportFragmentManager, "")

            picker.addOnPositiveButtonClickListener {
                hour = picker.hour
                minute = picker.minute
                var minuteString = minute.toString()
                if (minuteString.length == 1) {
                    minuteString = "0" + minuteString
                }
                Log.d(DrinkActivity.TAG, hour.toString() + ":" + minuteString)
                binding.hourEditRequestPassage.setText(hour.toString() + ":" + minuteString)
            }
        }

        binding.requirePassageButtonRequestPassage.setOnClickListener{
            error = false
            if(!validateRequest()){
                Log.d(TAG,"ERROR")
                return@setOnClickListener
            }
            Log.d(TAG,"Fields ok")
            savePassageToFirebase()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun savePassageToFirebase(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("passages/$uid")
        val passage = Passage(departureAddress!!,departureCity!!,arrivalAddress!!,arrivalCity!!,hour!!,minute!!,numberPerson!!,
        departureLatitude!!,departureLongitude!!,arrivalLatitude!!,arrivalLongitude!!)
        ref.setValue(passage)
                .addOnSuccessListener {
                    Toast.makeText(this,getString(R.string.passage_created),Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener {
                    Log.d(TAG,"Caricamento su firebase non è andato a buon fine")
                }
    }
    
    private fun validateRequest():Boolean{
        validateDepartureAddress()
        validateDepartureCity()
        validateArrivalAddress()
        validateArrivalCity()
        validateHour()
        validateNumberPerson()
        validateDepartureArrival()
        return !error
    }


    private fun validateDepartureArrival(){
        val geocoder = Geocoder(this, Locale.getDefault())

        try{

            val addressListDeparture : List<Address> = geocoder.getFromLocationName(departureAddress+" "+departureCity,1)

            if(addressListDeparture.size < 1){
                Log.d(TAG,"Departure address not found")
                Toast.makeText(this,getString(R.string.error_departure_address),Toast.LENGTH_LONG).show()
                return
            }

            val departureAddressGeocoder = addressListDeparture.get(0)
            departureLatitude = departureAddressGeocoder.latitude
            departureLongitude = departureAddressGeocoder.longitude
            Log.d(TAG,"departure latitude and longitude"+departureLatitude.toString()+" "+departureLongitude.toString())

            val addressListArrival : List<Address> = geocoder.getFromLocationName(arrivalAddress+" "+arrivalCity,1)

            if(addressListArrival.size < 1){
                Log.d(TAG,"Arrival address not found")
                Toast.makeText(this,getString(R.string.error_arrival_address),Toast.LENGTH_LONG).show()
                return
            }

            val arrivalAddressGeocoder = addressListArrival.get(0)
            arrivalLatitude = arrivalAddressGeocoder.latitude
            arrivalLongitude = arrivalAddressGeocoder.longitude
            Log.d(TAG,"arrival latitude and longitude"+arrivalLatitude.toString()+" "+arrivalLongitude.toString())

        }catch (e: IOException){
            Log.d(TAG,"Error Geocoder")
        }
    }
    
    private fun validateDepartureAddress(){
        departureAddress = binding.departureAddressEditRequestPassage.text.toString()

        if(departureAddress==null || departureAddress!!.isEmpty() || departureAddress!!.isBlank()) {
            binding.departureAddressInputLayoutRequestPassage.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.departureAddressInputLayoutRequestPassage.error = ""
        binding.departureAddressInputLayoutRequestPassage.isErrorEnabled = false
    }

    private fun validateDepartureCity(){
        departureCity = binding.departureCityEditRequestPassage.text.toString()

        if(departureCity==null || departureCity!!.isEmpty() || departureCity!!.isBlank()) {
            binding.departureCityInputLayoutRequestPassage.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.departureCityInputLayoutRequestPassage.error = ""
        binding.departureCityInputLayoutRequestPassage.isErrorEnabled = false
    }


    private fun validateArrivalAddress(){
        arrivalAddress = binding.arrivalAddressEditRequestPassage.text.toString()

        if(arrivalAddress==null || arrivalAddress!!.isEmpty() || arrivalAddress!!.isBlank()) {
            binding.arrivalAddressInputLayoutRequestPassage.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.arrivalAddressInputLayoutRequestPassage.error = ""
        binding.arrivalAddressInputLayoutRequestPassage.isErrorEnabled = false
    }

    private fun validateArrivalCity(){
        arrivalCity = binding.arrivalCityEditRequestPassage.text.toString()

        if(arrivalCity==null || arrivalCity!!.isEmpty() || arrivalCity!!.isBlank()) {
            binding.arrivalCityInputLayoutRequestPassage.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.arrivalCityInputLayoutRequestPassage.error = ""
        binding.arrivalCityInputLayoutRequestPassage.isErrorEnabled = false
    }

    private fun validateHour(){
        var hour = binding.hourEditRequestPassage.text.toString()

        if(hour==null || hour!!.isEmpty() || hour!!.isBlank()) {
            binding.hourInputLayoutRequestPassage.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.hourInputLayoutRequestPassage.error = ""
        binding.hourInputLayoutRequestPassage.isErrorEnabled = false
    }

    private fun validateNumberPerson(){
        var numberPersonString = binding.numberPersonEditRequestPassage.text.toString()

        if(numberPersonString==null || numberPersonString!!.isEmpty() || numberPersonString!!.isBlank()) {
            binding.numberPersonInputLayoutRequestPassage.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        numberPerson = numberPersonString.toInt()
        if(numberPerson!! > 4){
            binding.numberPersonInputLayoutRequestPassage.error = getString(R.string.error_number_person)
            error = true
            return
        }
        binding.numberPersonInputLayoutRequestPassage.error = ""
        binding.numberPersonInputLayoutRequestPassage.isErrorEnabled = false
    }

    private fun changeIconColor() {
        binding.departureAddressEditRequestPassage.setOnFocusChangeListener { v, hasFocus ->
            val color = if (hasFocus) Color.rgb(249, 170, 51) else Color.rgb(52, 73, 85)
            binding.departureAddressInputLayoutRequestPassage.setStartIconTintList(ColorStateList.valueOf(color))
        }
        binding.departureCityEditRequestPassage.setOnFocusChangeListener { v, hasFocus ->
            val color = if (hasFocus) Color.rgb(249, 170, 51) else Color.rgb(52, 73, 85)
            binding.departureCityInputLayoutRequestPassage.setStartIconTintList(ColorStateList.valueOf(color))
        }
        binding.arrivalAddressEditRequestPassage.setOnFocusChangeListener { v, hasFocus ->
            val color = if (hasFocus) Color.rgb(249, 170, 51) else Color.rgb(52, 73, 85)
            binding.arrivalAddressInputLayoutRequestPassage.setStartIconTintList(ColorStateList.valueOf(color))
        }
        binding.arrivalCityEditRequestPassage.setOnFocusChangeListener { v, hasFocus ->
            val color = if (hasFocus) Color.rgb(249, 170, 51) else Color.rgb(52, 73, 85)
            binding.arrivalCityInputLayoutRequestPassage.setStartIconTintList(ColorStateList.valueOf(color))
        }
        binding.numberPersonEditRequestPassage.setOnFocusChangeListener { v, hasFocus ->
            val color = if (hasFocus) Color.rgb(249, 170, 51) else Color.rgb(52, 73, 85)
            binding.numberPersonInputLayoutRequestPassage.setStartIconTintList(ColorStateList.valueOf(color))
        }
    }
}