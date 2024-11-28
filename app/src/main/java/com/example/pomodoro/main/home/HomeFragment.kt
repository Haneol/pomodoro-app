package com.example.pomodoro.main.home

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.R
import com.example.pomodoro.adapter.TimerSetAdapter
import com.example.pomodoro.data.TimerItem
import com.example.pomodoro.data.TimerItemState
import com.example.pomodoro.data.TimerSet
import com.example.pomodoro.databinding.FragmentHomeBinding
import com.example.pomodoro.handler.PermissionCallback
import com.example.pomodoro.handler.TimerPermissionHandler
import com.example.pomodoro.service.TimerService
import com.example.pomodoro.service.TimerState
import java.util.Locale

class HomeFragment : Fragment(), PermissionCallback {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var timerSetAdapter: TimerSetAdapter
    private lateinit var permissionHandler: TimerPermissionHandler

    private var timerService: TimerService? = null
    private var bound = false

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
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupRecyclerView()
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

    private fun setupRecyclerView() {
        timerSetAdapter = TimerSetAdapter(
            onAddButtonClick = {
                val nextSetNumber = (timerSetAdapter.itemCount)
                val newSet = TimerSet(
                    setNumber = nextSetNumber,
                    timerItems = List(4) { position ->
                        TimerItem(
                            position = position + 1,
                            duration = "25:00",
                            isActive = position == 0,
                            state = if (position == 0) TimerItemState.IN_PROGRESS
                            else TimerItemState.NOT_STARTED
                        )
                    }
                )
                timerSetAdapter.addSet(newSet)

                timerService?.resetTimer()
            }
        )

        binding.rvTimerSets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = timerSetAdapter.apply {
                submitList(createInitialData())
            }
        }
    }

    private fun createInitialData(): List<TimerSet> {
        return listOf(
            TimerSet(
                setNumber = 1,
                timerItems = List(4) { position ->
                    TimerItem(
                        position = position + 1,
                        duration = "25:00",
                        isActive = position == 0,  // 첫 번째 아이템만 활성화
                        state = if (position == 0) TimerItemState.IN_PROGRESS else TimerItemState.NOT_STARTED
                    )
                }
            )
        )
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
                Log.d("HomeFragment", "Observed timeLeft: $timeLeft")
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
        val timeString = String.format(Locale.ROOT, "%02d:%02d", minutes, seconds)

        // 현재 활성화된 타이머 아이템 시간 업데이트
        timerSetAdapter.updateCurrentTime(timeString)

        // 타이머가 완료되면 다음 아이템으로 이동
        if (timeLeftInMillis <= 0) {
            // 로그 추가
            Log.d("HomeFragment", "Timer completed: timeLeftInMillis = $timeLeftInMillis")
            moveToNextTimer()
        }
    }

    private fun updateTimerState(state: TimerState) {
        // FAB 상태 업데이트
        binding.fab.isEnabled = !timerSetAdapter.isAllTimersCompleted()

        // 기존 코드
        val (icon, contentDesc) = when (state) {
            TimerState.RUNNING -> Pair(R.drawable.ic_pause, getString(R.string.pause))
            else -> Pair(R.drawable.ic_play, getString(R.string.start))
        }
        binding.fab.setImageResource(icon)
        binding.fab.contentDescription = contentDesc

        timerSetAdapter.updateTimerState(state)
    }

    private fun moveToNextTimer() {
        val hasNextTimer = timerSetAdapter.moveToNextTimer()
        if (!hasNextTimer) {
            // 모든 타이머가 완료된 경우
            timerService?.resetTimer()
        } else {
            // 다음 타이머로 넘어간 경우, 타이머 재시작
            timerService?.let { service ->
                service.resetTimer()
                service.startTimer()
            }
        }
    }

    private fun bindTimerService() {
        Intent(requireContext(), TimerService::class.java).also { intent ->
            requireContext().bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    // PermissionCallback 구현
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
        if (bound) {
            requireContext().unbindService(connection)
            bound = false
        }
        _binding = null
    }
}