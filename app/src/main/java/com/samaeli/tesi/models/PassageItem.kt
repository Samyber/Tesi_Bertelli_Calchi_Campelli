package com.samaeli.tesi.models

import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.samaeli.tesi.R
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_passage.view.*

class PassageItem(val passage: Passage): Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        val ref = FirebaseDatabase.getInstance().getReference("users/${passage.uid}")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                viewHolder.itemView.nameTextViewPassageItem.text = user!!.name+" "+user!!.surname
                viewHolder.itemView.departureCityTextViewPassageItem.text = passage.departureCity
                viewHolder.itemView.arrivalCityTextViewPassageItem.text = passage.arrivalCity
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    override fun getLayout(): Int {
        return R.layout.item_passage
    }
}