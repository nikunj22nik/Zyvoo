package com.business.zyvo.fragment.guest.helpCenter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.business.zyvo.AppConstant
import com.business.zyvo.LoadingUtils
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.NetworkResult
import com.business.zyvo.OnClickListener1
import com.business.zyvo.R
import com.business.zyvo.adapter.AdapterAllArticles
import com.business.zyvo.adapter.AdapterAllGuides
import com.business.zyvo.databinding.FragmentHostHelpCenterBinding
import com.business.zyvo.fragment.guest.helpCenter.model.HelpCenterResponse
import com.business.zyvo.fragment.guest.helpCenter.viewModel.HelpCenterViewModel
import com.business.zyvo.session.SessionManager
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HostHelpCenterFragment : Fragment(), View.OnClickListener, OnClickListener1 {
    private var _binding: FragmentHostHelpCenterBinding? = null
    private  val binding get() = _binding!!
    private val viewModel: HelpCenterViewModel by lazy {
        ViewModelProvider(this)[HelpCenterViewModel::class.java]
    }
    private lateinit var adapterAllGuides: AdapterAllGuides
    private lateinit var adapterAllArticles: AdapterAllArticles

    lateinit var navController: NavController
    var session: SessionManager? = null
    var type: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHostHelpCenterBinding.inflate(
            LayoutInflater.from(requireActivity()),
            container,
            false
        )
        session = SessionManager(requireContext())
        adapterAllGuides =
            AdapterAllGuides(requireContext(), arrayListOf(), maxItemsToShow = 4, this)
        binding.recyclerViewGuests.adapter = adapterAllGuides

        adapterAllArticles =
            AdapterAllArticles(requireContext(), arrayListOf(),  this)
        binding.recyclerViewArticle.adapter = adapterAllArticles

        binding.textBrowseAllGuides.setOnClickListener(this)
        binding.textBrowseAllArticle.setOnClickListener(this)


        arguments?.let {
            Log.d("TESTING_ANDROID", "I AM ON CLICK OF HOST Arguments")

            if (it.containsKey(AppConstant.type) && it.getString(AppConstant.type)
                    .equals(AppConstant.Guest)
            ) {
                binding.radioGuest.isChecked = true
                binding.radioHost.isChecked = false
                type = AppConstant.Guest/*"guest"*/
                Log.d("TESTING_ANDROID", "Guest")

            } else {
                binding.radioHost.isChecked = true
                binding.radioGuest.isChecked = false
                Log.d("TESTING_ANDROID", "Host")
                type = AppConstant.Host/*"host"*/
            }
        }

        binding.textContactUsButton.setOnClickListener {
            findNavController().navigate(R.id.contact_us)
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        binding.imageBackIcon.setOnClickListener {
            navController.navigateUp()
        }
        val userType = session?.getUserType()?.replaceFirstChar { it.uppercase() } ?: ""
        binding.textGuidesForGuests.text = "Guides for $userType"
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
                        getHelpCenter()
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

    private fun getHelpCenter() {
        lifecycleScope.launch {
            viewModel.getHelpCenter(session?.getUserId().toString(), type.toString()).collect {
                when (it) {

                    is NetworkResult.Success -> {
                        if (it.data != null) {
                            val model = Gson().fromJson<HelpCenterResponse>(
                                it.data,
                                HelpCenterResponse::class.java
                            )
                            if (model.data.user_fname != null){
                               val  name = model.data.user_fname
                                session?.setFirstName(name)
                                binding.textTitle.text = "Hi $name, how can we help?"
                            }
                            if (model.data.guides.isNotEmpty()){
                                adapterAllGuides.updateItem(model.data.guides)
                            }
                            if (model.data.articles.isNotEmpty()){
                                adapterAllArticles.updateItem(model.data.articles)
                            }
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

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.textBrowseAllGuides -> {
                val bundle = Bundle()
                bundle.putString(AppConstant.type, AppConstant.GUIDES_TEXT/*"Guides"*/)
                findNavController().navigate(R.id.browse_article_host, bundle)
            }

            R.id.textBrowseAllArticle -> {
                val bundle = Bundle()
                bundle.putString(AppConstant.type, AppConstant.ARTICLE/*"Article"*/)
                findNavController().navigate(R.id.browse_article_host, bundle)
            }
        }
    }



    override fun itemClick(obj: Int, text: String) {
        val bundle = Bundle()
        bundle.putString(AppConstant.Id,obj.toString())
        bundle.putString(AppConstant.textType,text)
        findNavController().navigate(R.id.browse_aricle_details,bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}