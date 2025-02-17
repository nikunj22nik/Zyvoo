package com.business.zyvo.fragment.both.browseGuideArtcileDetail

import android.os.Build
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BrowseGuideArtcileDetailFragment : Fragment() {

    lateinit var binding: FragmentBrowseGuideArtcileDetailBinding
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
        binding = FragmentBrowseGuideArtcileDetailBinding.inflate(
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
                        if (textType == "article") {
                            getArticleDetails()
                        } else if (textType == "guides") {
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
                            binding.textDescription.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Html.fromHtml(model.data.description, Html.FROM_HTML_MODE_LEGACY)
                            } else {
                                Html.fromHtml(model.data.description)
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
                            binding.textDescription.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Html.fromHtml(model.data.description, Html.FROM_HTML_MODE_LEGACY)
                            } else {
                                Html.fromHtml(model.data.description)
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


}