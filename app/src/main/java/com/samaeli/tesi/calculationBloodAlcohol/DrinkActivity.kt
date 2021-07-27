package com.samaeli.tesi.calculationBloodAlcohol

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.samaeli.tesi.MainActivity
import com.samaeli.tesi.R
import com.samaeli.tesi.RegisterActivity
import com.samaeli.tesi.databinding.ActivityDrinkBinding
import com.samaeli.tesi.models.Drink
import com.samaeli.tesi.models.DrinkAdded
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_drink.*
import kotlinx.android.synthetic.main.fragment_profile.*

class DrinkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDrinkBinding

    companion object{
        val TAG = "Drink Activity"
    }

    private var drink : Drink? = null
    var hour : Int? = null
    var minute : Int? = null
    var alcoholContent : Double? = null
    var quantity : Int? = null
    var volume : Int? = null
    var error : Boolean = false

    var db : DrinkAddedDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_drink)
        binding = ActivityDrinkBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        db = DrinkAddedDB(this)

        drink = intent.getParcelableExtra(SelectDrinkActivity.DRINK_KEY)

        // Se drink == null => l'utente vuole inserire un drink custom e quindi si nasconde l'immagine
        if(drink==null){
            binding.imageViewDrink.visibility = View.GONE
            supportActionBar?.title = getString(R.string.custom_drink)
        }else{
            // Se l'utente ha selezionato un drink si nasconde l'EditText dal nome e si completano i
                // campi sulla base del drink scelto dall'utente
            binding.nameEditTextDrink.visibility = View.INVISIBLE
            supportActionBar?.title = drink!!.name
            completeFields()
        }

        // Show TimePicker for the time when the user take a drink
        binding.hourEditTextDrink.inputType = InputType.TYPE_NULL
        binding.hourEditTextDrink.setOnClickListener {
            Log.d(TAG, "Try to show timePicker")
            val picker = MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setTitleText(getString(R.string.time_taken_drink))
                    .build()

            picker.show(supportFragmentManager, "")

            picker.addOnPositiveButtonClickListener {
                hour = picker.hour
                minute = picker.minute
                var minuteString = minute.toString()
                if(minuteString.length==1){
                    minuteString = "0"+minuteString
                }
                Log.d(TAG, hour.toString() + ":" + minuteString)
                binding.hourEditTextDrink.setText(hour.toString() + ":" + minuteString)
            }
        }



        binding.addButtonDrink.setOnClickListener {
            error = false // variabile che vale true se si è verificato almeno un errore durante l'inserimento dei dati del drink
            if(!validateFields()){
                Log.d(TAG,"Error during adding drink")
                Toast.makeText(this,getString(R.string.error_check_fields),Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            saveToDB()
            // Go to MainActivity
            val intent = Intent(this,MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    // Metodo che salva il drink scelto dall'utente nel db locale
    private fun saveToDB(){
        val drinkAdded : DrinkAdded
        if(drink == null){
            val name = binding.nameEditTextDrink.text.toString()
            drinkAdded = DrinkAdded(null,Drink(name,volume!!,alcoholContent!!,null),quantity!!,hour!!,minute!!)
        }else{
            drinkAdded = DrinkAdded(null,Drink(drink!!.name,volume!!,alcoholContent!!,drink!!.imageUrl),quantity!!,hour!!,minute!!)
        }
        db!!.insertDrinkAdded(drinkAdded)
    }

    // Metodo ceh controlla che i dati inseriti dall'utente siano corretti
    private fun validateFields():Boolean{
        if(drink == null){
            validateName()
        }
        validateAlcoholContent()
        validateVolume()
        validateQuantity()
        validateTime()
        return !error
    }

    // Controllo che l'utente abbia inserito il nome
    private fun validateName(){
        val name = binding.nameEditTextDrink.text.toString()

        if(name.isEmpty() || name.isBlank()){
            binding.nameEditTextDrink.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.nameEditTextDrink.error = null
    }

    // Controllo che l'utente abbia inserito la percentuale alcolica del drink
    private fun validateAlcoholContent(){
        val alcoholContentString = binding.alcoholContentEditTextDrink.text.toString()

        if(alcoholContentString.isEmpty() || alcoholContentString.isBlank()){
            binding.alcoholContentEditTextDrink.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.alcoholContentEditTextDrink.error = null
        alcoholContent = alcoholContentString.toDouble()
        Log.d(TAG,"Alcohol content: ${alcoholContent.toString()}")
    }

    // Controllo che l'utente abbia inserito il volume
    private fun validateVolume(){
        val volumeString = binding.volumeEditTextDrink.text.toString()

        if(volumeString.isEmpty() || volumeString.isBlank()){
            binding.volumeEditTextDrink.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.volumeEditTextDrink.error = null
        volume = volumeString.toInt()
        Log.d(TAG,"Volume: ${volume.toString()}")
    }

    // Controllo che l'utente abbia inserito la quantità e che sia maggiore di 0
    private fun validateQuantity(){
        val quantityString = binding.quantityEditTextDrink.text.toString()

        if(quantityString.isEmpty() || quantityString.isBlank()){
            binding.quantityEditTextDrink.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        quantity = quantityString.toInt()
        if(quantity == 0){
            binding.quantityEditTextDrink.error = getString(R.string.error_quantity)
            error = true
            return
        }
        binding.quantityEditTextDrink.error = null
        Log.d(TAG,"Quantity: ${quantity.toString()}")
    }

    // Controllo che l'utente abbia inserito quando ha bevuto il drink
    private fun validateTime(){
        if(hour == null && minute == null){
            binding.hourEditTextDrink.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.hourEditTextDrink.error = null
    }

    // Completa i campi in base ai valori presenti nel db
    private fun completeFields(){
        val url = drink!!.imageUrl
        val targetImageView = binding.imageViewDrink
        Picasso.get().load(url).into(targetImageView)
        binding.nameTextViewDrink.text = drink!!.name
        binding.alcoholContentEditTextDrink.setText(drink!!.alcoholContent.toString())
        Log.d(TAG,drink!!.alcoholContent.toString())
        binding.volumeEditTextDrink.setText(drink!!.volume.toString())
    }
}