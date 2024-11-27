package com.example.pomodoro.data

data class TimerItem(
    val position: Int,
    val duration: String,
    val isActive: Boolean = false
)
