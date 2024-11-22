package com.yesitlab.zyvo.fragment.guest

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.yesitlab.zyvo.DateManager.DateManager
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.FragmentSelectHourDialogBinding


class SelectHourFragmentDialog : DialogFragment() {

    private  var _binding : FragmentSelectHourDialogBinding? = null
    private  val binding  get() =  _binding!!



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
        //    param1 = it.getString(ARG_PARAM1)
          //  param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentSelectHourDialogBinding.inflate(LayoutInflater.from(requireActivity()),container,false)
       // _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_select_hour_dialog, container, false)




        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rlSelectHour.setOnClickListener{
            DateManager(requireContext()).showHourSelectionDialog(requireContext()) { selectedHour ->
                binding.textSelectedHour.setText(selectedHour.toString())
            }
        }

        binding.textSaveButton.setOnClickListener {
            listener?.onSubmitClicked() // Notify parent
                dismiss() // Close the dialog
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}