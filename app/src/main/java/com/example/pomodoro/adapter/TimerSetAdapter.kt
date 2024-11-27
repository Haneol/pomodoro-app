package com.example.pomodoro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.data.TimerSet
import com.example.pomodoro.databinding.ItemTimerSetBinding

class TimerSetAdapter : RecyclerView.Adapter<TimerSetAdapter.SetViewHolder>() {
    private var sets = listOf<TimerSet>()

    class SetViewHolder(val binding: ItemTimerSetBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val binding = ItemTimerSetBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
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

    override fun getItemCount() = sets.size

    fun submitList(newSets: List<TimerSet>) {
        sets = newSets
        notifyDataSetChanged()
    }
}