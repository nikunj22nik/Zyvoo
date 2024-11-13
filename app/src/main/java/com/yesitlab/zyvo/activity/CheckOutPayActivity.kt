package com.yesitlab.zyvo.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.adapter.AdapterAddOn
import com.yesitlab.zyvo.databinding.ActivityCheckOutPayBinding

class CheckOutPayActivity : AppCompatActivity() {

    lateinit var binding : ActivityCheckOutPayBinding
    lateinit var adapterAddon : AdapterAddOn



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCheckOutPayBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initialization()
    }

    fun initialization(){

        adapterAddon = AdapterAddOn(this@CheckOutPayActivity,getAddOnList().subList(0,4))
        binding.recyclerAddOn.layoutManager = LinearLayoutManager(this@CheckOutPayActivity, LinearLayoutManager.VERTICAL ,false)
        binding.recyclerAddOn.adapter = adapterAddon

    }

    private fun getAddOnList(): MutableList<String> {

        var list = mutableListOf<String>()

        list.add("Computer Screen")

        list.add("Bed Sheets")

        list.add("Phone charger")

        list.add("Ring Light")

        list.add("Left Light")

        list.add("Water Bottle")

        return list

    }


}