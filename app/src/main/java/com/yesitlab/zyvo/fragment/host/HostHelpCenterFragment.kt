package com.yesitlab.zyvo.fragment.host

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
import com.yesitlab.zyvo.databinding.FragmentHostHelpCenterBinding
import com.yesitlab.zyvo.viewmodel.HelpCenterViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HostHelpCenterFragment : Fragment(),View.OnClickListener, OnClickListener {
    lateinit var binding :FragmentHostHelpCenterBinding
    private  val viewModel : HelpCenterViewModel by viewModels()
    private lateinit var adapterAllGuides: AdapterAllGuides
    private lateinit var adapterAllArticles: AdapterAllArticles



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
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


        return binding.root
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.textBrowseAllGuides ->{
                var bundle = Bundle()
                bundle.putString(AppConstant.type ,"Guides")
                  findNavController().navigate(R.id.browse_article_host)
            }
            R.id.textBrowseAllArticle ->{
                var bundle = Bundle()
                bundle.putString(AppConstant.article,"Article")
                 findNavController().navigate(R.id.browse_article_host)
            }
        }
    }

    override fun itemClick(obj: Int) {

    }


}