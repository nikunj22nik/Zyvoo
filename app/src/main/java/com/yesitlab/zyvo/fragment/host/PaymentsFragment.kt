package com.yesitlab.zyvo.fragment.host

import android.app.Dialog
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.powerspinner.PowerSpinnerView
import com.yesitlab.zyvo.AppConstant
import com.yesitlab.zyvo.DateManager.DateManager
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.CustomSpinnerWithImageAdapter
import com.yesitlab.zyvo.adapter.host.PaymentAdapter
import com.yesitlab.zyvo.adapter.host.TransactionAdapter
import com.yesitlab.zyvo.databinding.FragmentPaymentsBinding
import com.yesitlab.zyvo.fragment.guest.SelectHourFragmentDialog
import com.yesitlab.zyvo.model.SpinnerModel
import com.yesitlab.zyvo.model.TransactionModel
import com.yesitlab.zyvo.viewmodel.host.PaymentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentsFragment : Fragment(), FilterPaymentStatusFragment.DialogListener {

    private var _binding: FragmentPaymentsBinding? = null
    private val binding get() = _binding!!
    var close = 0;
    private val viewModel: PaymentViewModel by lazy {
        ViewModelProvider(this)[PaymentViewModel::class.java]
    }
    lateinit var adapter: TransactionAdapter
    lateinit var adapter1: PaymentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //  param1 = it.getString(ARG_PARAM1)
            //  param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_payments, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        adapter = TransactionAdapter(arrayListOf())
        binding.rvTransactions.adapter = adapter

        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.updateItem(it)
        })




        adapter1 = PaymentAdapter(requireContext(), mutableListOf())

        binding.rcvPaymentCard.adapter = adapter1


        viewModel.paymentCardList.observe(viewLifecycleOwner, Observer {
            adapter1.updateItem(it)
        })


        val maxHeight = resources.getDimensionPixelSize(R.dimen._350sdp)

        binding.rvTransactions.viewTreeObserver.addOnGlobalLayoutListener {
            if (binding.rvTransactions.height > maxHeight) {
                binding.rvTransactions.layoutParams.height = maxHeight
                binding.rvTransactions.requestLayout()
            }
        }

        binding.imageInfo.setOnClickListener {
            showPopupWindowForPets(it)
        }

        binding.llDateRangeSelect.setOnClickListener {
            DateManager(requireContext()).getRangeSelectedDateWithYear(
                fragmentManager = parentFragmentManager
            ) { selectedData ->
                selectedData?.let {

                    val (dateRange, year) = it
                    val (startDate, endDate) = dateRange
                    binding.tvDateRange.text = "$startDate - $endDate $year"
                    Toast.makeText(
                        requireContext(),
                        "Range: $startDate to $endDate, Year: $year",
                        Toast.LENGTH_SHORT
                    ).show()
                } ?: run {
                    Toast.makeText(requireContext(), "No date selected", Toast.LENGTH_SHORT).show()
                }
            }
        }



        binding.imageFilter.setOnClickListener {
            val dialog1 = FilterPaymentStatusFragment()
            dialog1.setDialogListener(this)
            dialog1.show(parentFragmentManager, "MYDIALOF")


        }

        binding.btnWithdrawFunds.setOnClickListener {
            dialogWithdraw()
        }


        return binding.root
    }


    private fun showPopupWindowForPets(anchorView: View) {
        // Inflate the popup layout
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.popup_layout_pets, null)

        // Create the PopupWindow
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Show the popup window at the bottom right of the TextView

        popupWindow.isOutsideTouchable = true
        popupWindow.isFocusable = true
        popupWindow.showAsDropDown(anchorView, anchorView.width, 0)
    }


    fun dialogWithdraw() {
        val dialog = Dialog(requireContext(), R.style.BottomSheetDialog)
        dialog.setContentView(R.layout.dialog_withdraw)

        dialog.setCancelable(false)
        dialog.apply {


            val imageCross = findViewById<ImageView>(R.id.imageCross)
            val btnWithdraw = findViewById<TextView>(R.id.btnWithdraw)
            val spinner = findViewById<Spinner>(R.id.spinner)

            val customList = arrayListOf(
                SpinnerModel("Standard (3 to 5 business days", 0),
                SpinnerModel("Instant (Fee 2%)", R.drawable.lightning_icon)

            )

            // Create adapter for spinner
            val customAdapter = CustomSpinnerWithImageAdapter(requireContext(), customList)

            spinner.adapter = customAdapter


            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {
//                    Toast.makeText(requireContext(),
//                        getString(R.string.selected_item) + " " +
//                                "" + items[position], Toast.LENGTH_SHORT).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // write code to perform some action
                }
            }


//            // Set listener for item selection
            spinner.setSelection(0)

            imageCross.setOnClickListener { dismiss() }
            btnWithdraw.setOnClickListener { dismiss() }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        dialog.show()
    }

