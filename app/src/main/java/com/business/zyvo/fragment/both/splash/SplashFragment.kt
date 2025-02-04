package com.business.zyvo.fragment.both.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.business.zyvo.AppConstant
import com.business.zyvo.R
import com.business.zyvo.activity.GuesMain
import com.business.zyvo.activity.HostMainActivity
import com.business.zyvo.databinding.FragmentSplashBinding
import com.business.zyvo.session.SessionManager
import retrofit2.http.Tag


class SplashFragment : Fragment() {

    private lateinit var binding: FragmentSplashBinding
    private lateinit var handler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            FragmentSplashBinding.inflate(LayoutInflater.from(requireActivity()), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handler = Handler(Looper.getMainLooper())

//        val animationDrawable = AnimationDrawable().apply {
//            images.forEach { addFrame(it, 1000 / images.size) } // Divide the duration equally between frames
//            isOneShot = false // Loop the animation
//        }
//        binding.imageIcon.setImageDrawable(animationDrawable) // Set the animation drawable to the ImageView
//        animationDrawable.start()


        handler.postDelayed({
            val session =SessionManager(requireContext())
            Log.d("TESTING","Session "+session.getUserId().toString())
            if(session.getUserId() !=-1){

                if(session.getCurrentPanel().equals(AppConstant.Host)){
                    val intent = Intent(requireContext(), HostMainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }else {
                    val intent = Intent(requireContext(), GuesMain::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                 //   findNavController().navigate(R.id.paymentsFragment2)
            }
            else {
                findNavController().navigate(R.id.loggedScreenFragment)
                //    binding.imageIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.aquamarine))
               }
             }, 3000)

//        val months = listOf(
//            "January", "February", "March", "April", "May", "June", "July",
//            "August", "September", "October", "November", "December"
//        )
//
//        val years = (2000..2050).toList()
//        val yearsStringList = years.map { it.toString() }
//        val days = resources.getStringArray(R.array.day).toList()
//
//        binding.spinnerLanguage.layoutDirection = View.LAYOUT_DIRECTION_LTR
//        binding.spinnerLanguage.arrowAnimate = false
//        binding.spinnerLanguage.setItems(days)
//        binding.spinnerLanguage.setIsFocusable(true)
//        val recyclerView = binding.spinnerLanguage.getSpinnerRecyclerView()
//
//// Add item decoration for spacing
//
//        val spacing = 16 // Spacing in pixels
//        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
//            override fun getItemOffsets(
//                outRect: Rect,
//                view: View,
//                parent: RecyclerView,
//                state: RecyclerView.State
//            ) {
//                outRect.top = spacing
//            }
//        })
//
//        binding.spinnermonth.layoutDirection = View.LAYOUT_DIRECTION_LTR
//        binding.spinnermonth.arrowAnimate = false
//        binding.spinnermonth.setItems(months)
//        binding.spinnermonth.setIsFocusable(true)
//        val recyclerView3 = binding.spinnermonth.getSpinnerRecyclerView()
//
//        recyclerView3.addItemDecoration(object : RecyclerView.ItemDecoration() {
//            override fun getItemOffsets(
//                outRect: Rect,
//                view: View,
//                parent: RecyclerView,
//                state: RecyclerView.State
//            ) {
//                outRect.top = spacing
//            }
//        })
//
//
//        binding.spinneryear.layoutDirection = View.LAYOUT_DIRECTION_LTR
//        binding.spinneryear.arrowAnimate = false
//        binding.spinneryear.setItems(yearsStringList)
//        binding.spinneryear.setIsFocusable(true)
//        binding.spinneryear.post {
//            binding.spinneryear.spinnerPopupWidth = binding.spinneryear.width
//        }
//
//        val recyclerView1 = binding.spinneryear.getSpinnerRecyclerView()
//        recyclerView1.addItemDecoration(object : RecyclerView.ItemDecoration() {
//            override fun getItemOffsets(
//                outRect: Rect,
//                view: View,
//                parent: RecyclerView,
//                state: RecyclerView.State
//            ) {
//                outRect.top = spacing
//            }
//
//        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }


}