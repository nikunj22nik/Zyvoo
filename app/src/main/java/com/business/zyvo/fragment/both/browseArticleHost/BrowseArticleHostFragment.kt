package com.business.zyvo.fragment.both.browseArticleHost

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.business.zyvo.AppConstant

import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.adapter.host.ExploreArticlesAdapter
import com.business.zyvo.databinding.FragmentBrowseAllGuidesAndArticlesBinding
import com.business.zyvo.databinding.FragmentBrowseArticleHostBinding
import com.business.zyvo.fragment.both.browseArticleHost.model.BrowseArticleModel
import com.business.zyvo.fragment.both.browseArticleHost.viewModel.BrowseArticleHostViewModel
import com.business.zyvo.session.SessionManager
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BrowseArticleHostFragment : Fragment() {


    private var _binding: FragmentBrowseArticleHostBinding? = null
    private val binding get() = _binding!!
    lateinit var adapter: ExploreArticlesAdapter
    val viewModel: BrowseArticleHostViewModel by lazy {
        ViewModelProvider(this)[BrowseArticleHostViewModel::class.java]
    }
    var type: String? = null
    lateinit var sessionManager: SessionManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBrowseArticleHostBinding.inflate(
            LayoutInflater.from(requireActivity()),
            container,
            false
        )
        binding.tvContactUs.setOnClickListener {
            findNavController().navigate(R.id.contact_us)
        }
        sessionManager = SessionManager(requireContext())

        arguments?.let {
            if (it.containsKey(AppConstant.type)) {
                if (it.getString(AppConstant.type).equals("Article")) {
                    type = "article"
                    binding.tvViewTitle.setText("Hi ${sessionManager.getFirstName()}, how can we help?")
                    val userType =
                        sessionManager?.getUserType()?.replaceFirstChar { it.uppercase() } ?: ""
                    binding.textLabel.setText("Articles for " + userType)
                } else {
                    type = "guides"
                    binding.tvViewTitle.setText("Hi ${sessionManager.getFirstName()}, how can we help?")
                    val userType =
                        sessionManager?.getUserType()?.replaceFirstChar { it.uppercase() } ?: ""
                    binding.textLabel.setText("Guides for " + userType)
                }
            }
        }

        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ExploreArticlesAdapter(requireContext(), mutableListOf(), type ?: "article")
        binding.recyclerNewArticles.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerNewArticles.adapter = adapter

        adapter.setOnItemClickListener(object : ExploreArticlesAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                if (type == "guides") {
                    val textType = "guides"
                    val bundle = Bundle()
                    bundle.apply {
                        putString(AppConstant.Id, position.toString())
                        putString(AppConstant.textType, textType)
                    }
                    findNavController().navigate(R.id.browse_aricle_details, bundle)
                } else {
                    val textType = "article"
                    val bundle = Bundle()
                    bundle.apply {
                        putString(AppConstant.Id, position.toString())
                        putString(AppConstant.textType, textType)
                    }
                    findNavController().navigate(R.id.browse_aricle_details, bundle)
                }

            }
        })

        lifecycleScope.launch {
            viewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        LoadingUtils.showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        if (type == "guides") {
                            getGuideList("")
                        } else {
                            getArticleList("")
                        }
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

        binding.imageSearchButton.setOnClickListener {
            val searchBar = binding.etSearch.text.toString().trim()

            if (type == "guides") {
                getGuideList(searchBar)
            } else {
                getArticleList(searchBar)
            }
        }
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchBar = binding.etSearch.text.toString().trim()
                // Handle search here
                if (type == "guides") {
                    getGuideList(searchBar)
                } else {
                    getArticleList(searchBar)
                }
                true
            } else {
                false
            }
        }

    }


    private fun getGuideList(text: String) {
        val sessionManager = SessionManager(requireContext())
        val usertype = sessionManager.getUserType() ?: ""
        lifecycleScope.launch {
            viewModel.getGuideList(text, usertype).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        val model = Gson().fromJson(it.data, BrowseArticleModel::class.java)
                        Log.d("checkDataList", model.data.toString())
                        if (!model.data.isNullOrEmpty()) {
                            binding.textNoDataFound.visibility = View.GONE
                            binding.recyclerNewArticles.visibility = View.VISIBLE
                            adapter.updateItem(model.data)
                        } else {
                            binding.textNoDataFound.visibility = View.VISIBLE
                            binding.recyclerNewArticles.visibility = View.GONE
                        }
                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(), it.message!!)
                    }

                    else -> {

                    }

                }
            }


        }
    }


    private fun getArticleList(text: String) {
        val sessionManager = SessionManager(requireContext())
        val usertype = sessionManager.getUserType() ?: ""
        lifecycleScope.launch {
            viewModel.getArticleList(text, usertype).collect {
                when (it) {

                    is NetworkResult.Success -> {
                        val model = Gson().fromJson(it.data, BrowseArticleModel::class.java)

//                        if (model.data != null) {
//                            adapter.updateItem(model.data)
//                        }

                        if (!model.data.isNullOrEmpty()) {
                            binding.textNoDataFound.visibility = View.GONE
                            binding.recyclerNewArticles.visibility = View.VISIBLE
                            adapter.updateItem(model.data)
                        } else {
                            binding.textNoDataFound.visibility = View.VISIBLE
                            binding.recyclerNewArticles.visibility = View.GONE
                        }


                    }

                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(), it.message!!)

                    }

                    else -> {

                    }

                }
            }


        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Clean up view references and other UI-related resources
    }


}