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

//fY0SwJAnSTab_3uDehpVZG:APA91bFdAaw3wU-gvBnNhAYhWYgcFhWF3qV8BlnnO0crO9298YhX07C3Delmhp0iG3pPJkNH2i8rw9bWV10gicPmb0qLs699kLVvgMNO_Gh1_31GC1u45qg

//$this->fcmService->sendNotification($body, $receiver->fcm_token, $data, $title);



//2025-05-25 16:02:00

//2025-04-16 18:27:00



//1st result 2025-02-10 08:00:00



// $now = Carbon::now()->seconds(0);
// $endTime = Carbon::now()->addMinutes(30)->seconds(59);

// $bookings = Booking::whereBetween('booking_start', [$now, $endTime])->get();

//  Log::info('Started booking notification job');
//  Log::info('Number of bookings found: ' . $bookings->count());




//   foreach ($bookings as $booking) {
//     $fcmToken = User::where('id', $booking->host_user_id)->value('fcm_token');
//     Log::info('HOST ID IS: ' .  $booking->host_user_id);
//     Log::info('FCM TOKEN IS: ' .  $fcmToken);

//     if (!empty($fcmToken)) {
//         $body = "Your Booking Will Start in 30 minutes";
//         Log::info('testing here inside fcm');
//         $fcmResponse = $this->service->sendNotification($body, $fcmToken);
//         Log::info('Notification sent succesfully');
//   }