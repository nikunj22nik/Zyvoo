package com.business.zyvo.fragment.both.contactUs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.TouchableMapFragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.business.zyvo.databinding.FragmentContactUsBinding
import com.business.zyvo.fragment.both.contactUs.viewmodel.ContactUsViewModel
import com.business.zyvo.model.Location
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.NetworkMonitorCheck
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@AndroidEntryPoint
class ContactUsFragment : Fragment() , OnMapReadyCallback {

    private var _binding :FragmentContactUsBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleMap: GoogleMap
    private var touchableMapFragment: TouchableMapFragment? = null
    lateinit var navController: NavController
    private val viewModel : ContactUsViewModel by lazy {
     ViewModelProvider(this)[ContactUsViewModel::class.java]
    }
    var session: SessionManager?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _binding = FragmentContactUsBinding.inflate(inflater, container, false)
        session = SessionManager(requireContext())
//        val mapView = binding.rl_map_view
//        mapView.onCreate(savedInstanceState)
//        mapView.getMapAsync(this)
        setupTouchableMapFragment()

        return binding.root
    }
    private fun setupTouchableMapFragment() {
        // Create and setup TouchableMapFragment
        touchableMapFragment = TouchableMapFragment()

        // Set touch event callbacks
        touchableMapFragment?.touchDown = {
            // Handle touch down event
            // For example, disable parent scroll view scrolling
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
        }

        touchableMapFragment?.touchUp = {
            // Handle touch up event
            // Re-enable parent scroll view scrolling
            binding.scrollView.requestDisallowInterceptTouchEvent(false)
        }

        // Add the fragment to the container
        childFragmentManager.beginTransaction()
            .replace(R.id.rl_map_view, touchableMapFragment!!)
            .commit()

        // Get map asynchronously
        touchableMapFragment?.getMapAsync(this)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        lifecycleScope.launch {
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()

                }
            }
        }

        binding.rlSubmitBtn.setOnClickListener {
            if (validation()){
                lifecycleScope.launch {
                    if (NetworkMonitorCheck._isConnected.value) {
                        contactUs()
                    }else{
                        showErrorDialog(requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    }
                }
            }
        }

        binding.imageBackIcon.setOnClickListener {
            navController.navigateUp()
        }
    }
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        val pattern = Pattern.compile(emailPattern)
        return pattern.matcher(email).matches()
    }

    private fun contactUs() {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.contactUs(session?.getUserId().toString(),binding.etName.text.toString().trim(), binding.etEmail.text.toString().trim(), binding.etMessage.text.toString().trim()).collect{
                when(it){
                    is NetworkResult.Success -> {
                        if (it.data != null){
                            LoadingUtils.showSuccessDialog(requireContext(),it.data)
                            binding.etName.text.clear()
                            binding.etEmail.text.clear()
                            binding.etMessage.text.clear()
                        }
                    }
                    is NetworkResult.Error -> {
                        showErrorDialog(requireContext(),it.message!!)

                    }
                    else ->{

                    }

                }
            }


        }
    }


    private fun validation(): Boolean{
        if (binding.etName.text.isEmpty()){
            showErrorDialog(requireContext(), AppConstant.name)
            return false
        }else if (binding.etEmail.text.isEmpty()){
            showErrorDialog(requireContext(), AppConstant.email)
            return false
        }else if (!isValidEmail(binding.etEmail.text.toString())){
            showErrorDialog(requireContext(),AppConstant.invalideemail)
            return false
        }
        else if (binding.etMessage.text.isEmpty()){
            showErrorDialog(requireContext(), AppConstant.message)
            return false
        }else if (!isValidEmail(binding.etEmail.text.toString().trim())){
            showErrorDialog(requireContext(), AppConstant.emailValid)
            return false
        }

        return true
    }

    override fun onMapReady(p0: GoogleMap) {

        googleMap = p0

        val locations = listOf(
            Location(37.7749, -122.4194, " $13/h"), Location(34.0522, -118.2437, " $15/h"),
            Location(40.7128, -74.0060, " $19/h"), Location(51.5074, -0.1278, " $23/h"),
            Location(48.8566, 2.3522, " $67/h")
        )

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
/*
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.TouchableMapFragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.business.zyvo.databinding.FragmentContactUsBinding
import com.business.zyvo.fragment.both.contactUs.viewmodel.ContactUsViewModel
import com.business.zyvo.model.Location
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.NetworkMonitorCheck
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

@AndroidEntryPoint
class ContactUsFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentContactUsBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleMap: GoogleMap
    private var touchableMapFragment: TouchableMapFragment? = null
    lateinit var navController: NavController
    private val viewModel: ContactUsViewModel by lazy {
        ViewModelProvider(this)[ContactUsViewModel::class.java]
    }
    var session: SessionManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentContactUsBinding.inflate(inflater, container, false)
        session = SessionManager(requireContext())

        // Initialize TouchableMapFragment instead of MapView
        setupTouchableMapFragment()

        return binding.root
    }

    private fun setupTouchableMapFragment() {
        // Create and setup TouchableMapFragment
        touchableMapFragment = TouchableMapFragment()

        // Set touch event callbacks
        touchableMapFragment?.touchDown = {
            // Handle touch down event
            // For example, disable parent scroll view scrolling
            binding.scrollView.requestDisallowInterceptTouchEvent(true)
        }

        touchableMapFragment?.touchUp = {
            // Handle touch up event
            // Re-enable parent scroll view scrolling
            binding.scrollView.requestDisallowInterceptTouchEvent(false)
        }

        // Add the fragment to the container
        childFragmentManager.beginTransaction()
            .replace(R.id.rl_map_view, touchableMapFragment!!)
            .commit()

        // Get map asynchronously
        touchableMapFragment?.getMapAsync(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        lifecycleScope.launch {
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }

        binding.rlSubmitBtn.setOnClickListener {
            if (validation()) {
                lifecycleScope.launch {
                    if (NetworkMonitorCheck._isConnected.value) {
                        contactUs()
                    } else {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    }
                }
            }
        }

        binding.imageBackIcon.setOnClickListener {
            navController.navigateUp()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
        val pattern = Pattern.compile(emailPattern)
        return pattern.matcher(email).matches()
    }

    private fun contactUs() {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.contactUs(
                session?.getUserId().toString(),
                binding.etName.text.toString().trim(),
                binding.etEmail.text.toString().trim(),
                binding.etMessage.text.toString().trim()
            ).collect {
                when (it) {
                    is NetworkResult.Success -> {
                        if (it.data != null) {
                            LoadingUtils.showSuccessDialog(requireContext(), it.data)
                            binding.etName.text.clear()
                            binding.etEmail.text.clear()
                            binding.etMessage.text.clear()
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

    private fun validation(): Boolean {
        if (binding.etName.text.isEmpty()) {
            showErrorDialog(requireContext(), AppConstant.name)
            return false
        } else if (binding.etEmail.text.isEmpty()) {
            showErrorDialog(requireContext(), AppConstant.email)
            return false
        } else if (!isValidEmail(binding.etEmail.text.toString())) {
            showErrorDialog(requireContext(), AppConstant.invalideemail)
            return false
        } else if (binding.etMessage.text.isEmpty()) {
            showErrorDialog(requireContext(), AppConstant.message)
            return false
        } else if (!isValidEmail(binding.etEmail.text.toString().trim())) {
            showErrorDialog(requireContext(), AppConstant.emailValid)
            return false
        }
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        // Example: Add markers for your locations
        val locations = listOf(
            Location(37.7749, -122.4194, "$13/h"),
            Location(34.0522, -118.2437, "$15/h"),
            Location(40.7128, -74.0060, "$19/h"),
            Location(51.5074, -0.1278, "$23/h"),
            Location(48.8566, 2.3522, "$67/h")
        )

        // Add markers to the map
        locations.forEach { location ->
            val latLng = LatLng(location.latitude, location.longitude)
            googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(location.title)
            )
        }

        // Move camera to first location or a default location
        if (locations.isNotEmpty()) {
            val firstLocation = LatLng(locations[0].latitude, locations[0].longitude)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 10f))
        }

        // Configure map settings
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
    }

    override fun onResume() {
        super.onResume()
        touchableMapFragment?.onResume()
    }

    override fun onPause() {
        super.onPause()
        touchableMapFragment?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        touchableMapFragment?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        touchableMapFragment?.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

 */