package com.business.zyvo.fragment.guest.feedback

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.databinding.FragmentFeedbackBinding
import com.business.zyvo.fragment.guest.feedback.viewModel.FeedbackViewModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.NetworkMonitorCheck
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FeedbackFragment : Fragment() {
    private var _binding: FragmentFeedbackBinding? = null
    private val binding get() = _binding!!
    private var close = 0
    lateinit var navController: NavController
    private var type =""
    private val viewModel : FeedbackViewModel by lazy {
        ViewModelProvider(this)[FeedbackViewModel::class.java]
    }
    var session: SessionManager?=null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding =
            FragmentFeedbackBinding.inflate(LayoutInflater.from(requireContext()), container, false)
        session = SessionManager(requireContext())
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.imageBackIcon.setOnClickListener {
            navController.navigateUp()
        }
        binding.spinnerFeedback.setItems(
            listOf(AppConstant.Host, AppConstant.Guest)
        )

    if (session?.getCurrentPanel().equals(AppConstant.Host)){
        binding.spinnerFeedback.isEnabled = false
        binding.spinnerFeedback.setText(AppConstant.HOST_TEXT/*"Host"*/)
        type = AppConstant.Host/*"host"*/
        binding.etAddDetails.visibility = View.VISIBLE
        binding.textAddDetails.visibility = View.VISIBLE
    }else {
        binding.spinnerFeedback.isEnabled = false
        binding.spinnerFeedback.setText(AppConstant.GUEST_TEXT/*"Guest"*/)
        type = AppConstant.Guest/*"guest"*/
        binding.etAddDetails.visibility = View.VISIBLE
        binding.textAddDetails.visibility = View.VISIBLE
    }


        binding.spinnerFeedback.setOnFocusChangeListener { _, b ->
            close = if (b) {
                1
            } else {
                0
            }
        }

        when (close) {
            0 -> {
                binding.spinnerFeedback.dismiss()
            }

            1 -> {
                binding.spinnerFeedback.show()
            }
        }

        binding.spinnerFeedback.setIsFocusable(true)

        binding.spinnerFeedback.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
            Log.d("checkItem", newItem)
            if (newItem == AppConstant.HOST_TEXT/*"Host"*/){
                type = AppConstant.Host /*"host"*/
            }else if(newItem == AppConstant.GUEST_TEXT /*"Guest"*/){
                type = AppConstant.Guest/*"guest"*/
            }
            if (newItem == AppConstant.PLEASE_SELECT /*"Please select"*/) {
                binding.etAddDetails.visibility = View.GONE
                binding.textAddDetails.visibility = View.GONE
            } else {
                binding.etAddDetails.visibility = View.VISIBLE
                binding.textAddDetails.visibility = View.VISIBLE
            }
        }

        binding.btnContactUs.setOnClickListener {
            findNavController().navigate(R.id.contact_us)
        }


        lifecycleScope.launch {
            viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                if (isLoading) {

                    LoadingUtils.showDialog(requireContext(), false)
                } else {
                    LoadingUtils.hideDialog()

                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (validation()){
                lifecycleScope.launch {
                    if (NetworkMonitorCheck._isConnected.value) {
                        feedback()

                    }else{
                        showErrorDialog(requireContext(),
                            resources.getString(R.string.no_internet_dialog_msg)
                        )}
                }
            }
        }
        setupWebView(getString(R.string.lorem_ipsum_is_simply_dummy_text_of_the_printing_and_typesetting_industry_lorem_ipsum_has_been_the_industry_s_standard_dummy_text_ever_since_the_1500s_when_an_unknown_printer_took_a_galley_of_type_and_scrambled_it_to_make_a_type_specimen_book_it_has_survived_not_only_five_centuries_but_also_the_leap_into_electronic_typesetting_remaining_essentially_unchanged))
    }

    private fun feedback() {
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.feedback(session?.getUserId().toString(),type, binding.etAddDetails.text.toString().trim()).collect{
                when(it){
                    is NetworkResult.Success -> {
                        if (it.data != null){
                           LoadingUtils.showSuccessDialog(requireContext(),it.data)
                            binding.etAddDetails.text.clear()
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
        if (type == ""){
            showErrorDialog(requireContext(), AppConstant.feedbackAbout)
            return false
        }else if (binding.etAddDetails.text.isEmpty()){
            showErrorDialog(requireContext(), AppConstant.details)
            return false
        }
        return true
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(htmlDescription: String) {
        val webView = binding.webViewIntroText

        // WebView settings
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.defaultTextEncodingName = "utf-8"
        webView.setBackgroundColor(0x00000000) // transparent

        // Make links clickable inside WebView
        webView.webViewClient = android.webkit.WebViewClient()

        // ✅ HTML with inline CSS for font, color, size, and justification
        val justifiedHtml = """
        <html>
        <head>
            <style>
                @font-face {
                    font-family: 'Poppins';
                    src: url('file:///android_asset/fonts/poppins_light.ttf');
                }
                body {
                    text-align: justify;
                    font-size: 13px; /* Same as your TextView's 13sp */
                    font-family: 'Poppins', sans-serif;
                    color: #000000;
                    line-height: 1.5;
                    margin: 0;
                    padding: 0;
                }
                a {
                    color: #3b82f6; /* Same blue as your TextView link */
                    text-decoration: none;
                }
                a:hover {
                    text-decoration: underline;
                }
            </style>
        </head>
        <body>
            $htmlDescription
        </body>
        </html>
    """.trimIndent()

        // ✅ Load HTML
        webView.loadDataWithBaseURL(null, justifiedHtml, "text/html", "utf-8", null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}