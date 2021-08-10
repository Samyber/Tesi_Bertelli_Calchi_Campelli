package com.samaeli.tesi.Passages

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.samaeli.tesi.LoadingDialog
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.ActivityPassageSummaryBinding
import com.samaeli.tesi.models.Offer
import com.samaeli.tesi.models.Passage
import com.samaeli.tesi.models.User
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_passage_provide.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Year
import java.time.temporal.ChronoUnit
import java.util.*

class PassageSummaryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPassageSummaryBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object{
        val TAG = "Passage Summary Activity"
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }

    private var passage : Passage? = null
    private var loadingDialog : LoadingDialog? = null

    private var day : String? = null
    private var month : String? = null
    private var year : String? = null
    private var price : Double? = null
    private var offer : Offer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPassageSummaryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        loadingDialog = LoadingDialog(this)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.passage)
        }

        // Si preleva il passaggio dall'intent
        passage = intent.getParcelableExtra(PassageProvideActivity.PASSAGE_KEY)

        if(passage == null){
            finish()
        }

        // Si mostra su Google Maps navigazione tra punto di partenza e punto di arrivo
        binding.showRouteButtonPassageSummary.setOnClickListener {
            val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?saddr=${passage!!.departureLatitude},${passage!!.departureLongitude}" +
                            "&daddr=${passage!!.arrivalLatitude},${passage!!.arrivalLongitude}")
            )
            startActivity(intent)
        }

        binding.goToDepartureButtonPassageSummary.setOnClickListener {
            showRouteToDeparture()
        }

        binding.offerPassageButtonPassageSummary.setOnClickListener {
            if(validatePriceField()){
                Log.d(TAG,"Price is ok")
                checkIfPassageExist()
            }else{
                Log.d(TAG,"Error price")
            }
        }

        // Quuando l'utente clicca questo bottone bisogna rimuovere l'offerta che ha fatto
        binding.withdrawOfferButtonPassageSummary.setOnClickListener {
            val uidBidder = FirebaseAuth.getInstance().uid
            val uidRequester = passage!!.uid
            val ref = FirebaseDatabase.getInstance().getReference("made_offers/$uidBidder/$uidRequester")
            ref.removeValue()
            val ref2 = FirebaseDatabase.getInstance().getReference("received_offers/$uidRequester/$uidBidder")
            ref2.removeValue()
            val ref3 = FirebaseDatabase.getInstance().getReference("withdraw_offers/$uidRequester")
            ref3.setValue(offer)
            finish()
        }

        completeUserFields()
        completePassageFields()
        blockMadeOffer()
        completeOfferFields()
        checkRequestPassage()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    // Metodo che controlla se il passaggio è ancora presente oppure se è stato eliminato
    private fun checkIfPassageExist(){
        val ref = FirebaseDatabase.getInstance().getReference("passages/${passage!!.uid}")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val passage = snapshot.getValue(Passage::class.java)
                    if(passage!!.visibility == false){
                        Toast.makeText(applicationContext,getString(R.string.error_passage_removed),Toast.LENGTH_LONG).show()
                        finish()
                    }
                    // Offerta caricata su firebase solo se il passaggio esiste ed è visibile
                    uploadOfferToFirebase()
                }else{
                    Toast.makeText(applicationContext,getString(R.string.error_passage_removed),Toast.LENGTH_LONG).show()
                    finish()
                }
                ref.removeEventListener(this)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    // Metodo che permette di caricare l'offerta fatta su firebase
    private fun uploadOfferToFirebase(){
        // Si inizia il dialog del loading
        loadingDialog!!.startLoadingDialog()
        val uidBidder = FirebaseAuth.getInstance().uid
        val uidRequester = passage!!.uid

        // Creazione dell'offerta
        val offer = Offer(uidBidder!!,uidRequester,price!!,Offer.WAIT,true)

        val ref = FirebaseDatabase.getInstance().getReference("made_offers/$uidBidder/$uidRequester")
        ref.setValue(offer)
                .addOnSuccessListener {
                    Log.d(TAG,"Offerta caricata in made_offers/$uidBidder/$uidRequester")
                    val ref2 = FirebaseDatabase.getInstance().getReference("received_offers/$uidRequester/$uidBidder")
                    ref2.setValue(offer)
                            .addOnSuccessListener {
                                Log.d(TAG,"Offerta caricata in received_offers/$uidRequester/$uidBidder")
                                loadingDialog!!.dismissLoadingDialog()
                                Toast.makeText(this,getString(R.string.offer_created),Toast.LENGTH_LONG).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this,getString(R.string.error_create_offer),Toast.LENGTH_LONG).show()
                            }
                }
                .addOnFailureListener {
                    Toast.makeText(this,getString(R.string.error_create_offer),Toast.LENGTH_LONG).show()
                }
        val ref2 = FirebaseDatabase.getInstance().getReference("wait_offers/$uidRequester")
        ref2.setValue(offer)
                .addOnSuccessListener {
                    Log.d(TAG,"Offerta caricata in wait_offers")
                }
    }

    //Metodo che ha il compito di visualizzare su Google maps la anavigazione dal punto in cui si trova
    // l'utente al punto di partenza del passaggio
    private fun showRouteToDeparture(){
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
                    val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?saddr=${it.latitude},${it.longitude}" +
                                    "&daddr=${passage!!.departureLatitude},${passage!!.departureLongitude}")
                    )
                    startActivity(intent)
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

    // Funzione eseguita quando l'utente da il permesso oppure no all'accesso alla posizione
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            showRouteToDeparture()
        }
    }

    // Metodo che ha il compito di riempire i campi con le informazioni dell'utente che ha richiesto il passaggio
    private fun completeUserFields(){
        val ref = FirebaseDatabase.getInstance().getReference("users/${passage!!.uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                binding.nameUserTextViewPassageSummary.text = user!!.name+" "+user!!.surname

                // Calcolo dell'età dell'utente
                getDate(user.birthdayDate)
                val now : LocalDate = LocalDate.now()
                val birthday = LocalDate.of(year!!.toInt() ,month!!.toInt(),day!!.toInt())
                val age = ChronoUnit.YEARS.between(birthday,now)
                Log.d(TAG,"The age is $age")
                binding.ageUserTextViewPassageSummary.text = getString(R.string.age)+": "+age.toString()

                binding.fidelityPointsTextViewPassageSummary.text = getString(R.string.fidelity_points)+": "+user.points

                if(user.gender.equals("male")){
                    binding.genderTextViewPassageSummary.text = "M"
                    binding.genderTextViewPassageSummary.setTextColor(Color.BLUE)
                }else{
                    binding.genderTextViewPassageSummary.text = "F"
                    binding.genderTextViewPassageSummary.setTextColor(Color.rgb(255,179,222))
                }

                if(!user.profileImageUrl.isNullOrEmpty()) {
                    val url = user.profileImageUrl
                    val targetImageView = binding.photoCircleImageViewPassageSummary
                    Picasso.get().load(url).into(targetImageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    // Metodo che ha il compito di riempire i campi con i dati del passaggio
    private fun completePassageFields(){
        var minuteString = passage!!.minute.toString()
        if(minuteString.length == 1){
            minuteString = "0"+minuteString
        }
        binding.hourTextViewPassageSummary.text = getString(R.string.hour)+": "+passage!!.hour+":"+minuteString
        binding.numberPersonTextViewPassageSummary.text = getString(R.string.number_person)+": "+passage!!.numberPerson

        binding.departureAddressTextViewPassageSummary.text = getString(R.string.address)+": "+passage!!.departureAddress
        binding.departureCityTextViewPassageSummary.text = getString(R.string.city)+": "+passage!!.departureCity

        binding.arrivalAddressTextViewPassageSummary.text = getString(R.string.address)+": "+passage!!.arrivalAddress
        binding.arrivalCityTextViewPassageSummary.text = getString(R.string.city)+": "+passage!!.arrivalCity
    }

    // Metodo che ha il compito di riempire i campi con i dati dell'ultima offerta fatta (se presente)
    private fun completeOfferFields(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("made_offers/$uid/${passage!!.uid}")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    offer = snapshot.getValue(Offer::class.java)

                    // In base allo stato dell'ooferta di decidono quali elementi devono essere visibili e quali no
                    if(offer!!.state.equals(Offer.ACCEPTED)){
                        binding.priceInputLayoutPassageSummary.visibility = View.INVISIBLE
                        binding.offerPassageButtonPassageSummary.visibility = View.INVISIBLE
                        binding.offerLinearLayoutPassageSummary.visibility = View.VISIBLE
                    }

                    if(offer!!.state.equals(Offer.WAIT)){
                        binding.withdrawOfferButtonPassageSummary.visibility = View.VISIBLE
                    }else{
                        binding.withdrawOfferButtonPassageSummary.visibility = View.INVISIBLE
                    }

                    if(offer!!.state.equals(Offer.DECLINED)){
                        binding.priceInputLayoutPassageSummary.visibility = View.VISIBLE
                        binding.offerPassageButtonPassageSummary.visibility = View.VISIBLE
                    }

                    binding.lastPriceTextViewPassageSummary.text = getString(R.string.last_price)+": "+offer!!.price+"€"
                    binding.stateTextViewPassageSummary.text = getString(R.string.state)+": "+offer!!.state
                    binding.offerLinearLayoutPassageSummary.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    // Metodo che ha il compito di controllare che l'utente non possa fare un'offerta per un passaggio
    // se ha già fatto un'offerta per un altro passaggio che è nello stato di wait
    private fun blockMadeOffer(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("made_offers/$uid/")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    snapshot.children.forEach {
                        val offer = it.getValue(Offer::class.java)
                        if(offer!!.state.equals(Offer.WAIT)){
                            binding.priceInputLayoutPassageSummary.visibility = View.INVISIBLE
                            binding.offerPassageButtonPassageSummary.visibility = View.INVISIBLE
                            return
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    // Se utente ha richiesto passaggio, non può fare un'offerta
    private fun checkRequestPassage(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("passages/$uid")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    binding.priceInputLayoutPassageSummary.visibility = View.INVISIBLE
                    binding.offerPassageButtonPassageSummary.visibility = View.INVISIBLE
                    binding.offerBlockedTextViewPassageSummary.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun getDate(timestamp:Long){
        val formatter_day = SimpleDateFormat("dd")
        val formatter_month = SimpleDateFormat("MM")
        val formatter_year = SimpleDateFormat("yyyy")

        day = formatter_day.format(timestamp)
        month = formatter_month.format(timestamp)
        year = formatter_year.format(timestamp)

    }

    // Metodo che ha il compito di controllare che l'utente abbia inserito il prezzo per l'offerta che sta facendo
    private fun validatePriceField():Boolean{
        val priceString = binding.priceEditTextPassageSummary.text
        if(priceString == null || priceString.isEmpty() || priceString.isBlank()){
            binding.priceInputLayoutPassageSummary.error = getString(R.string.field_not_empty)
            return false
        }
        binding.priceInputLayoutPassageSummary.error = ""
        binding.priceInputLayoutPassageSummary.isErrorEnabled = false
        price = priceString.toString().toDouble()
        return true
    }



}