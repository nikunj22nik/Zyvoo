package com.business.zyvo.fragment.both.faq

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.adapter.FaqAdapter
import com.business.zyvo.databinding.FragmentFrequentlyAskedQuestionsBinding
import com.business.zyvo.fragment.both.faq.model.FaqModel
import com.business.zyvo.fragment.both.faq.viewModel.FaqViewModel
import com.business.zyvo.utils.NetworkMonitorCheck
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FrequentlyAskedQuestionsFragment : Fragment() {
    private var _binding : FragmentFrequentlyAskedQuestionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter : FaqAdapter
    private var list : MutableList<FaqModel> = mutableListOf()
    lateinit var navController: NavController
    private val viewModel : FaqViewModel by lazy {
        ViewModelProvider(this)[FaqViewModel::class.java]
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
        _binding = FragmentFrequentlyAskedQuestionsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
          adapter = FaqAdapter(requireContext(), mutableListOf())
        binding.recyclerViewItem.adapter = adapter
        binding.imageBackButton.setOnClickListener {
            navController.navigateUp()
        }

        lifecycleScope.launch {
            if (NetworkMonitorCheck._isConnected.value) {
                getFaq()

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

    private fun getFaq() {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.getFaq().collect{
                when(it){

                    is NetworkResult.Success -> {

                        if (it.data?.isNotEmpty() == true){
                            it.data?.let { it1 -> adapter.updateItem(it1) }
                        }else{

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


}