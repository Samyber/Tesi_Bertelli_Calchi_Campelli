package com.samaeli.tesi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.samaeli.tesi.Passages.PassagesChoiceFragment
import com.samaeli.tesi.calculationBloodAlcohol.AlcoholLevelFragment
import com.samaeli.tesi.databinding.ActivityMainBinding

/*
    Activity principale che contiene la bottomNavigationView e in cui vengono visualizzati i vari
    fragment in base al bottone che viene premuto
 */

class MainActivity : AppCompatActivity() {

    companion object{
        val TAG = "Main Activity"
    }

    private var menu : Menu? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Display AlcoholLevelFragment
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerMainActivity,AlcoholLevelFragment(),
                "AlcoholLevelFragment").commit()

        binding.bottomNavigationView.selectedItemId = R.id.bottomNavigationAlcoholLevel
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.bottomNavigationAlcoholLevel ->{
                    val alcoholLevelFragment = supportFragmentManager.findFragmentByTag("AlcoholLevelFragment")
                    // Se l'AlcoholLevelFragment non è visibile lo si ricarica
                    if(alcoholLevelFragment == null || !alcoholLevelFragment.isVisible){
                        Log.d(TAG,"Try to show AlcoholLevelFragment")
                        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerMainActivity,AlcoholLevelFragment(),
                            "AlcoholLevelFragment").commit()
                    }
                }
                R.id.bottomNavigationPassages ->{
                    // Se l'utente non è loggato lo si rimanda all'activity del Login
                    if(FirebaseAuth.getInstance().uid == null){
                        val intent = Intent(this,LoginActivity::class.java)
                        startActivity(intent)
                    }else{
                        val passagesChoiceFragment = supportFragmentManager.findFragmentByTag("PassagesChoiceFragment")
                        // Se il PassageChoiceFragment non è visibile lo si ricarica
                        if(passagesChoiceFragment == null || !passagesChoiceFragment.isVisible) {
                            Log.d(TAG, "Try to show PassagesChoiceFragment")
                            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerMainActivity,
                                    PassagesChoiceFragment(),
                                    "PassagesChoiceFragment").commit()
                        }
                    }
                }
                R.id.bottomNavigationProfile ->{
                    // Se l'utente non è loggato lo si rimanda all'activity del Login
                    if(FirebaseAuth.getInstance().uid == null){
                        val intent = Intent(this,LoginActivity::class.java)
                        startActivity(intent)
                    }else {
                        val profileFragment = supportFragmentManager.findFragmentByTag("ProfileFragment")
                        // Se il ProfileFragment non è visibile lo si ricarica
                        if (profileFragment == null || !profileFragment.isVisible) {
                            Log.d(TAG, "Try to show ProfileFragment")
                            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerMainActivity, ProfileFragment(),
                                    "ProfileFragment").commit()
                        }
                    }
                }
            }
            true
        }

    }

    override fun onResume() {
        super.onResume()
        val alcoholLevelFragment = supportFragmentManager.findFragmentByTag("AlcoholLevelFragment")
        val passageChoiceFragment = supportFragmentManager.findFragmentByTag("PassagesChoiceFragment")
        val profileFragment = supportFragmentManager.findFragmentByTag("ProfileFragment")

        // If eseguito se l'AlcoholLevelFragment è visibile ma il bottone selezionato nella BottomNavigationView
        // è diverso si seleziona quello dell'AlcoholLevel.
        // Serve quando l'utente clicca il bottone MyProfile o Passages senza essere loggato
        if(alcoholLevelFragment != null && alcoholLevelFragment.isVisible && binding.bottomNavigationView.selectedItemId != R.id.bottomNavigationAlcoholLevel){
            binding.bottomNavigationView.selectedItemId = R.id.bottomNavigationAlcoholLevel
        }

        // Metodo che viene chiamato in onResume in modo tale che dopo che l'utente si è loggato o registrato e viene
        // fatto il resume della MainActivity il menu cambi mostrando solo le voci opportune
        displayCorrectItemsMenu()

        // Visualizzazione del fragment corretto se l'utente ruota il dispositivo
        if(binding.bottomNavigationView.selectedItemId == R.id.bottomNavigationPassages && (passageChoiceFragment==null || !passageChoiceFragment.isVisible)){
            Log.d(TAG,"Try to show PassagesChoiceFragment")
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerMainActivity,
                    PassagesChoiceFragment(),
                    "PassagesChoiceFragment").commit()
        }else if(binding.bottomNavigationView.selectedItemId == R.id.bottomNavigationProfile && (profileFragment==null || !profileFragment.isVisible)){
            Log.d(TAG, "Try to show ProfileFragment")
            supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerMainActivity, ProfileFragment(),
                    "ProfileFragment").commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.nav_menu,menu)
        displayCorrectItemsMenu()
        return super.onCreateOptionsMenu(menu)
    }

    // Metodo che il compito di far visualizzare le voci del menù opportune in base a se l'utente è
    // loggato oppure no
    private fun displayCorrectItemsMenu(){
        if(menu != null) {
            if (FirebaseAuth.getInstance().uid != null) {
                // Se l'utente è registrato si nascondono dal menu le voci login e Register
                this.menu!!.findItem(R.id.menuRegister).setVisible(false)
                this.menu!!.findItem(R.id.menuLogin).setVisible(false)
                this.menu!!.findItem(R.id.menuLogout).setVisible(true)
            } else {
                // Se l'utente non è registrato si nasconde dal menu la voce logout
                this.menu!!.findItem(R.id.menuLogout).setVisible(false)
                this.menu!!.findItem(R.id.menuRegister).setVisible(true)
                this.menu!!.findItem(R.id.menuLogin).setVisible(true)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menuLogout -> {
                val alcoholLevelFragment = supportFragmentManager.findFragmentByTag("AlcoholLevelFragment")
                // Se l'AlcoholLevelFragment non è visibile lo si ricarica
                if(alcoholLevelFragment == null || !alcoholLevelFragment.isVisible){
                    supportFragmentManager.beginTransaction().replace(R.id.fragmentContainerMainActivity,AlcoholLevelFragment(),
                            "AlcoholLevelFragment").commit()
                }
                // Logout dell'utente da Firebase
                FirebaseAuth.getInstance().signOut()

                // Si nasconde dal menu la voce di logout e si rendono visibili le voci Login e Register
                displayCorrectItemsMenu()

                // Stop del servizio in background
                val intent = Intent(this, DeletePassageAndNotificationService::class.java)
                stopService(intent)
            }
            R.id.menuLogin -> {
                // Go to Login Activity
                val intent = Intent(this,LoginActivity::class.java)
                startActivity(intent)
            }
            R.id.menuRegister -> {
                // Go to Register Activity
                val intent = Intent(this,RegisterActivity::class.java)
                startActivity(intent)
            }
            R.id.menuSettings -> {
                // Go to settings activity
                val intent = Intent(this,SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

}