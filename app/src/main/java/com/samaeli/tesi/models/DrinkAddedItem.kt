package com.samaeli.tesi.models


import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.samaeli.tesi.MainActivity
import com.samaeli.tesi.R
import com.samaeli.tesi.calculationBloodAlcohol.AlcoholLevelFragment
import com.samaeli.tesi.calculationBloodAlcohol.DrinkAddedDB
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_add_drink.view.*
import kotlinx.android.synthetic.main.item_drink_added.view.*
import java.lang.Object

class DrinkAddedItem(val drinkAdded: DrinkAdded, val context:Context):Item<ViewHolder>() {
    @SuppressLint("SetTextI18n")
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.nameTextViewItemDrinkAdded.text = drinkAdded.drink!!.name
        // Bisogna passare il contesto per poter accedere alle stringhe
        viewHolder.itemView.volumeTextViewDrinkAdded.text = context.getString(R.string.volume_drink_added) +" "+ drinkAdded.drink!!.volume.toString()+" ml"
        viewHolder.itemView.alcoholContentTextViewDrinkAdded.text = context.getString(R.string.alcohol_content_drink_added)+" "+drinkAdded.drink!!.alcoholContent.toString()+" %"
        viewHolder.itemView.quantityTextViewDrinkAdded.text = context.getString(R.string.quantity)+":  "+drinkAdded.quantity
        var minuteString = drinkAdded.minute.toString()
        if(minuteString.length == 1){
            minuteString = "0"+minuteString
        }
        viewHolder.itemView.timeTextViewDrinkAdded.text = context.getString(R.string.hour_taken)+" "+drinkAdded.hour.toString()+":"+minuteString
        val url = drinkAdded.drink!!.imageUrl
        if(url != null) {
            val targetImageView = viewHolder.itemView.imageViewItemDrinkAdded
            Picasso.get().load(url).into(targetImageView)
        }

        // Listener per cui se si clicca sul cestino rosso in corrispondenza del drink inserito lo si
        // cancella dal DB
        viewHolder.itemView.deleteImageViewDrinkAdded.setOnClickListener {
            AlcoholLevelFragment.db!!.deleteDrinkAdded(drinkAdded.id!!)
            AlcoholLevelFragment.displayDrinkAdded(context)
        }
    }

    override fun getLayout(): Int {
        return R.layout.item_drink_added
    }
}