package com.samaeli.tesi.calculationBloodAlcohol

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.samaeli.tesi.R
import com.samaeli.tesi.databinding.FragmentAlcoholLevelBinding
import kotlinx.android.synthetic.main.fragment_alcohol_level.*

class AlcoholLevelFragment : Fragment() {

    private var _binding : FragmentAlcoholLevelBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_alcohol_level, container, false)
        _binding = FragmentAlcoholLevelBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.buttonAddDrink.setOnClickListener {
            val intent = Intent(activity,SelectDrinkActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}