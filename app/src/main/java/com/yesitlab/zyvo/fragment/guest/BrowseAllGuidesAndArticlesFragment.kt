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
import com.yesitlab.zyvo.adapter.AdapterAllGuides
import com.yesitlab.zyvo.databinding.FragmentBrowseAllGuidesAndArticlesBinding
import com.yesitlab.zyvo.viewmodel.HelpCenterViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BrowseAllGuidesAndArticlesFragment : Fragment(), OnClickListener,View.OnClickListener {
  private var _binding : FragmentBrowseAllGuidesAndArticlesBinding? = null
    private val binding get() = _binding!!
    private  val viewModel : HelpCenterViewModel by viewModels()
    private lateinit var adapterAllGuides: AdapterAllGuides
    var textType : String? = null
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
        _binding = FragmentBrowseAllGuidesAndArticlesBinding.inflate(LayoutInflater.from(requireActivity()),container,false)
        binding.textType.setText(textType)
        adapterAllGuides = AdapterAllGuides(requireContext(), arrayListOf(),maxItemsToShow = null,this)
        binding.recyclerViewGuests.adapter = adapterAllGuides

        viewModel.list.observe(viewLifecycleOwner, Observer {
                list ->
            adapterAllGuides.updateItem(list)
        })


        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //val toggleGroup = findViewById<RadioGroup>(R.id.toggleGroup)
        binding.imageBackIcon.setOnClickListener(this)
//        binding.toggleGroup.setOnCheckedChangeListener { _, checkedId ->
//            when (checkedId) {
//                R.id.radioGuest -> {
//                    // Handle Guest selected
//                }
//                R.id.radioHost -> {
//                    // Handle Host selected
//                }
//            }
//        }

    }

    override fun itemClick(obj: Int) {
        findNavController().navigate(R.id.browseAllGuidesArticleOpenFragment)
    }

    override fun onClick(p0: View?) {
       when(p0?.id){
           R.id.imageBackIcon->{
               findNavController().navigate(R.id.helpCenterFragment)
           }
       }
    }


}