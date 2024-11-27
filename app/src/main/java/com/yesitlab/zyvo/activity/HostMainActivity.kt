package com.yesitlab.zyvo.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.ActivityGuesMainBinding
import com.yesitlab.zyvo.databinding.ActivityHostMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HostMainActivity : AppCompatActivity() {

    lateinit var binding : ActivityHostMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHostMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}