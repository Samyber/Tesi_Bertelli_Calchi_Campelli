package com.samaeli.tesi.Passages

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.samaeli.tesi.DeletePassageAndNotificationService
import com.samaeli.tesi.R
import com.samaeli.tesi.calculationBloodAlcohol.DrinkActivity
import com.samaeli.tesi.databinding.ActivityRequestPassageBinding
import com.samaeli.tesi.databinding.ActivityResultCalculationBinding
import com.samaeli.tesi.models.Passage
import java.io.IOException
import java.util.*
/*
    Activity che ha il compito di far richiedere all'utente un nuovo passaggio
 */
class RequestPassageActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRequestPassageBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object{
        val TAG = "Request Passage Activity"
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
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

        binding = ActivityRequestPassageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        changeIconColor()

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.insert_ride_informations)
        }

        // Mostro DataPicker quando utente clicca su editText dell'ora
        binding.hourEditRequestPassage.inputType = InputType.TYPE_NULL
        binding.hourEditRequestPassage.setOnClickListener {
            Log.d(TAG, "Try to show timePicker")
            val picker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setTitleText(getString(R.string.time_taken_drink))
                    .build()

            picker.show(supportFragmentManager, "")

            // Se l'utente decide di cliccare su ok l'ora inserita viene visualizzata nell'editText
            picker.addOnPositiveButtonClickListener {
                hour = picker.hour
                minute = picker.minute
                var minuteString = minute.toString()
                if (minuteString.length == 1) {
                    minuteString = "0" + minuteString
                }
                Log.d(TAG, hour.toString() + ":" + minuteString)
                binding.hourEditRequestPassage.setText(hour.toString() + ":" + minuteString)
            }
        }

        binding.requirePassageButtonRequestPassage.setOnClickListener{
            error = false // variabile che vale true se si è verificato almeno un errore nei valori inseriti dall'utente
            if(!validateRequest()){
                Log.d(TAG,"ERROR")
                return@setOnClickListener
            }
            Log.d(TAG,"Fields are ok")
            savePassageToFirebase()
        }

        binding.currentLocationPassageButtonRequestPassage.setOnClickListener {
            setDepartureFromCurrentLocation()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    // Metodo che setta come partenza la posizione attuale in cui si trova l'utente
    private fun setDepartureFromCurrentLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // if che viene eseguito se l'app non ha il permesso di accedere alla location del dispositivo
        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        }
        // Si preleva la location del dispositivo
        fusedLocationProviderClient.lastLocation
                .addOnSuccessListener {
                    val geocoder = Geocoder(this,Locale.getDefault())
                    try {
                        // Si preleva l'indirizzo corrispondente alle coordinate a cui si trova il dispositivo
                        val addressList: List<Address> =
                                geocoder.getFromLocation(it.latitude, it.longitude, 1)

                        // Se non è stato trovato un indirizzo si stampa un messaggio di errore
                        if(addressList.size < 1){
                            Toast.makeText(this,getString(R.string.error_location),Toast.LENGTH_LONG).show()
                            return@addOnSuccessListener
                        }
                        val address : Address = addressList.get(0)
                        departureLatitude = it.latitude
                        departureLongitude = it.longitude
                        binding.departureAddressEditRequestPassage.setText(address.thoroughfare+" "+address.subThoroughfare)
                        binding.departureCityEditRequestPassage.setText(address.locality)
                    }catch(e:IOException){
                        Log.d(TAG, "Error: " + e.toString())
                    }
                }
    }

    // Metodo che ha il compito di richiedere all'utente il permesso per poter accedere alla location
    private fun requestLocationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    MY_PERMISSIONS_REQUEST_LOCATION
            )
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    // Metodo che viene eseguitoquando l'utente ha selezionato se dare il permesso oppure no per accedere alla location
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            setDepartureFromCurrentLocation()
        }
    }

    // Metodo che ha il compito di salvare il passaggio su FirebaseDatabase
    private fun savePassageToFirebase(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("passages/$uid")
        val passage = Passage(uid.toString(),departureAddress!!,departureCity!!,arrivalAddress!!,arrivalCity!!,hour!!,minute!!,numberPerson!!,
        departureLatitude!!,departureLongitude!!,arrivalLatitude!!,arrivalLongitude!!,true)
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
        validateTime()
        return !error
    }

    // Metodo che ha il compito di prelevare le coordinate degli indirizzi che sono stati inseriti
    private fun validateDepartureArrival(){
        val geocoder = Geocoder(this, Locale.getDefault())

        try{
            val addressListDeparture: List<Address> = geocoder.getFromLocationName(departureAddress + " " + departureCity, 1)

            // Se non è stato trovato un indirizzo si stampa un messaggio di errore
            if (addressListDeparture.size < 1) {
                Log.d(TAG, "Departure address not found")
                Toast.makeText(this, getString(R.string.error_departure_address), Toast.LENGTH_LONG).show()
                return
            }

            val departureAddressGeocoder = addressListDeparture.get(0)
            departureLatitude = departureAddressGeocoder.latitude
            departureLongitude = departureAddressGeocoder.longitude

            Log.d(TAG, "departure latitude and longitude" + departureLatitude.toString() + " " + departureLongitude.toString())

            val addressListArrival : List<Address> = geocoder.getFromLocationName(arrivalAddress+" "+arrivalCity,1)

            // Se non è stato trovato un indirizzo si stampa un messaggio di errore
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

    // Metodo che controlla che sia stato inserito l'indirizzo di partenza
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

    // Metodo che controlla che sia stata inserita la città di partenza
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

    // Metodo che controlla che sia stato inserito l'indirizzo di arrivo
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

    // Metodo che controlla che sia stata inserita la città di arrivo
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

    // Metodo che controlla che sia stata inserita l'ora
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

    // Metodo che controlla che sia stato inserito il numero di persone e che questo si <= 4
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

    // Controllo che l'utente non possa chiedere un passaggio per un'ora antecedente a quella attule
    private fun validateTime(){
        if(hour == null || minute == null){
            return
        }
        // Ora del passaggio in minuti
        val timeMinute: Int = hour!! * 60 + minute!!
        // Ora Attuale in minuti
        val nowMinute: Int = Calendar.getInstance().get(Calendar.MINUTE) +
                (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60)
        Log.d(TAG, "Time minute: $timeMinute ; Now minute: $nowMinute")
        // Se sono passate le 17 e l'ora del passaggio è entro le 8 lo si può richiedere, se no no
        if(!(timeMinute<8*60 && nowMinute>17*60)) {
            if (nowMinute > timeMinute) {
                error=true
                binding.hourInputLayoutRequestPassage.error = getString(R.string.error_time_passage)
                return
            }
        }
        binding.hourInputLayoutRequestPassage.error = ""
        binding.hourInputLayoutRequestPassage.isErrorEnabled = false
    }

    // Metodo che cambia colore alla startIcon del TextInputLayout quando è evidenziato
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