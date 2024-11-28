package com.example.pomodoro.service

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.pomodoro.MainActivity
import com.example.pomodoro.R
import java.util.Locale
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.util.Log
import com.example.pomodoro.data.TimerItem

class TimerService : Service() {
    private val NOTIFICATION_CHANNEL_ID = "POMODORO_TIMER_CHANNEL"
    private val NOTIFICATION_ID = 1
    private val TIMER_DURATION = TimerItem.INIT_TIME

    private val binder = LocalBinder()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var alarmManager: AlarmManager
    private lateinit var timerPendingIntent: PendingIntent

    private var startTime: Long = 0L
    private var targetTime: Long = 0L
    private var timeLeftInMillis: Long = TIMER_DURATION
    private var timerState = TimerState.IDLE

    // LiveData for UI updates
    private val _timeLeft = MutableLiveData<Long>()
    val timeLeft: LiveData<Long> = _timeLeft

    private val _state = MutableLiveData<TimerState>()
    val state: LiveData<TimerState> = _state

    // Runnable for UI updates
    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            if (timerState == TimerState.RUNNING) {
                updateTimer()
                handler.postDelayed(this, 1000)
            }
        }
    }

    inner class LocalBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onCreate() {
        super.onCreate()
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        createNotificationChannel()
        setupAlarmIntent()

        // 서비스 생성 시 초기 시간 설정
        _timeLeft.postValue(TIMER_DURATION)
        _state.postValue(TimerState.IDLE)
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun setupAlarmIntent() {
        val intent = Intent(this, TimerReceiver::class.java)
        timerPendingIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun startTimer() {
        if (timerState == TimerState.IDLE || timerState == TimerState.PAUSED) {
            // 먼저 서비스를 시작
            startService(Intent(applicationContext, TimerService::class.java))

            if (timerState == TimerState.IDLE) {
                timeLeftInMillis = TIMER_DURATION
            }

            startTime = System.currentTimeMillis()
            targetTime = startTime + timeLeftInMillis

            // Set exact alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetTime,
                    timerPendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    targetTime,
                    timerPendingIntent
                )
            }

            timerState = TimerState.RUNNING
            _state.postValue(timerState)

            // 이제 Foreground로 전환
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    createNotification(),
                    FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NOTIFICATION_ID, createNotification())
            }

            handler.post(updateTimerRunnable)
        }
    }

    fun pauseTimer() {
        if (timerState == TimerState.RUNNING) {
            handler.removeCallbacks(updateTimerRunnable)
            alarmManager.cancel(timerPendingIntent)

            timerState = TimerState.PAUSED
            _state.postValue(timerState)

            updateNotification()
        }
    }

    fun resetTimer() {
        handler.removeCallbacks(updateTimerRunnable)
        alarmManager.cancel(timerPendingIntent)

        timeLeftInMillis = TIMER_DURATION
        timerState = TimerState.IDLE
        _state.postValue(timerState)
        _timeLeft.postValue(timeLeftInMillis)

        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun updateTimer() {
        timeLeftInMillis = targetTime - System.currentTimeMillis()
        if (timeLeftInMillis <= 0) {
            Log.d("TimerService", "Timer finished: timeLeftInMillis = $timeLeftInMillis")
            _timeLeft.postValue(0L)  // 먼저 0을 전달
            handler.postDelayed({  // 약간의 딜레이 후 리셋
                resetTimer()
            }, 100)
        } else {
            _timeLeft.postValue(timeLeftInMillis)
            updateNotification()
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.timer_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.timer_notification_channel_description)
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(formatTime(timeLeftInMillis))
            .setSmallIcon(R.drawable.ic_timer)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun formatTime(millis: Long): String {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}