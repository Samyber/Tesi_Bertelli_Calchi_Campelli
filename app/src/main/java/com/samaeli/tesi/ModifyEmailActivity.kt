package com.samaeli.tesi

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase
import com.samaeli.tesi.databinding.ActivityModifyEmailBinding
import com.samaeli.tesi.databinding.ActivityModifyPasswordBinding
import com.samaeli.tesi.models.User

class ModifyEmailActivity : AppCompatActivity() {

    companion object{
        val TAG = "Modify Email Activity"
    }

    private lateinit var binding : ActivityModifyEmailBinding

    var user : User? = null
    var oldEmail : String? = null
    var password : String? = null
    var newEmail : String? = null
    var error : Boolean = false

    private var loadingDialog : LoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_modify_email)

        binding = ActivityModifyEmailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        changeIconColor()
        loadingDialog = LoadingDialog(this)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.update_email)
        }

        user = intent.getParcelableExtra("user")

        binding.oldEmailEditTextModifyEmail.setText(user!!.email)

        binding.changeEmailButtonModifyEmail.setOnClickListener {
            Log.d(TAG,"Try to modify email")
            error = false
            if(!validateUpdateEmail()){
                Log.d(TAG,getString(R.string.error_update_email))
                Toast.makeText(this,getString(R.string.error_update_email),Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            Log.d(TAG, "Campi ok")
            // Start schermata di caricamento
            loadingDialog!!.startLoadingDialog()
            updateEmail()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    // Metodo che modifica la mail dell'utente su FirebaseAuth
    private fun updateEmail(){
        val credential = EmailAuthProvider.getCredential(oldEmail!!,password!!)
        val currentUser = FirebaseAuth.getInstance().currentUser
        // Reautenticazione dell'utente
        currentUser!!.reauthenticate(credential)
                .addOnSuccessListener {
                    // Modifica della mail
                    FirebaseAuth.getInstance().currentUser!!.updateEmail(newEmail!!)
                            .addOnSuccessListener {
                                user!!.email = newEmail!!
                                updateDatabase()
                            }
                            .addOnFailureListener {
                                // Stop schermata di caricamento
                                loadingDialog!!.dismissLoadingDialog()
                                // If che viene eseguito se la nuova mail inserita dall'utente esiste gia
                                if(it is FirebaseAuthUserCollisionException){
                                    //Toast.makeText(this, getString(R.string.error_update_email), Toast.LENGTH_LONG).show()
                                    Log.d(RegisterActivity.TAG, getString(R.string.error_update_email))
                                    binding.newEmailInputLayoutModifyEmail.error = getString(R.string.error_email_exist)
                                }else {
                                    Log.d(ProfileFragment.TAG, "Impossibile modificare email: ${it.message}")
                                    Toast.makeText(this, getString(R.string.error_update_email) + ": ${it.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                }
                .addOnFailureListener {
                    // Stop schermata di caricamento
                    loadingDialog!!.dismissLoadingDialog()
                    // If che viene eseguito se l'utente ha sbagliato la password
                    if(it is FirebaseAuthInvalidCredentialsException) {
                        binding.passwordInputLayoutModifyEmail.error = getString(R.string.error_password)
                    }else{
                        Toast.makeText(this,"Error: ${it.message}",Toast.LENGTH_LONG).show()
                    }
                    Log.d(TAG,"Error: ${it.message}")
                    //Toast.makeText(this,"Error: ${it.message}",Toast.LENGTH_LONG).show()
                }
    }

    // Modifica della mail dell'utente su FirebaseDatabase
    private fun updateDatabase(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d(TAG,"User saved in db")
                    Toast.makeText(this,getString(R.string.email_changed),Toast.LENGTH_LONG).show()
                    // Modifico mail di user in ProfileFragment prima che venga visualizzato
                    ProfileFragment.user!!.email = newEmail!!
                    // Stop schermata di caricamento
                    loadingDialog!!.dismissLoadingDialog()
                    finish()
                }
                .addOnFailureListener {
                    // Stop schermata di caricamento
                    loadingDialog!!.dismissLoadingDialog()
                    Log.d(TAG,"Impossibile modificare l'utente nel db: ${it.message}")
                    Toast.makeText(this,getString(R.string.error_update_email)+": ${it.message}",Toast.LENGTH_LONG).show()
                }
    }

    private fun validateUpdateEmail():Boolean{
        validateOldEmail()
        validatePassword()
        validateNewEmail()
        return !error
    }

    // Controllo che l'utente abbia inserito la vecchia mail e che sia corretta
    private fun validateOldEmail(){
        oldEmail = binding.oldEmailEditTextModifyEmail.text.toString()

        if(oldEmail==null || oldEmail!!.isEmpty() || oldEmail!!.isBlank()){
            binding.oldEmailInputLayoutModifyEmail.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        /*if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailInputLayoutLogin.error = getString(R.string.error_email)
            error = true
            return
        }*/
        if(!oldEmail.equals(user!!.email)){
            binding.oldEmailInputLayoutModifyEmail.error = getString(R.string.error_old_email)
            error = true
            return
        }
        binding.oldEmailInputLayoutModifyEmail.error = ""
        binding.oldEmailInputLayoutModifyEmail.isErrorEnabled = false
    }

    // Controllo che l'utente abbia inserito la password
    private fun validatePassword(){
        password = binding.passwordEditTextModifyEmail.text.toString()

        if(password==null || password!!.isEmpty() || password!!.isBlank()){
            binding.passwordInputLayoutModifyEmail.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.passwordInputLayoutModifyEmail.error = ""
        binding.passwordInputLayoutModifyEmail.isErrorEnabled = false
    }

    // Controllo che l'utente abbi inserito la nuova mail e sia corretta
    private fun validateNewEmail(){
        newEmail = binding.newEmailEditTextModifyEmail.text.toString()

        if(newEmail==null || newEmail!!.isEmpty() || newEmail!!.isBlank()){
            binding.newEmailInputLayoutModifyEmail.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()){
            binding.newEmailInputLayoutModifyEmail.error = getString(R.string.error_email)
            error = true
            return
        }
        binding.newEmailInputLayoutModifyEmail.error = ""
        binding.newEmailInputLayoutModifyEmail.isErrorEnabled = false
    }

    // Metodo che cambia colore alla startIcon del TextInputLayout quando è evidenziato
    private fun changeIconColor(){
        binding.oldEmailEditTextModifyEmail.setOnFocusChangeListener { v, hasFocus ->
            val color = if(hasFocus) Color.rgb(249,170,51) else Color.rgb(52,73,85)
            binding.oldEmailInputLayoutModifyEmail.setStartIconTintList(ColorStateList.valueOf(color))
        }
        binding.passwordEditTextModifyEmail.setOnFocusChangeListener { v, hasFocus ->
            val color = if(hasFocus) Color.rgb(249,170,51) else Color.rgb(52,73,85)
            binding.passwordInputLayoutModifyEmail.setStartIconTintList(ColorStateList.valueOf(color))
        }
        binding.newEmailEditTextModifyEmail.setOnFocusChangeListener { v, hasFocus ->
            val color = if(hasFocus) Color.rgb(249,170,51) else Color.rgb(52,73,85)
            binding.newEmailInputLayoutModifyEmail.setStartIconTintList(ColorStateList.valueOf(color))
        }
    }

}