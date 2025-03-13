package com.business.zyvo.fragment.host

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.business.zyvo.CallUpdateListener
import com.business.zyvo.R
import com.business.zyvo.databinding.FragmentFilterPaymentStatusBinding


class FilterPaymentStatusFragment : DialogFragment() {
  lateinit var  binding: FragmentFilterPaymentStatusBinding

    private var filter: String = "finished"
    private var updateListener: CallUpdateListener? = null

    interface DialogListener {
        fun onSubmitClicked()
    }

    private var listener: DialogListener? = null

    fun setDialogListener(listener: DialogListener) {
        this.listener = listener
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is CallUpdateListener) {
            updateListener = parentFragment as CallUpdateListener
        }
    }

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
        binding=  FragmentFilterPaymentStatusBinding.inflate(LayoutInflater.from(requireActivity()),container,false)
        selectingClickListener()
        bydefaultSelect()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageClearAll.setOnClickListener{

            val bundle = Bundle()
            bundle.putString("dialogFragment", "")
            setFragmentResult("FilterPaymentStatus", bundle)
            dismiss()
        }

        binding.imgSearch.setOnClickListener {

            val bundle = Bundle()
            bundle.putString("dialogFragment",filter)
            setFragmentResult("FilterPaymentStatus", bundle)
            dismiss()
        }

        binding.imageCross.setOnClickListener {

            dismiss()
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
            filter = "finished"
            binding.tvCompleted.setBackgroundResource(R.drawable.bg_inner_manage_place)
            binding.tvPending.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvCancelled.setBackgroundResource(R.drawable.bg_outer_manage_place)

        }
        binding.tvPending.setOnClickListener {
            filter = "pending"
            binding.tvCompleted.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvPending.setBackgroundResource(R.drawable.bg_inner_manage_place)
            binding.tvCancelled.setBackgroundResource(R.drawable.bg_outer_manage_place)


        }
        binding.tvCancelled.setOnClickListener {
            filter = "cancelled"
            binding.tvCompleted.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvPending.setBackgroundResource(R.drawable.bg_outer_manage_place)
            binding.tvCancelled.setBackgroundResource(R.drawable.bg_inner_manage_place)


        }

    }
}