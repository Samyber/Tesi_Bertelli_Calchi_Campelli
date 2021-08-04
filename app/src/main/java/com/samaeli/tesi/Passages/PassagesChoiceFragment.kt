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
import com.samaeli.tesi.databinding.FragmentPassagesChoiceBinding
import com.samaeli.tesi.models.Offer


class PassagesChoiceFragment : Fragment() {

    private var _binding : FragmentPassagesChoiceBinding? = null
    private val binding get() = _binding!!

    private var typeUser :String = "bidder"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_passages_choise, container, false)
        _binding = FragmentPassagesChoiceBinding.inflate(inflater,container,false)
        val view = binding.root

        setVisibilityRecentOffer()
        setVisibilityRequestNewPassage()

        binding.passageRequestButtonChoisePassages.setOnClickListener {
            if(typeUser=="requester"){
                val intent = Intent(activity,MyPassageSummaryActivity::class.java)
                startActivity(intent)
            }else{
                val intent = Intent(activity,RequestPassageActivity::class.java)
                startActivity(intent)
            }
        }

        binding.passageProvideButtonChoisePassages.setOnClickListener {
            val intent = Intent(activity,PassageProvideActivity::class.java)
            startActivity(intent)
        }

        binding.recentOffertsButtonChoisePassages.setOnClickListener {
            //if(typeUser.equals("requester")){
                val intent = Intent(activity,ReceivedOffersActivity::class.java)
                startActivity(intent)
            //}
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

    private fun setVisibilityRecentOffer(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("received_offers/$uid")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    binding.recentOffertsButtonChoisePassages.visibility = View.VISIBLE
                }else{
                    val ref2 = FirebaseDatabase.getInstance().getReference("made_offers/$uid")
                    ref2.addValueEventListener(object:ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.exists()){
                                binding.recentOffertsButtonChoisePassages.visibility = View.VISIBLE
                            }else{
                                binding.recentOffertsButtonChoisePassages.visibility = View.INVISIBLE
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun setVisibilityRequestNewPassage(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("received_offers/$uid")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()){
                    binding.newRequestPassageButtonPassageChoice.visibility = View.GONE
                }else{
                    snapshot.children.forEach{
                        val offer = it.getValue(Offer::class.java)
                        if(offer!!.state.equals("accepted")){
                            binding.newRequestPassageButtonPassageChoice.visibility = View.VISIBLE
                        }else{
                            binding.newRequestPassageButtonPassageChoice.visibility = View.GONE
                            return
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}