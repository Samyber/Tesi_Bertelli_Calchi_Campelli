package com.samaeli.tesi

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.samaeli.tesi.databinding.FragmentProfileBinding
import com.samaeli.tesi.models.User
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat

class ProfileFragment : Fragment() {

    companion object{
        val TAG = "Profile Fragment"
    }

    private var _binding : FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private var loadingDialog : LoadingDialog? = null

    private var error : Boolean = false
    private var selectPhotoUri : Uri? = null
    private var birthdayDate : String? = null
    private var licenseDate : String? = null
    private var name : String? = null
    private var surname : String? = null
    private var weight : Double? = null
    private var email : String? = null
    private var changePhoto : Boolean = false

    private var user : User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(FirebaseAuth.getInstance().uid == null){
            val intent = Intent(activity,LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        loadingDialog = LoadingDialog(requireActivity())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_profile, container, false)
        _binding = FragmentProfileBinding.inflate(inflater,container,false)
        val view = binding.root

        completeFields()
        changeIconColor()

        // Show dataPicker for birthday date
        binding.birthdayDateEditTextProfile.setOnClickListener {
            Log.d(RegisterActivity.TAG,"Try to show datePicker for birthday date")
            val timestamp = System.currentTimeMillis()
            // Controllo che la persona che si sta registrando abbia almeno 14 anni
            val constraintsBuilder = CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.before(timestamp - RegisterActivity.years_14_milliseconds))
                    .setEnd(timestamp - RegisterActivity.years_14_milliseconds)
                    .setOpenAt(timestamp - RegisterActivity.years_14_milliseconds)
            val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.birthday_date_picker_title)
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build()

            datePicker.show(parentFragmentManager,"")

