package com.samaeli.tesi.Passages

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.annotation.RequiresApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.ActivityPassageSummaryBinding
import com.samaeli.tesi.models.Passage
import com.samaeli.tesi.models.User
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Year
import java.time.temporal.ChronoUnit

class PassageSummaryActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPassageSummaryBinding

    companion object{
        val TAG = "Passage Summary Activity"
    }

    private var passage : Passage? = null

    private var day : String? = null
    private var month : String? = null
    private var year : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_passage_summary)
        binding = ActivityPassageSummaryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.passage)
        }

        passage = intent.getParcelableExtra(PassageProvideActivity.PASSAGE_KEY)
        Log.d(TAG,"departure city: ${passage!!.departureCity}")
        Log.d(TAG,"arrival city: ${passage!!.arrivalCity}")

        completeUserFields()
        completePassageFields()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun completeUserFields(){
        val ref = FirebaseDatabase.getInstance().getReference("users/${passage!!.uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                binding.nameUserTextViewPassageSummary.text = user!!.name+" "+user!!.surname

                getDate(user.birthdayDate)

                val now : LocalDate = LocalDate.now()
                val birthday = LocalDate.of(year!!.toInt() ,month!!.toInt(),day!!.toInt())
                val age = ChronoUnit.YEARS.between(birthday,now)
                Log.d(TAG,"The age is $age")
                binding.ageUserTextViewPassageSummary.text = getString(R.string.age)+": "+age.toString()

                if(user.gender.equals("male")){
                    binding.genderTextViewPassageSummary.text = "M"
                    binding.genderTextViewPassageSummary.setTextColor(Color.BLUE)
                }else{
                    binding.genderTextViewPassageSummary.text = "F"
                    binding.genderTextViewPassageSummary.setTextColor(Color.rgb(255,179,222))
                }

                if(!user.profileImageUrl.isNullOrEmpty()) {
                    val url = user.profileImageUrl
                    val targetImageView = binding.photoCircleImageViewPassageSummary
                    Picasso.get().load(url).into(targetImageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun completePassageFields(){
        var minuteString = passage!!.minute.toString()
        if(minuteString.length == 1){
            minuteString = "0"+minuteString
        }
        binding.hourTextViewPassageSummary.text = getString(R.string.hour)+": "+passage!!.hour+":"+minuteString
        binding.numberPersonTextViewPassageSummary.text = getString(R.string.number_person)+": "+passage!!.numberPerson

        binding.departureAddressTextViewPassageSummary.text = getString(R.string.address)+": "+passage!!.departureAddress
        binding.departureCityTextViewPassageSummary.text = getString(R.string.city)+": "+passage!!.departureCity

        binding.arrivalAddressTextViewPassageSummary.text = getString(R.string.address)+": "+passage!!.arrivalAddress
        binding.arrivalCityTextViewPassageSummary.text = getString(R.string.city)+": "+passage!!.arrivalCity
    }

    private fun getDate(timestamp:Long){
        val formatter_day = SimpleDateFormat("dd")
        val formatter_month = SimpleDateFormat("MM")
        val formatter_year = SimpleDateFormat("yyyy")

        day = formatter_day.format(timestamp)
        month = formatter_month.format(timestamp)
        year = formatter_year.format(timestamp)

        //Log.d(TAG,formatter_day.toString()+" "+formatter_month.toString()+" "+formatter_year.toString())
        //return formatter.format(timestamp)
    }

}