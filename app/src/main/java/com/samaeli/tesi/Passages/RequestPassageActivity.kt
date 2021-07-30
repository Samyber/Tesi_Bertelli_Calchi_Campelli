package com.samaeli.tesi.Passages

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.ActivityRequestPassageBinding
import com.samaeli.tesi.databinding.ActivityResultCalculationBinding

class RequestPassageActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRequestPassageBinding

    companion object{
        val TAG = "Request Passage Activity"
    }

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

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
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
        /*binding.hourEditRequestPassage.setOnFocusChangeListener { v, hasFocus ->
            val color = if (hasFocus) Color.rgb(249, 170, 51) else Color.rgb(52, 73, 85)
            binding.hourInputLayoutRequestPassage.setStartIconTintList(ColorStateList.valueOf(color))
        }*/
        binding.numberPersonEditRequestPassage.setOnFocusChangeListener { v, hasFocus ->
            val color = if (hasFocus) Color.rgb(249, 170, 51) else Color.rgb(52, 73, 85)
            binding.numberPersonInputLayoutRequestPassage.setStartIconTintList(ColorStateList.valueOf(color))
        }
    }
}