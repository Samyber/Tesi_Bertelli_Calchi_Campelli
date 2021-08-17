package com.samaeli.tesi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.samaeli.tesi.databinding.ActivityNotConnectedBinding

class NotConnectedActivity : AppCompatActivity() {

    private lateinit var binding : ActivityNotConnectedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_not_connected)
        binding = ActivityNotConnectedBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}