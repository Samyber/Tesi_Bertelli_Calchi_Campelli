package com.samaeli.tesi.calculationBloodAlcohol

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.SearchView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.ActivitySelectDrinkBinding
import com.samaeli.tesi.models.Drink
import com.samaeli.tesi.models.DrinkItem
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder

// Activity che ha il compito di mostrare la lista dei drink che l'utente può scegliere

class SelectDrinkActivity : AppCompatActivity() {

    companion object{
        val TAG = "Select Drink Activity"
        val DRINK_KEY = "DRINK_KEY"
    }

    var adapter = GroupAdapter<ViewHolder>()

    private lateinit var binding : ActivitySelectDrinkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_select_drink)
        binding = ActivitySelectDrinkBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.title = getString(R.string.select_drink)

        // Go to DrinkActivity
        adapter.setOnItemClickListener { item, view ->
            val drinkItem = item as DrinkItem

            val intent = Intent(view.context,DrinkActivity::class.java)
            intent.putExtra(DRINK_KEY,drinkItem.drink)
            startActivity(intent)
        }
        binding.recyclerViewSelectDrink.adapter = adapter

        fetchDrinks(null)


    }

    // Metodo che gestisce la barra di ricerca dei drink
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search,menu)

        val search = menu?.findItem(R.id.menuSearch)
        val searchView = search?.actionView as SearchView
        searchView.queryHint = getString(R.string.search_drink)

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d(TAG,"Search text changed")
                adapter.clear()
                fetchDrinks(newText)
                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }

    // Metodo che visualizza i drink che sono presenti su Firebase
    private fun fetchDrinks(search:String?){
        val ref = FirebaseDatabase.getInstance().getReference("/drinks")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //adapter.clear()
                snapshot.children.forEach {
                    val drink = it.getValue(Drink::class.java)
                    //Log.d(TAG,it.toString())
                    if(drink != null && (search==null || drink.name.contains(search,true))){
                        Log.d(TAG,"Drink name: "+drink.name)
                        Log.d(TAG,"Image url: "+drink.imageUrl)
                        adapter.add(DrinkItem(drink))
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
}