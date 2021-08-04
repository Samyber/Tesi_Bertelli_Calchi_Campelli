package com.samaeli.tesi.Passages

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.ActivityMyPassageSummaryBinding
import com.samaeli.tesi.models.Offer
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

        binding.showRouteButtonMyPassageSummary.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=${passage!!.departureLatitude},${passage!!.departureLongitude}" +
                        "&daddr=${passage!!.arrivalLatitude},${passage!!.arrivalLongitude}")
            )
            startActivity(intent)
        }

        displayCorrectButton()

        binding.deleteButtonMyPassageSummary.setOnClickListener {
            Log.d(TAG,"Try to delete passage")
            deletePassage()
            finish()
        }

        /*binding.newRequestButtonMyPassageSummary.setOnClickListener {
            Log.d(TAG,"Try to request new passage")
        }*/

        completeFields()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun displayCorrectButton(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("received_offers/$uid")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()){
                    //displayDeletePassageButton()
                    binding.deleteButtonMyPassageSummary.visibility = View.VISIBLE
                }else {
                    snapshot.children.forEach {
                        val offer = it.getValue(Offer::class.java)
                        if(offer!!.state.equals("accepted")){
                            //displayNewRequestPassageButton()
                            binding.deleteButtonMyPassageSummary.visibility = View.GONE
                        }else{
                            //displayDeletePassageButton()
                            binding.deleteButtonMyPassageSummary.visibility = View.VISIBLE
                            return
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    /*private fun displayDeletePassageButton(){
        binding.deleteButtonMyPassageSummary.visibility = View.VISIBLE
        binding.newRequestButtonMyPassageSummary.visibility = View.GONE
    }

    private fun displayNewRequestPassageButton(){
        binding.deleteButtonMyPassageSummary.visibility = View.GONE
        binding.newRequestButtonMyPassageSummary.visibility = View.VISIBLE
    }*/

    private fun deletePassage(){
        declineAllOffers()
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("passages/$uid")
        ref.removeValue()
                .addOnSuccessListener {
                    Log.d(TAG,getString(R.string.passage_canceled))
                    Toast.makeText(this,getString(R.string.passage_canceled),Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Log.d(TAG,getString(R.string.delete_passage_error))
                    Toast.makeText(this,getString(R.string.delete_passage_error),Toast.LENGTH_LONG).show()
                }
    }

    fun declineAllOffers(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("received_offers/$uid/")
        ref.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val offer = it.getValue(Offer::class.java)

                    val ref2 = FirebaseDatabase.getInstance().getReference("received_offers/$uid/${offer!!.uidBidder}")
                    ref2.removeValue()
                            .addOnSuccessListener {
                                val ref3 = FirebaseDatabase.getInstance().getReference("made_offers/${offer!!.uidBidder}/$uid")
                                ref3.removeValue()
                                        .addOnSuccessListener {
                                            addDeclinedOffer(offer!!.uidBidder,offer)
                                        }
                            }

                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun addDeclinedOffer(uidBidder:String,offer:Offer){
        val ref = FirebaseDatabase.getInstance().getReference("delete_offers/$uidBidder")
        offer.state = "declined"
        ref.setValue(offer)
                .addOnFailureListener {
                    Log.d(TAG,"Error during decline offer")
                }

    }

    private fun completeFields(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("passages/$uid")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    passage = snapshot.getValue(Passage::class.java)

                    var minuteString = passage!!.minute.toString()
                    if (minuteString.length == 1) {
                        minuteString = "0" + minuteString
                    }
                    binding.hourTextViewMyPassageSummary.text = getString(R.string.hour) + ": " + passage!!.hour + ":" + minuteString
                    binding.numberPersonTextViewMyPassageSummary.text = getString(R.string.number_person) + ": " + passage!!.numberPerson

                    binding.departureAddressTextViewMyPassageSummary.text = getString(R.string.address) + ": " + passage!!.departureAddress
                    binding.departureCityTextViewMyPassageSummary.text = getString(R.string.city) + ": " + passage!!.departureCity

                    binding.arrivalAddressTextViewMyPassageSummary.text = getString(R.string.address) + ": " + passage!!.arrivalAddress
                    binding.arrivalCityTextViewMyPassageSummary.text = getString(R.string.city) + ": " + passage!!.arrivalCity
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}