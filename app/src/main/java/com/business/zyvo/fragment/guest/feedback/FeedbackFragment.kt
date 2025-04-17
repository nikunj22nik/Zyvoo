package com.business.zyvo.fragment.guest.feedback

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.databinding.FragmentFeedbackBinding
import com.business.zyvo.fragment.guest.feedback.viewModel.FeedbackViewModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.NetworkMonitorCheck
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FeedbackFragment : Fragment() {
    private var _binding: FragmentFeedbackBinding? = null
    private val binding get() = _binding!!
    private var close = 0
    lateinit var navController: NavController
    private var type =""
    private val viewModel : FeedbackViewModel by lazy {
        ViewModelProvider(this)[FeedbackViewModel::class.java]
    }
    var session: SessionManager?=null



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
        _binding =
            FragmentFeedbackBinding.inflate(LayoutInflater.from(requireContext()), container, false)
        session = SessionManager(requireContext())
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.imageBackIcon.setOnClickListener {
            navController.navigateUp()
        }
        binding.spinnerFeedback.setItems(
            listOf(AppConstant.Host, AppConstant.Guest)
        )





    if (session?.getCurrentPanel().equals(AppConstant.Host)){
        binding.spinnerFeedback.isEnabled = false
        binding.spinnerFeedback.setText("Host")
        type = "host"
        binding.etAddDetails.visibility = View.VISIBLE
        binding.textAddDetails.visibility = View.VISIBLE
    }else {
        binding.spinnerFeedback.isEnabled = false
        binding.spinnerFeedback.setText("Guest")
        type = "guest"
        binding.etAddDetails.visibility = View.VISIBLE
        binding.textAddDetails.visibility = View.VISIBLE
    }





        binding.spinnerFeedback.setOnFocusChangeListener { _, b ->
            close = if (b) {
                1
            } else {
                0
            }
        }

        when (close) {
            0 -> {
                binding.spinnerFeedback.dismiss()
            }

            1 -> {
                binding.spinnerFeedback.show()
            }
        }

        binding.spinnerFeedback.setIsFocusable(true)

        binding.spinnerFeedback.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
            Log.d("checkItem", newItem)
            if (newItem == "Host"){
                type = "host"
            }else if(newItem == "Guest"){
                type = "guest"
            }
            if (newItem == "Please select") {
                binding.etAddDetails.visibility = View.GONE
                binding.textAddDetails.visibility = View.GONE
            } else {
                binding.etAddDetails.visibility = View.VISIBLE
                binding.textAddDetails.visibility = View.VISIBLE
            }
        }

        binding.btnContactUs.setOnClickListener {
            findNavController().navigate(R.id.contact_us)
        }


        lifecycleScope.launch {
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {

                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()

                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (validation()){
                lifecycleScope.launch {
                    if (NetworkMonitorCheck._isConnected.value) {
                        feedback()

                    }else{
                        showErrorDialog(requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )}
                }
            }
        }
    }

    private fun feedback() {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.feedback(session?.getUserId().toString(),type, binding.etAddDetails.text.toString().trim()).collect{
                when(it){
                    is NetworkResult.Success -> {
                        if (it.data != null){
                           LoadingUtils.showSuccessDialog(requireContext(),it.data)
                            binding.etAddDetails.text.clear()
                        }
                    }
                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(),it.message!!)

                    }
                    else ->{

                    }

                }
            }


        }
    }

    private fun validation(): Boolean{
        if (type == ""){
            showErrorDialog(requireContext(), AppConstant.feedbackAbout)
            return false
        }else if (binding.etAddDetails.text.isEmpty()){
            showErrorDialog(requireContext(), AppConstant.details)
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}