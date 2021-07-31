package com.samaeli.tesi.models

class Passage(val departureAddress:String, val departureCity:String, val arrivalAddress:String, val arrivalCity:String, val hour:Int, val minute:Int,
val numberPerson:Int, val departureLatitude : Double, val departureLongitude:Double, val arrivalLatitude:Double, val arrivalLongitude:Double) {

    constructor():this("","","","",-1,-1,0,
        0.0,0.0,0.0,0.0)
}