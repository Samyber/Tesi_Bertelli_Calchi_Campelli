package com.samaeli.tesi.Passages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.ActivityPassageProvideBinding
import com.samaeli.tesi.models.Passage
import com.samaeli.tesi.models.PassageItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
/*
    Activity che ha il compito di visualizzare la lista dei passaggi che sono stati richiesti dagli utenti
 */
class PassageProvideActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPassageProvideBinding

    companion object{
        val TAG = "Passage Provide Activity"
        val PASSAGE_KEY = "PASSAGE_KEY"
    }

    private val adapter = GroupAdapter<ViewHolder>()
    // HasMap che contiene tutti i passaggi che devono essere visualizzati
    private val passagesMap = HashMap<String, Passage>()

    private var departureCity : String? = null
    private var arrivalCity : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPassageProvideBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.passage_provide)
        }

        binding.searchButtonPassageProvide.setOnClickListener {
            departureCity = binding.departureCityEditProvidePassage.text.toString()
            arrivalCity = binding.arrivalCityEditProvidePassage.text.toString()
            refreshRecyclerView()
        }

        fetchPassage()

        adapter.setOnItemClickListener { item, view ->
            val passageItem = item as PassageItem
            // Go to PassageSummaryActivity
            val intent = Intent(this,PassageSummaryActivity::class.java)
            intent.putExtra(PASSAGE_KEY,passageItem.passage)
            startActivity(intent)
        }

        binding.passagesRecyclerViewPassageProvide.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    // Metodo che ha il compito di prelevare i passaggi da firebase che devono essere visualizzati
    private fun fetchPassage(){
        val ref = FirebaseDatabase.getInstance().getReference("passages/")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val passage = snapshot.getValue(Passage::class.java) ?: return
                passagesMap[snapshot.key!!] = passage
                refreshRecyclerView()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val passage = snapshot.getValue(Passage::class.java) ?: return
                passagesMap[snapshot.key!!] = passage
                refreshRecyclerView()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Se il passaggio era presente nell'HashMap, viene cancellato
                if(passagesMap.containsKey(snapshot.key!!)){
                    passagesMap.remove(snapshot.key!!)
                }
                refreshRecyclerView()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    // Metodo che ha il compito di verificare che un passaggio rispetti i vincoli di ricerca
    private fun checkPassage(passage:Passage){
        val uid = FirebaseAuth.getInstance().uid
        if (passage != null && passage.uid != uid && passage.visibility == true) {
            if (!arrivalCity.isNullOrEmpty() && !arrivalCity!!.isBlank()) {
                if (!departureCity.isNullOrEmpty() && !departureCity!!.isBlank()) {
                    // Utente ha inserito nella ricerca entrambe le città
                    showPassageItemArrivalDeparture(passage, arrivalCity!!, departureCity!!)
                } else {
                    // Utente ha inserito nella ricerca solo arrival city
                    showPassageItemArrival(passage, arrivalCity!!)
                }
            } else {
                if (!departureCity.isNullOrEmpty() && !departureCity!!.isBlank()) {
                    // Utente ha inserito nella ricerca solo departure city
                    showPassageItemDeparture(passage, departureCity!!)
                } else {
                    // utente non ha inserito nessuna città nella ricerca
                    showPassageItem(passage)
                }
            }
        }
    }

    // Metodo che ha il compito di aggiornare il recyclerView
    private fun refreshRecyclerView(){
        adapter.clear()
        passagesMap.values.forEach {
            checkPassage(it)
        }
        showHideNoResult()
    }

    // Se il numero di passaggi da visualizzare è zero si stampa la sctitta "No result"
    private fun showHideNoResult(){
        if(adapter.itemCount==0){
            binding.noResultTextViewPassageProvide.alpha = 1f
            binding.passagesRecyclerViewPassageProvide.alpha = 0f
        }else{
            binding.noResultTextViewPassageProvide.alpha = 0f
            binding.passagesRecyclerViewPassageProvide.alpha = 1f
        }
    }

    // Metodo che ha il compito di visualizzare un passaggio se non è stata fatta nessun tipo di ricerca
    private fun showPassageItem(passage: Passage){
        Log.d(TAG,"UID Passage: ${passage.uid}")
        adapter.add(PassageItem(passage))
    }

    // Metodo che ha il compito di visualizzare un passaggio se è stata ricercata la città di arrivo
    private fun showPassageItemArrival(passage: Passage,arrivalCity:String){
        if(passage.arrivalCity.contains(arrivalCity,true)){
            Log.d(TAG,"UID Passage: ${passage.uid}")
            adapter.add(PassageItem(passage))
        }
    }

    // Metodo che ha il compito di visualizzare un passaggio se è stata ricercata la città di partenza
    private fun showPassageItemDeparture(passage: Passage,departureCity:String){
        if(passage.departureCity.contains(departureCity,true)){
            Log.d(TAG,"UID Passage: ${passage.uid}")
            adapter.add(PassageItem(passage))
        }
    }

    // Metodo che ha il compito di visualizzare un passaggio se è stata ricercata sia la città di arrivo sia quella di partenza
    private fun showPassageItemArrivalDeparture(passage: Passage,arrivalCity:String,departureCity: String){
        if(passage.arrivalCity.contains(arrivalCity,true) &&
                passage.departureCity.contains(departureCity,true)){
            Log.d(TAG,"UID Passage: ${passage.uid}")
            adapter.add(PassageItem(passage))
        }
    }

}