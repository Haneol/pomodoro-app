package com.example.pomodoro.main.setting

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pomodoro.R
import com.example.pomodoro.adapter.TimerSetAdapter
import com.example.pomodoro.data.TimerItem
import com.example.pomodoro.data.TimerItemState
import com.example.pomodoro.data.TimerSet
import com.example.pomodoro.data.TimerSettings
import com.example.pomodoro.data.dataManager.DataManager
import com.example.pomodoro.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private val dataManager by lazy { DataManager(requireContext()) }
    private var timerSets: List<TimerSet> = emptyList()

    private val timerSettings = TimerSettings()

    private lateinit var timerSetAdapter: TimerSetAdapter

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
        binding.cvTimerSelect.setOnClickListener {
            showTimeSelectDialog()
        }

        binding.cvDataReset.setOnClickListener {
            showResetConfirmationDialog()
        }
    }

    private fun showTimeSelectDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.reset_data))
            .setMessage(getString(R.string.reset_data_confirmation))
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                dataManager.clearAllData()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.reset_data))
            .setMessage(getString(R.string.reset_data_confirmation))
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                dataManager.clearAllData()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}