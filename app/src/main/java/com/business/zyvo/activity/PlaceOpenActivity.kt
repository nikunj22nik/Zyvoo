package com.business.zyvo.activity

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.business.zyvo.DateManager.DateManager
import com.business.zyvo.R
import com.business.zyvo.adapter.host.AdapterOuterPlaceOrder
import com.business.zyvo.databinding.ActivityPlaceOpenBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

@AndroidEntryPoint
class PlaceOpenActivity : AppCompatActivity() {

    lateinit var binding: ActivityPlaceOpenBinding
    lateinit var adapter : AdapterOuterPlaceOrder
    lateinit var list : MutableList<Pair<String,List<String>>>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlaceOpenBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        list = mutableListOf()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.imageBackButton.setOnClickListener {
            onBackPressed()
        }
        fillDataInCalendar()
        settingRecyclerViewData()
    }

    private fun settingRecyclerViewData(){
        var adapterOuterPlaceOrder =AdapterOuterPlaceOrder(this,list)
        binding.recyclerPlaceOrder.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        binding.recyclerPlaceOrder.adapter = adapterOuterPlaceOrder
        binding.recyclerPlaceOrder.isNestedScrollingEnabled = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fillDataInCalendar(){
        var hourList = DateManager(this).generateHourList()
        var pos =0
        hourList.forEach {
            if(pos==0) {
                var p: Pair<String, List<String>> = Pair<String, List<String>>(it,DateManager(this).getCurrentWeek())
                list.add(p)
            }else{
                var p: Pair<String, List<String>> = Pair<String, List<String>>(it,getNewListOfData())
                list.add(p)
            }
            pos++
        }

    }

    fun getNewListOfData() : MutableList<String>{
        var dummyList :MutableList<String> = mutableListOf()
        for(i in 1..7){
            var rndNumber = generateRandomNumber()
            if(rndNumber == i) {
                dummyList.add("Add")
            }else{
                dummyList.add("")
            }
        }
        return dummyList
    }

    fun generateRandomNumber(): Int {
        return Random.nextInt(1, 8)
    }

}