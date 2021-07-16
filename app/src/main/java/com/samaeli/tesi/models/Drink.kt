package com.samaeli.tesi.models

class Drink(val name:String, val quantity : Int, val alcoholicContent:Double, val imageUrl:String) {
    constructor(): this("",0,0.0,"")
}