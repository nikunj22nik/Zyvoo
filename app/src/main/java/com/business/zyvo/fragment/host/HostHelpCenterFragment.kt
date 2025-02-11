package com.business.zyvo.fragment.host

import android.os.Bundle
import android.util.Log
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
import com.business.zyvo.databinding.FragmentHostHelpCenterBinding
import com.business.zyvo.viewmodel.HelpCenterViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HostHelpCenterFragment : Fragment(),View.OnClickListener, OnClickListener {
    lateinit var binding :FragmentHostHelpCenterBinding
    private  val viewModel : HelpCenterViewModel by viewModels()
    private lateinit var adapterAllGuides: AdapterAllGuides
    private lateinit var adapterAllArticles: AdapterAllArticles

    lateinit var  navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) :  View? {
        binding = FragmentHostHelpCenterBinding.inflate(LayoutInflater.from(requireActivity()),container,false)
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
        binding.textBrowseAllGuides.setOnClickListener(this)
        binding.textBrowseAllArticle.setOnClickListener(this)

        arguments?.let {
            Log.d("TESTING_ANDROID", "I AM ON CLICK OF HOST Arguments")

            if(it.containsKey(AppConstant.type) && it.getString(AppConstant.type).equals(AppConstant.Guest)){
                binding.radioGuest.isChecked = true

                binding.radioHost.isChecked = false

                Log.d("TESTING_ANDROID", "Guest")

            }else{
                binding.radioHost.isChecked = true
                binding.radioGuest.isChecked = false
                Log.d("TESTING_ANDROID", "Host")
            }
        }

        binding.textContactUsButton.setOnClickListener {
            findNavController().navigate(R.id.contact_us)
        }


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.imageBackIcon.setOnClickListener {
            navController.navigateUp()
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.textBrowseAllGuides ->{
                var bundle = Bundle()
                bundle.putString(AppConstant.type ,"Guides")
                  findNavController().navigate(R.id.browse_article_host,bundle)
            }
            R.id.textBrowseAllArticle ->{
                var bundle = Bundle()
                bundle.putString(AppConstant.article,"Article")
                 findNavController().navigate(R.id.browse_article_host,bundle)
            }
        }
    }

    override fun itemClick(obj: Int) {
        findNavController().navigate(R.id.browse_aricle_details)
    }


}