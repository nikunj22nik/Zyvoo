package com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.business.zyvo.AppConstant
import com.business.zyvo.ErrorMessage
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnItemAdapterClick
import com.business.zyvo.R
import com.business.zyvo.activity.GuesMain
import com.business.zyvo.adapter.MyBookingsAdapter
import com.business.zyvo.databinding.FragmentMyBookingsBinding
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.BookingModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.business.zyvo.utils.NetworkMonitorCheck
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyBookingsFragment : Fragment(), OnItemAdapterClick, View.OnClickListener {

    private var _binding: FragmentMyBookingsBinding? = null
    private val binding get() = _binding!!
    private val bookingViewModel: BookingViewModel by viewModels()
    private lateinit var sessionManager: SessionManager
    private lateinit var adapterMyBookingsAdapter: MyBookingsAdapter
    var bookingListModel: MutableList<BookingModel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       Log.d("TESTING_VIEW","INSIDE VIEW DETAILS")
        sessionManager = SessionManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBookingsBinding.inflate(inflater, container, false)

        // Observe the isLoading state
        lifecycleScope.launch {
            bookingViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }

        // Initialize adapter
        adapterMyBookingsAdapter = MyBookingsAdapter(requireContext(), mutableListOf(), this)
        binding.recyclerViewChat.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewChat.adapter = adapterMyBookingsAdapter
        bookingListAPI()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageFilter.setOnClickListener(this)
    }

    private fun bookingListAPI() {
        if (!NetworkMonitorCheck._isConnected.value) {
            showErrorDialog(requireContext(), resources.getString(R.string.no_internet_dialog_msg))
            return
        }
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val sessionManager = SessionManager(requireContext())
                val userId = sessionManager.getUserId()
                bookingViewModel.getBookingList(userId.toString()).collect {
                    when (it) {
                        is NetworkResult.Success -> {
                            var list = it.data
                            if (list != null) {
                                bookingListModel = list.toMutableList()
                                adapterMyBookingsAdapter.updateItem(list)
                                binding.recyclerViewChat.visibility = View.VISIBLE
                                binding.textNoBookingFound.visibility = View.GONE
                            }
                        }
                        is NetworkResult.Error -> {
                            Log.e(ErrorDialog.TAG, "Server Error: ${it.message}")
                            if (it.message == ErrorMessage.NO_BOOKING_FOUND ){
                               // showSuccessDialog(requireContext(), it.message)
                                binding.recyclerViewChat.visibility = View.GONE
                                binding.textNoBookingFound.visibility = View.VISIBLE
                            }else{
                                showErrorDialog(requireContext(), it.message ?: ErrorMessage.UNKNOWN_ERROR)
                            }

                        }
                        else -> {
                            Log.v(ErrorDialog.TAG, "Unexpected error: ${it.message ?: ErrorMessage.UNKNOWN_ERROR}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(ErrorDialog.TAG, "Unexpected API error", e)
                showErrorDialog(requireContext(), ErrorMessage.SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN)
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showPopupWindow(anchorView: View) {
        val popupView = LayoutInflater.from(context).inflate(R.layout.popup_all_booking_filter, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupView.findViewById<TextView>(R.id.itemAllBookings).setOnClickListener {
            sortBookingBy(popupView.findViewById<TextView>(R.id.itemAllBookings).text.toString())
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemConfirmed).setOnClickListener {
            sortBookingBy(popupView.findViewById<TextView>(R.id.itemConfirmed).text.toString())
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemPending).setOnClickListener {
            sortBookingBy(popupView.findViewById<TextView>(R.id.itemPending).text.toString())
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemFinished).setOnClickListener {
            sortBookingBy(popupView.findViewById<TextView>(R.id.itemFinished).text.toString())
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemCancelled).setOnClickListener {
            sortBookingBy(popupView.findViewById<TextView>(R.id.itemCancelled).text.toString())
            popupWindow.dismiss()
        }

        // Positioning logic
        popupWindow.elevation = 8.0f
        popupWindow.showAsDropDown(anchorView, 0, 0, Gravity.END)
    }

    private fun sortBookingBy(option: String) {
        var filterBooking:MutableList<BookingModel> = mutableListOf()
        when (option) {
          AppConstant.ALL_BOOKINGS  -> filterBooking = bookingListModel
           AppConstant.CONFIRMED_TEXT -> filterBooking =
                bookingListModel.filter { it.booking_status == AppConstant.CONFIRMED_TEXT }.toMutableList()
            AppConstant.PENDING_TEXT  -> filterBooking =
                bookingListModel.filter { it.booking_status == AppConstant.PENDING_TEXT }.toMutableList()
            AppConstant.FINISHED_TEXT  -> filterBooking =
                bookingListModel.filter { it.booking_status == AppConstant.FINISHED_TEXT }.toMutableList()
            AppConstant.CANCELLED_TEXT -> filterBooking =
                bookingListModel.filter { it.booking_status == AppConstant.CANCELLED_TEXT }.toMutableList()
        }
        adapterMyBookingsAdapter.updateItem(filterBooking)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.imageFilter -> showPopupWindow(binding.imageFilter)
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as? GuesMain)?.bookingResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun itemClickOn(obj: Int, bookingId: Int) {
        val bundle = Bundle().apply {
            putInt("BOOKING_ID", bookingId)
        }
        findNavController().navigate(R.id.reviewBookingFragment, bundle)
    }
}
