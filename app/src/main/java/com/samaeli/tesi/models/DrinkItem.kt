package com.samaeli.tesi.models

import com.samaeli.tesi.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.item_add_drink.view.*

class DrinkItem(val drink:Drink):Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.nameTextViewItemDrink.text = drink.name
        val url = drink.imageUrl
        val targetImageView = viewHolder.itemView.imageViewItemDrink
        Picasso.get().load(url).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.item_add_drink
    }
}