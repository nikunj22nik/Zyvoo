package com.business.zyvo.fragment.both.turnLocation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.business.zyvo.R
import com.business.zyvo.databinding.FragmentTurnLocationBinding


class TurnLocationFragment : Fragment() {
    private var _binding : FragmentTurnLocationBinding? = null
    private  val binding get() = _binding!!
    var data:String = ""
    var type:String = ""
    var email:String = ""
    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTurnLocationBinding.inflate(LayoutInflater.from(requireActivity()),container,false)
        arguments?.let {
            data = it.getString("data")?:""
            type = it.getString("type")?:""
            email = it.getString("email")?:""
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission granted

                    val bundle = Bundle()
                    bundle.putString("data",data)
                    bundle.putString("type",type)
                    bundle.putString("email",email)
                    findNavController().navigate(R.id.completeProfileFragment,bundle)
                } else {
                    val bundle = Bundle()
                    bundle.putString("data",data)
                    bundle.putString("type",type)
                    bundle.putString("email",email)
                    findNavController().navigate(R.id.completeProfileFragment,bundle)

                }
            }

        binding.btnLocation.setOnClickListener {


            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED
            ) {

                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)


            } else {
                val bundle = Bundle()
                bundle.putString("data",data)
                bundle.putString("type",type)
                bundle.putString("email",email)
                findNavController().navigate(R.id.completeProfileFragment,bundle)
            }
        }
        binding.textNotnow.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("data",data)
            bundle.putString("type",type)
            bundle.putString("email",email)
            findNavController().navigate(R.id.completeProfileFragment,bundle)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val bundle = Bundle()
        bundle.putString("data",data)
        bundle.putString("type",type)
        bundle.putString("email",email)
        findNavController().navigate(R.id.completeProfileFragment,bundle)

        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val bundle = Bundle()
                bundle.putString("data",data)
                bundle.putString("type",type)
                bundle.putString("email",email)
                findNavController().navigate(R.id.completeProfileFragment,bundle)
            } else {
                val bundle = Bundle()
                bundle.putString("data",data)
                bundle.putString("type",type)
                bundle.putString("email",email)
                findNavController().navigate(R.id.completeProfileFragment,bundle)
            }
        }

        //Toast.makeText(requireContext(), "Permission Hit", Toast.LENGTH_SHORT).show()

    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}