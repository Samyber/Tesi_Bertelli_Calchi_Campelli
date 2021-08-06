package com.samaeli.tesi.Passages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
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

    private var offer_wait = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_passages_choise, container, false)
        _binding = FragmentPassagesChoiceBinding.inflate(inflater,container,false)
        val view = binding.root

        //setVisibilityRecentOffer()
        //setVisibilityRequestNewPassage()

        binding.passageRequestButtonChoisePassages.setOnClickListener {
            if(offer_wait==true){
                Toast.makeText(activity,getString(R.string.already_offered),Toast.LENGTH_LONG).show()
            }else {
                if (typeUser == "requester") {
                    val intent = Intent(activity, MyPassageSummaryActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(activity, RequestPassageActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        binding.passageProvideButtonChoisePassages.setOnClickListener {
            val intent = Intent(activity,PassageProvideActivity::class.java)
            startActivity(intent)
        }

        binding.recentOffertsButtonChoisePassages.setOnClickListener {
            if(typeUser.equals("requester")){
                val intent = Intent(activity,ReceivedOffersActivity::class.java)
                startActivity(intent)
            }else{
                val intent = Intent(activity,MadeOffersActivity::class.java)
                startActivity(intent)
            }
        }

        // Controlla se il passaggio è stato cancellato dal servizio in background
        checkPassageDelete()

        return view
    }

    override fun onResume() {
        super.onResume()
        setVisibilityRecentOffer()
        setRequestPassageButtonText()
        disableRequestPassageButton()
    }

    private fun disableRequestPassageButton(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("made_offers/$uid")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var control = false
                    snapshot.children.forEach {
                        val offer = it.getValue(Offer::class.java)
                        if(offer!!.state.equals("wait")){
                            offer_wait = true
                            control = true
                            return
                        }
                    }
                    if(control == false) {
                        offer_wait = false
                    }
                }else{
                    offer_wait = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
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
        var offer_exist = false
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("received_offers/$uid")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    snapshot.children.forEach {
                        val offer = it.getValue(Offer::class.java)
                        if(offer!!.visibility==true){
                            binding.recentOffertsButtonChoisePassages.visibility = View.VISIBLE
                            Log.d("Fragment choice","Recent Offers visible1")
                            offer_exist = true
                            return
                        }
                    }
                }
                if(offer_exist==false){
                    val ref2 = FirebaseDatabase.getInstance().getReference("made_offers/$uid")
                    ref2.addValueEventListener(object:ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.exists()){
                                binding.recentOffertsButtonChoisePassages.visibility = View.VISIBLE
                                Log.d("Fragment choice","Recent Offers visible")
                            }else{
                                binding.recentOffertsButtonChoisePassages.visibility = View.INVISIBLE
                                Log.d("Fragment choice","Recent Offers invisible")
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

    private fun checkPassageDelete(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("passages/$uid")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                //binding.passageRequestButtonChoisePassages.text = activity!!.getString(R.string.passage_request)
                typeUser="bidder"
                binding.recentOffertsButtonChoisePassages.visibility = View.INVISIBLE
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    /*private fun setVisibilityRequestNewPassage(){
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
    }*/
}