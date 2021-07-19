package com.samaeli.tesi.models

class DrinkAdded(val id:Long?, val drink:Drink?,val quantity:Int, val hour:Int, val minute:Int) {
    constructor():this(null,null,0,-1,-1)
}