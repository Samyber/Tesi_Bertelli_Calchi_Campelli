package com.samaeli.tesi.Passages

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.ActivityReceivedOffersBinding
import com.samaeli.tesi.models.Offer
import com.samaeli.tesi.models.ReceivedOfferItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import okhttp3.internal.Internal.instance

class ReceivedOffersActivity : AppCompatActivity() {

    private lateinit var binding : ActivityReceivedOffersBinding

    companion object{
        val TAG = "Received offers activity"
        var adapter = GroupAdapter<ViewHolder>()

        fun displayReceivedOffers(context: Context){
            adapter.clear()
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("received_offers/$uid/")
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        val offer = it.getValue(Offer :: class.java)
                        Log.d(TAG,"Requester uid: ${offer!!.uidRequester}")
                        adapter.add(ReceivedOfferItem(offer!!,context))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_received_offers)
        binding = ActivityReceivedOffersBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.received_offers)
        }

        binding.recyclerViewReceivedOffers.adapter = adapter
        displayReceivedOffers(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}