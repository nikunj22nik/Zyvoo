package com.business.zyvo.fragment.guest.termAndCondition

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.databinding.FragmentTermsServicesBinding
import com.business.zyvo.fragment.guest.termAndCondition.viewModel.TermsViewModel
import com.business.zyvo.utils.NetworkMonitorCheck
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TermsServicesFragment : Fragment() ,OnClickListener{
    private  var  _binding : FragmentTermsServicesBinding? = null
    private  val binding  get() =  _binding!!

    private  val viewModel : TermsViewModel by lazy {
        ViewModelProvider(this)[TermsViewModel::class.java]
    }


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
        _binding = FragmentTermsServicesBinding.inflate(LayoutInflater.from(requireContext()),container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageBackButton.setOnClickListener(this)


        lifecycleScope.launch {
            if (NetworkMonitorCheck._isConnected.value) {
                        getTermsAndCondition()

                }else{
                LoadingUtils.showErrorDialog(requireContext(),
                    resources.getString(R.string.no_internet_dialog_msg)
                )}
        }


        lifecycleScope.launch {
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    Log.d("****","startLoading")
                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()
                    Log.d("****","stopLoading")
                }
            }
        }

    }

    private fun getTermsAndCondition() {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.getTermCondition().collect{
                when(it){

                    is NetworkResult.Success -> {
                        if (it.data != null){
                            Log.d("checkDataTerms",it.data)

                            val termsText = it.data
                            binding.textDescription.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Html.fromHtml(termsText, Html.FROM_HTML_MODE_LEGACY)
                            } else {
                                Html.fromHtml(termsText)
                            }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(p0: View?) {
       when(p0?.id){
           R.id.imageBackButton->{
               findNavController().navigate(R.id.profileFragment)
           }
       }
    }


}