package com.yesitlab.zyvo.fragment.host

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.FragmentFilterPaymentStatusBinding


class FilterPaymentStatusFragment : DialogFragment() {
  lateinit var  binding: FragmentFilterPaymentStatusBinding

    private var param1: String? = null
    private var param2: String? = null


    interface DialogListener {
        fun onSubmitClicked() // Callback when the submit button is clicked
    }

    private var listener: DialogListener? = null

    fun setDialogListener(listener: DialogListener) {
        this.listener = listener
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
           // param1 = it.getString(ARG_PARAM1)
           // param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding=  FragmentFilterPaymentStatusBinding.inflate(LayoutInflater.from(requireActivity()),container,false)
        selectingClickListener()
        bydefaultSelect()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageClearAll.setOnClickListener{
          //  listener?.onSubmitClicked() // Notify parent
            dismiss() // Close the dialog
        }

        binding.imgSearch.setOnClickListener {
          //  listener?.onSubmitClicked() // Notify parent
            dismiss() // Close the dialog
        }

        binding.imageCross.setOnClickListener {
            //  listener?.onSubmitClicked() // Notify parent
            dismiss() // Close the dialog
        }

    }
    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),  // Width 90% of screen
            ViewGroup.LayoutParams.WRAP_CONTENT                   // Height wrap content
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent) // Optional
    }



    private fun bydefaultSelect(){
        binding.tvCompleted.setBackgroundResource(R.drawable.bg_inner_manage_place)
        binding.tvPending.setBackgroundResource(R.drawable.bg_outer_manage_place)
        binding.tvCancelled.setBackgroundResource(R.drawable.bg_outer_manage_place)
    }


    private fun selectingClickListener(){
        binding.tvCompleted.setOnClickListener {

            binding.tvCompleted.setBackgroundResource(R.drawable.bg_inner_manage_place)
            binding.tvPending.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvCancelled.setBackgroundResource(R.drawable.bg_outer_manage_place)

        }
        binding.tvPending.setOnClickListener {

            binding.tvCompleted.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvPending.setBackgroundResource(R.drawable.bg_inner_manage_place)
            binding.tvCancelled.setBackgroundResource(R.drawable.bg_outer_manage_place)


        }
        binding.tvCancelled.setOnClickListener {

            binding.tvCompleted.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvPending.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvCancelled.setBackgroundResource(R.drawable.bg_inner_manage_place)


        }

    }
}