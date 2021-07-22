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

        /*if(FirebaseAuth.getInstance().uid==null){
            val intent = Intent(this,LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }*/

        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerMainActivity,AlcoholLevelFragment(),
                "AlcoholLevelFragment").commit()

        /*if(FirebaseAuth.getInstance().uid == null) {
            FirebaseAuth.getInstance().signInAnonymously()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerMainActivity,AlcoholLevelFragment(),
                                    "AlcoholLevelFragment").commit()
                        }
                    }
        }else{
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerMainActivity,AlcoholLevelFragment(),
                    "AlcoholLevelFragment").commit()
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        this.menu = menu
        menuInflater.inflate(R.menu.nav_menu,menu)

        if(FirebaseAuth.getInstance().uid != null){
        //if(!FirebaseAuth.getInstance().currentUser!!.email.isNullOrEmpty()){
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
                //Ricreo un utente anonimo
                //FirebaseAuth.getInstance().signInAnonymously()
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
            R.id.menuSettings -> {
                val intent = Intent(this,SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}