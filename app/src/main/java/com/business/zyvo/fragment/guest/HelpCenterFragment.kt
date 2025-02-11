package com.business.zyvo.fragment.guest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.business.zyvo.AppConstant
import com.business.zyvo.OnClickListener
import com.business.zyvo.R
import com.business.zyvo.adapter.AdapterAllArticles
import com.business.zyvo.adapter.AdapterAllGuides
import com.business.zyvo.databinding.FragmentHelpCenterBinding
import com.business.zyvo.viewmodel.HelpCenterViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HelpCenterFragment : Fragment() ,View.OnClickListener, OnClickListener{

    private var binding : FragmentHelpCenterBinding?=null

    private  val viewModel : HelpCenterViewModel by viewModels()
    private lateinit var adapterAllGuides: AdapterAllGuides
    private lateinit var adapterAllArticles: AdapterAllArticles
    lateinit var  navController: NavController


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentHelpCenterBinding.inflate(layoutInflater,container,false)

        adapterAllGuides = AdapterAllGuides(requireContext(), arrayListOf(),maxItemsToShow = 4,this)
        binding!!.recyclerViewGuests.adapter = adapterAllGuides
        viewModel.list.observe(viewLifecycleOwner, Observer {
            list -> adapterAllGuides.updateItem(list)
        })

        adapterAllArticles = AdapterAllArticles(requireContext(), arrayListOf(),maxItemsToShow = 3,this)
        binding!!.recyclerViewArticle.adapter = adapterAllArticles

        viewModel.articlesList.observe(viewLifecycleOwner, Observer {
             articleList -> adapterAllArticles.updateItem(articleList)
        })


        binding!!.imageBackIcon.setOnClickListener {
            findNavController().navigateUp()
        }



        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        //val toggleGroup = findViewById<RadioGroup>(R.id.toggleGroup)

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