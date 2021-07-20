package com.samaeli.tesi.calculationBloodAlcohol

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
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

        if(drink==null){
            binding.imageViewDrink.visibility = View.GONE
            supportActionBar?.title = getString(R.string.custom_drink)
        }else{
            binding.nameEditTextDrink.visibility = View.INVISIBLE
            supportActionBar?.title = drink!!.name
            completeFields()
        }

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
                Log.d(TAG, hour.toString() + ":" + minute.toString())
                binding.hourEditTextDrink.setText(hour.toString() + ":" + minute.toString())
            }
        }



        binding.addButtonDrink.setOnClickListener {
            error = false
            if(!validateFields()){
                Log.d(TAG,"Error during adding drink")
                return@setOnClickListener
            }
            saveToDB()
            val intent = Intent(this,MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

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

    private fun validateName(){
        val name = binding.nameEditTextDrink.text.toString()

        if(name.isEmpty() || name.isBlank()){
            binding.nameEditTextDrink.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.nameEditTextDrink.error = null
    }

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

    private fun validateQuantity(){
        val quantityString = binding.quantityEditTextDrink.text.toString()

        if(quantityString.isEmpty() || quantityString.isBlank()){
            binding.quantityEditTextDrink.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.quantityEditTextDrink.error = null
        quantity = quantityString.toInt()
        Log.d(TAG,"Quantity: ${quantity.toString()}")
    }

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