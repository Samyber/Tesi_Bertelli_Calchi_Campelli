package com.samaeli.tesi.models

class User(val uid:String, var email:String, var name:String, var surname:String, var birthdayDate:Long,
           var licenseDate:Long, var gender:String, var profileImageUrl:String?, var weight:Double) {
    constructor():this("","","","",-1,-1,"",null,0.0)
}