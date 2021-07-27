package com.samaeli.tesi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
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

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun validateUpdatePassword():Boolean{

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

}