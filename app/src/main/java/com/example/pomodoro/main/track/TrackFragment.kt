package com.example.pomodoro.main.track

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pomodoro.databinding.FragmentTrackBinding

class TrackFragment : Fragment() {
    private var _binding: FragmentTrackBinding? = null
    private val binding get() = _binding!!
    private var isStart = false
        set(value) {
            field = value
            updateButtonUI()
        }

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
        // 여기서 뷰 초기화 및 이벤트 처리
        setupViews()
    }

    private fun setupViews() {
        // 뷰 설정 및 이벤트 처리 코드
        binding.timerButton2.apply {
            text = "시작"  // 초기 상태
            setOnClickListener {
                isStart = !isStart  // 상태 토글만 하면 UI는 자동으로 업데이트
                handleTimerState()
            }
        }

    }

    private fun updateButtonUI() {
        binding.timerButton2.text = if (isStart) "중지" else "시작"
    }

    private fun handleTimerState() {
        if (isStart) {
            // startTimer()
        } else {
            // stopTimer()
        }
    }

    private fun startTimer() {

    }

    private fun stopTimer() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}