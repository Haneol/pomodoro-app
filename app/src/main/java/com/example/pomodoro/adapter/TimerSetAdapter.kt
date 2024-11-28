package com.example.pomodoro.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.data.TimerItem
import com.example.pomodoro.data.TimerItemState
import com.example.pomodoro.data.TimerSet
import com.example.pomodoro.databinding.ItemAddButtonBinding
import com.example.pomodoro.databinding.ItemTimerSetBinding
import com.example.pomodoro.service.TimerState

class TimerSetAdapter(
    private val onAddButtonClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var sets = mutableListOf<TimerSet>()
    private val timerItemAdapters = mutableMapOf<Int, TimerItemAdapter>()
    private var timerServiceState: TimerState = TimerState.IDLE

    companion object {
        private const val TYPE_SET = 0
        private const val TYPE_ADD_BUTTON = 1
    }

    class SetViewHolder(val binding: ItemTimerSetBinding) : RecyclerView.ViewHolder(binding.root)
    class AddButtonViewHolder(val binding: ItemAddButtonBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) TYPE_ADD_BUTTON else TYPE_SET
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SET -> {
                val binding = ItemTimerSetBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SetViewHolder(binding)
            }
            else -> {
                val binding = ItemAddButtonBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                AddButtonViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SetViewHolder -> {
                val set = sets[position]
                holder.binding.apply {
                    tvSetNumber.text = "${set.setNumber}세트"
                    rvTimerItems.apply {
                        layoutManager = LinearLayoutManager(context)
                        // 기존 어댑터가 있으면 재사용, 없으면 새로 생성
                        val adapter = timerItemAdapters.getOrPut(position) {
                            TimerItemAdapter().apply {
                                updateTimerState(timerServiceState)
                            }
                        }
                        this.adapter = adapter
                        adapter.submitList(set.timerItems)
                    }
                }
            }
            is AddButtonViewHolder -> {
                val shouldEnable = isAllTimersCompleted()

                holder.binding.btnAddSet.apply {
                    this.isEnabled = shouldEnable

                    backgroundTintList = ColorStateList.valueOf(
                        if (shouldEnable) Color.parseColor("#F46F6F")
                        else Color.parseColor("#E9E8E8")
                    )
                    setTextColor(
                        if (shouldEnable) Color.WHITE
                        else Color.parseColor("#AAAAAA")
                    )

                    setOnClickListener {
                        if (shouldEnable) {
                            onAddButtonClick()
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount() = sets.size + 1  // 세트 개수 + 추가 버튼

    fun submitList(newSets: List<TimerSet>) {
        sets = newSets.toMutableList()
        // 어댑터 맵 초기화
        timerItemAdapters.clear()
        notifyDataSetChanged()
    }

    fun addSet(newSet: TimerSet) {
        // 기존의 모든 타이머의 active 상태를 false로 변경
        sets.forEach { set ->
            set.timerItems.forEach { item ->
                item.isActive = false
            }
        }

        // 새로운 세트 추가 시 첫 번째 아이템을 활성화 상태로
        newSet.timerItems[0].apply {
            isActive = true
            state = TimerItemState.IN_PROGRESS
        }

        sets.add(newSet)
        notifyDataSetChanged() // 전체 업데이트 필요 (기존 아이템의 상태도 변경되므로)
    }

    fun updateTimerState(state: TimerState) {
        timerServiceState = state
        // 활성화된 세트의 어댑터만 업데이트
        for (setIndex in sets.indices) {
            if (sets[setIndex].timerItems.any { it.isActive }) {
                timerItemAdapters[setIndex]?.updateTimerState(state)
                break
            }
        }
    }

    fun isAllTimersCompleted(): Boolean {
        return sets.all { set ->
            set.timerItems.all { item ->
                item.state == TimerItemState.COMPLETED
            }
        }
    }

    fun updateCurrentTime(timeString: String) {
        // 활성화된 세트의 어댑터만 업데이트
        for (setIndex in sets.indices) {
            if (sets[setIndex].timerItems.any { it.isActive }) {
                timerItemAdapters[setIndex]?.updateCurrentTime(timeString)
                break
            }
        }
    }

    fun moveToNextTimer(): Boolean {
        // 현재 활성화된 타이머 찾기
        for (setIndex in sets.indices) {
            val set = sets[setIndex]
            val activeItemIndex = set.timerItems.indexOfFirst { it.isActive }

            if (activeItemIndex != -1) {
                // 현재 활성화된 타이머를 완료 상태로 변경
                set.timerItems[activeItemIndex].apply {
                    isActive = false
                    state = TimerItemState.COMPLETED
                }

                // 다음 타이머 찾기
                val nextItemIndex = activeItemIndex + 1
                if (nextItemIndex < set.timerItems.size) {
                    // 같은 세트의 다음 타이머
                    set.timerItems[nextItemIndex].apply {
                        isActive = true
                        state = TimerItemState.IN_PROGRESS
                    }
                    notifyItemChanged(setIndex)
                    return true
                } else if (setIndex + 1 < sets.size) {
                    // 다음 세트의 첫 번째 타이머
                    sets[setIndex + 1].timerItems[0].apply {
                        isActive = true
                        state = TimerItemState.IN_PROGRESS
                    }
                    notifyDataSetChanged()
                    return true
                }

                // 모든 타이머가 완료된 경우
                notifyItemChanged(setIndex)
                notifyItemChanged(itemCount - 1)
                return false
            }
        }
        return false
    }
}