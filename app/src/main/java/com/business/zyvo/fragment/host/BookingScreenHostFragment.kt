package com.business.zyvo.fragment.host

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.business.zyvo.AppConstant
import com.business.zyvo.BookingRemoveListener
import com.business.zyvo.LoadingUtils
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnClickListener
import com.business.zyvo.R
import com.business.zyvo.activity.HostMainActivity
import com.business.zyvo.adapter.host.HostBookingsAdapter
import com.business.zyvo.databinding.FragmentBookingScreenHostBinding
import com.business.zyvo.model.MyBookingsModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.NetworkMonitorCheck
import com.business.zyvo.viewmodel.host.HostBookingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch




@AndroidEntryPoint
class BookingScreenHostFragment : Fragment(), OnClickListener, View.OnClickListener {

    private var _binding: FragmentBookingScreenHostBinding? = null
    private val binding get() = _binding!!
    private var adapterMyBookingsAdapter: HostBookingsAdapter? = null
    private lateinit var viewModel: HostBookingsViewModel
    private var list: MutableList<MyBookingsModel> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingScreenHostBinding.inflate(
            LayoutInflater.from(requireContext()),
            container,
            false
        )

        viewModel = ViewModelProvider(this)[HostBookingsViewModel::class.java]

        adapterMyBookingsAdapter = HostBookingsAdapter(requireContext(), mutableListOf(), this)

        setUpAdapterMyBookings()

        binding.recyclerViewChat.adapter = adapterMyBookingsAdapter

        viewModel.list.observe(viewLifecycleOwner, Observer { list1 ->
            adapterMyBookingsAdapter!!.updateItem(list1)
            list = list1
        })

