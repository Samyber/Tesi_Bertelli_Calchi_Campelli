package com.samaeli.tesi.calculationBloodAlcohol

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
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

class SelectDrinkActivity : AppCompatActivity() {

    companion object{
        val TAG = "Select Drink Activity"
        val DRINK_KEY = "DRINK_KEY"
    }

    private lateinit var binding : ActivitySelectDrinkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_select_drink)
        binding = ActivitySelectDrinkBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.title = getString(R.string.select_drink)
        fetchDrinks()
    }

    private fun fetchDrinks(){
        val ref = FirebaseDatabase.getInstance().getReference("/drinks")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val adapter = GroupAdapter<ViewHolder>()
                snapshot.children.forEach {
                    val drink = it.getValue(Drink::class.java)
                    //Log.d(TAG,it.toString())
                    if(drink != null){
                        Log.d(TAG,"Drink name: "+drink.name)
                        Log.d(TAG,"Image url: "+drink.imageUrl)
                        adapter.add(DrinkItem(drink))
                    }
                }
                adapter.setOnItemClickListener { item, view ->
                    val drinkItem = item as DrinkItem

                    val intent = Intent(view.context,DrinkActivity::class.java)
                    intent.putExtra(DRINK_KEY,drinkItem.drink)
                    startActivity(intent)
                }
                binding.recyclerViewSelectDrink.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}