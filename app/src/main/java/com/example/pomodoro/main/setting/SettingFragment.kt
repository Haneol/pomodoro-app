package com.example.pomodoro.main.setting

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.pomodoro.R
import com.example.pomodoro.adapter.TimerSetAdapter
import com.example.pomodoro.data.TimerItem
import com.example.pomodoro.data.TimerItemState
import com.example.pomodoro.data.TimerSet
import com.example.pomodoro.data.TimerSettings
import com.example.pomodoro.data.dataManager.DataManager
import com.example.pomodoro.databinding.FragmentSettingBinding
import com.example.pomodoro.service.TimerService

class SettingFragment : Fragment() {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    private val dataManager by lazy { DataManager(requireContext()) }

    private var timerService: TimerService? = null
    private var bound = false

    // Service Connection
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.LocalBinder
            timerService = binder.getService()
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindTimerService()
    }

    private fun bindTimerService() {
        Intent(requireContext(), TimerService::class.java).also { intent ->
            requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

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
        updateTimerDisplay()
    }

    private fun setupViews() {
        // 뷰 설정 및 이벤트 처리 코드
        binding.cvTimerSelect.setOnClickListener {
            showTimerSettingDialog()
        }

        binding.cvDataReset.setOnClickListener {
            showResetConfirmationDialog()
        }
    }

    private fun showTimerSettingDialog() {
        val currentSettings = dataManager.loadTimerSettings()
        val currentSeconds = (currentSettings.defaultDuration / 1000).toInt()
        val currentMinutes = currentSeconds / 60
        val currentRemainingSeconds = currentSeconds % 60

        // 분과 초를 선택할 수 있는 레이아웃 생성
        val dialogView = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(32, 16, 32, 16)
        }

        // 분 선택 NumberPicker
        val minutePicker = NumberPicker(requireContext()).apply {
            minValue = 0
            maxValue = 59
            value = currentMinutes
        }

        // 분 레이블
        val minuteLabel = TextView(requireContext()).apply {
            text = "분"
            setPadding(8, 0, 16, 0)
        }

        // 초 선택 NumberPicker
        val secondPicker = NumberPicker(requireContext()).apply {
            minValue = 0
            maxValue = 59
            value = currentRemainingSeconds
        }

        // 초 레이블
        val secondLabel = TextView(requireContext()).apply {
            text = "초"
            setPadding(8, 0, 0, 0)
        }

        // 레이아웃에 뷰들 추가
        dialogView.addView(minutePicker)
        dialogView.addView(minuteLabel)
        dialogView.addView(secondPicker)
        dialogView.addView(secondLabel)

        AlertDialog.Builder(requireContext())
            .setTitle("타이머 시간 설정")
            .setView(dialogView)
            .setPositiveButton("확인") { _, _ ->
                val totalSeconds = (minutePicker.value * 60 + secondPicker.value)
                val newDuration = totalSeconds * 1000L
                val newSettings = TimerSettings(newDuration)

                dataManager.saveTimerSettings(newSettings)

                val currentSets = dataManager.loadTimerSets()

                val updatedSets = currentSets.map { set ->
                    set.copy(
                        timerItems = set.timerItems.map { item ->
                            if (item.isActive) {
                                item.copy(
                                    duration = newSettings.getFormattedTime(),
                                    currentTimeLeft = newSettings.getFormattedTime(),
                                    state = TimerItemState.IN_PROGRESS
                                )
                            } else {
                                item
                            }
                        }
                    )
                }
                dataManager.saveTimerSets(updatedSets)

                timerService?.resetTimer()

                updateTimerDisplay()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateTimerDisplay() {
        val settings = dataManager.loadTimerSettings()
        val totalSeconds = (settings.defaultDuration / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        binding.tvTimerDuration.text = if (seconds == 0) {
            "${minutes}분"
        } else {
            "${minutes}분 ${seconds}초"
        }
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

    override fun onStop() {
        super.onStop()
        if (bound) {
            requireContext().unbindService(connection)
            bound = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (bound) {
            requireContext().unbindService(connection)
            bound = false
        }
        _binding = null
    }
}