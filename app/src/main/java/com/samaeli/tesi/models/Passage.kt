package com.samaeli.tesi.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Passage(val uid:String, val departureAddress:String, val departureCity:String, val arrivalAddress:String, val arrivalCity:String, val hour:Int, val minute:Int,
              val numberPerson:Int, val departureLatitude : Double, val departureLongitude:Double, val arrivalLatitude:Double, val arrivalLongitude:Double, var visibility:Boolean):Parcelable {

    constructor():this("","","","","",-1,-1,0,
        0.0,0.0,0.0,0.0,true)
}