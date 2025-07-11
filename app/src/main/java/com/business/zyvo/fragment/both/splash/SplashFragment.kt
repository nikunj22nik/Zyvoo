package com.business.zyvo.fragment.both.splash

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.business.zyvo.AppConstant
import com.business.zyvo.R
import com.business.zyvo.activity.GuesMain
import com.business.zyvo.activity.HostMainActivity
import com.business.zyvo.activity.guest.propertydetails.RestaurantDetailActivity
import com.business.zyvo.databinding.FragmentSplashBinding
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.CommonAuthWorkUtils
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.MultipartUtils
import retrofit2.http.Tag


class SplashFragment : Fragment() {

  var binding : FragmentSplashBinding? = null
      //  private var _binding: FragmentSplashBinding? = null

    //private val binding get() = _binding!!

    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentSplashBinding.inflate(LayoutInflater.from(requireActivity()), container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("TESTING_android",""+MultipartUtils.isPhoneNumberMatchingCountryCode("443951858","+48"))

        Log.d("Testing","I AM ON SPLASH SCREEN")

          handler = Handler(Looper.getMainLooper())
          handler.postDelayed({
                val session = SessionManager(requireContext())
                if (session.getUserId() != -1 && session.getUserSession()!! && !session.getAuthToken().equals("") && session.getName() != "") {
                    if (session.getCurrentPanel().equals(AppConstant.Host)) {
                        handelDeeplinkHost()
                    }
                    else {
                        handlingDeepLink()
                    }
                }
                else if (session.getUserId() != -1 && session.getName() == "") {
                    findNavController().navigate(R.id.completeProfileFragment)
                }
                else {
                    findNavController().navigate(R.id.loggedScreenFragment)
                }

            }, 2000)

    }

    private fun handelDeeplinkHost() {
        // Get the intent that started this activity
        val intent = requireActivity().intent
        // Check if the intent contains a URI (deep link)
        if (intent?.action == Intent.ACTION_VIEW) {
            val data: Uri? = intent.data
            if (data != null && data.scheme == "zyvoo" && data.host == "property") {
                val location = data.getQueryParameter("location")
                val type = data.getQueryParameter("user_type")?.replace("://","")
                // Now you can use the propertyId in your activity
                // Fetch property details using the propertyId
                 if (location.equals("Article") && type.equals(AppConstant.Host)) {
                    val guideId = data.getQueryParameter("guide_id")
                     val textType = data.getQueryParameter("textType")
                    Log.d(ErrorDialog.TAG, "guide  ID: $guideId")
                    val intent = Intent(requireContext(), HostMainActivity::class.java)
                    intent.putExtra("location",location)
                    intent.putExtra("guideId",guideId)
                     intent.putExtra("textType",textType)
                    startActivity(intent)
                    requireActivity().finish()
                }else{
                     val intent = Intent(requireContext(), HostMainActivity::class.java)
                     startActivity(intent)
                     requireActivity().finish()
                }

            }
        }else{
            val intent = Intent(requireContext(), HostMainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun handlingDeepLink() {
        // Get the intent that started this activity
        val intent = requireActivity().intent
        // Check if the intent contains a URI (deep link)
        if (intent?.action == Intent.ACTION_VIEW) {
            val data: Uri? = intent.data
            if (data != null && data.scheme == "zyvoo" && data.host == "property") {
                val location = data.getQueryParameter("location")
                val type = data.getQueryParameter("user_type")?.replace("://","")

                // Now you can use the propertyId in your activity
                // Fetch property details using the propertyId
                if (location.equals("PropertyDetails") &&  type.equals(AppConstant.Guest)){
                    val propertyId = data.getQueryParameter("propertyId")
                    Log.d(ErrorDialog.TAG, "Property ID: $propertyId")
                    val intent = Intent(requireContext(), GuesMain::class.java)
                    intent.putExtra("propertyId", propertyId)
                    intent.putExtra("location",location)
                    intent.putExtra("propertyMile", "")
                    startActivity(intent)
                    requireActivity().finish()
                }else if (location.equals("Article") &&  type.equals(AppConstant.Guest)) {
                    val guideId = data.getQueryParameter("guide_id")
                    val textType = data.getQueryParameter("textType")
                    Log.d(ErrorDialog.TAG, "guide  ID: $guideId")
                    val intent = Intent(requireContext(), GuesMain::class.java)
                    intent.putExtra("location",location)
                    intent.putExtra("guideId",guideId)
                    intent.putExtra("textType",textType)
                    startActivity(intent)
                    requireActivity().finish()
                }else{
                    val intent = Intent(requireContext(), GuesMain::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }

            }
        }else{
             val intent = Intent(requireContext(), GuesMain::class.java)
                       startActivity(intent)
                       requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
        binding = null
       // _binding = null
    }

}