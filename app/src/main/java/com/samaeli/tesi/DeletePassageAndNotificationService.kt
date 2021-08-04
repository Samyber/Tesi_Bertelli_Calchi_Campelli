package com.samaeli.tesi

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.samaeli.tesi.Passages.MyPassageSummaryActivity
import com.samaeli.tesi.models.Offer
import com.samaeli.tesi.models.Passage
import java.util.*

class DeletePassageAndNotificationService : Service() {

    var timerDeletePassage : Timer? = null

    companion object{
        val TAG = "DeletePassageAndNotificationService"
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG,"onBind Not used")
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG,"Service created")
        startTimerDeletePassage()
    }

    override fun onDestroy() {
        Log.d(TAG,"Service destroyed")
        stopTimer()
        super.onDestroy()
    }

    private fun startTimerDeletePassage(){
        timerDeletePassage = Timer(true)
        timerDeletePassage!!.schedule(object : TimerTask(){
            override fun run() {
                checkPassage()
            }
        },0,1000 * 60 *5)
    }

    private fun stopTimer(){
        if(timerDeletePassage!=null){
            timerDeletePassage!!.cancel()
        }
    }

    private fun checkPassage(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("passages/$uid")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val passage = snapshot.getValue(Passage::class.java)
                    // Quando si è assunto il drink
                    val timeMinute: Int = passage!!.hour * 60 + passage.minute
                    // Ora
                    val nowMinute: Int = Calendar.getInstance().get(Calendar.MINUTE) +
                            (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60)
                    Log.d(TAG, "Time minute: $timeMinute ; Now minute: $nowMinute")
                    // Se sono passate le 17 e l'ora del passaggio è entro le 8 si può fare se no no
                    if(!(timeMinute<8*60 && nowMinute>16*60)) {
                        if (nowMinute > timeMinute) {
                            ref.removeValue()
                            declineAllOffers()
                            // TODO USE BROADCAST RECEIVER TO UPDATE UI
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
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
                                if(!offer.state.equals("accepted")) {
                                    val ref3 = FirebaseDatabase.getInstance().getReference("made_offers/${offer!!.uidBidder}/$uid")
                                    ref3.removeValue()
                                            .addOnSuccessListener {
                                                addDeclinedOffer(offer!!.uidBidder, offer)
                                            }
                                }
                            }

                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun addDeclinedOffer(uidBidder:String,offer: Offer){
        val ref = FirebaseDatabase.getInstance().getReference("delete_offers/$uidBidder")
        offer.state = "declined"
        ref.setValue(offer)
                .addOnFailureListener {
                    Log.d(MyPassageSummaryActivity.TAG,"Error during decline offer")
                }

    }

}