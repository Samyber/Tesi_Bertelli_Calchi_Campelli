package com.samaeli.tesi.calculationBloodAlcohol

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.ActivityDrinkBinding
import com.samaeli.tesi.models.Drink
import com.squareup.picasso.Picasso

class DrinkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDrinkBinding

    companion object{
        val TAG = "Drink Activity"
    }

    private var drink : Drink? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_drink)
        binding = ActivityDrinkBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        drink = intent.getParcelableExtra(SelectDrinkActivity.DRINK_KEY)
        supportActionBar?.title = drink!!.name

        completeFields()
    }

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