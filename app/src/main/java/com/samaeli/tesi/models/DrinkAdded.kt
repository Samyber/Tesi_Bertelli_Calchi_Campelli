package com.samaeli.tesi.models

class DrinkAdded(val drink:Drink?,val quantity:Int, val hour:Int, val minute:Int) {
    constructor():this(null,0,-1,-1)
}