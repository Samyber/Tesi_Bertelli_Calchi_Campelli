package com.samaeli.tesi.calculationBloodAlcohol

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.FragmentAlcoholLevelBinding
import com.samaeli.tesi.models.DrinkAddedItem
import com.samaeli.tesi.models.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_alcohol_level.*

class AlcoholLevelFragment : Fragment() {

    private var _binding : FragmentAlcoholLevelBinding? = null
    private val binding get() = _binding!!

    var prefs : SharedPreferences? = null

    companion object {
        const val MALE_EMPTY_STOMACH = 0.7
        const val MALE_FULL_STOMACH = 1.2
        const val FEMALE_EMPTY_STOMACH = 0.5
        const val FEMALE_FULL_STOMACH = 0.9
        // 3 years in milliseconds for the new drivers
        const val YEARS_3 = 94672800000
        const val YEARS_21 = 662709600000

        var db: DrinkAddedDB? = null
        var adapter = GroupAdapter<ViewHolder>()

        fun displayDrinkAdded(context: Context){
            adapter.clear()
            val drinksAdded = db!!.getDrinksAdded()
            if(drinksAdded.size > 0){
                for(drinkAdded in drinksAdded){
                    adapter.add(DrinkAddedItem(drinkAdded,context))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_alcohol_level, container, false)
        _binding = FragmentAlcoholLevelBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.buttonAddDrink.setOnClickListener {
            val intent = Intent(activity,SelectDrinkActivity::class.java)
            startActivity(intent)
        }

        binding.buttonAddCustomDrink.setOnClickListener {
            val intent = Intent(activity,DrinkActivity::class.java)
            startActivity(intent)
        }

        // Controllo che l'utente non sia un utente anonimo
        //if(!FirebaseAuth.getInstance().currentUser!!.email.isNullOrEmpty()){
        if(FirebaseAuth.getInstance().uid != null) {
            completeFieldsFromFirebase()
        }
        completeFieldsFromSharedPreference()

        db = DrinkAddedDB(requireActivity().applicationContext)

        displayDrinkAdded(requireActivity().applicationContext)
        binding.recyclerViewAlcoholLevelFragmnet.adapter = adapter

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //_binding = null
    }

    override fun onPause() {
        val editor = prefs?.edit()
        if(binding.genderRadioGroupAlcoholLevel.checkedRadioButtonId == R.id.maleRadioButtonAlcoholLevel){
            editor?.putString("gender","male")
        }else{
            editor?.putString("gender","female")
        }
        if(binding.questionFullStomachRadioGroupAlcoholLevel.checkedRadioButtonId == R.id.yesFullStomachRadioButtonAlcoholLevel){
            editor?.putString("full_stomach","yes")
        }else{
            editor?.putString("full_stomach","no")
        }
        if(binding.newDriverRadioGroupAlcoholFragment.checkedRadioButtonId == R.id.yesNewDriverRadioButtonAlcoholFragment){
            editor?.putString("new_driver","yes")
        }else{
            editor?.putString("new_driver","no")
        }
        editor?.putString("weight",binding.weightEditTextAlcoholLevel.text.toString())
        editor?.commit()
        super.onPause()
    }

    // Se l'utente è loggato e si ruota lo schermo si manteien memorizzzato solo se è a stomaco pieno oopure no.
    // Gli altri valori vengono rispristinati a quelli memorizzati nel profilo dell'utente
    private fun completeFieldsFromSharedPreference(){
        if(FirebaseAuth.getInstance().uid == null){
            val gender = prefs?.getString("gender","")
            if(!gender.equals("")){
                if(gender.equals("male")){
                    binding.genderRadioGroupAlcoholLevel.check(R.id.maleRadioButtonAlcoholLevel)
                }else{
                    binding.genderRadioGroupAlcoholLevel.check(R.id.femaleRadioButtonAlcoholLevel)
                }
            }
            val newDriver = prefs?.getString("new_driver","")
            if(!newDriver.equals("")){
                if(newDriver.equals("yes")){
                    binding.newDriverRadioGroupAlcoholFragment.check(R.id.yesNewDriverRadioButtonAlcoholFragment)
                }else{
                    binding.newDriverRadioGroupAlcoholFragment.check(R.id.noNewDriverRadioButtonAlcoholFragment)
                }
            }
            val weight = prefs?.getString("weight","")
            if(!weight.equals("")){
                binding.weightEditTextAlcoholLevel.setText(weight)
            }
        }
        val fullStomach = prefs?.getString("full_stomach","")
        if(!fullStomach.equals("")){
            if(fullStomach.equals("yes")){
                binding.questionFullStomachRadioGroupAlcoholLevel.check(R.id.yesFullStomachRadioButtonAlcoholLevel)
            }else{
                binding.questionFullStomachRadioGroupAlcoholLevel.check(R.id.noFullStomachRadioButtonAlcoholLevel)
            }
        }
    }

    private fun completeFieldsFromFirebase(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                binding.weightEditTextAlcoholLevel.setText(user!!.weight.toString())
                if(user.gender.equals("male")){
                    binding.genderRadioGroupAlcoholLevel.check(R.id.maleRadioButtonAlcoholLevel)
                }else{
                    binding.genderRadioGroupAlcoholLevel.check(R.id.femaleRadioButtonAlcoholLevel)
                }
                val timestamp = System.currentTimeMillis()
                // Controllo se l'utente è neopatentato oppure no
                if(timestamp - user.birthdayDate > YEARS_21 && timestamp - user.licenseDate > YEARS_3){
                    binding.newDriverRadioGroupAlcoholFragment.check(R.id.noNewDriverRadioButtonAlcoholFragment)
                    Log.d("Fragment alcohol","Old driver")
                }else{
                    binding.newDriverRadioGroupAlcoholFragment.check(R.id.yesNewDriverRadioButtonAlcoholFragment)
                    Log.d("Fragment alcohol","New driver")
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

}