package com.business.zyvo.fragment.host

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.business.zyvo.DateManager.DateManager

import com.business.zyvo.R
import com.business.zyvo.databinding.FragmentPayoutBinding


class PayoutFragment : Fragment() {

    private var _binding : FragmentPayoutBinding? = null
    private  val binding get() = _binding!!

    private var closeSelectIDType = 0
    private var closeSelectCountry = 0
    private var closeSelectState = 0
    private var closeSelectCity = 0
    private var closeSelectOption = 0
    private lateinit var dateManager: DateManager

    lateinit var navController: NavController
    // TODO: Rename and change types of parameters


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPayoutBinding.inflate(LayoutInflater.from(requireContext()),container,false)
        dateManager = DateManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        toggleBankAccountAndDebitCard()
        clickListener()
        spinners()

    }
    private fun toggleBankAccountAndDebitCard(){


        binding.textBankAccountToggle.setOnClickListener {
            binding.textDebitCardToggle.setBackgroundColor(Color.TRANSPARENT)
            binding.textBankAccountToggle.setBackgroundResource(R.drawable.selected_green_toogle_bg)
            binding.textBankAccountToggle.setTextColor(Color.parseColor("#FFFFFF"))
            binding.textDebitCardToggle.setTextColor(Color.parseColor("#06C169"))
            binding.cvBankAccount2.visibility = View.VISIBLE
            binding.cvDebitCard3.visibility = View.GONE


        }

        binding.textDebitCardToggle.setOnClickListener {
            binding.textBankAccountToggle.setBackgroundColor(Color.TRANSPARENT)
            binding.textDebitCardToggle.setBackgroundResource(R.drawable.selected_green_toogle_bg)
            binding.textBankAccountToggle.setTextColor(Color.parseColor("#06C169"))
            binding.textDebitCardToggle.setTextColor(Color.parseColor("#FFFFFF"))
            binding.cvBankAccount2.visibility = View.GONE
            binding.cvDebitCard3.visibility = View.VISIBLE
        }


    }


    private fun clickListener(){

        binding.textAddBank.setOnClickListener {
          //  binding.cvBankAccount2.visibility = View.GONE
           // binding.cvDebitCard3.visibility = View.GONE
           // binding.llBankAccount.visibility = View.GONE
         //   binding.llSavedBankAccountDetails4.visibility = View.VISIBLE


            navController.navigateUp()
        }
        binding.textAddCardDebitCard.setOnClickListener {
         //   binding.cvBankAccount2.visibility = View.GONE
          //  binding.cvDebitCard3.visibility = View.GONE
        //    binding.llBankAccount.visibility = View.GONE
        //    binding.llSavedBankAccountDetails4.visibility = View.VISIBLE
            navController.navigateUp()
        }


        binding.imageBackIcon.setOnClickListener {
            navController.navigateUp()
        }

        binding.etMonth.setOnClickListener {

                dateManager.showMonthSelectorDialog { selectedMonth ->
                    binding.etMonth.text = selectedMonth


                }


        }

        binding.etYear.setOnClickListener {
            dateManager.showYearPickerDialog { selectedYear ->
                binding.etYear.text = selectedYear.toString()
            }
        }


    }

    fun spinners(){


        binding.spinnerSelectIDType.setItems(
            listOf("Driver license", "Passport")
        )


        binding.spinnerSelectIDType.setOnFocusChangeListener { _, b ->
            closeSelectIDType = if (b) {
                1
            } else {
                0
            }
        }

        when (closeSelectIDType) {
            0 -> {
                binding.spinnerSelectIDType.dismiss()
            }
            1 -> {
                binding.spinnerSelectIDType.show()
            }
        }

        binding.spinnerSelectIDType.setIsFocusable(true)

        binding.spinnerSelectIDType.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->

        }


        binding.spinnerSelectCountry.setItems(
            listOf("USA", "UK","INDIA", "BRAZIL", "RUSSIA","CHINA")
        )


        binding.spinnerSelectCountry.setOnFocusChangeListener { _, b ->
            closeSelectCountry = if (b) {
                1
            } else {
                0
            }
        }

        when (closeSelectCountry) {
            0 -> {
                binding.spinnerSelectCountry.dismiss()
            }
            1 -> {
                binding.spinnerSelectCountry.show()
            }
        }

        binding.spinnerSelectCountry.setIsFocusable(true)

        binding.spinnerSelectCountry.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->

        }

        binding.spinnerSelectState.setItems(
            listOf("UP", "MP","HARYANA", "PUNJAB", "ODISHA")
        )


        binding.spinnerSelectState.setOnFocusChangeListener { _, b ->
            closeSelectState = if (b) {
                1
            } else {
                0
            }
        }

        when (closeSelectState) {
            0 -> {
                binding.spinnerSelectState.dismiss()
            }
            1 -> {
                binding.spinnerSelectState.show()
            }
        }

        binding.spinnerSelectState.setIsFocusable(true)

        binding.spinnerSelectState.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->

        }
        binding.spinnerSelectCity.setItems(
            listOf("NEW DELHI", "MUMBAI","KANPUR", "NOIDA")
        )


        binding.spinnerSelectCity.setOnFocusChangeListener { _, b ->
            closeSelectCity = if (b) {
                1
            } else {
                0
            }
        }

        when (closeSelectCity) {
            0 -> {
                binding.spinnerSelectCity.dismiss()
            }
            1 -> {
                binding.spinnerSelectCity.show()
            }
        }

        binding.spinnerSelectCity.setIsFocusable(true)

        binding.spinnerSelectCity.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->

        }
        binding.spinnerSelectOption.setItems(
            listOf("Bank account statement", "Voided cheque","Bank letterhead")
        )


        binding.spinnerSelectOption.setOnFocusChangeListener { _, b ->
            closeSelectOption = if (b) {
                1
            } else {
                0
            }
        }

        when (closeSelectOption) {
            0 -> {
                binding.spinnerSelectOption.dismiss()
            }
            1 -> {
                binding.spinnerSelectOption.show()
            }
        }

        binding.spinnerSelectOption.setIsFocusable(true)

        binding.spinnerSelectOption.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->

        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}