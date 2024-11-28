package com.example.pomodoro.data

import java.util.Locale

data class TimerItem(
    val position: Int,
    var duration: String,
    var isActive: Boolean = false,
    var state: TimerItemState = TimerItemState.NOT_STARTED,
    var currentTimeLeft: String = duration
) {
    companion object {
        // 타이머 기본 시간
        const val INIT_TIME = 2 * 1000L
        val INIT_TIME_TO_STR : String
            get() {
                val seconds = (INIT_TIME / 1000).toInt()
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                return String.format(Locale.ROOT, "%02d:%02d", minutes, remainingSeconds)
            }
    }
}

enum class TimerItemState {
    NOT_STARTED,   // 한번도 실행되지 않은 상태
    IN_PROGRESS,   // 현재 실행 중 (재생/일시정지 포함)
    COMPLETED      // 완료된 상태
}
