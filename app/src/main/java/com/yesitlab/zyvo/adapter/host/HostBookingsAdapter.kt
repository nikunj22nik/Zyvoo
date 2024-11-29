package com.yesitlab.zyvo.adapter.host

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.LayoutAllBookingsBinding
import com.yesitlab.zyvo.databinding.LayoutHostBookingBinding
import com.yesitlab.zyvo.model.MyBookingsModel

class HostBookingsAdapter (
        var context: Context,
        var list: MutableList<MyBookingsModel>,
        var listner: OnClickListener
    ) : RecyclerView.Adapter<HostBookingsAdapter.MyBookingsViewHolder>()
    {

        lateinit var textStatus : TextView
        lateinit var text : String

        inner class MyBookingsViewHolder(var binding: LayoutHostBookingBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(currentItem: MyBookingsModel) {



                binding.textApporve.setOnClickListener {

                    binding.llAcceptRequest.visibility = View.VISIBLE
                    binding.llDeclineRequest.visibility = View.GONE

                }

                binding.rlAcceptRequestBtn.setOnClickListener {
                    binding.llApproveAndDecline.visibility = View.GONE
                    binding.llAcceptRequest.visibility = View.GONE

                    binding.fl.visibility = View.VISIBLE
                }






                binding.rlDeclineRequestBtn.setOnClickListener {
                    binding.llApproveAndDecline.visibility = View.GONE
                    binding.llDeclineRequest.visibility = View.GONE
                    removeItem(position)
                }


                binding.textDecline.setOnClickListener {
                    binding.llDeclineRequest.visibility = View.VISIBLE
                    binding.llAcceptRequest.visibility = View.GONE
                }





                textStatus = binding.textStatus
                binding.imagePicture.setImageResource(currentItem.image)


                when (list.get(position).textStatus) {
                    "Confirmed" -> binding.textStatus.setBackgroundResource(R.drawable.blue_button_bg)
                    "Waiting payment" -> binding.textStatus.setBackgroundResource(R.drawable.yellow_button_bg)
                    "Canceled" -> binding.textStatus.setBackgroundResource(R.drawable.grey_button_bg)
                    else -> binding.textStatus.setBackgroundResource(R.drawable.button_bg) // Optional fallback
                }
                binding.root.setOnClickListener{
                    listner.itemClick(position)
                }

                binding.textName.setText(currentItem.textName)
                binding.textDate.setText(currentItem.textDate)
                binding.textStatus.setText(currentItem.textStatus)
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBookingsViewHolder {
            val binding =
                LayoutHostBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return MyBookingsViewHolder(binding)
        }

        override fun getItemCount() = list.size

        override fun onBindViewHolder(holder: MyBookingsViewHolder, position: Int) {
            val currentItem = list[position]

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
            notifyDataSetChanged()
        }


    }