package com.motasemziad.restaurant.internet

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.motasemziad.restaurant.R

class LostConnectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lost_connection)

        retry_connection_button.setOnClickListener {
            retry_connection_progress.visibility = View.VISIBLE
            retry_result.visibility = View.INVISIBLE

            val is_conncted = InternetBroadcast.isConnectedOrConnecting(this)
            if (is_conncted)
            {
                Thread{
                    Thread.sleep(3000)
                    runOnUiThread {
                        retry_connection_progress.visibility = View.INVISIBLE
                        retry_result.text = "تم التوصيل"
                        retry_result.visibility = View.VISIBLE
                        finish()
                    }
                }.start()

            } else
            {
                Thread {
                    Thread.sleep(3000)
                    runOnUiThread {
                        retry_connection_progress.visibility = View.INVISIBLE
                        retry_result.text = "لا يوجد انترنت"
                        retry_result.visibility = View.VISIBLE
                    }
                }.start()
            }
        }

    }
}