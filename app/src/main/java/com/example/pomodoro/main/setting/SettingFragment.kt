package com.example.pomodoro.main.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pomodoro.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 여기서 뷰 초기화 및 이벤트 처리
        setupViews()
    }

    private fun setupViews() {
        // 뷰 설정 및 이벤트 처리 코드
        binding.textView3.setOnClickListener {
            // 클릭 이벤트 처리
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}