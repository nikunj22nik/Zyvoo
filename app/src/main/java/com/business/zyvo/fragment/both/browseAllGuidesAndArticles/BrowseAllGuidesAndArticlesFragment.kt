package com.business.zyvo.fragment.both.browseAllGuidesAndArticles

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.business.zyvo.AppConstant
import com.business.zyvo.OnClickListener1
import com.business.zyvo.R
import com.business.zyvo.adapter.AdapterAllGuides
import com.business.zyvo.databinding.FragmentBrowseAllGuidesAndArticlesBinding
import com.business.zyvo.fragment.guest.helpCenter.viewModel.HelpCenterViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BrowseAllGuidesAndArticlesFragment : Fragment(), OnClickListener1, View.OnClickListener {
    private var _binding: FragmentBrowseAllGuidesAndArticlesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HelpCenterViewModel by viewModels()
    private lateinit var adapterAllGuides: AdapterAllGuides
    var textType: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            textType = it.getString(AppConstant.textType)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBrowseAllGuidesAndArticlesBinding.inflate(
            LayoutInflater.from(requireActivity()),
            container,
            false
        )
        binding.textType.setText(textType)
        adapterAllGuides =
            AdapterAllGuides(requireContext(), arrayListOf(), maxItemsToShow = null, this)
        binding.recyclerViewGuests.adapter = adapterAllGuides

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageBackIcon.setOnClickListener(this)

    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imageBackIcon -> {
                findNavController().navigate(R.id.helpCenterFragment)
            }
        }
    }

    override fun itemClick(obj: Int, text: String) {
        findNavController().navigate(R.id.browseAllGuidesArticleOpenFragment)
    }

}