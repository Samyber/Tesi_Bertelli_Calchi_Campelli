package com.samaeli.tesi.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Drink(val name:String, val volume : Int, val alcoholContent:Double, val imageUrl:String, val cocktail:Boolean):Parcelable {
    constructor(): this("",0,0.0,"",false)
}