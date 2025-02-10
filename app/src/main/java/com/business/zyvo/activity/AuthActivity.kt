package com.business.zyvo.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.business.zyvo.LoadingUtils.Companion.showErrorDialog
import com.business.zyvo.MyApp
import com.business.zyvo.R
import com.business.zyvo.utils.NetworkMonitorCheck
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {
    private val app by lazy { application as MyApp }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_auth)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        observeButtonState()
    }

    private fun observeButtonState() {
        lifecycleScope.launch {
            NetworkMonitorCheck._isConnected.collect { isConnected ->
                if (!isConnected) {
                    showErrorDialog(
                        this@AuthActivity,
                        resources.getString(R.string.no_internet_dialog_msg)
                    )
                }
            }
        }
    }


}