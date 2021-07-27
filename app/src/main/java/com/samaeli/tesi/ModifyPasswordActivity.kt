package com.samaeli.tesi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.samaeli.tesi.databinding.ActivityModifyPasswordBinding

class ModifyPasswordActivity : AppCompatActivity() {

    private lateinit var binding : ActivityModifyPasswordBinding

    companion object{
        val TAG = "Modify Password Activity"
    }

    private var error : Boolean = false
    private var oldPassword : String? = null
    private var newPassword : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_modify_password)
        binding = ActivityModifyPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Update password"
        }
        binding.changePasswordButtonModifyPassword.setOnClickListener {
            Log.d(TAG,"Try to modify password")
            error = false
            if(!validateUpdatePassword()){
                Log.d(TAG,getString(R.string.error_update_password))
                Toast.makeText(this,getString(R.string.error_update_password),Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val user = FirebaseAuth.getInstance().currentUser
            val credential = EmailAuthProvider.getCredential(user!!.email.toString(),oldPassword.toString())
            user.reauthenticate(credential)
                    .addOnSuccessListener {
                        binding.oldPasswordInputLayoutModifyPassword.error = ""
                        binding.oldPasswordInputLayoutModifyPassword.isErrorEnabled = false
                        user.updatePassword(newPassword.toString())
                        Toast.makeText(this,getString(R.string.password_changed),Toast.LENGTH_LONG).show()
                        finish()
                    }
                    .addOnFailureListener {
                        binding.oldPasswordInputLayoutModifyPassword.error = getString(R.string.error_old_password)
                        Log.d(TAG,getString(R.string.error_old_password))
                    }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun validateUpdatePassword():Boolean{
        validateOldPassword()
        validateNewPassword()
        validateConfirmNewPassword()
        return !error
    }

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

}