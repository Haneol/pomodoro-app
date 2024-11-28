package com.example.pomodoro.data

import java.util.Locale

data class TimerSettings(
    val defaultDuration: Long = 2 * 1000L,
) {
    fun getFormattedTime(): String {
        val seconds = (defaultDuration / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.ROOT, "%02d:%02d", minutes, remainingSeconds)
    }
}
