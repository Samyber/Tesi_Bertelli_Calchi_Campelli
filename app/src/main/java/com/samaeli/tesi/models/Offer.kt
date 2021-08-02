package com.samaeli.tesi.models

class Offer(val uidBidder:String,val uidRequester:String,val price:Double, val state:String) {
    constructor():this("","",0.0,"")
}