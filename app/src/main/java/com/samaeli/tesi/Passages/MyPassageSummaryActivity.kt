package com.samaeli.tesi.Passages

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.ActivityMyPassageSummaryBinding
import com.samaeli.tesi.models.Passage

class MyPassageSummaryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMyPassageSummaryBinding

    companion object{
        val TAG = "My Passage Summary Activity"
    }

    var passage : Passage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_my_passage_summary)
        binding = ActivityMyPassageSummaryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.my_passage_summary)
        }

        completeFields()

        binding.showRouteButtonMyPassageSummary.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=${passage!!.departureLatitude},${passage!!.departureLongitude}" +
                        "&daddr=${passage!!.arrivalLatitude},${passage!!.arrivalLongitude}")
            )
            startActivity(intent)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
    
    private fun completeFields(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("passages/$uid")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                passage = snapshot.getValue(Passage::class.java)

                var minuteString = passage!!.minute.toString()
                if(minuteString.length == 1){
                    minuteString = "0"+minuteString
                }
                binding.hourTextViewMyPassageSummary.text = getString(R.string.hour)+": "+passage!!.hour+":"+minuteString
                binding.numberPersonTextViewMyPassageSummary.text = getString(R.string.number_person)+": "+passage!!.numberPerson

                binding.departureAddressTextViewMyPassageSummary.text = getString(R.string.address)+": "+passage!!.departureAddress
                binding.departureCityTextViewMyPassageSummary.text = getString(R.string.city)+": "+passage!!.departureCity

                binding.arrivalAddressTextViewMyPassageSummary.text = getString(R.string.address)+": "+passage!!.arrivalAddress
                binding.arrivalCityTextViewMyPassageSummary.text = getString(R.string.city)+": "+passage!!.arrivalCity
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}