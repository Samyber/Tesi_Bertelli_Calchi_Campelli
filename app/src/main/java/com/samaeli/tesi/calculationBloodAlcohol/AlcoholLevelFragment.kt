package com.samaeli.tesi.calculationBloodAlcohol

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.FragmentAlcoholLevelBinding
import com.samaeli.tesi.models.DrinkAdded
import com.samaeli.tesi.models.DrinkAddedItem
import com.samaeli.tesi.models.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.ViewHolder
import java.lang.Double.parseDouble
import java.util.*
import kotlin.collections.ArrayList

/*
    Fragment che ha il compito di effettuare il calcolo del tasso alcolemico dell'utente
    sulla base dei drink che ha inserito
 */

class AlcoholLevelFragment : Fragment() {

    private var _binding : FragmentAlcoholLevelBinding? = null
    private val binding get() = _binding!!

    private var prefs : SharedPreferences? = null

    // Variabile che contiene il limite del tasso alcolemico per far guidare i NON neopatentati
    private var limitOldDriver : Double = 0.5
    // Variabile che contiene il limite del tasso alcolemico per far guidare i neopatentati
    private var limitNewDriver : Double = 0.0

    companion object {
        val TAG = "Alcohol Level Fragment"

        // Parametri forniti dall'Istituto superiore della sanità per il calcolo del tasso alcolemico
        const val MALE_EMPTY_STOMACH = 0.7
        const val MALE_FULL_STOMACH = 1.2
        const val FEMALE_EMPTY_STOMACH = 0.5
        const val FEMALE_FULL_STOMACH = 0.9

        // 3 years in milliseconds for the new drivers
        const val YEARS_3 = 94672800000
        // 21 years in milliseconds for the new drivers
        const val YEARS_21 = 662709600000
        // mg/l di alcol che si smaltiscono ogni minuto
        const val DIGESTION_MINUTE = 0.0025
        const val MINUTE_IN_A_DAY = 60 * 24
        private var drinksAdded : ArrayList<DrinkAdded>? = null

        var db: DrinkAddedDB? = null
        var adapter = GroupAdapter<ViewHolder>()

        // Metodo che visualizza i drink che sono stati aggiunti da un utente
        fun displayDrinkAdded(context: Context){
            adapter.clear()
            drinksAdded = db!!.getDrinksAdded()
            if(drinksAdded!!.size > 0){
                for(drinkAdded in drinksAdded!!){
                    adapter.add(DrinkAddedItem(drinkAdded,context))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        PreferenceManager.setDefaultValues(activity,R.xml.root_preferences,false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentAlcoholLevelBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.buttonAddDrink.setOnClickListener {
            // Go to SelectDrinkActivity
            val intent = Intent(activity,SelectDrinkActivity::class.java)
            startActivity(intent)
        }

        binding.buttonAddCustomDrink.setOnClickListener {
            // Go to DrinkActivity
            val intent = Intent(activity,DrinkActivity::class.java)
            startActivity(intent)
        }

        binding.calculateButtonAlcoholLevel.setOnClickListener {
            // Inizio calcolo del tasso alcolemico
            Log.d(TAG,"Inizio calcolo tasso alcolemico")
            if(!checkWeight()){
                Log.d(TAG,getString(R.string.error_weight))
                Toast.makeText(activity,getString(R.string.error_weight),Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            calculateAlcoholLevel()
        }

        //  Oggetto di tipo DrinkAddedDb per eseguire le query su SQLite Database
        db = DrinkAddedDB(requireActivity().applicationContext)

        // display drink aggiunti dall'utente
        displayDrinkAdded(requireActivity().applicationContext)
        binding.recyclerViewAlcoholLevelFragmnet.adapter = adapter

        return view
    }

    override fun onResume() {
        super.onResume()
        // Se l'utente è registrato si caricano i valori con quelli inseriti dall'utente nel suo profilo
        if(FirebaseAuth.getInstance().uid != null) {
            completeFieldsFromFirebase()
        }
        // Si caricano i valori da SharedPreference
        completeFieldsFromSharedPreference()
        limitOldDriver = parseDouble(prefs?.getString("limit_edit_text_preference","0.5"))
        limitNewDriver = parseDouble(prefs?.getString("limit_new_driver_edit_text_preference","0.0"))
        Log.d(TAG,"Limit old driver: $limitOldDriver")
        Log.d(TAG,"Limit new driver: $limitNewDriver")
    }

    override fun onPause() {
        val editor = prefs?.edit()
        // Salvataggio dei valori inseriti dall'utente in modo che vengano ripristinati se si ruota lo schermo
        if(binding.genderRadioGroupAlcoholLevel.checkedRadioButtonId == R.id.maleRadioButtonAlcoholLevel){
            editor?.putString("gender","male")
        }else{
            editor?.putString("gender","female")
        }
        if(binding.questionFullStomachRadioGroupAlcoholLevel.checkedRadioButtonId == R.id.yesFullStomachRadioButtonAlcoholLevel){
            editor?.putString("full_stomach","yes")
        }else{
            editor?.putString("full_stomach","no")
        }
        if(binding.newDriverRadioGroupAlcoholLevel.checkedRadioButtonId == R.id.yesNewDriverRadioButtonAlcoholLevel){
            editor?.putString("new_driver","yes")
        }else{
            editor?.putString("new_driver","no")
        }
        editor?.putString("weight",binding.weightEditTextAlcoholLevel.text.toString())
        editor?.commit()
        super.onPause()
    }

    // Calcolo il tasso alcolemico dell'utente
    private fun calculateAlcoholLevel(){
        // Controllo che l'utente abbia inserito almeno un drink
        if(drinksAdded == null || drinksAdded!!.size == 0){
            Toast.makeText(activity,getString(R.string.error_enter_drink),Toast.LENGTH_LONG).show()
            return
        }
        val c = calculateCParameter()
        var alcoholLevelFinal : Double = 0.0
        val weight : Double = binding.weightEditTextAlcoholLevel.text.toString().toDouble()

        // For eseguito per ogni drink inserito dall'utente
        for(drinkAdded in drinksAdded!!){
            Log.d(TAG,"Ciclo")
            // Grammi di alcol = (ml di bevanda x grado alcolico x 0,79) / 100
            val gAlcohol : Double = (drinkAdded.drink!!.volume * drinkAdded.quantity * drinkAdded.drink!!.alcoholContent * 0.79)/100
            var alcoholLevel : Double = gAlcohol/(weight * c)
            Log.d(TAG,"first alcohol level "+alcoholLevel.toString())
            // Quando si è assunto il drink
            val timeMinute : Int = drinkAdded.hour * 60 + drinkAdded.minute
            // Ora attuale
            val nowMinute : Int = Calendar.getInstance().get(Calendar.MINUTE) +
                    (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)*60)
            Log.d(TAG,"timeMinute: "+timeMinute+" now minute "+nowMinute)

            // Quanto è passato da quando si è assunto il drink ad ora
            /*val differenceTime : Int = if(timeMinute < nowMinute){
                nowMinute - timeMinute
            }else{
                timeMinute - nowMinute
            }*/
            val differenceTime : Int = if(timeMinute < nowMinute){
                nowMinute - timeMinute
            }else{
                (nowMinute + MINUTE_IN_A_DAY) - timeMinute
            }
            // Calcolo del tasso alcolemico che il drink ha dato all'utente tenendo conto del tempo
            // che è trascorso da quando l'ha assunto
            alcoholLevel -= (DIGESTION_MINUTE * differenceTime)
            // Se maggiore di zero lo sommo al tasso alcolemico totale
            if(alcoholLevel > 0){
                alcoholLevelFinal += alcoholLevel
            }
        }
        Log.d(TAG,alcoholLevelFinal.toString())

        // Calcolo il limite del tasso alcolemico
        val limit = calculateLimit()

        // Calcolo quanto tempo bisogna aspettare per potersi rimettere alla guida
        val time = calculateTimeMinuteBeforeDriving(alcoholLevelFinal,limit)

        // Go to ResultCalculationActivity
        val intent = Intent(activity,ResultCalculationActivity::class.java)
        intent.putExtra("alcohol_level",alcoholLevelFinal)
        intent.putExtra("time_before_driving",time)
        startActivity(intent)

        // Cancello i drink selezionati dall'utente dal DB locale
        db!!.deleteDrinksAdded()
    }

    // Metodo che calcola quanto tempo l'utente deve aspettare per potersi rimettere alla guida
    private fun calculateTimeMinuteBeforeDriving(alcoholLevelFinal : Double,limit:Double):Int{
        val time  = if(alcoholLevelFinal > limit){
                (alcoholLevelFinal - limit)/ DIGESTION_MINUTE
        }else{
            0.0
        }
        return time.toInt()
    }

    // Metodo che calcola il limite del tasso alcolemico per poter guidare
    private fun calculateLimit():Double{
        return if(binding.newDriverRadioGroupAlcoholLevel.checkedRadioButtonId == R.id.noNewDriverRadioButtonAlcoholLevel){
            limitOldDriver
        }else{
            limitNewDriver
        }
    }

    // Calcolo del parametro C che serve della formula del calcolo del tasso alcolemico
    private fun calculateCParameter() : Double{
        if(binding.genderRadioGroupAlcoholLevel.checkedRadioButtonId == R.id.maleRadioButtonAlcoholLevel){
            if(binding.questionFullStomachRadioGroupAlcoholLevel.checkedRadioButtonId == R.id.noFullStomachRadioButtonAlcoholLevel){
                return MALE_EMPTY_STOMACH
            }else{
                return MALE_FULL_STOMACH
            }
        }else{
            if(binding.questionFullStomachRadioGroupAlcoholLevel.checkedRadioButtonId == R.id.noFullStomachRadioButtonAlcoholLevel){
                return FEMALE_EMPTY_STOMACH
            }else{
                return FEMALE_FULL_STOMACH
            }
        }
    }

    // Se l'utente è loggato e si ruota lo schermo si mantiene memorizzzato solo se è a stomaco pieno oopure no.
    // Gli altri valori vengono rispristinati a quelli memorizzati nel profilo dell'utente
    // Metodo che ripristina i valori inseriti dall'utente se si ruota lo schermo
    private fun completeFieldsFromSharedPreference(){
        if(FirebaseAuth.getInstance().uid == null){
            val gender = prefs?.getString("gender","")
            if(!gender.equals("")){
                if(gender.equals("male")){
                    binding.genderRadioGroupAlcoholLevel.check(R.id.maleRadioButtonAlcoholLevel)
                }else{
                    binding.genderRadioGroupAlcoholLevel.check(R.id.femaleRadioButtonAlcoholLevel)
                }
            }
            val newDriver = prefs?.getString("new_driver","")
            if(!newDriver.equals("")){
                if(newDriver.equals("yes")){
                    binding.newDriverRadioGroupAlcoholLevel.check(R.id.yesNewDriverRadioButtonAlcoholLevel)
                }else{
                    binding.newDriverRadioGroupAlcoholLevel.check(R.id.noNewDriverRadioButtonAlcoholLevel)
                }
            }
            val weight = prefs?.getString("weight","")
            if(!weight.equals("")){
                binding.weightEditTextAlcoholLevel.setText(weight)
            }
        }
        val fullStomach = prefs?.getString("full_stomach","")
        if(!fullStomach.equals("")){
            if(fullStomach.equals("yes")){
                binding.questionFullStomachRadioGroupAlcoholLevel.check(R.id.yesFullStomachRadioButtonAlcoholLevel)
            }else{
                binding.questionFullStomachRadioGroupAlcoholLevel.check(R.id.noFullStomachRadioButtonAlcoholLevel)
            }
        }
    }

    // Metodo che compila i vari campi in base ai valori memorizzati nel profilo dell'utente
    private fun completeFieldsFromFirebase(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                binding.weightEditTextAlcoholLevel.setText(user!!.weight.toString())
                if(user.gender.equals("male")){
                    binding.genderRadioGroupAlcoholLevel.check(R.id.maleRadioButtonAlcoholLevel)
                }else{
                    binding.genderRadioGroupAlcoholLevel.check(R.id.femaleRadioButtonAlcoholLevel)
                }
                val timestamp = System.currentTimeMillis()
                // Controllo se l'utente è neopatentato oppure no
                if(timestamp - user.birthdayDate > YEARS_21 && timestamp - user.licenseDate > YEARS_3){
                    binding.newDriverRadioGroupAlcoholLevel.check(R.id.noNewDriverRadioButtonAlcoholLevel)
                    Log.d(TAG,"Old driver")
                }else{
                    binding.newDriverRadioGroupAlcoholLevel.check(R.id.yesNewDriverRadioButtonAlcoholLevel)
                    Log.d(TAG,"New driver")
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    // Controllo che l'utente abbia inserito il peso e che sia maggiore di 0
    private fun checkWeight():Boolean{
        val stringWeight = binding.weightEditTextAlcoholLevel.text.toString()

        if(stringWeight == null || stringWeight.isEmpty() || stringWeight.isBlank()){
            binding.weightEditTextAlcoholLevel.error = getString(R.string.field_not_empty)
            return false
        }
        val weight = stringWeight.toDouble()
        if(weight <= 0.0){
            binding.weightEditTextAlcoholLevel.error = getString(R.string.error_weight)
            return false
        }
        binding.weightEditTextAlcoholLevel.error = null
        return true
    }

}