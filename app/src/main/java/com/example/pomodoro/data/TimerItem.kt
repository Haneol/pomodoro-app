package com.example.pomodoro.data

data class TimerItem(
    val position: Int,
    var duration: String,
    var isActive: Boolean = false,
    var state: TimerItemState = TimerItemState.NOT_STARTED,
    var currentTimeLeft: String = duration
)

enum class TimerItemState {
    NOT_STARTED,   // 한번도 실행되지 않은 상태
    IN_PROGRESS,   // 현재 실행 중 (재생/일시정지 포함)
    COMPLETED      // 완료된 상태
}
