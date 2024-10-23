package com.yesitlab.zyvo.fragment.both.splash

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.activity.GuesMain
import com.yesitlab.zyvo.databinding.FragmentSplashBinding
import com.yesitlab.zyvo.session.SessionManager


class SplashFragment : Fragment() {
    private lateinit var binding: FragmentSplashBinding
    private lateinit var handler : Handler


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
        binding = FragmentSplashBinding.inflate(LayoutInflater.from(requireActivity()),container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handler = Handler(Looper.getMainLooper())
        val images = listOfNotNull(ContextCompat.getDrawable(requireActivity(), R.drawable.splash_icon),
            ContextCompat.getDrawable(requireActivity(), R.drawable.splash_icon1),
            )

//        val animationDrawable = AnimationDrawable().apply {
//            images.forEach { addFrame(it, 1000 / images.size) } // Divide the duration equally between frames
//            isOneShot = false // Loop the animation
//        }
//        binding.imageIcon.setImageDrawable(animationDrawable) // Set the animation drawable to the ImageView
//        animationDrawable.start()


        handler.postDelayed({

            var session =SessionManager(requireContext())
            if(session.getUserId() ==1){
                var intent = Intent(requireContext(),GuesMain::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
            else {
                // Task to be executed after 3 seconds
                findNavController().navigate(R.id.loggedScreenFragment)
                //    binding.imageIcon.setColorFilter(ContextCompat.getColor(requireContext(), R.color.aquamarine))
            }





                            }, 3000)

    }

    override fun onDestroyView() {
        super.onDestroyView()

        handler.removeCallbacksAndMessages(null)
    }


}