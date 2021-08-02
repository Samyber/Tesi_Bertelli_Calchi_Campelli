package com.samaeli.tesi.Passages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.ActivityPassageProvideBinding
import com.samaeli.tesi.models.Passage
import com.samaeli.tesi.models.PassageItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

class PassageProvideActivity : AppCompatActivity() {

    private lateinit var binding : ActivityPassageProvideBinding

    companion object{
        val TAG = "Passage Provide Activity"
        val PASSAGE_KEY = "PASSAGE_KEY"
    }

    private val adapter = GroupAdapter<ViewHolder>()

    private var departureCity : String? = null
    private var arrivalCity : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_passage_provide)
        binding = ActivityPassageProvideBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.passage_provide)
        }

        binding.searchButtonPassageProvide.setOnClickListener {
            /*if(!validateFields()){
                return@setOnClickListener
            }*/
            departureCity = binding.departureCityEditProvidePassage.text.toString()
            arrivalCity = binding.arrivalCityEditProvidePassage.text.toString()
            fetchPassage()
        }

        // TODO In onResume?
        fetchPassage()

        adapter.setOnItemClickListener { item, view ->
            val passageItem = item as PassageItem

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

    /*private fun validateFields():Boolean{
        departureCity = binding.departureCityEditProvidePassage.text.toString()
        arrivalCity = binding.arrivalCityEditProvidePassage.text.toString()

        if((departureCity.isNullOrEmpty() || departureCity!!.isBlank()) &&
            (arrivalCity.isNullOrEmpty() || departureCity!!.isBlank())){
            Toast.makeText(this,getString(R.string.error_search_passage),Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }*/

    private fun fetchPassage(){
        adapter.clear()
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("passages/")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val passage = it.getValue(Passage::class.java)
                    if(passage != null && passage.uid != uid){
                        if(!arrivalCity.isNullOrEmpty() && !arrivalCity!!.isBlank()){
                            if(!departureCity.isNullOrEmpty() && !departureCity!!.isBlank()){
                                // Utente ha inserito entrambe le città
                                addPassageItemArrivalDeparture(passage,arrivalCity!!,departureCity!!)
                            }else{
                                // Utente ha inserito solo arrival city
                                addPassageItemArrival(passage, arrivalCity!!)
                            }
                        }else {
                            if (!departureCity.isNullOrEmpty() && !departureCity!!.isBlank()) {
                                // Utente ha inserito solo departure city
                                addPassageItemDeparture(passage, departureCity!!)
                            } else {
                                // utente non ha inserito nulla
                                addPassageItem(passage)
                            }
                        }
                        /*Log.d(TAG,"UID Passage: ${passage.uid}")
                        adapter.add(PassageItem(passage))*/
                    }
                }
                showHideNoResult()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun showHideNoResult(){
        if(adapter.itemCount==0){
            binding.noResultTextViewPassageProvide.alpha = 1f
            binding.passagesRecyclerViewPassageProvide.alpha = 0f
        }else{
            binding.noResultTextViewPassageProvide.alpha = 0f
            binding.passagesRecyclerViewPassageProvide.alpha = 1f
        }
    }

    private fun addPassageItem(passage: Passage){
        Log.d(TAG,"UID Passage: ${passage.uid}")
        adapter.add(PassageItem(passage))
    }

    private fun addPassageItemArrival(passage: Passage,arrivalCity:String){
        if(passage.arrivalCity.contains(arrivalCity,true)){
            Log.d(TAG,"UID Passage: ${passage.uid}")
            adapter.add(PassageItem(passage))
        }
    }

    private fun addPassageItemDeparture(passage: Passage,departureCity:String){
        if(passage.departureCity.contains(departureCity,true)){
            Log.d(TAG,"UID Passage: ${passage.uid}")
            adapter.add(PassageItem(passage))
        }
    }

    private fun addPassageItemArrivalDeparture(passage: Passage,arrivalCity:String,departureCity: String){
        if(passage.arrivalCity.contains(arrivalCity,true) &&
                passage.departureCity.contains(departureCity,true)){
            Log.d(TAG,"UID Passage: ${passage.uid}")
            adapter.add(PassageItem(passage))
        }
    }

}