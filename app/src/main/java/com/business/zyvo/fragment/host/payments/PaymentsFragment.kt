package com.business.zyvo.fragment.host.payments

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
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.CallUpdateListener
import com.skydoves.powerspinner.PowerSpinnerView
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.LoadingUtils.Companion.showSuccessDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.adapter.CustomSpinnerWithImageAdapter
import com.business.zyvo.adapter.host.PaymentAdapter
import com.business.zyvo.adapter.host.TransactionAdapter
import com.business.zyvo.databinding.FragmentPaymentsBinding
import com.business.zyvo.fragment.guest.profile.model.GetPayoutResponse
import com.business.zyvo.fragment.host.FilterPaymentStatusFragment
import com.business.zyvo.fragment.host.payments.model.GetBookingResponse
import com.business.zyvo.fragment.host.payments.viewModel.PaymentsViewModel
import com.business.zyvo.model.PaymentCardModel
import com.business.zyvo.model.SpinnerModel
import com.business.zyvo.onItemClickData
import com.business.zyvo.session.SessionManager
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class PaymentsFragment : Fragment(), FilterPaymentStatusFragment.DialogListener,
    CallUpdateListener , onItemClickData {
    var _binding : FragmentPaymentsBinding? = null
    val binding get() = _binding!!
    var close = 0;
    lateinit var navController: NavController
    val viewModel: PaymentsViewModel by lazy {
            ViewModelProvider(this)[PaymentsViewModel::class.java]
         }
    lateinit var adapter: TransactionAdapter
    lateinit var adapter1: PaymentAdapter
    var session: SessionManager? = null
    var startDate : String? = null
    var endDate : String? = null
    var filterStatus : String? = null
    var totalAmount : String? = null

         override fun onCreate(savedInstanceState: Bundle?) {
           super.onCreate(savedInstanceState)
           arguments?.let {}
           Log.d("TESTING_ZYVOO_PROJ","i AM HERE IN Payment fragment")
         }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_payments, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        session = SessionManager(requireActivity())
        binding.rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        adapter = TransactionAdapter(requireContext(),arrayListOf())
        binding.rvTransactions.adapter = adapter
//        viewModel.list.observe(viewLifecycleOwner, Observer {
//            adapter.updateItem(it)
//        })
        adapter1 = PaymentAdapter(requireContext(), mutableListOf(),this)

        binding.rcvPaymentCard.adapter = adapter1

//        viewModel.paymentCardList.observe(viewLifecycleOwner, Observer {
//            adapter1.updateItem(it)
//        })

     parentFragmentManager.setFragmentResultListener(
         "FilterPaymentStatus", viewLifecycleOwner
     ) { _, bundle ->
         val message = bundle.getString("dialogFragment")
     /*    if (message != null) {
             Log.d("statusCheck",message)
             Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

             filterStatus = message

             paymentWithdrawalList()

         }

      */
         message?.let {
             Log.d("statusCheck", it)
             Log.d("i'm Here","2")
             Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
             filterStatus = it
             paymentWithdrawalList()
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
                    val (startDate1, endDate1) = dateRange
                    startDate = startDate1 + year
                    endDate = endDate1 + year
                    binding.tvDateRange.text = "$startDate1 - $endDate1 $year"
                    Toast.makeText(
                        requireContext(),
                        "Range: $startDate to $endDate, Year: $year",
                        Toast.LENGTH_SHORT
                    ).show()
                    paymentWithdrawalList()

                } ?: run {
                    Toast.makeText(requireContext(), "No date selected", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.imageFilter.setOnClickListener {
            val dialog1 = FilterPaymentStatusFragment()
           // dialog1.setDialogListener(this)
            dialog1.show(parentFragmentManager, "FilterPaymentStatusFragment")

         //   findNavController().navigate(R.id.filterPaymentStatusFragment)

        }
        binding.btnWithdrawFunds.setOnClickListener {
            dialogWithdraw()
        }
//        binding.textAddPyoutMethodButton.setOnClickListener {
//            dialogSelectPaymentMethod()
//        }

        lifecycleScope.launch {
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {
                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()
                }
            }
        }

        if (startDate == null && endDate == null ){
            val currentDate = Date()
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val dateFormat1 = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
            val currentDateStr = dateFormat.format(currentDate)
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.DAY_OF_MONTH, 7)
            val futureDateStr = dateFormat1.format(calendar.time)
            binding.tvDateRange.text = "$currentDateStr - $futureDateStr"
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        paymentWithdrawalList()
        getPayoutMethods()
        payoutBalanceMethods()
        binding.imageBackButton.setOnClickListener {
            navController.navigateUp()
        }

        binding.textAddPyoutMethodButton.setOnClickListener {
            findNavController().navigate(R.id.hostPayoutFragment)
        }

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
            val etAmount = findViewById<EditText>(R.id.etAmount)
            val textAvailableBalance = findViewById<TextView>(R.id.textAvailableBalance)
            val btnWithdraw = findViewById<TextView>(R.id.btnWithdraw)
            val spinner = findViewById<Spinner>(R.id.spinner)

            val customList = arrayListOf(
                SpinnerModel("Standard (3 to 5 business days", 0),
                SpinnerModel("Instant (Fee 2%)", R.drawable.lightning_icon)

            )
            if (totalAmount != null){
                textAvailableBalance.text = "Available Balance :" + "$totalAmount"
            }

            val customAdapter = CustomSpinnerWithImageAdapter(requireContext(), customList)

            spinner.adapter = customAdapter

            var selectedItem: String? = null

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {
//                    Toast.makeText(requireContext(),
//                        getString(R.string.selected_item) + " " +
//                                "" + items[position], Toast.LENGTH_SHORT).show()
                    selectedItem = customList[position].spinnerText
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    selectedItem = null
                }
            }

          //  spinner.setSelection(0)

            imageCross.setOnClickListener { dismiss() }
            btnWithdraw.setOnClickListener {
                var amount = ""
                 amount = etAmount.text.toString().trim()

                // Check if a valid spinner item is selected
                if (selectedItem == null) {
                    Toast.makeText(requireContext(), "Please select a withdrawal method", Toast.LENGTH_SHORT).show()
                    dismiss()
                    return@setOnClickListener

                }

              val item =   when(selectedItem)  {
                    "Standard (3 to 5 business days" ->  {
                        "standard"
                    }
                        "Instant (Fee 2%)" -> {
                            "instant"
                  }

                  else -> {
                      "standard"
                  }
              }
                   if (amount != "") {
                       requestWithdrawalMethods(amount, item)
                   }else{
                       LoadingUtils.showErrorDialog(requireContext(),"Amount cannot be empty.")
                   }
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        }
        dialog.show()
    }
    /*
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

     */

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

    }
    private fun paymentWithdrawalList(){
        lifecycleScope.launch {
            viewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        LoadingUtils.showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        paymentWithdrawalListApi()
                    }

                }
        }
    }

    private fun paymentWithdrawalListApi(){
        val inputFormat = SimpleDateFormat("MMM ddyyyy", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        if (startDate == null){
            //2025-02-10
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            startDate = currentDate
            Log.d("startDateCheck2" , startDate.toString())
        }else{
            try {
                val date = inputFormat.parse(startDate)
                val formattedDate = outputFormat.format(date)
                startDate = formattedDate
                Log.d("startDateCheck", formattedDate) // Output: 2025-03-12
            } catch (e: Exception) {
                Log.e("DateParseError", "Error parsing date", e)
            }
          // Log.d("startDateCheck" , startDate.toString())
        }
        if (endDate == null){
            val currentDate = Date()
            val dateFormat1 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.DAY_OF_MONTH, 7)
            val futureDateStr = dateFormat1.format(calendar.time)
            endDate = futureDateStr
            Log.d("startDateCheck3" , endDate.toString())
        }else{
           Log.d("startDateCheck1" , endDate.toString())

            try {
                val date = inputFormat.parse(endDate)
                val formattedDate = outputFormat.format(date)
                endDate = formattedDate
                Log.d("startDateCheck1", formattedDate) // Output: 2025-03-12
            } catch (e: Exception) {
                Log.e("DateParseError", "Error parsing date", e)
            }
        }

        if (filterStatus == null){
            filterStatus = ""
            Log.d("startDateCheck5" , filterStatus.toString())
        }else{
            Log.d("startDateCheck4" , filterStatus.toString())
        }
        //session?.getUserId().toString()
        // 78
        lifecycleScope.launch {
            viewModel.paymentWithdrawalList(session?.getUserId().toString(),startDate.toString(),endDate.toString(),filterStatus.toString()).collect {
                when (it) {

                    is NetworkResult.Success -> {
                        val model = Gson().fromJson(it.data, GetBookingResponse::class.java)

                        Log.d("API_RESPONSE_Payout", it.data.toString())
if (model.data?.isNotEmpty() == true){
    binding.horizontalScrollView.visibility = View.VISIBLE
    binding.tvNoDataFound.visibility = View.GONE
    model.data.let { it1 -> adapter.updateItem(it1)
        adapter.notifyDataSetChanged()
    }
}else{
    binding.horizontalScrollView.visibility = View.GONE
    binding.tvNoDataFound.visibility = View.VISIBLE
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

    override fun onCallAdded() {
        Log.d("i'm Here","1")
        paymentWithdrawalList()
    }

    private fun getPayoutMethods(){
        lifecycleScope.launch {
            viewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        LoadingUtils.showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        getPayoutApi()
                    }

                }
        }
    }


    private fun getPayoutApi(){
        lifecycleScope.launch {
            viewModel.getPayoutMethods(session?.getUserId().toString()).collect {
                when (it) {

                    is NetworkResult.Success -> {
                        val model = Gson().fromJson(it.data, GetPayoutResponse::class.java)

                        Log.d("API_RESPONSE_Payout", it.data.toString())

                        /*
                        // Ensure data is not null before accessing its properties
                        model.data?.let { payoutData ->
                            payoutData.bankAccounts?.let { bankList ->
                                if (bankList.isNotEmpty()) {
                                    adapter1.updateItem(bankList)
                                 //   bankNameAdapterPayout.addItems(bankList)
                                }
                            }

                            payoutData.cards?.let { cardList ->
                                if (cardList.isNotEmpty()) {
                                    cardNumberAdapterPayout.addItems(cardList)
                                }
                            }
                        } ?: run {
                            // Handle case where `data` is null
                            showErrorDialog(requireContext(), "Payout data is unavailable")
                        }

                         */

                        val mergedList = mutableListOf<PaymentCardModel>()

                        model.data?.let { payoutData ->
                            // Convert BankAccountPayout to PaymentCardModel
                            payoutData.bankAccounts?.forEach { bank ->
                                mergedList.add(
                                    PaymentCardModel(
                                        id = bank.id,
                                        bankName = bank.bankName,
                                        cardEndNumber = bank.lastFourDigits,
                                        isBankAccount = true,
                                        defaultForCurrency = bank.defaultForCurrency
                                    )
                                )
                            }

                            // Convert CardPayout to PaymentCardModel
                            payoutData.cards?.forEach { card ->
                                mergedList.add(
                                    PaymentCardModel(
                                        id = card.id,
                                        cardHolderName = card.cardHolderName,
                                        cardEndNumber = card.lastFourDigits,
                                        isBankAccount = false,
                                        defaultForCurrency = card.defaultForCurrency
                                    )
                                )
                            }
                        }

                        // Update adapter with combined list
                        adapter1.updateItem(mergedList)
                        adapter1.notifyDataSetChanged()


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


    private fun setPrimaryPayoutMethods(id: String){
        lifecycleScope.launch {
            viewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        LoadingUtils.showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        setPrimaryPayoutApi(id)
                    }

                }
        }
    }


    private fun setPrimaryPayoutApi(id: String){
        Log.d("idType",id)
        lifecycleScope.launch {
            viewModel.setPrimaryPayoutMethod(session?.getUserId().toString(),id).collect {
                when (it) {

                    is NetworkResult.Success -> {
                        it.data?.let { it1 -> showSuccessDialog(requireContext(), it1) }
                        getPayoutApi()

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

    private fun deletePayoutMethods(id: String){
        lifecycleScope.launch {
            viewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        LoadingUtils.showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        deletePayoutApi(id)
                    }

                }
        }
    }


    private fun deletePayoutApi(id: String){
        Log.d("idType",id)
        lifecycleScope.launch {
            viewModel.deletePayoutMethod(session?.getUserId().toString(),id).collect {
                when (it) {

                    is NetworkResult.Success -> {
                        it.data?.let { it1 -> showSuccessDialog(requireContext(), it1) }
                        getPayoutApi()

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

    override fun itemClick(obj: Int, text: String, enteredText: String) {
        when (text) {
        "setPrimary" -> {
            setPrimaryPayoutMethods(enteredText)
        }

        "delete" -> {
            deletePayoutMethods(enteredText)
        }
        }
    }

    private fun payoutBalanceMethods(){
        lifecycleScope.launch {
            viewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        payoutBalance()
                    }

                }
        }
    }
    private fun payoutBalance(){

        lifecycleScope.launch {
            viewModel.payoutBalance(session?.getUserId().toString()).collect {
                when (it) {

                    is NetworkResult.Success -> {

                        it.data?.first.let {it1 -> binding.tvNextPayoutAmount.text = "$" + it1 }
                        it.data?.first.let {it1 -> totalAmount = "$" + it1 }
                        it.data?.second.let {it1 -> binding.tvNextPayoutDate.text = "On " + it1 }


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


    private fun requestWithdrawalMethods(amount: String, selectedItem: String) {
        lifecycleScope.launch {
            viewModel.networkMonitor.isConnected
                .distinctUntilChanged()
                .collect { isConn ->
                    if (!isConn) {
                        showErrorDialog(
                            requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )
                    } else {
                        requestWithdrawal(amount,selectedItem)
                    }

                }
        }
    }

    private fun requestWithdrawal(amount: String, selectedItem: String){
        lifecycleScope.launch {
            viewModel.requestWithdrawal(session?.getUserId().toString(),amount,selectedItem).collect {
                when (it) {

                    is NetworkResult.Success -> {
showSuccessDialog(requireContext(),"Payout Request Sent Successfully")
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
}