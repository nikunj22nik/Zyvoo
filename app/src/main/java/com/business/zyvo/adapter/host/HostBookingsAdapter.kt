package com.business.zyvo.adapter.host

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.BuildConfig
import com.business.zyvo.OnClickListener
import com.business.zyvo.R
import com.business.zyvo.databinding.LayoutHostBookingBinding
import com.business.zyvo.model.MyBookingsModel
import com.business.zyvo.utils.ErrorDialog


class HostBookingsAdapter (var context: Context, var list: MutableList<MyBookingsModel>, var listner: OnClickListener)
    : RecyclerView.Adapter<HostBookingsAdapter.MyBookingsViewHolder>(), Filterable {


        private lateinit var mListener: onItemClickListener
        private var filteredList: List<MyBookingsModel> = list

        interface onItemClickListener {
            fun onItemClick(bookingId : Int,status :String, message :String,reason:String,
                            extension_id : String)
        }

       fun setOnItemClickListener(listener: onItemClickListener) {
            mListener = listener
       }

        lateinit var textStatus : TextView
        lateinit var text : String

        inner class MyBookingsViewHolder(var binding: LayoutHostBookingBinding) : RecyclerView.ViewHolder(binding.root) {
            fun bind(currentItem: MyBookingsModel) {

                binding.textApporve.setOnClickListener {
                    if(binding.llAcceptRequest.visibility == View.GONE) {
                        binding.llAcceptRequest.visibility = View.VISIBLE
                        binding.llDeclineRequest.visibility = View.GONE
                    }
                    else{
                        binding.llAcceptRequest.visibility = View.GONE
                        binding.llDeclineRequest.visibility = View.GONE
                    }
                }


                binding.rlAcceptRequestBtn.setOnClickListener {
                    binding.llApproveAndDecline.visibility = View.GONE
                    binding.llAcceptRequest.visibility = View.GONE
                    binding.fl.visibility = View.VISIBLE
                    val bookingId = list.get(position).booking_id
                    val status = "approve"
                    val message = binding.tvShareMessage.text.toString()
                    val extensionId = currentItem?.extension_id?.let { extId ->
                        if (extId == 0) "" else extId.toString()
                    } ?: ""
                    mListener.onItemClick(bookingId,status,message,"",extensionId)
                }

                binding.clMainHeader.setOnClickListener {
                    binding.llDeclineRequest.visibility = View.GONE
                    binding.llAcceptRequest.visibility = View.GONE
                }
                var reason ="other"

                binding.doubt.setOnClickListener {
                   reason = "I'm overbooked"
                    binding.doubt.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box)
                    binding.tvAvailableDay.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
                }
                binding.otherReasonEt.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {}

                    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                        reason = charSequence.toString()
                        binding.doubt.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
                        binding.tvAvailableDay.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
                    }

                    override fun afterTextChanged(editable: Editable) {  }
                })

                binding.tvAvailableDay.setOnClickListener {
                    reason ="Maintenance day"
                    binding.doubt.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box_grey_light)
                    binding.tvAvailableDay.setBackgroundResource(R.drawable.bg_four_side_corner_msg_box)
                }


                binding.rlDeclineRequestBtn.setOnClickListener {
                    binding.llApproveAndDecline.visibility = View.GONE
                    binding.llDeclineRequest.visibility = View.GONE
                    val msg =binding.tvShareMessage1.text.toString()
                    val extensionId = currentItem?.extension_id?.let { extId ->
                        if (extId == 0) "" else extId.toString()
                    } ?: ""
                    mListener.onItemClick(list.get(position).booking_id,"decline",msg,reason,
                        extensionId)

                }

                binding.textDecline.setOnClickListener {
                    if(binding.llDeclineRequest.visibility == View.GONE) {
                        binding.llDeclineRequest.visibility = View.VISIBLE
                        binding.llAcceptRequest.visibility = View.GONE
                    }
                    else{
                        binding.llDeclineRequest.visibility = View.GONE
                        }
                }

                textStatus = binding.textStatus

                Glide.with(context).load(BuildConfig.MEDIA_URL+currentItem.guest_avatar).into( binding.imagePicture)
                if(currentItem.booking_status.equals("Pending")){
                    binding.llApproveAndDecline.visibility = View.VISIBLE
                    binding.textStatus.visibility = View.GONE
                    binding.fl.visibility = View.VISIBLE
                }
                else{
                    binding.llApproveAndDecline.visibility = View.GONE
                    binding.textStatus.visibility = View.VISIBLE
                    binding.fl.visibility = View.VISIBLE
                }
                when (filteredList.get(position).booking_status) {
                    "Confirmed" ->  binding.textStatus.setBackgroundResource(R.drawable.blue_button_bg)

                    "Awaiting Payment" -> binding.textStatus.setBackgroundResource(R.drawable.yellow_button_bg)

                    "Cancelled" -> binding.textStatus.setBackgroundResource(R.drawable.grey_button_bg)

                    else -> binding.textStatus.setBackgroundResource(R.drawable.button_bg) // Optional fallback
                }
                binding.clMain.setOnClickListener{
                    val extensionId = currentItem?.extension_id?.let { extId ->
                        if (extId == 0) "" else extId.toString()
                    } ?: ""
                    Log.d(ErrorDialog.TAG," $extensionId")
                   mListener.onItemClick(filteredList.get(position).booking_id,
                       "-11","","",extensionId)
                }
                binding.textName.setText(currentItem.guest_name)
                binding.textDate.setText(currentItem.booking_date)
             //   binding.textStatus.setText(currentItem.booking_status)
                binding.textStatus.setText(currentItem.booking_status.replaceFirstChar { it.uppercase() })

                currentItem?.type?.let {
                    when(it){
                        "extension"-> binding.imageOverlay.visibility = View.VISIBLE
                        else -> binding.imageOverlay.visibility = View.GONE
                    }
                }?: run {
                    // Handle null case if needed, or just hide the overlay by default
                    binding.imageOverlay.visibility = View.GONE
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBookingsViewHolder {
            val binding = LayoutHostBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return MyBookingsViewHolder(binding)
        }

        override fun getItemCount() = filteredList.size

        override fun onBindViewHolder(holder: MyBookingsViewHolder, position: Int) {
            val currentItem = filteredList[position]
            holder.bind(currentItem)
        }

        private fun removeItem(position: Int) {
            if (position >= 0 && position < list.size) {
                list.removeAt(position)            // Remove item from list
                notifyItemRemoved(position)        // Notify adapter about item removal
                notifyItemRangeChanged(position, list.size) // Optional: refresh range
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun updateItem(newList : MutableList<MyBookingsModel>){
            this.list = newList
            filteredList = newList
            notifyDataSetChanged()
        }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.toLowerCase()
                val results = FilterResults()

                if (query.isNullOrBlank()) {
                    results.values = list
                }
                else {
                    val filteredItems = list.filter {
                         it.guest_name.toLowerCase().contains(query)
                    }
                    results.values = filteredItems
                }
                return results
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as List<MyBookingsModel>
                notifyDataSetChanged()
            }
        }
    }


}