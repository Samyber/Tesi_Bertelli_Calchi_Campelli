package com.samaeli.tesi

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.samaeli.tesi.databinding.ActivityLoginBinding
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding

    private var email : String? = null
    private var password : String? = null
    private var error : Boolean = false

    companion object{
        val TAG = "Login Activity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Change color of startIcon of textInputLayout when their focused
        changeIconColor()

        // Go to Register Activity
        binding.goToRegistrationLogin.setOnClickListener {
            Log.d(TAG,"Go to Register Activity")
            val intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.buttonLogin.setOnClickListener {
            error = false // variabile che vale true se si è verificato almeno un errore durante la registrazione
            if(!validateLogin()){
                Log.d(TAG,getString(R.string.error_login))
                Toast.makeText(this,getString(R.string.error_login),Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            Log.d(TAG,"Fields are ok")
            performLogin()
        }

        // Go to MainActivity and Alcohol Level Fragment
        binding.alcoholCalculatorTextViewLogin.setOnClickListener {
            Log.d(TAG,"Main Activity")
            finish()
        }
    }


    private fun performLogin(){
        // Login dell'utente su Firebase
        val instance = FirebaseAuth.getInstance()
        instance.signInWithEmailAndPassword(email!!,password!!)
                .addOnSuccessListener {
                    Log.d(TAG,"Login effettuato con utente ${it.user?.uid}")

                    // Viene fatto partire il servizio in background
                    val intent = Intent(this,DeletePassageAndNotificationService::class.java)
                    startService(intent)

                    // Go to MainActivity
                    finish()
            }
            .addOnFailureListener {
                if(it is FirebaseAuthInvalidCredentialsException){
                    Log.d(TAG,getString(R.string.error_email_password_incorrect))
                    Toast.makeText(this,getString(R.string.error_email_password_incorrect),Toast.LENGTH_LONG).show()
                }else {
                    Log.d(TAG, getString(R.string.error_login) + ": ${it.message}")
                    Toast.makeText(this, getString(R.string.error_login) + ": ${it.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Metodo che ritorna false se in dati inseriti dall'utente non sono validi altrimenti ritorna true
    private fun validateLogin():Boolean{
        validateEmail()
        validatePassword()
        return !error
    }

    // Controllo che l'utente abbia inserito una mail valida
    private fun validateEmail(){
        email = binding.emailEditTextLogin.text.toString()

        if(email==null || email!!.isEmpty() || email!!.isBlank()){
            binding.emailInputLayoutLogin.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailInputLayoutLogin.error = getString(R.string.error_email)
            error = true
            return
        }
        binding.emailInputLayoutLogin.error = ""
        binding.emailInputLayoutLogin.isErrorEnabled = false
    }

    // Controllo che l'utente abbia inserito una password valida
    private fun validatePassword(){
        password = binding.passwordEditTextLogin.text.toString()

        if(password==null || password!!.isEmpty() || password!!.isBlank()){
            binding.passwordInputLayoutLogin.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.passwordInputLayoutLogin.error = ""
        binding.passwordInputLayoutLogin.isErrorEnabled = false
    }

    // Metodo che cambia colore alla startIcon del TextInputLayout quando è evidenziato
    private fun changeIconColor(){
        binding.emailEditTextLogin.setOnFocusChangeListener { v, hasFocus ->
            val color = if(hasFocus) Color.rgb(249,170,51) else Color.rgb(52,73,85)
            binding.emailInputLayoutLogin.setStartIconTintList(ColorStateList.valueOf(color))
        }
        binding.passwordEditTextLogin.setOnFocusChangeListener { v, hasFocus ->
            val color = if(hasFocus) Color.rgb(249,170,51) else Color.rgb(52,73,85)
            binding.passwordInputLayoutLogin.setStartIconTintList(ColorStateList.valueOf(color))
        }
    }
}