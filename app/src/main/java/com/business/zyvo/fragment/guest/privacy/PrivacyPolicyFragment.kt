package com.business.zyvo.fragment.guest.privacy

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
import com.business.zyvo.databinding.FragmentPrivacyPolicyBinding
import com.business.zyvo.fragment.guest.privacy.viewModel.PrivacyPolicyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


@AndroidEntryPoint
class PrivacyPolicyFragment : Fragment(), OnClickListener {
    private var _binding: FragmentPrivacyPolicyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PrivacyPolicyViewModel by lazy {
        ViewModelProvider(this).get(PrivacyPolicyViewModel::class.java)
    }
    private var privacy: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //  param1 = it.getString(ARG_PARAM1)
            privacy = it.getInt("privacy")


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPrivacyPolicyBinding.inflate(
            LayoutInflater.from(requireContext()),
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageBackButton.setOnClickListener(this)

        lifecycleScope.launch {
            viewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect{isConn ->
                    if (!isConn){
                        LoadingUtils.showErrorDialog(requireContext(),resources.getString(R.string.no_internet_dialog_msg))
                    }else{
                        getPrivacyPolicy()
                    }

                }
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

    }

    private fun getPrivacyPolicy() {
        lifecycleScope.launch {
            viewModel.getPrivacyPolicy().collect{
                when(it){

                    is NetworkResult.Success -> {
                        if (it.data != null){
                            Log.d("checkDataPrivacy",it.data)

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
        when (p0?.id) {
            R.id.imageBackButton -> {
                if (privacy == 0) {
                    findNavController().navigate(R.id.profileFragment)
                } else {
                    findNavController().navigate(R.id.hostProfileFragment)
                }


            }
        }
    }


}