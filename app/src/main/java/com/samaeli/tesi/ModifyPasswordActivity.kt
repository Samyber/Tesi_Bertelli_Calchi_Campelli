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
import com.samaeli.tesi.databinding.ActivityModifyPasswordBinding

class ModifyPasswordActivity : AppCompatActivity() {

    private lateinit var binding : ActivityModifyPasswordBinding

    companion object{
        val TAG = "Modify Password Activity"
    }

    private var error : Boolean = false
    private var oldPassword : String? = null
    private var newPassword : String? = null

    private var loadingDialog : LoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_modify_password)
        binding = ActivityModifyPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.update_password)
        }

        changeIconColor()
        loadingDialog = LoadingDialog(this)

        binding.changePasswordButtonModifyPassword.setOnClickListener {
            Log.d(TAG,"Try to modify password")
            error = false
            if(!validateUpdatePassword()){
                Log.d(TAG,getString(R.string.error_update_password))
                Toast.makeText(this,getString(R.string.error_update_password),Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            // Start schermata di caricamento
            loadingDialog!!.startLoadingDialog()
            updatePassword()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    // Metodo che ha il compito di modificare la password dell'utente su FirebaseAuth
    private fun updatePassword(){
        val user = FirebaseAuth.getInstance().currentUser
        val credential = EmailAuthProvider.getCredential(user!!.email.toString(),oldPassword.toString())
        // Reautenticazione dell'utente
        user.reauthenticate(credential)
                .addOnSuccessListener {
                    /*binding.oldPasswordInputLayoutModifyPassword.error = ""
                    binding.oldPasswordInputLayoutModifyPassword.isErrorEnabled = false*/
                    user.updatePassword(newPassword.toString())
                    // Stop schermata di caricamento
                    loadingDialog!!.dismissLoadingDialog()
                    Toast.makeText(this,getString(R.string.password_changed),Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener {
                    // Stop schermata di caricamento
                    loadingDialog!!.dismissLoadingDialog()
                    // If che viene eseguito se l'utente ha sbagliato la vecchia password
                    if(it is FirebaseAuthInvalidCredentialsException) {
                        binding.oldPasswordInputLayoutModifyPassword.error = getString(R.string.error_old_password)
                    }else{
                        Toast.makeText(this,"Error: ${it.message}",Toast.LENGTH_LONG).show()
                    }
                    Log.d(TAG, "Error: ${it.message}")
                }
    }

    private fun validateUpdatePassword():Boolean{
        validateOldPassword()
        validateNewPassword()
        validateConfirmNewPassword()
        return !error
    }

    // Controllo che l'utente abbia inserito la vecchia password
    private fun validateOldPassword(){
        oldPassword = binding.oldPasswordEditTextModifyPassword.text.toString()

        if(oldPassword==null || oldPassword!!.isEmpty() || oldPassword!!.isBlank()){
            binding.oldPasswordInputLayoutModifyPassword.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.oldPasswordInputLayoutModifyPassword.error = ""
        binding.oldPasswordInputLayoutModifyPassword.isErrorEnabled = false
    }

    // Controllo che l'utente abbia inserito la nuova password
    private fun validateNewPassword(){
        newPassword = binding.newPasswordEditTextModifyPassword.text.toString()

        if(newPassword==null || newPassword!!.isEmpty() || newPassword!!.isBlank()){
            binding.newPasswordInputLayoutModifyPassword.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        if(newPassword!!.length < 8){
            binding.newPasswordInputLayoutModifyPassword.error = getString(R.string.error_password_too_short)
            error = true
            return
        }
        binding.newPasswordInputLayoutModifyPassword.error = ""
        binding.newPasswordInputLayoutModifyPassword.isErrorEnabled = false
    }

    // Controllo che l'utente abbia confermato la nuova password
    private fun validateConfirmNewPassword(){
        val confirmPassword = binding.confirmNewPasswordEditTextModifyPassword.text.toString()

        if(confirmPassword.isEmpty() || confirmPassword.isBlank()){
            binding.confirmNewPasswordInputLayoutModifyPassword.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        if(!confirmPassword.equals(newPassword)){
            binding.confirmNewPasswordInputLayoutModifyPassword.error = getString(R.string.error_password_not_match)
            error = true
            return
        }
        binding.confirmNewPasswordInputLayoutModifyPassword.error = ""
        binding.confirmNewPasswordInputLayoutModifyPassword.isErrorEnabled = false
    }

    // Metodo che cambia colore alla startIcon del TextInputLayout quando è evidenziato
    private fun changeIconColor(){
        binding.oldPasswordEditTextModifyPassword.setOnFocusChangeListener { v, hasFocus ->
            val color = if(hasFocus) Color.rgb(249,170,51) else Color.rgb(52,73,85)
            binding.oldPasswordInputLayoutModifyPassword.setStartIconTintList(ColorStateList.valueOf(color))
        }
        binding.newPasswordEditTextModifyPassword.setOnFocusChangeListener { v, hasFocus ->
            val color = if(hasFocus) Color.rgb(249,170,51) else Color.rgb(52,73,85)
            binding.newPasswordInputLayoutModifyPassword.setStartIconTintList(ColorStateList.valueOf(color))
        }
        binding.confirmNewPasswordEditTextModifyPassword.setOnFocusChangeListener { v, hasFocus ->
            val color = if(hasFocus) Color.rgb(249,170,51) else Color.rgb(52,73,85)
            binding.confirmNewPasswordInputLayoutModifyPassword.setStartIconTintList(ColorStateList.valueOf(color))
        }
    }

}