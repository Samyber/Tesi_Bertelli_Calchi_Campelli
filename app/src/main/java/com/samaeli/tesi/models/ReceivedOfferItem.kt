package com.samaeli.tesi.models

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.samaeli.tesi.Passages.ReceivedOffersActivity
import com.samaeli.tesi.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_passage.view.*
import kotlinx.android.synthetic.main.item_received_offer.view.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class ReceivedOfferItem(val offer:Offer, val context:Context):Item<ViewHolder>() {

    companion object{
        val TAG = "ReceivedOfferItem"
    }

    private var day :String? = null
    private var month:String? = null
    private var year:String? = null

    override fun bind(viewHolder: ViewHolder, position: Int) {
        val uidBidder = offer.uidBidder

        viewHolder.itemView.declineButtonReceivedOffer.setOnClickListener {
            declineOffer()
        }

        viewHolder.itemView.acceptButtonReceivedOffer.setOnClickListener {
            acceptOffer()
        }

        // Compilazione campi in base ai dati dell'offerta e dell'utente che l'ha fatta
        val ref = FirebaseDatabase.getInstance().getReference("users/$uidBidder")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                viewHolder.itemView.nameTextViewReceivedOffer.text = user!!.name+" "+user!!.surname

                // Calcolo dell'età dell'utente
                getDate(user!!.birthdayDate)
                val now : LocalDate = LocalDate.now()
                val birthday = LocalDate.of(year!!.toInt() ,month!!.toInt(),day!!.toInt())
                val age = ChronoUnit.YEARS.between(birthday,now)
                viewHolder.itemView.ageTextViewReceivedOffer.text = context.getString(R.string.age)+": "+age

                viewHolder.itemView.fidelityPointsTextViewReceivedOffer.text = context.getString(R.string.fidelity_points)+": "+user.points

                viewHolder.itemView.PriceTextViewReceivedOffer.text = context.getString(R.string.price)+": "+offer.price.toString()+"€"

                if(user!!.gender.equals("male")){
                    viewHolder.itemView.genderTextViewReceivedOffer.text="M"
                    viewHolder.itemView.genderTextViewReceivedOffer.setTextColor(Color.BLUE)
                }else{
                    viewHolder.itemView.genderTextViewReceivedOffer.text="F"
                    viewHolder.itemView.genderTextViewReceivedOffer.setTextColor(Color.rgb(255,179,222))
                }

                if(offer.state.equals(Offer.ACCEPTED)){
                    viewHolder.itemView.buttonsLinearLayoutReceivedOffer.visibility = View.INVISIBLE
                }

                if(user.profileImageUrl!=null) {
                    val url = user.profileImageUrl
                    val targetImageView = viewHolder.itemView.photoImaveViewReceivedOffer
                    Picasso.get().load(url).into(targetImageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    override fun getLayout(): Int {
        return R.layout.item_received_offer
    }

    // Metodo che ha il compito di declinare un'offerta
    private fun declineOffer(){
        val uidBidder = offer.uidBidder
        val uidRequester = offer.uidRequester
        val ref = FirebaseDatabase.getInstance().getReference("received_offers/$uidRequester/$uidBidder")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val offerReceived = snapshot.getValue(Offer::class.java)
                offerReceived!!.visibility = false
                ref.setValue(offerReceived)
                        .addOnSuccessListener {
                            val ref2 = FirebaseDatabase.getInstance().getReference("made_offers/$uidBidder/$uidRequester")

                            val newOffer = Offer(offer!!.uidBidder,offer.uidRequester,offer.price,"declined",true)
                            ref2.setValue(newOffer)
                                    .addOnSuccessListener {
                                        addDeclinedOffer(uidBidder)
                                        ref.removeEventListener(this)
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context,context.getString(R.string.error_decline_offer),Toast.LENGTH_LONG).show()
                                    }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context,context.getString(R.string.error_decline_offer),Toast.LENGTH_LONG).show()
                        }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    // Metodo che ha il compito di accettare un'offerta
    private fun acceptOffer(){
        val uidBidder = offer.uidBidder
        val uidRequester = offer.uidRequester
        // Si imposta lo stato dell'offerta ad "accepted"
        offer.state=Offer.ACCEPTED
        // Caricamento dell'offerta accettata
        val ref = FirebaseDatabase.getInstance().getReference("received_offers/$uidRequester/$uidBidder")
        ref.setValue(offer)
                .addOnSuccessListener {
                    val ref2 = FirebaseDatabase.getInstance().getReference("made_offers/$uidBidder/$uidRequester")
                    ref2.setValue(offer)
                            .addOnSuccessListener {
                                ReceivedOffersActivity.declineAllOffers(context)
                            }
                            .addOnFailureListener {
                                Toast.makeText(context,context.getString(R.string.error_accept_offer),Toast.LENGTH_LONG).show()
                            }
                }
                .addOnFailureListener {
                    Toast.makeText(context,context.getString(R.string.error_accept_offer),Toast.LENGTH_LONG).show()
                }
        val ref3 = FirebaseDatabase.getInstance().getReference("accepted_offers/$uidBidder")
        ref3.setValue(offer)
                .addOnSuccessListener {
                    Log.d(TAG,"Offerta caricata in accepted_offers")
                }
        // Si incrementa il punteggio dell'utente a cui è stata accettata l'offerta
        val ref4 = FirebaseDatabase.getInstance().getReference("users/$uidBidder")
        ref4.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                user!!.points = user.points + 1
                ref4.setValue(user)
                        .addOnSuccessListener {
                            Log.d(TAG,"Punteggio incrementato correttamente: ${user.points}")
                        }
                ref4.removeEventListener(this)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }

    // Metodo che ha il compito di caricare l'offerta declinata in "delete_offers"
    private fun addDeclinedOffer(uidBidder:String){
        val ref = FirebaseDatabase.getInstance().getReference("delete_offers/$uidBidder")
        offer.state = Offer.DECLINED
        ref.setValue(offer)
                .addOnSuccessListener {
                    Log.d(TAG,"ADD Declined offer")
                    Toast.makeText(context,context.getString(R.string.declined_offer),Toast.LENGTH_LONG).show()
                    ReceivedOffersActivity.displayReceivedOffers(context)
                }
                .addOnFailureListener {
                    Toast.makeText(context,context.getString(R.string.error_decline_offer),Toast.LENGTH_LONG).show()
                }

    }

    private fun getDate(timestamp:Long){
        val formatter_day = SimpleDateFormat("dd")
        val formatter_month = SimpleDateFormat("MM")
        val formatter_year = SimpleDateFormat("yyyy")

        day = formatter_day.format(timestamp)
        month = formatter_month.format(timestamp)
        year = formatter_year.format(timestamp)
    }
}