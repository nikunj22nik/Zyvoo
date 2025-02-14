package com.business.zyvo.fragment.guest.helpCenter

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
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

    private var binding : FragmentHelpCenterBinding?=null

    private  val viewModel : HelpCenterViewModel by lazy {
        ViewModelProvider(this)[HelpCenterViewModel::class.java]
    }

    private lateinit var adapterAllGuides: AdapterAllGuides
    private lateinit var adapterAllArticles: AdapterAllArticles
    lateinit var  navController: NavController
    var session: SessionManager?=null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentHelpCenterBinding.inflate(layoutInflater,container,false)
        session = SessionManager(requireContext())
//        adapterAllGuides = AdapterAllGuides(requireContext(), arrayListOf(),maxItemsToShow = 4,this)
//        binding!!.recyclerViewGuests.adapter = adapterAllGuides
////        viewModel.list.observe(viewLifecycleOwner, Observer {
////            list -> adapterAllGuides.updateItem(list)
////        })
//
//        adapterAllArticles = AdapterAllArticles(requireContext(), arrayListOf(),maxItemsToShow = 3,this)
//        binding!!.recyclerViewArticle.adapter = adapterAllArticles

//        viewModel.articlesList.observe(viewLifecycleOwner, Observer {
//             articleList -> adapterAllArticles.updateItem(articleList)
//        })


        binding!!.imageBackIcon.setOnClickListener {
            findNavController().navigateUp()
        }



        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        //val toggleGroup = findViewById<RadioGroup>(R.id.toggleGroup)
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
        binding!!.textBrowseAllGuides.setOnClickListener(this)
        binding!!.textBrowseAllArticle.setOnClickListener(this)
        binding!!.toggleGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioGuest -> {
                    // Handle Guest selected
                }
                R.id.radioHost -> {
                    // Handle Host selected
                }
            }
        }
        binding!!.radioGuest.isChecked = true


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
               bundle.putString(AppConstant.textType,"Guides for Guests")
               findNavController().navigate(R.id.browseAllGuidesAndArticlesFragment,bundle)
           }
           R.id.textBrowseAllArticle->{
               val bundle = Bundle()
               bundle.putString(AppConstant.textType,"Guides for Articles")
               findNavController().navigate(R.id.browseAllGuidesAndArticlesFragment,bundle)
           }
       }
    }

    override fun itemClick(obj: Int) {
       findNavController().navigate(R.id.browseAllGuidesArticleOpenFragment)
    }



}