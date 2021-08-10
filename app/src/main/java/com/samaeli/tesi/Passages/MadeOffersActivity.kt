package com.samaeli.tesi.Passages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.ActivityMadeOffersBinding
import com.samaeli.tesi.databinding.ActivityMainBinding
import com.samaeli.tesi.models.MadeOfferItem
import com.samaeli.tesi.models.Offer
import com.samaeli.tesi.models.Passage
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

class MadeOffersActivity : AppCompatActivity() {

    companion object{
        val TAG = "MadeOffersActivity"
    }

    private lateinit var binding : ActivityMadeOffersBinding
    private var adapter : GroupAdapter<ViewHolder>? = null

    private var madeOffersMap = HashMap<String,Offer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMadeOffersBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.made_offers)
        }

        adapter = GroupAdapter<ViewHolder>()

        binding.recyclerViewMadeOffers.adapter = adapter

        // Quando si clicca su un elemnto dell'adapter si apre la schermata del reseconto del passaggio a
        // cui si riferisce quell'offerta
        adapter!!.setOnItemClickListener { item, view ->
            val madeOfferItem = item as MadeOfferItem
            val offer = madeOfferItem.offer

            val ref = FirebaseDatabase.getInstance().getReference("passages/${offer.uidRequester}")
            ref.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Si controlla che il passaggio sia ancora presente
                    if(!snapshot.exists()) {
                        Toast.makeText(applicationContext,getString(R.string.error_passage_removed),Toast.LENGTH_LONG).show()
                        finish()
                    }else{
                        val passage = snapshot.getValue(Passage::class.java)
                        val intent = Intent(applicationContext, PassageSummaryActivity::class.java)
                        intent.putExtra(PassageProvideActivity.PASSAGE_KEY, passage)
                        startActivity(intent)
                    }
                    ref.removeEventListener(this)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }

    }

    override fun onResume() {
        super.onResume()
        displayMadeOffers()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    // Metodo che ha il compito di mostrare tutte le offerte che sono state fatte
    /*private fun displayMadeOffers(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("made_offers/$uid/")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                adapter!!.clear()
                snapshot.children.forEach {
                    val offer = it.getValue(Offer::class.java)
                    adapter!!.add(MadeOfferItem(offer!!,applicationContext))
                }
                // Se non ci sono offerte si termina l'activity
                if (adapter!!.itemCount==0 && applicationContext == MadeOffersActivity.javaClass){
                    Toast.makeText(applicationContext,getString(R.string.there_are_not_offers),Toast.LENGTH_LONG).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }*/

    private fun displayMadeOffers(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("made_offers/$uid")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val offer = snapshot.getValue(Offer::class.java) ?: return
                madeOffersMap[snapshot.key!!] = offer
                refreshRecyclerView()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val offer = snapshot.getValue(Offer::class.java) ?: return
                madeOffersMap[snapshot.key!!] = offer
                refreshRecyclerView()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                if(madeOffersMap.containsKey(snapshot.key!!)){
                    madeOffersMap.remove(snapshot.key)
                }
                refreshRecyclerView()
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun refreshRecyclerView(){
        adapter!!.clear()
        madeOffersMap.values.forEach {
            adapter!!.add(MadeOfferItem(it,applicationContext))
        }
        if(adapter!!.itemCount==0){
            Toast.makeText(applicationContext,getString(R.string.there_are_not_offers),Toast.LENGTH_LONG).show()
            finish()
        }
    }
}