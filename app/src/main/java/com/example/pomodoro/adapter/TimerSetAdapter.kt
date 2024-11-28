package com.example.pomodoro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.data.TimerSet
import com.example.pomodoro.databinding.ItemAddButtonBinding
import com.example.pomodoro.databinding.ItemTimerSetBinding

class TimerSetAdapter(
    private val onAddButtonClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var sets = mutableListOf<TimerSet>()

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
                        adapter = TimerItemAdapter().apply {
                            submitList(set.timerItems)
                        }
                    }
                }
            }
            is AddButtonViewHolder -> {
                holder.binding.btnAddSet.setOnClickListener {
                    // 여기에 추가 버튼 클릭 시 동작 구현
                    onAddButtonClick()
                }
            }
        }
    }

    override fun getItemCount() = sets.size + 1  // 세트 개수 + 추가 버튼

    fun submitList(newSets: List<TimerSet>) {
        sets = newSets.toMutableList()
        notifyDataSetChanged()
    }

    fun addSet(newSet: TimerSet) {
        sets.add(newSet)
        notifyItemInserted(sets.size - 1)
    }
}