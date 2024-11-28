package com.example.pomodoro.data.dataManager

import android.content.Context
import com.example.pomodoro.data.TimerItem
import com.example.pomodoro.data.TimerItemState
import com.example.pomodoro.data.TimerSet
import java.util.Calendar

class DataManager(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("pomodoro_data", Context.MODE_PRIVATE)

    fun saveTimerSets(timerSets: List<TimerSet>, date: String = getTodaysDate()) {
        val editor = sharedPreferences.edit()
        editor.putInt("timer_sets_count_$date", timerSets.size)

        for ((index, timerSet) in timerSets.withIndex()) {
            editor.putInt("set_number_$date.$index", timerSet.setNumber)
            editor.putInt("timer_items_count_$date.$index", timerSet.timerItems.size)

            for ((itemIndex, timerItem) in timerSet.timerItems.withIndex()) {
                editor.putInt("item_position_$date.$index.$itemIndex", timerItem.position)
                editor.putString("item_duration_$date.$index.$itemIndex", timerItem.duration)
                editor.putBoolean("item_is_active_$date.$index.$itemIndex", timerItem.isActive)
                editor.putInt("item_state_$date.$index.$itemIndex", timerItem.state.ordinal)
                editor.putString("item_current_time_$date.$index.$itemIndex", timerItem.currentTimeLeft)
            }
        }

        editor.apply()
    }

    fun loadTimerSets(date: String = getTodaysDate()): List<TimerSet> {
        val timerSets = mutableListOf<TimerSet>()
        val setsCount = sharedPreferences.getInt("timer_sets_count_$date", 0)

        for (i in 0 until setsCount) {
            val setNumber = sharedPreferences.getInt("set_number_$date.$i", 0)
            val itemsCount = sharedPreferences.getInt("timer_items_count_$date.$i", 0)
            val timerItems = mutableListOf<TimerItem>()

            for (j in 0 until itemsCount) {
                val position = sharedPreferences.getInt("item_position_$date.$i.$j", 0)
                val duration = sharedPreferences.getString("item_duration_$date.$i.$j", "25:00") ?: "25:00"
                val isActive = sharedPreferences.getBoolean("item_is_active_$date.$i.$j", false)
                val state = TimerItemState.entries[sharedPreferences.getInt("item_state_$date.$i.$j", 0)]
                val currentTimeLeft = sharedPreferences.getString("item_current_time_$date.$i.$j", duration) ?: duration

                timerItems.add(
                    TimerItem(
                        position = position,
                        duration = duration,
                        isActive = isActive,
                        state = state,
                        currentTimeLeft = currentTimeLeft
                    )
                )
            }

            timerSets.add(TimerSet(setNumber, timerItems))
        }

        return timerSets
    }

    fun hasTimerSets(date: String = getTodaysDate()): Boolean {
        return sharedPreferences.contains("timer_sets_count_$date") && sharedPreferences.getInt("timer_sets_count_$date", 0) > 0
    }

    fun clearAllData() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    private fun getTodaysDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year-$month-$day"
    }
}