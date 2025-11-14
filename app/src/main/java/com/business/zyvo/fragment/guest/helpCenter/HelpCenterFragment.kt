package com.business.zyvo.fragment.guest.helpCenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnClickListener
import com.business.zyvo.R
import com.business.zyvo.adapter.AdapterAllArticles
import com.business.zyvo.adapter.AdapterAllGuides
import com.business.zyvo.databinding.FragmentHelpCenterBinding
import com.business.zyvo.fragment.guest.helpCenter.model.HelpCenterResponse
import com.business.zyvo.fragment.guest.helpCenter.viewModel.HelpCenterViewModel
import com.business.zyvo.session.SessionManager
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HelpCenterFragment : Fragment() ,View.OnClickListener, OnClickListener{

    private var _binding : FragmentHelpCenterBinding?=null
    private  val binding get() = _binding!!

    private  val viewModel : HelpCenterViewModel by lazy {
        ViewModelProvider(this)[HelpCenterViewModel::class.java]
    }

    private lateinit var adapterAllGuides: AdapterAllGuides
    private lateinit var adapterAllArticles: AdapterAllArticles
    lateinit var  navController: NavController
    var session: SessionManager?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHelpCenterBinding.inflate(layoutInflater,container,false)
        session = SessionManager(requireContext())
        binding.imageBackIcon.setOnClickListener {
            findNavController().navigateUp()
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        lifecycleScope.launch {
            viewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect{isConn ->
                    if (!isConn){
                        LoadingUtils.showErrorDialog(requireContext(),resources.getString(R.string.no_internet_dialog_msg))
                    }else{
                        getHelpCenter()
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
        binding.textBrowseAllGuides.setOnClickListener(this)
        binding.textBrowseAllArticle.setOnClickListener(this)
        binding.toggleGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioGuest -> {
                    // Handle Guest selected
                }
                R.id.radioHost -> {
                    // Handle Host selected
                }
            }
        }
        binding.radioGuest.isChecked = true


    }


 private  fun  getHelpCenter(){
     lifecycleScope.launch {
         viewModel.getHelpCenter(session?.getUserId().toString(),"guest").collect{
             when(it){

                 is NetworkResult.Success -> {

                     if (it.data != null){
                      var model = Gson().fromJson<HelpCenterResponse>(it.data,HelpCenterResponse::class.java)

                       adapterAllGuides.updateItem(model.data.guides)
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


    override fun onClick(p0: View?) {
       when(p0?.id){

           R.id.textBrowseAllGuides->{
               val bundle = Bundle()
               bundle.putString(AppConstant.textType, AppConstant.GUIDES_FOR_GUEST/*"Guides for Guests"*/)
               findNavController().navigate(R.id.browseAllGuidesAndArticlesFragment,bundle)
           }
           R.id.textBrowseAllArticle->{
               val bundle = Bundle()
               bundle.putString(AppConstant.textType,AppConstant.GUIDES_FOR_ARTICLES/*"Guides for Articles"*/)
               findNavController().navigate(R.id.browseAllGuidesAndArticlesFragment,bundle)
           }
       }
    }

    override fun itemClick(obj: Int) {
       findNavController().navigate(R.id.browseAllGuidesArticleOpenFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}