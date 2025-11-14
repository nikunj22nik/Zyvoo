package com.business.zyvo.activity.guest.sorryresult

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.business.zyvo.R

class SorryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sorry)
        val imgBack = findViewById<ImageView>(R.id.img_back)
        imgBack.setOnClickListener {
            onBackPressed()
        }
    }
}