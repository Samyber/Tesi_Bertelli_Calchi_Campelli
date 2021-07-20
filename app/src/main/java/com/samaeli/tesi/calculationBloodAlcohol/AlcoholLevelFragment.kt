package com.samaeli.tesi.calculationBloodAlcohol

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.samaeli.tesi.MainActivity
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.FragmentAlcoholLevelBinding
import com.samaeli.tesi.models.DrinkAdded
import com.samaeli.tesi.models.DrinkAddedItem
import com.samaeli.tesi.models.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.fragment_alcohol_level.*

class AlcoholLevelFragment : Fragment() {

    private var _binding : FragmentAlcoholLevelBinding? = null
    private val binding get() = _binding!!


    companion object {
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
            completeField()
        }

        db = DrinkAddedDB(requireActivity().applicationContext)

        displayDrinkAdded(requireActivity().applicationContext)
        binding.recyclerViewAlcoholLevelFragmnet.adapter = adapter

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*public fun displayDrinkAdded(){
        val adapter = GroupAdapter<ViewHolder>()
        val drinksAdded = db!!.getDrinksAdded()
        if(drinksAdded.size > 0){
            for(drinkAdded in drinksAdded){
                adapter.add(DrinkAddedItem(drinkAdded))
            }
        }
        Log.d("MAIN ACTIVITY",adapter.)
        binding.recyclerViewAlcoholLevelFragmnet.adapter = adapter
    }*/

    private fun completeField(){
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
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

}