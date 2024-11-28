package com.example.pomodoro.main.track

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.adapter.TrackSetAdapter
import com.example.pomodoro.data.TrackSetData
import com.example.pomodoro.databinding.FragmentTrackBinding

class TrackFragment : Fragment() {

    private var _binding: FragmentTrackBinding? = null
    private val binding get() = _binding!!
    private val trackSetAdapter = TrackSetAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        // 테스트용 데이터 생성
        loadTestData()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = trackSetAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun loadTestData() {
        // 테스트용 데이터
        val testData = listOf(
            TrackSetData("1 세트", "02 : 00 : 00"),
            TrackSetData("2 세트", "02 : 00 : 00"),
            TrackSetData("3 세트", "02 : 00 : 00")
        )
        trackSetAdapter.submitList(testData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}