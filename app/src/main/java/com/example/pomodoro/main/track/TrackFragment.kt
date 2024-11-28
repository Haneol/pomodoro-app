package com.example.pomodoro.main.track

import android.app.DatePickerDialog
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pomodoro.R
import com.example.pomodoro.databinding.FragmentTrackBinding
import com.example.pomodoro.handler.PermissionCallback
import com.example.pomodoro.handler.TimerPermissionHandler
import com.example.pomodoro.service.TimerService
import com.example.pomodoro.service.TimerState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TrackFragment : Fragment(), PermissionCallback {
    private var _binding: FragmentTrackBinding? = null
    private val binding get() = _binding!!

    private var selectedDate: Calendar = Calendar.getInstance()
    private var timerService: TimerService? = null
    private var bound = false
    private lateinit var permissionHandler: TimerPermissionHandler

    // Service Connection
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as TimerService.LocalBinder
            timerService = binder.getService()
            bound = true
            observeTimer()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            timerService = null
            bound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionHandler = TimerPermissionHandler(this, this)
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

        // 날짜 선택 및 초기화 관련
        updateDateButton()
        binding.dateButton.setOnClickListener { showDatePickerDialog() }
        binding.dateButton.setOnLongClickListener {
            resetToToday()
            true
        }

        // 타이머 관련 설정
        setupViews()
        checkPermissionsAndBindService()
    }

    private fun setupViews() {
        binding.fab.setOnClickListener {
            if (permissionHandler.checkAllPermissions()) {
                handleTimerControl()
            } else {
                permissionHandler.requestPermissions()
            }
        }

        binding.fab.setOnLongClickListener {
            showResetConfirmationDialog()
            true
        }
    }

    private fun checkPermissionsAndBindService() {
        if (permissionHandler.checkAllPermissions()) {
            bindTimerService()
        } else {
            permissionHandler.requestPermissions()
        }
    }

    private fun handleTimerControl() {
        timerService?.let { service ->
            when (service.state.value) {
                TimerState.IDLE -> service.startTimer()
                TimerState.RUNNING -> service.pauseTimer()
                TimerState.PAUSED -> service.startTimer()
                else -> { /* 처리 불필요 */ }
            }
        }
    }

    private fun showResetConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.reset_timer))
            .setMessage(getString(R.string.reset_timer_confirmation))
            .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                timerService?.resetTimer()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.permission_explanation))
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                permissionHandler.openAppSettings()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun observeTimer() {
        timerService?.let { service ->
            service.timeLeft.observe(viewLifecycleOwner) { timeLeft ->
                updateTimerDisplay(timeLeft)
            }

            service.state.observe(viewLifecycleOwner) { state ->
                updateTimerState(state)
            }
        }
    }

    private fun updateTimerDisplay(timeLeftInMillis: Long) {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.textView2.text = String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)
    }

    private fun updateTimerState(state: TimerState) {
        val (icon, contentDesc) = when (state) {
            TimerState.RUNNING -> Pair(R.drawable.ic_pause, getString(R.string.pause))
            else -> Pair(R.drawable.ic_play, getString(R.string.start))
        }
        binding.fab.setImageResource(icon)
        binding.fab.contentDescription = contentDesc
    }

    private fun bindTimerService() {
        Intent(requireContext(), TimerService::class.java).also { intent ->
            requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onPermissionsGranted() {
        bindTimerService()
    }

    override fun onPermissionsDenied(permanentlyDenied: Boolean) {
        if (permanentlyDenied) {
            showPermissionExplanationDialog()
        }
    }

    override fun onStart() {
        super.onStart()
        if (!bound) {
            checkPermissionsAndBindService()
        }
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
        _binding = null
    }

    // 날짜 관련 메서드
    private fun showDatePickerDialog() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.CustomDatePickerTheme,
            { _, pickedYear, pickedMonth, pickedDay ->
                selectedDate.set(pickedYear, pickedMonth, pickedDay)
                updateDateButton()
                loadDataForSelectedDate()
            },
            year,
            month,
            day
        )

        val today = Calendar.getInstance()
        datePickerDialog.datePicker.minDate = today.timeInMillis - 180L * 24 * 60 * 60 * 1000
        datePickerDialog.datePicker.maxDate = today.timeInMillis
        datePickerDialog.show()
    }

    private fun updateDateButton() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.dateButton.text = dateFormat.format(selectedDate.time)
    }

    private fun loadDataForSelectedDate() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateString = dateFormat.format(selectedDate.time)
        // 데이터 표시: 추가 구현 필요
    }

    private fun resetToToday() {
        selectedDate = Calendar.getInstance()
        updateDateButton()
        loadDataForSelectedDate()
    }
}
