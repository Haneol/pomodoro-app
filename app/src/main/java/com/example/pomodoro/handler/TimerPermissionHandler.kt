package com.example.pomodoro.handler

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

interface PermissionCallback {
    fun onPermissionsGranted()
    fun onPermissionsDenied(permanentlyDenied: Boolean = false)
}

class TimerPermissionHandler(
    private val fragment: Fragment,
    private val callback: PermissionCallback
) {
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>

    init {
        registerPermissionLauncher()
    }

    private fun registerPermissionLauncher() {
        notificationPermissionLauncher = fragment.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                if (checkAllPermissions()) {
                    callback.onPermissionsGranted()
                }
            } else {
                val permanentlyDenied = !fragment.shouldShowRequestPermissionRationale(
                    Manifest.permission.POST_NOTIFICATIONS
                )
                callback.onPermissionsDenied(permanentlyDenied)
            }
        }
    }

    fun checkAllPermissions(): Boolean {
        val context = fragment.requireContext()

        // 알림 권한 체크 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        // Exact Alarm 권한 체크 (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                return false
            }
        }

        return true
    }

    fun requestPermissions() {
        val context = fragment.requireContext()

        // Android 13+ 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        // Android 12+ Exact Alarm 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                openExactAlarmSettings()
                return
            }
        }

        // 모든 권한이 있는 경우
        callback.onPermissionsGranted()
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", fragment.requireContext().packageName, null)
        }
        fragment.startActivity(intent)
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            fragment.startActivity(intent)
        }
    }
}