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
/*
    Fragment che viene visualizzato se l'utente clicca su Passages nel bottomNavigationView della
    Main Activity. Questo fragment mostra i bottoni tramite i quali un utente può richiedere un passagio,
    visualizzare i passaggi richiesti dagli altri utenti e visualizzare le offere recenti
 */

class PassagesChoiceFragment : Fragment() {

    private var _binding : FragmentPassagesChoiceBinding? = null
    private val binding get() = _binding!!

    private var typeUser :String = BIDDER

    // Variabile che vale true se l'utente ha fatto un'offerta che è nello stato di wait
    private var offer_wait = false

    companion object{
        val TAG = "Passages Choice Fragment"
        const val BIDDER = "bidder"
        const val REQUESTER = "requester"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentPassagesChoiceBinding.inflate(inflater,container,false)
        val view = binding.root

        binding.passageRequestButtonChoisePassages.setOnClickListener {
            // Se l'utente ha fatto un'offerta che è nello stato di wait non può richiedere un passaggio
            if(offer_wait==true){
                Toast.makeText(activity,getString(R.string.already_offered),Toast.LENGTH_LONG).show()
            }else {
                // Se l'utente ha richiesto un passaggio si apre la schermata del resoconto del passaggio chiesto
                    // altrimenti quella per poter richiedere un nuovo passaggio
                if (typeUser.equals(REQUESTER)) {
                    val intent = Intent(activity, MyPassageSummaryActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(activity, RequestPassageActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        // Si apre schermata con lista dei passaggi richiesti dagli altri utenti
        binding.passageProvideButtonChoisePassages.setOnClickListener {
            val intent = Intent(activity,PassageProvideActivity::class.java)
            startActivity(intent)
        }

        // Si apre schermata delle offerte recenti
        binding.recentOffertsButtonChoisePassages.setOnClickListener {
            if(typeUser.equals(REQUESTER)){
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

    // Metodo che ha il compito di impostare offer_wait a true se l'utente ha fatto un'offerta
    // che è nello stato di wait (In questo caso non deve poter richiedere un passaggio)
    private fun disableRequestPassageButton(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("made_offers/$uid")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var control = false
                    snapshot.children.forEach {
                        val offer = it.getValue(Offer::class.java)
                        if(offer!!.state.equals(Offer.WAIT)){
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

    // Metodo che cambia il testo del primo bottone in base a se l'utente ha già chiesto un passaggio
    // oppure no
    private fun setRequestPassageButtonText(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("passages/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()){
                    binding.passageRequestButtonChoisePassages.text = getString(R.string.passage_request)
                    typeUser = BIDDER
                }else{
                    binding.passageRequestButtonChoisePassages.text = getString(R.string.summary_required_passage)
                    typeUser = REQUESTER
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    // Metodo che ha il compito di cambiare la visibilità del bottone delle offerti recenti.
    // Se c'è anche solo una offerta ricevuta o fatta il bottone deve essere visibile, altrimenti
    // viene nascosto
    private fun setVisibilityRecentOffer(){
        var offer_exist = false
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("received_offers/$uid")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // Si controlla se è stata ricevuta un'offerta
                if(snapshot.exists()){
                    snapshot.children.forEach {
                        val offer = it.getValue(Offer::class.java)
                        if(offer!!.visibility==true){
                            binding.recentOffertsButtonChoisePassages.visibility = View.VISIBLE
                            Log.d(TAG,"Recent Offers visible1")
                            offer_exist = true
                            return
                        }
                    }
                }
                if(offer_exist==false){
                    val ref2 = FirebaseDatabase.getInstance().getReference("made_offers/$uid")
                    ref2.addValueEventListener(object:ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            // Si controlla se è stata fatta un'offerta
                            if(snapshot.exists()){
                                binding.recentOffertsButtonChoisePassages.visibility = View.VISIBLE
                                Log.d(TAG,"Recent Offers visible")
                            }else{
                                binding.recentOffertsButtonChoisePassages.visibility = View.INVISIBLE
                                Log.d(TAG,"Recent Offers invisible")
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

    // Controlla se il passaggio è stato cancellato dal servizio in background
    private fun checkPassageDelete(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("passages/$uid")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                typeUser=BIDDER
                binding.recentOffertsButtonChoisePassages.visibility = View.INVISIBLE
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

}