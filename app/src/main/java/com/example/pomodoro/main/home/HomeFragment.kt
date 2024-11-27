package com.example.pomodoro.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.adapter.TimerSetAdapter
import com.example.pomodoro.data.TimerItem
import com.example.pomodoro.data.TimerSet
import com.example.pomodoro.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 여기서 뷰 초기화 및 이벤트 처리
        setupViews()
        setupRecyclerView()
    }

    private fun setupViews() {
        // 뷰 설정 및 이벤트 처리 코드


    }


    private fun setupRecyclerView() {
        binding.rvTimerSets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = TimerSetAdapter().apply {
                submitList(createSampleData())
            }
        }
    }

    private fun createSampleData() = listOf(
        TimerSet(1, List(4) { TimerItem(it + 1, "00:00") }),
        TimerSet(2, List(4) { TimerItem(it + 1, "25:00") }),
        TimerSet(3, List(4) { TimerItem(it + 1, "25:00") })
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}