package com.business.zyvo.fragment.both.turnNotifications

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.business.zyvo.R
import com.business.zyvo.databinding.FragmentTurnNotificationsBinding


class TurnNotificationsFragment : Fragment() {
private var  _binding: FragmentTurnNotificationsBinding? = null
    private val binding get() = _binding!!

    var data:String = ""
    var type:String = ""
    var email:String = ""
    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            data = requireArguments().getString("data")!!
            type = requireArguments().getString("type")!!
            email = requireArguments().getString("email")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTurnNotificationsBinding.inflate(LayoutInflater.from(requireActivity()),container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                val bundle = Bundle()
                bundle.putString("data",data)
                bundle.putString("type",type)
                bundle.putString("email",email)
                findNavController().navigate(R.id.turnLocationFragment,bundle)
            }else{
                val bundle = Bundle()
                bundle.putString("data",data)
                bundle.putString("type",type)
                bundle.putString("email",email)
                findNavController().navigate(R.id.turnLocationFragment,bundle)
            }

        }

        binding.textNotnow.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("data",data)
            bundle.putString("type",type)
            bundle.putString("email",email)
            findNavController().navigate(R.id.turnLocationFragment,bundle)
        }

        binding.btnNotification.setOnClickListener{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    val bundle = Bundle()
                    bundle.putString("data",data)
                    bundle.putString("type",type)
                    bundle.putString("email",email)
                    findNavController().navigate(R.id.turnLocationFragment,bundle)
                }
                else {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }else{
                val bundle = Bundle()
                bundle.putString("data",data)
                bundle.putString("type",type)
                bundle.putString("email",email)
                findNavController().navigate(R.id.turnLocationFragment,bundle)
            }

        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}