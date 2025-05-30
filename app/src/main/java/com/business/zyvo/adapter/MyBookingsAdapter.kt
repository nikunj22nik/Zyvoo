package com.business.zyvo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.BuildConfig
import com.business.zyvo.OnItemAdapterClick
import com.business.zyvo.R
import com.business.zyvo.databinding.LayoutAllBookingsBinding
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.BookingModel


class MyBookingsAdapter(
    private val context: Context, private var list: MutableList<BookingModel>, private val listener: OnItemAdapterClick
) : RecyclerView.Adapter<MyBookingsAdapter.MyBookingsViewHolder>() {

    inner class MyBookingsViewHolder(private val binding: LayoutAllBookingsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(currentItem: BookingModel) {

            Glide.with(context).load(BuildConfig.MEDIA_URL+currentItem.property_image).placeholder(R.drawable.image_hotel).
            error(R.drawable.image_hotel).into(binding.imagePicture)

            binding.textName.text = currentItem.property_name
            binding.textDate.text = currentItem.booking_date
            //binding.textStatus.text = currentItem.booking_status
            val status = currentItem.booking_status
            binding.textStatus.text = status?.replaceFirstChar { it.uppercaseChar() } ?: ""

            // Set background based on booking status
            when (currentItem.booking_status) {
                "Confirmed" -> binding.textStatus.setBackgroundResource(R.drawable.blue_button_bg)
                "Waiting Payment" -> binding.textStatus.setBackgroundResource(R.drawable.yellow_button_bg)
                "Cancelled" -> binding.textStatus.setBackgroundResource(R.drawable.grey_button_bg)
                else -> binding.textStatus.setBackgroundResource(R.drawable.button_bg)
            }

            // Set click listener
            binding.root.setOnClickListener {
                listener.itemClickOn(adapterPosition,currentItem.booking_id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBookingsViewHolder {
        val binding =
            LayoutAllBookingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyBookingsViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: MyBookingsViewHolder, position: Int) {
        holder.bind(list[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(newList: MutableList<BookingModel>) {
        list = newList
        notifyDataSetChanged()
    }
}
