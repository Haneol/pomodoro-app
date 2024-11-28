package com.example.pomodoro.adapter

import android.content.res.Resources
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.R
import com.example.pomodoro.data.TimerItem
import com.example.pomodoro.data.TimerItemState
import com.example.pomodoro.databinding.ItemTimerBinding
import com.example.pomodoro.service.TimerState
import com.google.android.material.shape.ShapeAppearanceModel

class TimerItemAdapter : RecyclerView.Adapter<TimerItemAdapter.ItemViewHolder>() {
    private var items = listOf<TimerItem>()
    private var timerServiceState: TimerState = TimerState.IDLE

    companion object {
        private const val PAYLOAD_TIME_UPDATE = "PAYLOAD_TIME_UPDATE"
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty() && payloads[0] == PAYLOAD_TIME_UPDATE) {
            // 시간만 업데이트
            val item = items[position]
            holder.binding.tvDuration.text = item.currentTimeLeft
        } else {
            // 전체 업데이트
            onBindViewHolder(holder, position)
        }
    }

    class ItemViewHolder(val binding: ItemTimerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemTimerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.binding.apply {
            root.shapeAppearanceModel = ShapeAppearanceModel.builder()
                .setTopLeftCornerSize(if (position == 0) 20f.dpToPx() else 0f)
                .setTopRightCornerSize(if (position == 0) 20f.dpToPx() else 0f)
                .setBottomLeftCornerSize(if (position == items.lastIndex) 8f.dpToPx() else 0f)
                .setBottomRightCornerSize(if (position == items.lastIndex) 8f.dpToPx() else 0f)
                .build()

            // 배경색 설정
            root.setCardBackgroundColor(
                when {
                    item.isActive -> Color.parseColor("#F46F6F")
                    position % 2 == 0 -> Color.parseColor("#E9E8E8")
                    else -> Color.parseColor("#F6F6F6")
                }
            )

            // 텍스트 색상 설정
            val textColor = if (item.isActive) Color.WHITE else Color.parseColor("#333333")
            tvPosition.setTextColor(textColor)
            tvDuration.setTextColor(if (item.isActive) Color.WHITE else Color.parseColor("#777777"))

            // 텍스트 설정
            tvPosition.text = "${item.position}."
            tvDuration.text = if (item.isActive) item.currentTimeLeft else item.duration

            // 아이콘 설정
            iconArea.apply {
                when {
                    item.state == TimerItemState.COMPLETED -> {
                        setImageResource(R.drawable.ic_check)
                        setColorFilter(if (item.isActive) Color.WHITE else Color.parseColor("#333333"))
                        visibility = android.view.View.VISIBLE
                    }
                    item.isActive && item.state == TimerItemState.IN_PROGRESS -> {
                        setImageResource(
                            if (timerServiceState == TimerState.RUNNING) R.drawable.ic_pause
                            else R.drawable.ic_play
                        )
                        setColorFilter(Color.WHITE)
                        visibility = android.view.View.VISIBLE
                    }
                    else -> {
                        visibility = android.view.View.GONE
                    }
                }
            }
        }
    }

    fun Float.dpToPx() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

    override fun getItemCount() = items.size

    fun submitList(newItems: List<TimerItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun updateTimerState(state: TimerState) {
        if (timerServiceState != state) {
            timerServiceState = state
            val activeIndex = items.indexOfFirst { it.isActive }
            if (activeIndex != -1) {
                notifyItemChanged(activeIndex)
            }
        }
    }

    fun updateCurrentTime(timeString: String) {
        val activeItem = items.find { it.isActive }
        activeItem?.let {
            it.currentTimeLeft = timeString
            val position = items.indexOf(it)
            notifyItemChanged(position, PAYLOAD_TIME_UPDATE)
        }
    }
}