        binding.etSearchButton.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapterMyBookingsAdapter!!.filter.filter(p0.toString().trim())
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })



        return binding.root
    }


    private fun setUpAdapterMyBookings() {
        adapterMyBookingsAdapter?.setOnItemClickListener(object :
            HostBookingsAdapter.onItemClickListener {
            override fun onItemClick(
                bookingId: Int, status: String, message: String, reason: String,extension_id:String
            ) {
                lifecycleScope.launch {
                    LoadingUtils.showDialog(requireContext(), false)
                    if (status.equals("-11")) {
                        val bundle = Bundle()
                        bundle.putInt(AppConstant.BOOKING_ID, bookingId)
                        bundle.putString(AppConstant.EXTENSION_ID,extension_id)
                        Log.d(ErrorDialog.TAG,"$bookingId $extension_id")
                        findNavController().navigate(R.id.reviewBookingHostFragment, bundle)
                    } else {
                        viewModel.approveDeclineBooking(bookingId, status, message, reason,
                            extension_id)
                            .collect {
                                when (it) {
                                    is NetworkResult.Success -> {
                                        LoadingUtils.hideDialog()
                                        if (status.equals("decline")) {
                                            list.removeAll { it.booking_id == bookingId }
                                            adapterMyBookingsAdapter!!.updateItem(list)
                                        }
                                        LoadingUtils.showSuccessDialog(
                                            requireContext(),
                                            it.data.toString()
                                        )
                                        callingBookingData()
                                    }

                                    is NetworkResult.Error -> {
                                        LoadingUtils.hideDialog()
                                        LoadingUtils.showErrorDialog(
                                            requireContext(),
                                            it.message.toString()
                                        )
                                        callingBookingData()
                                    }

                                    else -> {

                                    }
                                }
                            }
                    }
                }
            }
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageFilter.setOnClickListener(this)
        callingBookingData()
//        GlobalScope.launch {
//            delay(3000) // Delay of 3 seconds (3000 milliseconds)
//            // Call your function after the delay
//            sendMessageToActivity()
//        }

    }


    private fun sendMessageToActivity() {

        // Create an Intent to send the message
        val intent = Intent("com.example.broadcast.ACTION_SEND_MESSAGE")
        intent.putExtra("message", "Hello from Fragment")

        // Send the broadcast using LocalBroadcastManager
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)

    }

    private fun callingBookingData() {
        if (NetworkMonitorCheck._isConnected.value) {
            lifecycleScope.launch {
                val session = SessionManager(requireContext())
                val userId = session.getUserId()
                if (userId != null) {
                    Log.d("TESTING_BOOKING", "calling booking data " + userId)
                    LoadingUtils.showDialog(requireContext(), false)
                    viewModel.load(userId).collect {
                        when (it) {
                            is NetworkResult.Success -> {
                                adapterMyBookingsAdapter?.updateItem(viewModel.finishedList)
                                it.data?.let {
                                    it1 -> adapterMyBookingsAdapter?.updateItem(it1)
                                    binding.tvNoBooking.visibility = View.GONE
                                    binding.recyclerViewChat.visibility = View.VISIBLE
                                }?:run {
                                    binding.tvNoBooking.visibility = View.VISIBLE
                                    binding.recyclerViewChat.visibility = View.GONE
                                }
                                callingResetData()
                            }
                            is NetworkResult.Error -> {
                                LoadingUtils.hideDialog()
                                binding.tvNoBooking.visibility = View.VISIBLE
                                binding.recyclerViewChat.visibility = View.GONE
                                callingResetData()
                            }else -> {

                            }
                        }
                    }
                }
            }
        } else {
            LoadingUtils.showErrorDialog(
                requireContext(),
                resources.getString(R.string.no_internet_dialog_msg)
            )
        }
    }

    private fun callingResetData() {
        lifecycleScope.launch {
            val sessionManager = SessionManager(requireContext())
            sessionManager.getUserId()?.let {
                viewModel.markHostBooking(it).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            Log.d("TESTING", "Booking Count is " + 0)
                            LoadingUtils.hideDialog()
                        }

                        is NetworkResult.Error -> {

                            Log.d("TESTING", "Booking Count inside Error" + 0)
                            LoadingUtils.hideDialog()

                        }

                        else -> {
                            LoadingUtils.hideDialog()
                        }
                    }
                }
            }

        }
    }

    private fun showPopupWindow(anchorView: View, position: Int) {
        // Inflate the custom layout for the popup menu
        val popupView =
            LayoutInflater.from(context).inflate(R.layout.popup_all_booking_host_filter, null)

        // Create PopupWindow with the custom layout
        val popupWindow = PopupWindow(
            popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true
        )

        // Set click listeners for each menu item in the popup layout
        popupView.findViewById<TextView>(R.id.itemAllBookings).setOnClickListener {
            adapterMyBookingsAdapter?.updateItem(viewModel.finalList)
            if (viewModel.finalList.size == 0) {
                binding.tvNoBooking.visibility = View.VISIBLE
                binding.recyclerViewChat.visibility = View.GONE
            } else {
                binding.tvNoBooking.visibility = View.GONE
                binding.recyclerViewChat.visibility = View.VISIBLE
            }
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemConfirmed).setOnClickListener {
            adapterMyBookingsAdapter?.updateItem(viewModel.confirmedList)
            if (viewModel.confirmedList.size == 0) {
                binding.tvNoBooking.visibility = View.VISIBLE
                binding.recyclerViewChat.visibility = View.GONE
            } else {
                binding.tvNoBooking.visibility = View.GONE
                binding.recyclerViewChat.visibility = View.VISIBLE
            }

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemBookingRequests).setOnClickListener {
            adapterMyBookingsAdapter?.updateItem(viewModel.pendingList)
            if (viewModel.pendingList.size == 0) {
                binding.tvNoBooking.visibility = View.VISIBLE
                binding.recyclerViewChat.visibility = View.GONE
            } else {
                binding.tvNoBooking.visibility = View.GONE
                binding.recyclerViewChat.visibility = View.VISIBLE
            }

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemWaitingPayment).setOnClickListener {
            adapterMyBookingsAdapter?.updateItem(viewModel.waitingPaymentList)

            if (viewModel.waitingPaymentList.size == 0) {
                binding.tvNoBooking.visibility = View.VISIBLE
                binding.recyclerViewChat.visibility = View.GONE
            } else {
                binding.tvNoBooking.visibility = View.GONE
                binding.recyclerViewChat.visibility = View.VISIBLE
            }

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemFinished).setOnClickListener {
            adapterMyBookingsAdapter?.updateItem(viewModel.finishedList)

            if (viewModel.finishedList.size == 0) {
                binding.tvNoBooking.visibility = View.VISIBLE
                binding.recyclerViewChat.visibility = View.GONE
            } else {
                binding.tvNoBooking.visibility = View.GONE
                binding.recyclerViewChat.visibility = View.VISIBLE
            }

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemCancelled).setOnClickListener {
            adapterMyBookingsAdapter?.updateItem(viewModel.cancelledList)

            if (viewModel.cancelledList.size == 0) {
                binding.tvNoBooking.visibility = View.VISIBLE
                binding.recyclerViewChat.visibility = View.GONE
            } else {
                binding.tvNoBooking.visibility = View.GONE
                binding.recyclerViewChat.visibility = View.VISIBLE
            }


            popupWindow.dismiss()
        }


        // Get the location of the anchor view (three-dot icon)
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        // Get the height of the PopupView after inflating it
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popupView.measuredHeight
        val popupWeight = popupView.measuredWidth
        val screenWidht = context?.resources?.displayMetrics?.widthPixels
        val anchorX = location[1]
        val spaceEnd = screenWidht?.minus((anchorX + anchorView.width))

        val xOffset = if (popupWeight > spaceEnd!!) {
            // If there is not enough space below, show it above
            -(popupWeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            // 20 // This adds a small gap between the popup and the anchor view
            -(popupWeight + 20)
        }
        // Calculate the Y offset to make the popup appear above the three-dot icon
        val screenHeight = context?.resources?.displayMetrics?.heightPixels
        val anchorY = location[1]

        // Calculate the available space above the anchorView
        val spaceAbove = anchorY
        val spaceBelow = screenHeight?.minus((anchorY + anchorView.height))

        // Determine the Y offset
        val yOffset = if (popupHeight > spaceBelow!!) {
            // If there is not enough space below, show it above
            -(popupHeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            20 // This adds a small gap between the popup and the anchor view
        }

        // Show the popup window anchored to the view (three-dot icon)
        popupWindow.elevation = 8.0f  // Optional: Add elevation for shadow effect
        popupWindow.showAsDropDown(
            anchorView,
            xOffset,
            yOffset,
            Gravity.END
        )  // Adjust the Y offset dynamically
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun itemClick(obj: Int) {

        //  Toast.makeText(requireContext(),obj.toString(),Toast.LENGTH_LONG).show()


    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.imageFilter -> {
                showPopupWindow(binding.imageFilter, 0)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? HostMainActivity)?.bookingResume()
    }


}