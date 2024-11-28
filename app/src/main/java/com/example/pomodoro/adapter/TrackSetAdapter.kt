package com.example.pomodoro.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.data.TrackSetData
import com.example.pomodoro.databinding.ItemTrackSetBinding

class TrackSetAdapter : RecyclerView.Adapter<TrackSetAdapter.TrackSetViewHolder>() {

    private var trackSetList = mutableListOf<TrackSetData>()

    inner class TrackSetViewHolder(private val binding: ItemTrackSetBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(trackSetData: TrackSetData) {
            binding.apply {
                timerSet.text = trackSetData.setNumber
                timerData.text = trackSetData.time
                timerStudyContent.setText(trackSetData.content)

                // EditText의 텍스트 변경 감지
                timerStudyContent.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                    override fun afterTextChanged(p0: Editable?) {
                        trackSetData.content = p0.toString()
                    }
                })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackSetViewHolder {
        val binding = ItemTrackSetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TrackSetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackSetViewHolder, position: Int) {
        holder.bind(trackSetList[position])
    }

    override fun getItemCount(): Int = trackSetList.size

    fun submitList(list: List<TrackSetData>) {
        trackSetList.clear()
        trackSetList.addAll(list)
        notifyDataSetChanged()
    }
}