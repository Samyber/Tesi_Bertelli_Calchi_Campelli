package com.samaeli.tesi.models

class User(val uid:String, val email:String, val name:String, val surname:String, val birthdayDate:Long,
           val licenseDate:Long, val gender:String, val profileImageUrl:String?) {
    constructor():this("","","","",-1,-1,"",null)
}