package com.samaeli.tesi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.samaeli.tesi.databinding.ActivityNotConnectedBinding

/*
    Activity che mostra un messaggio di errore se il telefono non è connesso ad internet
 */
class NotConnectedActivity : AppCompatActivity() {

    private lateinit var binding : ActivityNotConnectedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotConnectedBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
}