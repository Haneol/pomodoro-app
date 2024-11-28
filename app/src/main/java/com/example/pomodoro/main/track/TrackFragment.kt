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
import com.example.pomodoro.data.TrackSetData
import com.example.pomodoro.databinding.FragmentTrackBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TrackFragment : Fragment() {
    private var _binding: FragmentTrackBinding? = null
    private val binding get() = _binding!!
    private val trackSetAdapter = TrackSetAdapter()

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
            TrackSetData("1 세트", "02 : 00 : 00"),
            TrackSetData("2 세트", "02 : 00 : 00"),
            TrackSetData("3 세트", "02 : 00 : 00")
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

        // 데이터 표시
        // binding.dataTextView.text = "Data for $selectedDateString: \n[Sample Data]"
    }

    // 오늘 날짜로 초기화
    private fun resetToToday() {
        selectedDate = Calendar.getInstance() // 오늘 날짜로 초기화
        updateDateButton()
        loadDataForSelectedDate()
    }
}
