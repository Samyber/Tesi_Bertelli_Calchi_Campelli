package com.samaeli.tesi.Passages

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.FragmentPassagesChoiseBinding


class PassagesChoiseFragment : Fragment() {

    private var _binding : FragmentPassagesChoiseBinding? = null
    private val binding get() = _binding!!

    private var typeUser :String = "bidder"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_passages_choise, container, false)
        _binding = FragmentPassagesChoiseBinding.inflate(inflater,container,false)
        val view = binding.root

        binding.passageRequestButtonChoisePassages.setOnClickListener {
            val intent = Intent(activity,RequestPassageActivity::class.java)
            startActivity(intent)
        }

        binding.passageProvideButtonChoisePassages.setOnClickListener {
            val intent = Intent(activity,PassageProvideActivity::class.java)
            startActivity(intent)
        }

        binding.recentOffertsButtonChoisePassages.setOnClickListener {
            if(typeUser.equals("requester")){
                val intent = Intent(activity,ReceivedOffersActivity::class.java)
                startActivity(intent)
            }
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        setRequestPassageButtonText()
    }

    private fun setRequestPassageButtonText(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("passages/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()){
                    binding.passageRequestButtonChoisePassages.text = getString(R.string.passage_request)
                    typeUser = "bidder"
                }else{
                    binding.passageRequestButtonChoisePassages.text = getString(R.string.summary_required_passage)
                    typeUser = "requester"
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }


}