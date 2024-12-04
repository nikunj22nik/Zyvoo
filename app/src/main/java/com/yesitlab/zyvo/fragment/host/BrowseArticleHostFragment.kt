package com.yesitlab.zyvo.fragment.host

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.yesitlab.zyvo.AppConstant
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.host.ExploreArticlesAdapter
import com.yesitlab.zyvo.databinding.FragmentBrowseArticleHostBinding


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

        arguments?.let {
            if(it.containsKey(AppConstant.type)){
                if(it.getString(AppConstant.type).equals("Article")) binding.tvViewTitle.setText("Browse all Articles") else  binding.tvViewTitle.setText("Browse all Guides")
            }
        }

        return binding.root

    }


}