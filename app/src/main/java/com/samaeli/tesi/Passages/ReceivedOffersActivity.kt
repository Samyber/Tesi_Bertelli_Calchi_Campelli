package com.samaeli.tesi.Passages

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.ActivityReceivedOffersBinding
import com.samaeli.tesi.models.Offer
import com.samaeli.tesi.models.Passage
import com.samaeli.tesi.models.ReceivedOfferItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import okhttp3.internal.Internal.instance

class ReceivedOffersActivity : AppCompatActivity() {

    private lateinit var binding : ActivityReceivedOffersBinding

    companion object{
        val TAG = "Received offers activity"
        var adapter = GroupAdapter<ViewHolder>()
        var act : Activity? = null
        // HashMap che contiene le offerte che sono state ricevute
        var offersMap = HashMap<String,Offer>()

        fun setActivity(a:Activity){
            act = a
        }

        // Metodo che ha il compito di visualizzare le offerte che sono state ricevute
        fun displayReceivedOffers(context:Context){
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("received_offers/$uid/")
            ref.addChildEventListener(object : ChildEventListener{
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val offer = snapshot.getValue(Offer::class.java)
                    if(offer!!.visibility!=false){
                        offersMap[snapshot.key!!] = offer
                    }
                    refreshRecyclerView(context)
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val offer = snapshot.getValue(Offer::class.java)
                    if(offer!!.visibility==false && offersMap.containsKey(snapshot.key!!)){
                        offersMap.remove(snapshot.key!!)
                    }
                    refreshRecyclerView(context)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    //val offer = snapshot.getValue(Offer::class.java)
                    if(offersMap.containsKey(snapshot.key!!)){
                        offersMap.remove(snapshot.key!!)
                    }
                    refreshRecyclerView(context)
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }

        // Metodo che ha il compito di aggiornare il recyclerView con le offerte che ci sono in
        // offersMap
        private fun refreshRecyclerView(context : Context){
            adapter.clear()
            offersMap.values.forEach {
                adapter.add(ReceivedOfferItem(it,context))
            }
            if(adapter.itemCount == 0){
                act!!.finish()
            }
        }

        /*fun displayReceivedOffers(context: Context){
            //adapter.clear()
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("received_offers/$uid/")
            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    adapter.clear()
                    snapshot.children.forEach {
                        val offer = it.getValue(Offer :: class.java)
                        if(offer!!.visibility==true) {
                            Log.d(TAG, "Requester uid: ${offer!!.uidRequester}")
                            adapter.add(ReceivedOfferItem(offer!!, context))
                        }
                    }
                    if(adapter.itemCount == 0){
                        act!!.finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }*/

        // Metodo che ha il compito di declinare tutte le offerte che sono arrivate
        fun declineAllOffers(context:Context){
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("received_offers/$uid/")
            val listener = object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        val offer = it.getValue(Offer::class.java)
                        if(!offer!!.state.equals(Offer.ACCEPTED)){
                            val ref2 = FirebaseDatabase.getInstance().getReference("received_offers/$uid/${offer.uidBidder}")
                            ref2.removeValue()
                                .addOnSuccessListener {
                                    val ref3 = FirebaseDatabase.getInstance().getReference("made_offers/${offer.uidBidder}/$uid")
                                    ref3.removeValue()
                                        .addOnSuccessListener {
                                            if(offer.state.equals(Offer.WAIT)) {
                                                addDeclinedOffer(offer.uidBidder, offer, context)
                                            }
                                        }
                                }
                        }
                    }
                    Toast.makeText(context,context.getString(R.string.accepted_offer),Toast.LENGTH_LONG).show()
                    setPassageInvisible(context)
                    displayReceivedOffers(context)

                    //Rimuovo listener
                    ref.removeEventListener(this)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            }
            ref.addValueEventListener(listener)
        }

        /*fun declineAllOffers(context:Context){
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("received_offers/$uid/")
            val listener = object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach {
                        val offer = it.getValue(Offer::class.java)
                        if(!offer!!.state.equals("accepted")){
                            val ref2 = FirebaseDatabase.getInstance().getReference("received_offers/$uid/${offer.uidBidder}")
                            ref2.addValueEventListener(object : ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val offerReceived = snapshot.getValue(Offer::class.java)
                                    offerReceived!!.visibility = false
                                    ref2.setValue(offerReceived)
                                            .addOnSuccessListener {
                                                val ref3 = FirebaseDatabase.getInstance().getReference("made_offers/${offer.uidBidder}/$uid")
                                                ref3.removeValue()
                                                        .addOnSuccessListener {
                                                            addDeclinedOffer(offer.uidBidder,offer,context)
                                                        }
                                            }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }

                            })
                            /*ref2.removeValue()
                                    .addOnSuccessListener {
                                        val ref3 = FirebaseDatabase.getInstance().getReference("made_offers/${offer.uidBidder}/$uid")
                                        ref3.removeValue()
                                                .addOnSuccessListener {
                                                    addDeclinedOffer(offer.uidBidder,offer,context)
                                                }
                                    }*/
                        }
                    }
                    Toast.makeText(context,context.getString(R.string.accepted_offer),Toast.LENGTH_LONG).show()
                    setPassageInvisible(context)
                    displayReceivedOffers(context)

                    //Rimuovo listener
                    ref.removeEventListener(this)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            }
            ref.addValueEventListener(listener)
        }*/

        // Metodo che aggiunge l'offerta declinata in "delete_offers"
        private fun addDeclinedOffer(uidBidder:String,offer:Offer,context: Context){
            val ref = FirebaseDatabase.getInstance().getReference("delete_offers/$uidBidder")
            offer.state = Offer.DECLINED
            //val newOffer = Offer(offer.uidBidder,offer.uidRequester,offer.price,"declined")
            ref.setValue(offer)
                    .addOnSuccessListener {
                        Toast.makeText(context,context.getString(R.string.accepted_offer), Toast.LENGTH_LONG).show()
                        Log.d(TAG,"TUTTO OK!!!!")
                        act!!.finish()
                        //setPassageInvisible(context)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context,context.getString(R.string.error_decline_offer), Toast.LENGTH_LONG).show()
                    }

        }

        /*private fun deletePassage(context:Context){
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("passages/$uid")
            ref.removeValue()
                    .addOnFailureListener {
                        Toast.makeText(context,context.getString(R.string.error_delete_passage),Toast.LENGTH_LONG).show()
                    }
        }*/

        // Metodo che ha il compito di rendere il passaggio invisibile se viene accettata un'offerta per quel passaggio
        private fun setPassageInvisible(context: Context){
            val uid = FirebaseAuth.getInstance().uid
            val ref = FirebaseDatabase.getInstance().getReference("passages/$uid")
            ref.addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        val passage = snapshot.getValue(Passage::class.java)
                        passage!!.visibility = false
                        ref.setValue(passage)
                                .addOnSuccessListener {
                                    Log.d(TAG, "TUTTO OK!!!!")
                                    act!!.finish()
                                }
                                .addOnFailureListener {
                                    Log.d(TAG, "Error change passage visibility")
                                }
                    }
                    ref.removeEventListener(this)
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

        setActivity(this)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

}