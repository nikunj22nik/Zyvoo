package com.business.zyvo.fragment.host

import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.business.zyvo.AppConstant
import com.business.zyvo.activity.GuesMain
import com.business.zyvo.adapter.host.LanguageAdapter
import com.business.zyvo.databinding.FragmentLanguageBinding


class LanguageFragment : Fragment() {

    lateinit var binding :FragmentLanguageBinding
    lateinit var adapterLanguage :LanguageAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
         binding = FragmentLanguageBinding.inflate(LayoutInflater.from(requireContext()), container, false)
         adapterLanguage =LanguageAdapter(requireContext(),AppConstant.countriesAndLanguages)
        val screenWidth = getScreenWidth()
        val columns = if (screenWidth < 500) {
            2
        }
        else {
            3
        }
        val layoutManager = GridLayoutManager(requireContext(), columns)
        binding.recyclerLanguage.layoutManager = layoutManager
        binding.recyclerLanguage.adapter = adapterLanguage

        binding.switchHost.setOnClickListener {
            val intent = Intent(requireContext(), GuesMain::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            requireActivity().finish()
        }

        return binding.root
    }

    private fun getScreenWidth(): Int {
        val metrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(metrics)
        return metrics.widthPixels
    }

}