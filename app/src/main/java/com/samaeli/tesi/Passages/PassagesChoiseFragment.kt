package com.samaeli.tesi.Passages

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.samaeli.tesi.databinding.FragmentPassagesChoiseBinding


class PassagesChoiseFragment : Fragment() {

    private var _binding : FragmentPassagesChoiseBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_passages_choise, container, false)
        _binding = FragmentPassagesChoiseBinding.inflate(inflater,container,false)
        val view = binding.root

        binding.passageRequestButtonChoisePassages.setOnClickListener {
            val intent = Intent(activity,RequestPassageActivity::class.java)
            startActivity(intent)
        }

        return view
    }


}