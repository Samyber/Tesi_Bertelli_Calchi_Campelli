package com.samaeli.tesi.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid:String, var email:String, var name:String, var surname:String, var birthdayDate:Long,
           var licenseDate:Long, var gender:String, var profileImageUrl:String?, var weight:Double) : Parcelable {
    constructor():this("","","","",-1,-1,"",null,0.0)
}