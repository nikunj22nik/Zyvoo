package com.yesitlab.zyvo.fragment.host

import android.app.Dialog
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.skydoves.powerspinner.PowerSpinnerView
import com.yesitlab.zyvo.DateManager.DateManager
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.CustomSpinnerWithImageAdapter
import com.yesitlab.zyvo.adapter.host.PaymentAdapter
import com.yesitlab.zyvo.adapter.host.TransactionAdapter
import com.yesitlab.zyvo.databinding.FragmentPaymentsBinding
import com.yesitlab.zyvo.model.SpinnerModel
import com.yesitlab.zyvo.viewmodel.host.PaymentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PaymentsFragment : Fragment(), FilterPaymentStatusFragment.DialogListener {


         var _binding: FragmentPaymentsBinding? = null
         val binding get() = _binding!!
        var close = 0;
         val viewModel: PaymentViewModel by lazy {
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

        Log.d("TESTING_ZYVOO_PROJ","i AM HERE IN Payment fragment")

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
        binding.textAddPyoutMethodButton.setOnClickListener {
            dialogSelectPaymentMethod()
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


    private fun dialogWithdraw() {
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




    private fun dialogSelectPaymentMethod() {
        val dialog1 = Dialog(requireContext(), R.style.BottomSheetDialog)
        dialog1.setContentView(R.layout.dialog_select_payment_host)

        dialog1.setCancelable(false)
        dialog1.apply {



            val togglePaymentTypeSelectButton = findViewById<ToggleButton>(R.id.togglePaymentTypeSelectButton)
            val rlBankAccount = findViewById<RelativeLayout>(R.id.rlBankAccount)
            val llDebitCard = findViewById<LinearLayout>(R.id.llDebitCard)
            val spinnermonth = findViewById<PowerSpinnerView>(R.id.spinnermonth)
            val spinneryear = findViewById<PowerSpinnerView>(R.id.spinneryear)

            val btnAddPayment = findViewById<TextView>(R.id.btnAddPayment)


            callingSelectionOfDate(spinnermonth,spinneryear)


           togglePaymentTypeSelectButton.setOnCheckedChangeListener{v1, isChecked->
                if (!isChecked){
                    llDebitCard.visibility = View.GONE
                    rlBankAccount.visibility = View.VISIBLE

                }
                else {
                    rlBankAccount.visibility = View.GONE
                    llDebitCard.visibility = View.VISIBLE
                }

            }


            btnAddPayment.setOnClickListener { dismiss() }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }

    }


    fun callingSelectionOfDate(spinnermonth: PowerSpinnerView, spinneryear: PowerSpinnerView) {
        val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
       // val am_pm_list = listOf("AM","PM")
        val years = (2024..2050).toList()
        val yearsStringList = years.map { it.toString() }
        Toast.makeText(requireContext(),"Year String List: "+yearsStringList.size,Toast.LENGTH_LONG).show()
        val days = resources.getStringArray(R.array.day).toList()


        // Add item decoration for spacing
        val spacing = 16 // Spacing in pixels


        spinnermonth.layoutDirection = View.LAYOUT_DIRECTION_LTR
        spinnermonth.arrowAnimate = false
        spinnermonth.spinnerPopupHeight = 400
        spinnermonth.setItems(months)
        spinnermonth.setIsFocusable(true)

        val recyclerView3 = spinnermonth.getSpinnerRecyclerView()

        recyclerView3.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }
        })

        spinneryear.layoutDirection = View.LAYOUT_DIRECTION_LTR
        spinneryear.arrowAnimate = false
        spinneryear.spinnerPopupHeight = 400
        spinneryear.setItems(yearsStringList.subList(0,16))
        spinneryear.setIsFocusable(true)
//        binding.spinneryear.post {
//            binding.spinneryear.spinnerPopupWidth = binding.spinneryear.width
//        }


//        binding.endAmPm.post {
//            binding.endAmPm.spinnerPopupWidth = binding.endAmPm.width
//        }



//        binding.startAmPm.post {
//            binding.startAmPm.spinnerPopupWidth = binding.startAmPm.width
//        }






        val recyclerView1 = spinneryear.getSpinnerRecyclerView()
        recyclerView1.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = spacing
            }

        })



    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSubmitClicked() {
        TODO("Not yet implemented")
    }

}