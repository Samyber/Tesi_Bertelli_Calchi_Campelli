package com.samaeli.tesi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.samaeli.tesi.calculationBloodAlcohol.AlcoholLevelFragment
import com.samaeli.tesi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object{
        val TAG = "Main Activity"
    }

    private var menu : Menu? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerMainActivity,AlcoholLevelFragment(),
                "AlcoholLevelFragment").commit()

        /*FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener {
                    if(it.isSuccessful) {
                        if (FirebaseAuth.getInstance().currentUser!!.email == null) {
                            Log.d(TAG, "ciaoooooooooooooooooooooooooooooooo Anonymous user")
                            Log.d(TAG, "ciaoooooooooooooooooooooooooooooooo"+FirebaseAuth.getInstance().uid)
                        }
                        FirebaseAuth.getInstance().currentUser!!.delete()
                    }
                }*/
        //FirebaseAuth.getInstance().currentUser!!.delete()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        this.menu = menu
        menuInflater.inflate(R.menu.nav_menu,menu)

        if(FirebaseAuth.getInstance().uid != null){
            this.menu!!.findItem(R.id.menuRegister).setVisible(false)
            this.menu!!.findItem(R.id.menuLogin).setVisible(false)
        }else{
            this.menu!!.findItem(R.id.menuLogout).setVisible(false)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menuLogout -> {
                val alcoholLevelFragment = supportFragmentManager.findFragmentByTag("AlcoholLevelFragment")
                if(alcoholLevelFragment == null || !alcoholLevelFragment.isVisible){
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerMainActivity,AlcoholLevelFragment(),
                            "AlcoholLevelFragment").commit()
                }
                FirebaseAuth.getInstance().signOut()
                menu!!.findItem(R.id.menuLogout).setVisible(false)
                menu!!.findItem(R.id.menuLogin).setVisible(true)
                menu!!.findItem(R.id.menuRegister).setVisible(true)
            }
            R.id.menuLogin -> {
                val intent = Intent(this,LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            R.id.menuRegister -> {
                val intent = Intent(this,RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}