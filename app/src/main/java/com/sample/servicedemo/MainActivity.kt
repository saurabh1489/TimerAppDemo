package com.sample.servicedemo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sample.servicedemo.databinding.ActivityMainBinding
import com.sample.servicedemo.service.ACTION_TIMER_START
import com.sample.servicedemo.service.EXTRA_TIMER_TIME
import com.sample.servicedemo.service.TimerService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.submitBtn.setOnClickListener {
            val text = binding.inputTime.text.trimStart().trimEnd()
            text?.run {
                if (contains(":")) {
                    val minSeconds = text.split(":")
                    if (minSeconds.get(0).toInt() in 0..61 && minSeconds.get(1)
                            .toInt() in 0..61
                    ) {
                        Intent(this@MainActivity, TimerService::class.java)
                            .apply {
                                val minSeconds = binding.inputTime.text.split(":")
                                val timeInSeconds =
                                    minSeconds[0].toLong() * 60 + minSeconds[1].toLong()
                                action = ACTION_TIMER_START
                                putExtra(EXTRA_TIMER_TIME, timeInSeconds)
                            }
                            .also { intent ->
                                startService(intent)
                            }
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Please input correct time in MM:SS format!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Please input correct time in MM:SS format!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
