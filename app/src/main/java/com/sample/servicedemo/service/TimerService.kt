package com.sample.servicedemo.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sample.servicedemo.MainActivity
import com.sample.servicedemo.ResultActivity
import java.util.concurrent.TimeUnit

const val CHANNEL_ID = "timer"
const val EXTRA_NOTIFICATION_ID = "notification_id"
const val EXTRA_TIMER_TIME = "timer_time"
const val ACTION_TIMER_CANCEL = "com.sample.servicedemo.TIMER_CANCEL"
const val ACTION_TIMER_START = "com.sample.servicedemo.TIMER_START"
const val ACTION_TIMER_RESUME = "com.sample.servicedemo.TIMER_RESUME"

const val PREF_KEY_TIMER_DURATION = "timer_duration"
const val PREF_KEY_TIMER_CREATED = "timer_created"
const val PREF_KEY_TIMER_RUNNING = "timer_running"

class TimerService : Service() {

    companion object {
        private const val TAG = "TimerService"
    }

    private val notificationId: Int = 1
    private var isTimerRunning = false;
    private lateinit var timer: CountDownTimer
    private val notificationBuilder by lazy {

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val cancelIntent = Intent(this, TimerBroadcastReceiver::class.java).apply {
            action = ACTION_TIMER_CANCEL
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
        }

        val cancelPendingIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, 0)

        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_notification_overlay)
            .setContentTitle("Timer")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .addAction(0, "Cancel", cancelPendingIntent)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "OnCreate()")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val timeInSeconds = intent?.getLongExtra(EXTRA_TIMER_TIME, 0L) ?: -1
        Log.d(TAG, "onStartCommand()::$action, timerInSeconds:$timeInSeconds")
        when (action) {
            ACTION_TIMER_START -> handleStartTimer(timeInSeconds)
            ACTION_TIMER_CANCEL -> handleCancelTimer()
            ACTION_TIMER_RESUME -> handleResumeTimer()
        }
        return START_STICKY
    }

    private fun handleResumeTimer() {
        if (getPreferenceValueBoolean(PREF_KEY_TIMER_RUNNING)) {
            val timerCreationTime = getPreferenceValueLong(PREF_KEY_TIMER_CREATED)
            val timerDuration = getPreferenceValueLong(PREF_KEY_TIMER_DURATION)
            val timeInSeconds =
                (timerDuration - (System.currentTimeMillis() - timerCreationTime)) / 1000
            createNotification(timeInSeconds)
            startTimer(timeInSeconds)
        } else {
            stopSelf()
        }
    }

    private fun handleCancelTimer() {
        timer.cancel()
        saveTimerValue(-1, false)
        stopForeground(true)
        stopSelf()
    }

    private fun handleStartTimer(timeInSeconds: Long) {
        saveTimerValue(timeInSeconds, true)
        createNotification(timeInSeconds)
        startTimer(timeInSeconds)
    }

    private fun getPreferenceValueLong(key: String): Long {
        return getSharedPreferences(baseContext.packageName, Context.MODE_PRIVATE).getLong(key, -1L)
    }

    private fun getPreferenceValueBoolean(key: String): Boolean {
        return getSharedPreferences(baseContext.packageName, Context.MODE_PRIVATE).getBoolean(
            key,
            false
        )
    }

    private fun saveTimerValue(timeInSeconds: Long, isTimerRunning: Boolean) {
        val prefs = baseContext.getSharedPreferences(baseContext.packageName, Context.MODE_PRIVATE)
            ?: return
        prefs.edit().apply {
            putLong(PREF_KEY_TIMER_CREATED, System.currentTimeMillis())
            putLong(PREF_KEY_TIMER_DURATION, timeInSeconds * 1000)
            putBoolean(PREF_KEY_TIMER_RUNNING, isTimerRunning)
            apply()
        }
    }

    private fun startTimer(timeInSeconds: Long) {
        timer = object : CountDownTimer(timeInSeconds * 1000, 1000) {
            override fun onFinish() {
                isTimerRunning = false
                saveTimerValue(-1, false)
                stopForeground(true)
                Intent(this@TimerService, ResultActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(this)
                }
            }

            override fun onTick(millisUntilFinished: Long) {
                isTimerRunning = true
                notificationBuilder.setContentText(
                    "Remaining Time - ${formatTime(
                        millisUntilFinished
                    )}"
                )
                with(NotificationManagerCompat.from(this@TimerService)) {
                    notify(notificationId, notificationBuilder.build())
                }
            }
        }
        timer.start()
    }

    private fun createNotification(timeInSeconds: Long) {
        with(notificationBuilder) {
            setContentText("Remaining Time - ${formatTime(timeInSeconds * 1000)}")
        }
        startForeground(notificationId, notificationBuilder.build())
    }

    private fun formatTime(timeInMs: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMs)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMs % (1000 * 60))
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        Log.d(TAG, "Service Destroyed")
    }
}