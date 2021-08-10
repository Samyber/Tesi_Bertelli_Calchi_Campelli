package com.samaeli.tesi.calculationBloodAlcohol

import android.graphics.Color
import android.graphics.Color.red
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.ActivityResultCalculationBinding
import com.samaeli.tesi.databinding.FragmentAlcoholLevelBinding
import kotlinx.android.synthetic.main.activity_result_calculation.*
import java.math.RoundingMode

// Activity che mostra il risultato del calcolo del tasso alcolemico
class ResultCalculationActivity : AppCompatActivity() {

    companion object{
        val TAG = "Result Calculation Activity"
    }

    private lateinit var binding : ActivityResultCalculationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultCalculationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val alcoholLevel = intent.getDoubleExtra("alcohol_level",0.0)
        val timeBeforeDriving = intent.getIntExtra("time_before_driving",0)

        Log.d(TAG,alcoholLevel.toString())
        Log.d(TAG,timeBeforeDriving.toString())

        // Arrotondamento del tasso alcolemico alla seconda cifra decimale
        val alcoholLevelTwoDecimal = alcoholLevel.toBigDecimal().setScale(2,RoundingMode.HALF_EVEN)

        binding.alcoholContentTextViewResultCalculation.text = getString(R.string.your_alcohol_content)+" "+alcoholLevelTwoDecimal+" mg/l"

        // Se il tempo che bisogna aspettare per potersi mettere alla guida è zero =>
        // Si mostra la schermata Seccess altrimenti not success
        if(timeBeforeDriving == 0){
            displaySuccess()
        }else{
            displayNotSuccess(timeBeforeDriving)
        }
    }

    // Metodo che permette di visualizzare la schemata che indica che il test è stato passato
    private fun displaySuccess(){
        binding.resultTextViewResultCalculation.text = getString(R.string.passed)
        binding.timeTextViewAlcoholContent.alpha = 0f
        binding.timeStringTextViewAlcoholContent.alpha = 0f
    }

    // Metodo che permette di visualizzare la schemata che indica che il test non è stato passato
    private fun displayNotSuccess(timeBeforeDriving : Int){
        binding.resultTextViewResultCalculation.text = getString(R.string.not_passed)
        constraintLayoutResultCalculation.setBackgroundColor(Color.RED)
        val hour : Int = (timeBeforeDriving/60)
        val minute : Int = timeBeforeDriving - (hour * 60)
        if(hour > 0) {
            binding.timeTextViewAlcoholContent.text = " $hour " + getString(R.string.hour) + " $minute " + getString(R.string.minute)
        }else{
            binding.timeTextViewAlcoholContent.text = " $minute " + getString(R.string.minute)
        }
    }
}