/*
    fun callingSelectionOfDate(){
        val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        val am_pm_list = listOf("AM","PM")
        val years = (2024..2050).toList()
        val yearsStringList = years.map { it.toString() }
        Toast.makeText(this,"Year String List: "+yearsStringList.size,Toast.LENGTH_LONG).show()
        val days = resources.getStringArray(R.array.day).toList()


        binding.spinnerLanguage.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.spinnerLanguage.spinnerPopupHeight = 400
        binding.spinnerLanguage.arrowAnimate = false
        binding.spinnerLanguage.setItems(days)
        binding.spinnerLanguage.setIsFocusable(true)
        val recyclerView = binding.spinnerLanguage.getSpinnerRecyclerView()

        // Add item decoration for spacing
        val spacing = 16 // Spacing in pixels
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }
        })

        binding.spinnermonth.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.spinnermonth.arrowAnimate = false
        binding.spinnermonth.spinnerPopupHeight = 400
        binding.spinnermonth.setItems(months)
        binding.spinnermonth.setIsFocusable(true)

        val recyclerView3 = binding.spinnermonth.getSpinnerRecyclerView()

        recyclerView3.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }
        })

        binding.spinneryear.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.spinneryear.arrowAnimate = false
        binding.spinneryear.spinnerPopupHeight = 400
        binding.spinneryear.setItems(yearsStringList.subList(0,16))
        binding.spinneryear.setIsFocusable(true)
//        binding.spinneryear.post {
//            binding.spinneryear.spinnerPopupWidth = binding.spinneryear.width
//        }

        binding.endAmPm.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.endAmPm.arrowAnimate = false
        binding.endAmPm.spinnerPopupHeight = 200
        binding.endAmPm.setItems(am_pm_list)
        binding.endAmPm.setIsFocusable(true)
//        binding.endAmPm.post {
//            binding.endAmPm.spinnerPopupWidth = binding.endAmPm.width
//        }


        binding.startAmPm.layoutDirection = View.LAYOUT_DIRECTION_LTR
        binding.startAmPm.arrowAnimate = false
        binding.startAmPm.spinnerPopupHeight = 200
        binding.startAmPm.setItems(am_pm_list)
        binding.startAmPm.setIsFocusable(true)

//        binding.startAmPm.post {
//            binding.startAmPm.spinnerPopupWidth = binding.startAmPm.width
//        }

        val recyclerView6 = binding.startAmPm.getSpinnerRecyclerView()

        recyclerView6.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }
        })


        val recyclerView5 = binding.endAmPm.getSpinnerRecyclerView()
        recyclerView5.addItemDecoration(object : RecyclerView.ItemDecoration() {

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }

        })

        val recyclerView1 = binding.spinneryear.getSpinnerRecyclerView()
        recyclerView1.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }

        })

        binding.dateView.setOnClickListener {
            if(binding.relCalendarLayouts.visibility == View.VISIBLE){
                binding.relCalendarLayouts.visibility = View.GONE
            }
            else{
                binding.relCalendarLayouts.visibility = View.VISIBLE
            }
        }

        binding.textSaveChangesButton.setOnClickListener {
            binding.tvDate.setText(binding.spinnermonth.text.toString()+" "+binding.spinnerLanguage.text.toString()+","+binding.spinneryear.text.toString())
            binding.relCalendarLayouts.visibility = View.GONE
        }

    }

 */

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSubmitClicked() {
        TODO("Not yet implemented")
    }

}