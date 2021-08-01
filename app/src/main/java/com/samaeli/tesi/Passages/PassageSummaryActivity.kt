package com.samaeli.tesi.Passages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.ActivityPassageSummaryBinding
import com.samaeli.tesi.models.Passage

class PassageSummaryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPassageSummaryBinding

    companion object{
        val TAG = "Passage Summary Activity"
    }

    private var passage : Passage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_passage_summary)
        binding = ActivityPassageSummaryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.passage)
        }

        passage = intent.getParcelableExtra(PassageProvideActivity.PASSAGE_KEY)
        Log.d(TAG,"departure city: ${passage!!.departureCity}")
        Log.d(TAG,"arrival city: ${passage!!.arrivalCity}")

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}