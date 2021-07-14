package com.samaeli.tesi

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.widget.ScrollView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.activity_register.*
import java.lang.String.format
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {

    companion object{
        val TAG = "Register Activity"
    }

    private var error : Boolean = false
    private var selectPhotoUri : Uri? = null

    private var birthdayDate : Long? = null
    private var licenseDate : Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        // Disable soft keyboard when the user click on date input box
        birthdayDateInputLayoutRegister.editText?.inputType = InputType.TYPE_NULL
        licenseDateInputLayoutRegister.editText?.inputType = InputType.TYPE_NULL

        birthdayDateInputLayoutRegister.editText?.setOnClickListener {
            Log.d(TAG,"Try to show datePicker for birthday date")
            val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.birthday_date_picker_title)
                    .build()

            datePicker.show(supportFragmentManager,"")

            datePicker.addOnPositiveButtonClickListener {
                val date = getDate(it)
                birthdayDate = it
                Log.d(TAG,"Birthday date insert: $date")
                birthdayDateInputLayoutRegister.editText?.setText(date)
            }

        }

        licenseDateInputLayoutRegister.editText?.setOnClickListener {
            Log.d(TAG,"Try to show datePicker for birthday date")
            val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.license_date_picker_title)
                    .build()

            datePicker.show(supportFragmentManager,"")

            datePicker.addOnPositiveButtonClickListener {
                val date = getDate(it)
                licenseDate = it
                Log.d(TAG,"License date insert: $date")
                licenseDateInputLayoutRegister.editText?.setText(date)
            }
        }

        // Go to Login Activity
        alreadyRegisterTextView.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        selectImageButtonRegister.setOnClickListener {
            Log.d(TAG,"Try to show photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startForResult.launch(intent)
        }

        buttonRegister.setOnClickListener {
            error = false
            if(validateRegistration()){
                Log.d(TAG,"TUTTO OK")
            }else{
                Log.d(TAG,"Errore in fase di registrazione")
                //scrollViewRegister.fullScroll(ScrollView.SCROLL_INDICATOR_TOP)
                scrollViewRegister.scrollTo(0,0)
            }
        }

    }

    var startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                if(data != null){
                    Log.d(TAG,"Photo was selected")
                    selectPhotoUri = data.data
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectPhotoUri)
                    circleImageRegister.setImageBitmap(bitmap)
                    selectImageButtonRegister.alpha = 0f
                }
            }
    }

    private fun getDate(timestamp:Long):String{
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        return formatter.format(timestamp)
    }

    private fun validateRegistration():Boolean{
        validateName()
        validateSurname()
        validateBirthdayDate()
        validateLicenseDate()
        validateEmail()
        validatePassword()
        validateConfirmPassword()
        return !error
    }

    private fun validateName(){
        val name = nameInputLayoutRegister.editText?.text.toString()

        if(name.isEmpty() || name.isBlank()) {
            nameInputLayoutRegister.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        nameInputLayoutRegister.error = ""
        nameInputLayoutRegister.isErrorEnabled = false
    }

    private fun validateSurname(){
        val surname = surnameInputLayoutRegister.editText?.text.toString()

        if(surname.isEmpty() || surname.isBlank()) {
            surnameInputLayoutRegister.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        surnameInputLayoutRegister.error = ""
        surnameInputLayoutRegister.isErrorEnabled = false
    }

    private fun validateBirthdayDate(){
        val birthday = birthdayDateInputLayoutRegister.editText?.text.toString()

        if(birthday.isEmpty() || birthday.isBlank()){
            birthdayDateInputLayoutRegister.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        birthdayDateInputLayoutRegister.error = ""
        birthdayDateInputLayoutRegister.isErrorEnabled = false
    }

    private fun validateLicenseDate(){
        val birthday = licenseDateInputLayoutRegister.editText?.text.toString()

        if(birthday.isEmpty() || birthday.isBlank()){
            licenseDateInputLayoutRegister.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        licenseDateInputLayoutRegister.error = ""
        licenseDateInputLayoutRegister.isErrorEnabled = false
    }

    private fun validateEmail(){
        val email = emailInputLayoutRegister.editText?.text.toString()

        if(email.isEmpty() || email.isBlank()){
            emailInputLayoutRegister.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailInputLayoutRegister.error = getString(R.string.error_email)
            error = true
            return
        }
        emailInputLayoutRegister.error = ""
        emailInputLayoutRegister.isErrorEnabled = false
    }

    private fun validatePassword(){
        val password = passwordInputLayoutRegister.editText?.text.toString()

        if(password.isEmpty() || password.isBlank()){
            passwordInputLayoutRegister.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        if(password.length < 8){
            passwordInputLayoutRegister.error = getString(R.string.error_password_too_short)
            error = true
            return
        }
        passwordInputLayoutRegister.error = ""
        passwordInputLayoutRegister.isErrorEnabled = false
    }

    private fun validateConfirmPassword(){
        val confirmPassword = confirmPasswordInputLayoutRegister.editText?.text.toString()

        if(confirmPassword.isEmpty() || confirmPassword.isBlank()){
            confirmPasswordInputLayoutRegister.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        val password = passwordInputLayoutRegister.editText?.text.toString()
        if(!confirmPassword.equals(password)){
            confirmPasswordInputLayoutRegister.error = getString(R.string.error_password_not_match)
            error = true
            return
        }
        confirmPasswordInputLayoutRegister.error = ""
        confirmPasswordInputLayoutRegister.isErrorEnabled = false
    }
}