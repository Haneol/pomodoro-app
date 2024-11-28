package com.example.pomodoro.main.track

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.R
import com.example.pomodoro.adapter.TrackSetAdapter
import com.example.pomodoro.data.TimerItem
import com.example.pomodoro.data.TimerItemState
import com.example.pomodoro.data.TimerSet
import com.example.pomodoro.data.TrackSetData
import com.example.pomodoro.data.dataManager.DataManager
import com.example.pomodoro.databinding.FragmentTrackBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TrackFragment : Fragment() {
    private var _binding: FragmentTrackBinding? = null
    private val binding get() = _binding!!
    private val trackSetAdapter = TrackSetAdapter()
    private val dataManager by lazy { DataManager(requireContext()) }

    private var selectedDate: Calendar = Calendar.getInstance()

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
        setupDateButton()  // 날짜 버튼 설정 추가
        loadTestData()
    }

    private fun setupRecyclerView() {
        trackSetAdapter.onContentChanged = { setNumber, newContent ->
            saveContent(setNumber, newContent)
        }

        binding.recyclerView.apply {
            adapter = trackSetAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupDateButton() {
        // 오늘 날짜를 버튼에 표시
        updateDateButton()

        // 날짜 선택 버튼 클릭 리스너
        binding.dateButton.setOnClickListener {
            showDatePickerDialog()
        }

        // 버튼 꾹 누를 시 오늘 날짜로 초기화
        binding.dateButton.setOnLongClickListener {
            resetToToday() // 오늘 날짜로 초기화
            true // 이벤트 소비 표시
        }
    }

    private fun loadTestData() {
        // 테스트용 데이터
        val testData = listOf(
            TrackSetData("1 세트", "00 : 00 : 08"),
            TrackSetData("2 세트", "00 : 00 : 03"),
        )
        trackSetAdapter.submitList(testData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // binding null 처리만 남김
    }

    // 날짜 선택 대화상자 표시
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

        // 오늘 날짜 기준으로 6개월(180일) 캘린더 제한 설정
        val today = Calendar.getInstance()
        datePickerDialog.datePicker.minDate =
            today.timeInMillis - 180L * 24 * 60 * 60 * 1000 // 180일 전
        datePickerDialog.datePicker.maxDate = today.timeInMillis
        datePickerDialog.show()
    }

    // 선택한 날짜를 버튼에 표시
    private fun updateDateButton() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.dateButton.text = dateFormat.format(selectedDate.time)
    }

    // 선택한 날짜에 해당하는 데이터 로드
    private fun loadDataForSelectedDate() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateString = dateFormat.format(selectedDate.time)

        // 해당 날짜의 타이머 세트 데이터 로드
        if (dataManager.hasTimerSets(selectedDateString)) {
            val timerSets = dataManager.loadTimerSets(selectedDateString)
            val trackData = convertTimerSetsToTrackData(timerSets)
            trackSetAdapter.submitList(trackData)
        } else {
            // 데이터가 없는 경우 빈 리스트 표시
            trackSetAdapter.submitList(emptyList())
        }
    }

    private fun convertTimerSetsToTrackData(timerSets: List<TimerSet>): List<TrackSetData> {
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(selectedDate.time)

        return timerSets.map { timerSet ->
            TrackSetData(
                setNumber = "${timerSet.setNumber} 세트",
                time = calculateTotalTime(timerSet.timerItems),
                content = dataManager.loadSetContent(dateString, timerSet.setNumber)
            )
        }
    }

    private fun calculateTotalTime(timerItems: List<TimerItem>): String {
        var totalMinutes = 0
        var totalSeconds = 0

        // COMPLETED 상태인 타이머만 계산
        timerItems.filter { it.state == TimerItemState.COMPLETED }.forEach { item ->
            val timeParts = item.duration.split(":")
            if (timeParts.size == 2) {
                totalMinutes += timeParts[0].toInt()
                totalSeconds += timeParts[1].toInt()
            }
        }

        // 초를 분으로 변환
        totalMinutes += totalSeconds / 60
        totalSeconds %= 60

        // 시간 계산
        val hours = totalMinutes / 60
        totalMinutes %= 60

        return String.format(Locale.ROOT, "%02d : %02d : %02d", hours, totalMinutes, totalSeconds)
    }

    // content 저장 메서드 추가
    private fun saveContent(setNumber: String, content: String) {
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(selectedDate.time)

        // "1 세트" 에서 숫자만 추출
        val setNumberInt = setNumber.split(" ")[0].toInt()

        dataManager.saveSetContent(dateString, setNumberInt, content)
    }


    override fun onResume() {
        super.onResume()
        loadDataForSelectedDate()
    }

    // 오늘 날짜로 초기화
    private fun resetToToday() {
        selectedDate = Calendar.getInstance() // 오늘 날짜로 초기화
        updateDateButton()
        loadDataForSelectedDate()
    }
}