            // Se l'utente decidedi clicca su ok la data inserita viene visualizzata neel'editText
            datePicker.addOnPositiveButtonClickListener {
                val date = getDate(it)
                Log.d(RegisterActivity.TAG,"Birthday date insert: $date")
                binding.birthdayDateEditTextProfile.setText(date)
            }

        }

        // Show dataPicker for License date
        binding.drivingLicenseEditTextProfile.setOnClickListener {
            Log.d(RegisterActivity.TAG,"Try to show datePicker for birthday date")
            // Controllo che l'utente non possa scegliere una data futura
            val constraintsBuilder = CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.now())
                    .setEnd(System.currentTimeMillis())
            val datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText(R.string.license_date_picker_title)
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build()

            datePicker.show(parentFragmentManager,"")

            // Se l'utente decidedi clicca su ok la data inserita viene visualizzata neel'editText
            datePicker.addOnPositiveButtonClickListener {
                val date = getDate(it)
                Log.d(RegisterActivity.TAG,"License date insert: $date")
                binding.drivingLicenseEditTextProfile.setText(date)
            }
        }

        // Go to select photo
        binding.selectPhotoButtonProfile.setOnClickListener {
            Log.d(RegisterActivity.TAG,"Try to show photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startForResult.launch(intent)
        }

        binding.saveDataButtonProfile.setOnClickListener {
            error = false
            if(!validateSaveData()){
                Log.d(TAG,getString(R.string.error_update_profile))
                binding.scrollViewProfile.scrollTo(0,0)
                Toast.makeText(activity,getString(R.string.error_update_profile),Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            Log.d(TAG,"Campi ok")
            loadingDialog!!.startLoadingDialog()
            if(changePhoto == true){
                uploadImageToFirebase()
            }else{
                modifyUserToFirebaseDatabase(null)
            }
        }

        return view
    }

    var startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result: ActivityResult ->
        if(result.resultCode == Activity.RESULT_OK){
            val data = result.data
            if(data != null){
                Log.d(RegisterActivity.TAG,"Photo was selected")
                selectPhotoUri = data.data
                val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, selectPhotoUri)
                binding.circleImageProfile.setImageBitmap(bitmap)
                binding.selectPhotoButtonProfile.alpha = 0f
                changePhoto = true
            }
        }
    }

    private fun uploadImageToFirebase(){
        //val fileName = UUID.randomUUID().toString
        val fileName = FirebaseAuth.getInstance().uid
        val ref = FirebaseStorage.getInstance().getReference("/images/$fileName")

        // Ridimensionamento immagine prima del caricamento su FirebaseStorage
        // Se il caricamento dell'immagine richiede molto (passa molto tempo da quando si clicca il bottone register a quando
        // si viene rimandata nell'altra activity) ridurre valore di quality in compress
        val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,selectPhotoUri)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG,15,baos)
        val data = baos.toByteArray()

        //ref.putFile(selectPhotoUri!!)
        ref.putBytes(data)
                .addOnSuccessListener {
                    Log.d(RegisterActivity.TAG,"Image uploaded successfully")
                    ref.downloadUrl.addOnSuccessListener {
                        Log.d(RegisterActivity.TAG,"Image url: $it")
                        modifyUserToFirebaseDatabase(it.toString())
                    }
                }
    }

    private fun modifyUserToFirebaseDatabase(newProfileImageUrl : String?){
        if(newProfileImageUrl != null){
            user!!.profileImageUrl = newProfileImageUrl
        }
        user!!.name = name!!
        user!!.surname = surname!!
        user!!.birthdayDate = SimpleDateFormat("dd/MM/yyyy").parse(birthdayDate!!).time
        user!!.weight = weight!!
        user!!.licenseDate = SimpleDateFormat("dd/MM/yyyy").parse(licenseDate!!).time
        if(binding.genderRadioGroupProfile.checkedRadioButtonId == R.id.maleRadioButtonProfile){
            user!!.gender = "male"
        }else{
            user!!.gender = "female"
        }
        if(!user!!.email.equals(email)){
            FirebaseAuth.getInstance().currentUser!!.updateEmail(email!!)
            user!!.email = email!!
        }
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d(TAG,"User saved in db")
                    loadingDialog!!.dismissLoadingDialog()
                    Toast.makeText(activity,getString(R.string.ok_update_profile),Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { Log.d(TAG,"Impossibile modificare l'utente nel db: ${it.message}")
                    Toast.makeText(activity,getString(R.string.error_update_profile)+": ${it.message}",Toast.LENGTH_LONG).show() }
    }

    private fun validateSaveData():Boolean{
        validateName()
        validateSurname()
        validateBirthdayDate()
        validateLicenseDate()
        validateEmail()
        validateWeight()
        return !error
    }

    private fun completeFields(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java)
                binding.nameEditTextProfile.setText(user!!.name)
                binding.surnameEditTextProfile.setText(user!!.surname)
                binding.birthdayDateEditTextProfile.setText(getDate(user!!.birthdayDate))
                if(user!!.gender.equals("male")){
                    binding.genderRadioGroupProfile.check(R.id.maleRadioButtonProfile)
                }else{
                    binding.genderRadioGroupProfile.check(R.id.femaleRadioButtonProfile)
                }
                binding.weightEditTextProfile.setText(user!!.weight.toString())
                binding.drivingLicenseEditTextProfile.setText(getDate(user!!.licenseDate))
                binding.emailEditTextProfile.setText(user!!.email)
                if(user!!.profileImageUrl == null){
                    binding.circleImageProfile.alpha = 0f
                }else{
                    binding.selectPhotoButtonProfile.alpha = 0f
                    val url = user!!.profileImageUrl
                    val targetImageView = binding.circleImageProfile
                    Picasso.get().load(url).into(targetImageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun getDate(timestamp:Long):String{
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        return formatter.format(timestamp)
    }

    // Controllo che l'utente abbia inserito il nome
    private fun validateName(){
        name = binding.nameEditTextProfile.text.toString()

        if(name==null || name!!.isEmpty() || name!!.isBlank()) {
            binding.nameInputLayoutProfile.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.nameInputLayoutProfile.error = ""
        binding.nameInputLayoutProfile.isErrorEnabled = false
    }

    // Controllo che l'utente abbia inserito il cognome
    private fun validateSurname(){
        surname = binding.surnameEditTextProfile.text.toString()

        if(surname==null || surname!!.isEmpty() || surname!!.isBlank()) {
            binding.surnameInputLayoutProfile.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.surnameInputLayoutProfile.error = ""
        binding.surnameInputLayoutProfile.isErrorEnabled = false
    }

    // Controllo che l'utente abbia inserito la data di nascita
    private fun validateBirthdayDate(){
        birthdayDate = binding.birthdayDateEditTextProfile.text.toString()

        if(birthdayDate == null || birthdayDate!!.isEmpty() || birthdayDate!!.isBlank()){
            binding.birthdayDateInputLayoutProfile.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.birthdayDateInputLayoutProfile.error = ""
        binding.birthdayDateInputLayoutProfile.isErrorEnabled = false
    }

    // Controllo che l'utente abbia inserito il peso
    private fun validateWeight(){
        val stringWeight = binding.weightEditTextProfile.text.toString()

        if(stringWeight == null || stringWeight.isEmpty() || stringWeight.isBlank()){
            binding.weightInputLayoutProfile.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        weight = stringWeight.toDouble()
        if(weight!! <= 0.0){
            binding.weightInputLayoutProfile.error = getString(R.string.error_weight)
            error = true
            return
        }
        binding.weightInputLayoutProfile.error = ""
        binding.weightInputLayoutProfile.isErrorEnabled = false
    }

    // Controllo che l'utente abbia inserito la data in cui ha preso la patente
    private fun validateLicenseDate(){
        licenseDate = binding.drivingLicenseEditTextProfile.text.toString()

        if(licenseDate == null || licenseDate!!.isEmpty() || licenseDate!!.isBlank()){
            binding.licenseDateInputLayoutProfile.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        binding.licenseDateInputLayoutProfile.error = ""
        binding.licenseDateInputLayoutProfile.isErrorEnabled = false
    }

    // Controllo che l'utente abbia inserito una mail valida
    private fun validateEmail(){
        email = binding.emailEditTextProfile.text.toString()

        if(email==null || email!!.isEmpty() || email!!.isBlank()){
            binding.emailInputLayoutProfile.error = getString(R.string.field_not_empty)
            error = true
            return
        }
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailInputLayoutProfile.error = getString(R.string.error_email)
            error = true
            return
        }
        binding.emailInputLayoutProfile.error = ""
        binding.emailInputLayoutProfile.isErrorEnabled = false
    }

    private fun changeIconColor(){
        binding.nameEditTextProfile.setOnFocusChangeListener { v, hasFocus ->
            val color = if(hasFocus) Color.rgb(249,170,51) else Color.rgb(52,73,85)
            binding.nameInputLayoutProfile.setStartIconTintList(ColorStateList.valueOf(color))
        }
        binding.surnameEditTextProfile.setOnFocusChangeListener { v, hasFocus ->
            val color = if(hasFocus) Color.rgb(249,170,51) else Color.rgb(52,73,85)
            binding.surnameInputLayoutProfile.setStartIconTintList(ColorStateList.valueOf(color))
        }
        binding.emailEditTextProfile.setOnFocusChangeListener { v, hasFocus ->
            val color = if(hasFocus) Color.rgb(249,170,51) else Color.rgb(52,73,85)
            binding.emailInputLayoutProfile.setStartIconTintList(ColorStateList.valueOf(color))
        }
        binding.weightEditTextProfile.setOnFocusChangeListener { v, hasFocus ->
            val color = if(hasFocus) Color.rgb(249,170,51) else Color.rgb(52,73,85)
            binding.weightInputLayoutProfile.setStartIconTintList(ColorStateList.valueOf(color))
        }
    }
}