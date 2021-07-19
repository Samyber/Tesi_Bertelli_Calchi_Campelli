package com.samaeli.tesi.calculationBloodAlcohol

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
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.FragmentAlcoholLevelBinding
import com.samaeli.tesi.models.User
import kotlinx.android.synthetic.main.fragment_alcohol_level.*

class AlcoholLevelFragment : Fragment() {

    private var _binding : FragmentAlcoholLevelBinding? = null
    private val binding get() = _binding!!

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
        //if(FirebaseAuth.getInstance().uid != null) {
        // Controllo che l'utente non sia un utente anonimo
        if(!FirebaseAuth.getInstance().currentUser!!.email.isNullOrEmpty()){
            completeField()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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