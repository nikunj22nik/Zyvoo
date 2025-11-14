package com.business.zyvo.fragment.both.browseGuideArtcileDetail

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.text.LineBreaker
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Layout
import android.text.method.LinkMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.appsflyer.share.LinkGenerator
import com.appsflyer.share.ShareInviteHelper
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.BuildConfig
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.R
import com.business.zyvo.databinding.FragmentBrowseGuideArtcileDetailBinding
import com.business.zyvo.fragment.both.browseGuideArtcileDetail.model.ArticleDetailsResponse
import com.business.zyvo.fragment.both.browseGuideArtcileDetail.model.GuideDetailResponse
import com.business.zyvo.fragment.both.browseGuideArtcileDetail.viewModel.BrowseGuideArtcileDetailViewModel
import com.business.zyvo.fragment.guest.helpCenter.model.HelpCenterResponse
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.ErrorDialog
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BrowseGuideArtcileDetailFragment : Fragment() {

    private var _binding: FragmentBrowseGuideArtcileDetailBinding? = null
    private val binding get() =  _binding!!
    val viewModel: BrowseGuideArtcileDetailViewModel by lazy {
        ViewModelProvider(this)[BrowseGuideArtcileDetailViewModel::class.java]
    }
    var id: String = ""
    var textType: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            id = arguments?.getString(AppConstant.Id).toString()
            textType = arguments?.getString(AppConstant.textType).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBrowseGuideArtcileDetailBinding.inflate(
            LayoutInflater.from(requireActivity()),
            container,
            false
        )

        binding.imgBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvContactUs.setOnClickListener {
            findNavController().navigate(R.id.contact_us)
        }
        binding.imgShareIcon.setOnClickListener {
            //shareApp()
            generateDeepLink()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                        if (textType == AppConstant.ARTICLE_SMALL_TEXT ) {
                            binding.tvShareThisArticle.text = AppConstant.SHARE_THIS_ARTICLE
                            getArticleDetails()
                        } else if (textType == AppConstant.GUIDES) {
                            binding.tvShareThisArticle.text = AppConstant.SHARE_THIS_GUIDE
                            getGuideDetails()
                        }
                    }

                }
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
    }


    private fun getGuideDetails() {
        lifecycleScope.launch {
            viewModel.getGuideDetails(id).collect {
                when (it) {

                    is NetworkResult.Success -> {
                        val model = Gson().fromJson(it.data, GuideDetailResponse::class.java)

                        if (model.data.title != null) {
                            binding.textTitle.text = model.data.title
                        }
                        if (model.data.date != null) {
                            binding.textDate.text = model.data.date
                        }
                        if (model.data.category != null) {
                            binding.textCategory.text = model.data.category
                        }
                        if (model.data.time_required != null) {
                            val timeRequired = model.data.time_required
                            binding.textTimeRequired.text = "$timeRequired read"
                        }

                        if (model.data.description != null) {

                            Log.d("descriptiondescription","****"+model.data.description)
                            binding.textDescription1.setJustifiedText(model.data.description ?: "")
                            setupWebView(model.data.description ?: "")

                            val htmlText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Html.fromHtml(model.data.description, Html.FROM_HTML_MODE_LEGACY)
                            } else {
                                Html.fromHtml(model.data.description)
                            }



                            binding.textDescription.text = htmlText
                            binding.textDescription.movementMethod = LinkMovementMethod.getInstance()

                            binding.textDescription.post {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    binding.textDescription.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                                }
                            }

                        }
                        if (model.data.author_name != null){
                            binding.textAuthorName.text = model.data.author_name
                        }
                        if (model.data.cover_image != null){
                            Glide.with(requireContext())
                                .load(BuildConfig.MEDIA_URL + model.data.cover_image)
                                .centerCrop()
                                .error(R.drawable.ic_img_not_found)
                                .placeholder(R.drawable.ic_img_not_found)
                                .into(binding.imageMain)
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


    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(htmlDescription: String) {
        val webView = binding.webViewDescription

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

    private fun getArticleDetails() {
        lifecycleScope.launch {
            viewModel.getArticleDetails(id).collect {
                when (it) {

                    is NetworkResult.Success -> {
                        val model = Gson().fromJson(it.data, ArticleDetailsResponse::class.java)

                        if (model.data.title != null) {
                            binding.textTitle.text = model.data.title
                        }
                        if (model.data.date != null) {
                            binding.textDate.text = model.data.date
                        }
                        if (model.data.category != null) {
                            binding.textCategory.text = model.data.category
                        }
                        if (model.data.time_required != null) {
                            val timeRequired = model.data.time_required
                            binding.textTimeRequired.text = "$timeRequired read"
                        }

                        if (model.data.description != null) {
                            binding.textDescription1.setJustifiedText(model.data.description ?: "")
                            setupWebView(model.data.description ?: "")

                        }
                        if (model.data.author_name != null){
                            binding.textAuthorName.text = model.data.author_name
                        }
                        if (model.data.cover_image != null){
                            Glide.with(requireContext())
                                .load(BuildConfig.MEDIA_URL + model.data.cover_image)
                                .centerCrop()
                                .error(R.drawable.ic_img_not_found)
                                .placeholder(R.drawable.ic_img_not_found)
                                .into(binding.imageMain)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun generateDeepLink() {
        // Your OneLink base URL and campaign details
        val currentCampaign = AppConstant.PROPERTY_SHARE
        val oneLinkId = AppConstant.SCFP // Replace with your OneLink ID
        val brandDomain = "zyvobusiness.onelink.me" // Your OneLink domain

        val session = SessionManager(requireContext())
        val type = session.getCurrentPanel()
        val location = AppConstant.ARTICLE

        // Prepare the deep link values
        val deepLink = "zyvoo://property"
        val webLink =
            "https://zyvo.tgastaging.com/property" // Web fallback link

        // Create the link generator
        val linkGenerator = ShareInviteHelper.generateInviteUrl(requireContext())
            .setBaseDeeplink("https://$brandDomain/$oneLinkId")
            .setCampaign(currentCampaign)
            .addParameter("af_dp", deepLink) // App deep link
            .addParameter("af_web_dp", webLink) // Web fallback URL
            .addParameter("guide_id", id)     // Custom key 1
            .addParameter("textType", textType)     // Custom key 1
            .addParameter("user_type", type)    // Custom key 2
            .addParameter("location", location)         // Custom key 3

        // Generate the link
        linkGenerator.generateLink(requireContext(), object : LinkGenerator.ResponseListener {
            override fun onResponse(s: String) {
                // Successfully generated the link
                Log.d(ErrorDialog.TAG, s)
                // Example share message with the generated link
                val message = "Check out this article: $s"
                shareLink(message)

            }

            override fun onResponseError(s: String) {
                // Handle error if link generation fails
                Log.e("Error", "Error Generating Link: $s")
            }
        })
    }

    private fun shareLink(message: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, AppConstant.SHARE_VIA)
        startActivity(shareIntent)
    }


}