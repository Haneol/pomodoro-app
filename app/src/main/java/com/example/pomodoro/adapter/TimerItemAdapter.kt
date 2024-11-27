package com.example.pomodoro.adapter

import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.data.TimerItem
import com.example.pomodoro.databinding.ItemTimerBinding
import com.google.android.material.shape.ShapeAppearanceModel

class TimerItemAdapter : RecyclerView.Adapter<TimerItemAdapter.ItemViewHolder>() {
    private var items = listOf<TimerItem>()

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
                .setTopLeftCornerSize(if (position == 0) 8f.dpToPx() else 0f)
                .setTopRightCornerSize(if (position == 0) 8f.dpToPx() else 0f)
                .setBottomLeftCornerSize(if (position == items.lastIndex) 8f.dpToPx() else 0f)
                .setBottomRightCornerSize(if (position == items.lastIndex) 8f.dpToPx() else 0f)
                .build()

            // 나머지 코드는 동일
            root.setCardBackgroundColor(
                when {
                    item.isActive -> Color.parseColor("#F46F6F")
                    position % 2 == 0 -> Color.parseColor("#E9E8E8")
                    else -> Color.parseColor("#F6F6F6")
                }
            )

            tvPosition.setTextColor(if (item.isActive) Color.WHITE else Color.parseColor("#666666"))
            tvDuration.setTextColor(if (item.isActive) Color.WHITE else Color.parseColor("#333333"))
            tvPosition.text = "${item.position}."
            tvDuration.text = item.duration
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
}