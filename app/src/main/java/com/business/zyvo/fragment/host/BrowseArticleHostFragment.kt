package com.business.zyvo.fragment.host

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.business.zyvo.AppConstant
import com.business.zyvo.R
import com.business.zyvo.adapter.host.ExploreArticlesAdapter
import com.business.zyvo.databinding.FragmentBrowseArticleHostBinding


class BrowseArticleHostFragment : Fragment() {

    lateinit var binding : FragmentBrowseArticleHostBinding
    lateinit var adapter : ExploreArticlesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        adapter = ExploreArticlesAdapter(mutableListOf())
        binding = FragmentBrowseArticleHostBinding.inflate(LayoutInflater.from(requireActivity()),container,false)
        binding.recyclerNewArticles.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        binding.recyclerNewArticles.adapter = adapter

        adapter.setOnItemClickListener(object :ExploreArticlesAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                findNavController().navigate(R.id.browse_aricle_details)
            }
        })

        binding.tvContactUs.setOnClickListener {
            findNavController().navigate(R.id.contact_us)
        }

        arguments?.let {
            if(it.containsKey(AppConstant.type)){
                Log.d("TESTING_ANDROID", "I am on Type")
                if(it.getString(AppConstant.type).equals("Article")) binding.tvViewTitle.setText("Browse all Articles") else  binding.tvViewTitle.setText("Browse all Guides")
            }
        }

        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }


}