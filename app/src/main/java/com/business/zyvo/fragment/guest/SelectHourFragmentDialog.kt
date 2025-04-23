package com.business.zyvo.fragment.guest

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.business.zyvo.CircularSeekBar.OnSeekBarChangeListener
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.databinding.FragmentSelectHourDialogBinding
import com.business.zyvo.utils.ErrorDialog


class SelectHourFragmentDialog : DialogFragment() {

    private  var _binding : FragmentSelectHourDialogBinding? = null
    private  val binding  get() =  _binding!!
    private var hour = ""



    interface DialogListener {
        fun onSubmitClicked(hour:String) // Callback when the submit button is clicked
    }

    private var listener: DialogListener? = null

    fun setDialogListener(listener: DialogListener) {
        this.listener = listener
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

        _binding = FragmentSelectHourDialogBinding.inflate(LayoutInflater.from(requireActivity()),container,false)
       // _binding = DataBindingUtil.inflate(inflater,R.layout.fragment_select_hour_dialog, container, false)




        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rlSelectHour.setOnClickListener{
            DateManager(requireContext()).showHourSelectionDialog(requireContext()) { selectedHour ->
                binding.textSelectedHour.setText(selectedHour)
                hour = selectedHour.replace(" hours","")
                Log.d("checkHoursValue",selectedHour.toString())
                // Remove "hours", trim spaces, keep only number
              var  hour1 = selectedHour.replace("hours", "", ignoreCase = true).trim()
                binding.circularSeekBar.endHours = (hour1.toIntOrNull() ?: 0).toFloat()
            }
        }
        binding.circularSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener
        {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(progress: String) {
                try {
                    hour = progress
                    binding.textSelectedHour.setText(hour +" hours")
                    Log.d("checkHoursValue",hour.toString())
                }catch (e:Exception){
                    Log.d(ErrorDialog.TAG,e.message!!)
                }
            }

        })

        binding.textSaveButton.setOnClickListener {
            listener?.onSubmitClicked(hour) // Notify parent
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}