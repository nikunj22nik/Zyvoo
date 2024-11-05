package com.yesitlab.zyvo.fragment.both

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.yesitlab.zyvo.OnClickListener

import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.AdapterNotificationScreen
import com.yesitlab.zyvo.databinding.FragmentNotificationBinding
import com.yesitlab.zyvo.model.NotificationScreenModel
import com.yesitlab.zyvo.viewmodel.NotificationViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationFragment : Fragment(),OnClickListener,View.OnClickListener {


    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationViewModel by viewModels()
    private lateinit var adapterNotificationScreen : AdapterNotificationScreen
    // var  list : ArrayList<NotificationScreenModel> = arrayListOf()


    // 1. Called when fragment is attached to its parent activity
    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        // Initialize any context-related resources here
    }

    // 2. Called when the fragment is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize essential non-UI components


    }



    // 3. Called to create the view hierarchy of the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        // binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        // Set up RecyclerView and Adapter
        adapterNotificationScreen = AdapterNotificationScreen(requireContext(), arrayListOf(),this)
        binding.recyclerView.adapter = adapterNotificationScreen  // Ensure the RecyclerView ID is correct

        // Observe the LiveData from ViewModel
        viewModel.list.observe(viewLifecycleOwner, Observer { list ->
            adapterNotificationScreen.updateItem(list)
        })

        return binding.root
    }

    // 4. Called once the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize view-related components and observers
    }

    // 5. Called when the fragment becomes visible
    override fun onStart() {
        super.onStart()
        // Start processes related to UI visibility
    }

    // 6. Called when the fragment is in the foreground
    override fun onResume() {
        super.onResume()
        // Resume any updates or animations for active interaction
    }

    // 7. Called when the fragment loses focus
    override fun onPause() {
        super.onPause()
        // Pause animations or updates not needed in the background
    }

    // 8. Called when the fragment is no longer visible
    override fun onStop() {
        super.onStop()
        // Stop ongoing background tasks or updates
    }

    // 9. Called to clean up resources related to the view
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Clean up view references and other UI-related resources
    }

    // 10. Called to perform final fragment cleanup
    override fun onDestroy() {
        super.onDestroy()
        // Release remaining resources or listeners
    }

    // 11. Called right before the fragment detaches from the activity
    override fun onDetach() {
        super.onDetach()
        // Clean up any references to the context or activity
    }

    override fun itemClick(obj: Int) {
        viewModel.removeItemAt(obj) // Update the adapter with the new list
    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }
}
