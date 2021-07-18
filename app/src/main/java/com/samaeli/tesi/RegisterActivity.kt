package com.samaeli.tesi

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.samaeli.tesi.databinding.ActivityRegisterBinding
import com.samaeli.tesi.models.User
import java.io.ByteArrayOutputStream
import java.lang.Exception
//import kotlinx.android.synthetic.main.activity_register.*
import java.lang.String.format
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    companion object{
        val TAG = "Register Activity"
        val years_14_milliseconds : Long = 441806400000
    }

    private var error : Boolean = false
    private var selectPhotoUri : Uri? = null

    private var name : String? = null
    private var surname : String? = null
    private var birthdayDate : Long? = null
    private var licenseDate : Long? = null
    private var gender : String? = null
    private var weight : Double? = null
    private var email : String? = null
    private var password : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_register)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Change color of startIcon of textInputLayout when their focused
        changeIconColor()

        // Disable soft keyboard when the user click on date input box
        binding.birthdayDateInputLayoutRegister.editText?.inputType = InputType.TYPE_NULL
        binding.licenseDateInputLayoutRegister.editText?.inputType = InputType.TYPE_NULL

        binding.birthdayDateInputLayoutRegister.editText?.setOnClickListener {
            Log.d(TAG,"Try to show datePicker for birthday date")
            val timestamp = System.currentTimeMillis()
            // Si possono registrare solo persone che hanno un'età maggiore di 14 anni
            val constraintsBuilder = CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.before(timestamp - years_14_milliseconds))
                    .setEnd(timestamp - years_14_milliseconds)
                    .setOpenAt(timestamp - years_14_milliseconds)
            val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.birthday_date_picker_title)
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build()

            datePicker.show(supportFragmentManager,"")

            datePicker.addOnPositiveButtonClickListener {
                val date = getDate(it)
                birthdayDate = it
                Log.d(TAG,"Birthday date insert: $date")
                binding.birthdayDateInputLayoutRegister.editText?.setText(date)
            }

        }

        binding.licenseDateInputLayoutRegister.editText?.setOnClickListener {
            Log.d(TAG,"Try to show datePicker for birthday date")
            // Non si possono scegliere date future
            val constraintsBuilder = CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.now())
                    .setEnd(System.currentTimeMillis())
            val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.license_date_picker_title)
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build()

            datePicker.show(supportFragmentManager,"")

            datePicker.addOnPositiveButtonClickListener {
                val date = getDate(it)
                licenseDate = it
                Log.d(TAG,"License date insert: $date")
                binding.licenseDateInputLayoutRegister.editText?.setText(date)
            }
        }

        // Go to Login Activity
        binding.alreadyRegisterTextView.setOnClickListener {
            Log.d(TAG,"Go to Login Activity")
            val intent = Intent(this,LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        binding.selectImageButtonRegister.setOnClickListener {
            Log.d(TAG,"Try to show photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startForResult.launch(intent)
        }

        binding.buttonRegister.setOnClickListener {
            error = false
            if(!validateRegistration()){
                Log.d(TAG,getString(R.string.error_login))
                binding.scrollViewRegister.scrollTo(0,0)
                Toast.makeText(this,getString(R.string.error_registration),Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            Log.d(TAG,"Campi ok")
            performRegistration()
        }

        binding.alcoholCalculatorTextViewRegister.setOnClickListener {
            Log.d(LoginActivity.TAG,"Main Activity")
            val intent = Intent(this,MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
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
                    binding.circleImageRegister.setImageBitmap(bitmap)
                    binding.selectImageButtonRegister.alpha = 0f
                }
            }
    }

    private fun performRegistration(){
        /*val email = binding.emailInputLayoutRegister.editText!!.text.toString()
        val password = binding.passwordInputLayoutRegister.editText!!.text.toString()*/

        Log.d(TAG,"Email: $email and password: $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener {
                    if(!it.isSuccessful){
                        try {
                            throw it.exception!!
                        }catch (e : Exception) {
                            if(e is FirebaseAuthUserCollisionException){
                                Log.d(TAG,"The email exist")
                                //Il toast compare automaticamente senza doverlo stampare
                                //Toast.makeText(this,getString(R.string.error_registration)+": "+getString(R.string.error_email_exist),Toast.LENGTH_LONG).show()
                                binding.emailInputLayoutRegister.error = getString(R.string.error_email_exist)
                            }else {
                                Toast.makeText(this, getString(R.string.error_registration), Toast.LENGTH_LONG).show()
                                Log.d(TAG, getString(R.string.error_registration))
                            }
                        }
                        return@addOnCompleteListener
                    }
                    Log.d(TAG,"Success Registration")
                    if(selectPhotoUri != null){
                        uploadImageToFirebase()
                    }else{
                        saveUserToFirebaseDatabase(null)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this,getString(R.string.error_registration)+": ${it.message}",Toast.LENGTH_LONG).show()
                    Log.d(TAG,getString(R.string.error_registration)+": ${it.message}")
                }

    }

    // Il nome dell'immagine caricata è uguale a l'uid dell'utente. Questo semplificherà la vita quando l'utente dovrà modificare la sua
    // immagine del profilo. Infatti quando bisognerà sostiuirla la si troverà facilmente conoscendo solo l'uid dell'utente. Altrimenti bisognerebbe
    // salvare nel profilo dell'utente, oltre all'url dell'immagine per visualizzarla, anche il suo nome nel database.
    private fun uploadImageToFirebase(){
        //val fileName = UUID.randomUUID().toString
        val fileName = FirebaseAuth.getInstance().uid
        val ref = FirebaseStorage.getInstance().getReference("/images/$fileName")

        //Ridimensionamento immagine prima del caricamento su FirebaseStorage
        // Se il caricamento dell'immagine richiede molto (passa molto tempo da quando si clicca il bottone register a quando
        // si viene rimandata nell'altra activity) ridurre valore di quality in compress
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectPhotoUri)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,15,baos)
        val data = baos.toByteArray()

        //ref.putFile(selectPhotoUri!!)
        ref.putBytes(data)
                .addOnSuccessListener {
                    Log.d(TAG,"Image uploaded successfully")
                    ref.downloadUrl.addOnSuccessListener {
                        Log.d(TAG,"Image url: $it")
                        saveUserToFirebaseDatabase(it.toString())
                    }
                }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String?){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("users/$uid")

        if(binding.genderRadioGroupRegistration.checkedRadioButtonId == R.id.maleRadioButtonRegistration){
            gender = "male"
        }else{
            gender = "female"
        }

        val user = User(uid,email!!,name!!,surname!!,birthdayDate!!,licenseDate!!,gender!!,profileImageUrl,weight!!)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG,"User saved in db")

                val intent = Intent(this,MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d(TAG,"Impossibile salvare l'utente nel db: ${it.message}")
                Toast.makeText(this,getString(R.string.error_registration)+": ${it.message}",Toast.LENGTH_LONG).show()
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
        validateWeight()
        return !error
    }

    private fun validateName(){
        name = binding.nameInputLayoutRegister.editText?.text.toString()

        if(name==null || name!!.isEmpty() || name!!.isBlank()) {
            binding.nameInputLayoutRegister.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.nameInputLayoutRegister.error = ""
        binding.nameInputLayoutRegister.isErrorEnabled = false
    }

    private fun validateSurname(){
        surname = binding.surnameInputLayoutRegister.editText?.text.toString()

        if(surname==null || surname!!.isEmpty() || surname!!.isBlank()) {
            binding.surnameInputLayoutRegister.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.surnameInputLayoutRegister.error = ""
        binding.surnameInputLayoutRegister.isErrorEnabled = false
    }

    private fun validateBirthdayDate(){
        val birthday = binding.birthdayDateInputLayoutRegister.editText?.text.toString()

        if(birthday.isEmpty() || birthday.isBlank()){
            binding.birthdayDateInputLayoutRegister.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.birthdayDateInputLayoutRegister.error = ""
        binding.birthdayDateInputLayoutRegister.isErrorEnabled = false
    }

    private fun validateWeight(){
        val stringWeight = binding.weightInputLayoutRegister.editText?.text.toString()

        if(stringWeight == null || stringWeight.isEmpty() || stringWeight.isBlank()){
            binding.weightInputLayoutRegister.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.weightInputLayoutRegister.error = ""
        binding.weightInputLayoutRegister.isErrorEnabled = false
        weight = stringWeight.toDouble()
    }

    private fun validateLicenseDate(){
        val licenseDate = binding.licenseDateInputLayoutRegister.editText?.text.toString()

        if(licenseDate.isEmpty() || licenseDate.isBlank()){
            binding.licenseDateInputLayoutRegister.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.licenseDateInputLayoutRegister.error = ""
        binding.licenseDateInputLayoutRegister.isErrorEnabled = false
    }

    private fun validateEmail(){
        email = binding.emailInputLayoutRegister.editText?.text.toString()

        if(email==null || email!!.isEmpty() || email!!.isBlank()){
            binding.emailInputLayoutRegister.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailInputLayoutRegister.error = getString(R.string.error_email)
            error = true
            return
        }
        binding.emailInputLayoutRegister.error = ""
        binding.emailInputLayoutRegister.isErrorEnabled = false
    }

    private fun validatePassword(){
        password = binding.passwordInputLayoutRegister.editText?.text.toString()

        if(password==null || password!!.isEmpty() || password!!.isBlank()){
            binding.passwordInputLayoutRegister.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        if(password!!.length < 8){
            binding.passwordInputLayoutRegister.error = getString(R.string.error_password_too_short)
            error = true
            return
        }
        binding.passwordInputLayoutRegister.error = ""
        binding.passwordInputLayoutRegister.isErrorEnabled = false
    }

    private fun validateConfirmPassword(){
        val confirmPassword = binding.confirmPasswordInputLayoutRegister.editText?.text.toString()

        if(confirmPassword.isEmpty() || confirmPassword.isBlank()){
            binding.confirmPasswordInputLayoutRegister.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        val password = binding.passwordInputLayoutRegister.editText?.text.toString()
        if(!confirmPassword.equals(password)){
            binding.confirmPasswordInputLayoutRegister.error = getString(R.string.error_password_not_match)
            error = true
            return
        }
        binding.confirmPasswordInputLayoutRegister.error = ""
        binding.confirmPasswordInputLayoutRegister.isErrorEnabled = false
    }

    private fun changeIconColor(){
        binding.nameEditTextRegister.setOnFocusChangeListener { v, hasFocus ->
            val color = if(hasFocus) Color.rgb(249,170,51) else Color.rgb(52,73,85)
            binding.nameInputLayoutRegister.setStartIconTintList(ColorStateList.valueOf(color))
        }
        binding.surnameEditTextRegister.setOnFocusChangeListener { v, hasFocus ->
            val color = if(hasFocus) Color.rgb(249,170,51) else Color.rgb(52,73,85)
            binding.surnameInputLayoutRegister.setStartIconTintList(ColorStateList.valueOf(color))
        }
        binding.emailEditTextRegister.setOnFocusChangeListener { v, hasFocus ->
            val color = if(hasFocus) Color.rgb(249,170,51) else Color.rgb(52,73,85)
            binding.emailInputLayoutRegister.setStartIconTintList(ColorStateList.valueOf(color))
        }
        binding.passwordEditTextRegister.setOnFocusChangeListener { v, hasFocus ->
            val color = if(hasFocus) Color.rgb(249,170,51) else Color.rgb(52,73,85)
            binding.passwordInputLayoutRegister.setStartIconTintList(ColorStateList.valueOf(color))
        }
        binding.confirmPasswordEditTextRegister.setOnFocusChangeListener { v, hasFocus ->
            val color = if(hasFocus) Color.rgb(249,170,51) else Color.rgb(52,73,85)
            binding.confirmPasswordInputLayoutRegister.setStartIconTintList(ColorStateList.valueOf(color))
        }
    }

}