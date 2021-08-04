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

        val ref = FirebaseDatabase.getInstance().getReference("users/$uidBidder")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                viewHolder.itemView.nameTextViewReceivedOffer.text = user!!.name+" "+user!!.surname

                getDate(user!!.birthdayDate)
                val now : LocalDate = LocalDate.now()
                val birthday = LocalDate.of(year!!.toInt() ,month!!.toInt(),day!!.toInt())
                val age = ChronoUnit.YEARS.between(birthday,now)
                viewHolder.itemView.ageTextViewReceivedOffer.text = context.getString(R.string.age)+": "+age

                viewHolder.itemView.PriceTextViewReceivedOffer.text = context.getString(R.string.price)+": "+offer.price.toString()+"€"

                if(user!!.gender.equals("male")){
                    viewHolder.itemView.genderTextViewReceivedOffer.text="M"
                    viewHolder.itemView.genderTextViewReceivedOffer.setTextColor(Color.BLUE)
                }else{
                    viewHolder.itemView.genderTextViewReceivedOffer.text="F"
                    viewHolder.itemView.genderTextViewReceivedOffer.setTextColor(Color.rgb(255,179,222))
                }

                if(offer.state.equals("accepted")){
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

    private fun declineOffer(){
        val uidBidder = offer.uidBidder
        val uidRequester = offer.uidRequester
        val ref = FirebaseDatabase.getInstance().getReference("received_offers/$uidRequester/$uidBidder")
        ref.removeValue()
                .addOnSuccessListener {
                    val ref2 = FirebaseDatabase.getInstance().getReference("made_offers/$uidBidder/$uidRequester")
                    ref2.removeValue()
                            .addOnSuccessListener {
                                addDeclinedOffer(uidBidder)
                            }
                            .addOnFailureListener {
                                Toast.makeText(context,context.getString(R.string.error_decline_offer),Toast.LENGTH_LONG).show()
                            }
                }
                .addOnFailureListener {
                    Toast.makeText(context,context.getString(R.string.error_decline_offer),Toast.LENGTH_LONG).show()
                }
    }

    private fun acceptOffer(){
        val uidBidder = offer.uidBidder
        val uidRequester = offer.uidRequester
        offer.state="accepted"
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
    }

    private fun addDeclinedOffer(uidBidder:String){
        val ref = FirebaseDatabase.getInstance().getReference("delete_offers/$uidBidder")
        offer.state = "declined"
        ref.setValue(offer)
                .addOnSuccessListener {
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