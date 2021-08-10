package com.samaeli.tesi.models

import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.samaeli.tesi.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_made_offer.view.*

class MadeOfferItem(val offer:Offer, val context:Context):Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        completeUserInformations(viewHolder)
        viewHolder.itemView.stateTextViewMadeOffer.text = context.getString(R.string.state)+": "+offer.state
        completePassageInformations(viewHolder)
    }

    override fun getLayout(): Int {
        return R.layout.item_made_offer
    }

    // Metodo che ha il compito di completare i campi con le informazioni dell'utente
    private fun completeUserInformations(viewHolder: ViewHolder){
        val ref = FirebaseDatabase.getInstance().getReference("users/${offer.uidRequester}")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                viewHolder.itemView.nameTextViewMadeOffer.text = user!!.name+" "+user!!.surname
                ref.removeEventListener(this)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    // Metodo che ha il compito di completare i campi con le informazioni del passaggio
    private fun completePassageInformations(viewHolder: ViewHolder){
        val ref = FirebaseDatabase.getInstance().getReference("passages/${offer.uidRequester}")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val passage = snapshot.getValue(Passage::class.java)
                viewHolder.itemView.departureCityTextViewMadeOffer.text = passage!!.departureCity
                viewHolder.itemView.arrivalCityTextViewMadeOffer.text = passage.arrivalCity
                ref.removeEventListener(this)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}