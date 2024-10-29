package com.yesitlab.zyvo.fragment.guest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.activity.guest.FiltersActivity
import com.yesitlab.zyvo.activity.guest.WhereTimeActivity
import com.yesitlab.zyvo.adapter.LoggedScreenAdapter
import com.yesitlab.zyvo.databinding.FragmentGuestDiscoverBinding
import com.yesitlab.zyvo.databinding.FragmentLoggedScreenBinding
import com.yesitlab.zyvo.utils.CommonAuthWorkUtils
import com.yesitlab.zyvo.viewmodel.ImagePopViewModel
import com.yesitlab.zyvo.viewmodel.LoggedScreenViewModel
import com.yesitlab.zyvo.viewmodel.guest.GuestDiscoverViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GuestDiscoverFragment : Fragment(),View.OnClickListener,OnClickListener {

    lateinit var binding :FragmentGuestDiscoverBinding ;
    private lateinit var startForResult: ActivityResultLauncher<Intent>
    private lateinit var adapter: LoggedScreenAdapter
    private var commonAuthWorkUtils: CommonAuthWorkUtils? = null
    private val loggedScreenViewModel: GuestDiscoverViewModel by lazy {
        ViewModelProvider(this)[GuestDiscoverViewModel::class.java]
    }
    private val imagePopViewModel: ImagePopViewModel by lazy {
        ViewModelProvider(this)[ImagePopViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {

        binding = FragmentGuestDiscoverBinding.inflate(LayoutInflater.from(requireContext()))

        val navController = findNavController()

        startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                // Handle the result
            }
        }

        commonAuthWorkUtils = CommonAuthWorkUtils(requireActivity(),navController)

        adapter = LoggedScreenAdapter(requireContext(),
            mutableListOf(),
            this,
            viewLifecycleOwner,
            imagePopViewModel)

        binding.recyclerViewBooking.adapter = adapter


        binding.filterIcon.setOnClickListener {
            var intent = Intent(requireContext(),FiltersActivity::class.java)
            startForResult.launch(intent)
        }

        binding.textWhere.setOnClickListener(this)


        loggedScreenViewModel.imageList.observe(viewLifecycleOwner, Observer {
            images -> adapter.updateData(images)
        })

        return binding.root
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.textWhere ->{
                var intent = Intent(requireContext(),WhereTimeActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun itemClick(obj: Int) {

    }

}