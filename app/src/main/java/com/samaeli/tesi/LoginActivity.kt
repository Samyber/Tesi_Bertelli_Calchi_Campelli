package com.samaeli.tesi

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
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
        //setContentView(R.layout.activity_login)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Change color of startIcon of textInputLayout when their focused
        changeIconColor()

        binding.goToRegistrationLogin.setOnClickListener {
            Log.d(TAG,"Go to Register Activity")
            val intent = Intent(this,RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        binding.buttonLogin.setOnClickListener {
            error = false
            if(!validateLogin()){
                Log.d(TAG,getString(R.string.error_login))
                Toast.makeText(this,getString(R.string.error_login),Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            Log.d(TAG,"Campi ok")
            performLogin()
        }
    }

    private fun performLogin(){
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email!!,password!!)
            .addOnCompleteListener {
                if(!it.isSuccessful){
                    Log.d(TAG,getString(R.string.error_login))
                    Toast.makeText(this,getString(R.string.error_login),Toast.LENGTH_LONG).show()
                    return@addOnCompleteListener
                }
                Log.d(TAG,"Login effettuato con utente ${it.result?.user?.uid}")

                val intent = Intent(this,MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d(TAG,getString(R.string.error_login)+": ${it.message}")
                Toast.makeText(this,getString(R.string.error_login)+": ${it.message}",Toast.LENGTH_LONG).show()
            }
    }

    private fun validateLogin():Boolean{
        validateEmail()
        validatePassword()
        return !error
    }

    private fun validateEmail(){
        email = binding.emailInputLayoutLogin.editText?.text.toString()

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

    private fun validatePassword(){
        password = binding.passwordInputLayoutLogin.editText?.text.toString()

        if(password==null || password!!.isEmpty() || password!!.isBlank()){
            binding.passwordInputLayoutLogin.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.passwordInputLayoutLogin.error = ""
        binding.passwordInputLayoutLogin.isErrorEnabled = false
    }

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