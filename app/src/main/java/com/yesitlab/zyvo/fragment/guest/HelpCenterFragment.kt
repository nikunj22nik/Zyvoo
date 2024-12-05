package com.yesitlab.zyvo.fragment.guest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.yesitlab.zyvo.AppConstant
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.AdapterAllArticles
import com.yesitlab.zyvo.adapter.AdapterAllGuides
import com.yesitlab.zyvo.databinding.FragmentHelpCenterBinding
import com.yesitlab.zyvo.viewmodel.HelpCenterViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HelpCenterFragment : Fragment() ,View.OnClickListener, OnClickListener{

    private var _binding : FragmentHelpCenterBinding? = null
    private val binding get() = _binding!!
    private  val viewModel : HelpCenterViewModel by viewModels()
    private lateinit var adapterAllGuides: AdapterAllGuides
    private lateinit var adapterAllArticles: AdapterAllArticles

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHelpCenterBinding.inflate(LayoutInflater.from(requireContext()),container,false)

        adapterAllGuides = AdapterAllGuides(requireContext(), arrayListOf(),maxItemsToShow = 4,this)
        binding.recyclerViewGuests.adapter = adapterAllGuides
        viewModel.list.observe(viewLifecycleOwner, Observer {
            list -> adapterAllGuides.updateItem(list)
        })

        adapterAllArticles = AdapterAllArticles(requireContext(), arrayListOf(),maxItemsToShow = 3,this)
        binding.recyclerViewArticle.adapter = adapterAllArticles

        viewModel.articlesList.observe(viewLifecycleOwner, Observer {
             articleList -> adapterAllArticles.updateItem(articleList)
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //val toggleGroup = findViewById<RadioGroup>(R.id.toggleGroup)
        binding.imageBackIcon.setOnClickListener(this)
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

    }

    override fun onClick(p0: View?) {
       when(p0?.id){
           R.id.imageBackIcon->{
               findNavController().navigate(R.id.profileFragment)
           }
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