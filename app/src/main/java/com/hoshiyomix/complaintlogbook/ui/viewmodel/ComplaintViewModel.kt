package com.hoshiyomix.complaintlogbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hoshiyomix.complaintlogbook.ComplaintApplication
import com.hoshiyomix.complaintlogbook.data.local.ComplaintEntity
import com.hoshiyomix.complaintlogbook.data.local.DateMarkerTuple
import com.hoshiyomix.complaintlogbook.data.repository.ComplaintRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class PeriodView { DAY, WEEK, MONTH, YEAR }
enum class StatusFilter { ALL, ACTIVE, COMPLETED }

data class UiState(
    val complaints: List<ComplaintEntity> = emptyList(),
    val dateMarkers: Map<String, IntArray> = emptyMap(),
    val selectedDate: Calendar = Calendar.getInstance(),
    val calendarMonth: Calendar = Calendar.getInstance(),
    val periodView: PeriodView = PeriodView.DAY,
    val statusFilter: StatusFilter = StatusFilter.ALL,
    val isAddSheetOpen: Boolean = false,
    val snackbarMessage: String? = null,
    val confirmDeleteId: Long? = null
) {
    val filteredComplaints: List<ComplaintEntity>
        get() = when (statusFilter) {
            StatusFilter.ACTIVE -> complaints.filter { !it.isCompleted }
            StatusFilter.COMPLETED -> complaints.filter { it.isCompleted }
            StatusFilter.ALL -> complaints
        }

    val periodLabel: String
        get() {
            val sdf = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
            val sdfDay = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            return when (periodView) {
                PeriodView.DAY -> sdfDay.format(selectedDate.time)
                PeriodView.WEEK -> {
                    val start = (selectedDate.clone() as Calendar).apply {
                        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    }
                    val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 6) }
                    val fmt = SimpleDateFormat("dd MMM", Locale("id", "ID"))
                    "${fmt.format(start.time)} \u2014 ${fmt.format(end.time)} ${end[Calendar.YEAR]}"
                }
                PeriodView.MONTH -> sdf.format(selectedDate.time)
                PeriodView.YEAR -> "${selectedDate[Calendar.YEAR]}"
            }
        }

    val activeCount: Int get() = complaints.count { !it.isCompleted }
    val completedCount: Int get() = complaints.count { it.isCompleted }

    fun getPeriodRange(): Pair<Long, Long> {
        val cal = selectedDate.clone() as Calendar
        return when (periodView) {
            PeriodView.DAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
                Pair(start, cal.timeInMillis)
            }
            PeriodView.WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_MONTH, 7)
                cal.add(Calendar.MILLISECOND, -1)
                Pair(start, cal.timeInMillis)
            }
            PeriodView.MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
                Pair(start, cal.timeInMillis)
            }
            PeriodView.YEAR -> {
                cal.set(Calendar.MONTH, Calendar.JANUARY)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.set(Calendar.MONTH, Calendar.DECEMBER)
                cal.set(Calendar.DAY_OF_MONTH, 31)
                cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
                Pair(start, cal.timeInMillis)
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ComplaintViewModel(private val repository: ComplaintRepository) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        loadDateMarkers()
    }

    private fun loadDateMarkers() {
        viewModelScope.launch {
            val markers = repository.getDateMarkers()
            val map = mutableMapOf<String, IntArray>()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            for (m in markers) {
                val key = sdf.format(Date(m.createdAt))
                val arr = map.getOrPut(key) { intArrayOf(0, 0) }
                if (m.isCompleted) arr[1]++ else arr[0]++
            }
            _state.update { it.copy(dateMarkers = map) }
        }
    }

    fun selectDate(date: Calendar) {
        val cal = date.clone() as Calendar
        _state.update {
            it.copy(
                selectedDate = cal,
                calendarMonth = (cal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
            )
        }
        refreshList()
    }

    fun setPeriodView(view: PeriodView) {
        _state.update { it.copy(periodView = view) }
        refreshList()
    }

    fun setStatusFilter(filter: StatusFilter) {
        _state.update { it.copy(statusFilter = filter) }
    }

    fun setCalendarMonth(delta: Int) {
        val cal = _state.value.calendarMonth.clone() as Calendar
        cal.add(Calendar.MONTH, delta)
        _state.update { it.copy(calendarMonth = cal) }
    }

    fun navigatePeriod(delta: Int) {
        val cal = _state.value.selectedDate.clone() as Calendar
        when (_state.value.periodView) {
            PeriodView.DAY -> cal.add(Calendar.DAY_OF_MONTH, delta)
            PeriodView.WEEK -> cal.add(Calendar.WEEK_OF_YEAR, delta)
            PeriodView.MONTH -> cal.add(Calendar.MONTH, delta)
            PeriodView.YEAR -> cal.add(Calendar.YEAR, delta)
        }
        selectDate(cal)
    }

    fun goToday() {
        selectDate(Calendar.getInstance())
    }

    fun toggleAddSheet(open: Boolean) {
        _state.update { it.copy(isAddSheetOpen = open) }
    }

    fun addComplaint(roomNumber: String, category: String, description: String) {
        viewModelScope.launch {
            repository.insert(
                ComplaintEntity(
                    roomNumber = roomNumber,
                    category = category,
                    description = description
                )
            )
            loadDateMarkers()
            refreshList()
            showSnackbar("Komplain berhasil ditambahkan")
        }
    }

    fun toggleComplete(complaint: ComplaintEntity) {
        viewModelScope.launch {
            val updated = complaint.copy(
                isCompleted = !complaint.isCompleted,
                completedAt = if (!complaint.isCompleted) System.currentTimeMillis() else null
            )
            repository.update(updated)
            loadDateMarkers()
            refreshList()
            showSnackbar(
                if (updated.isCompleted) "Komplain ditandai selesai"
                else "Komplain dikembalikan ke aktif"
            )
        }
    }

    fun requestDelete(id: Long) {
        _state.update { it.copy(confirmDeleteId = id) }
    }

    fun dismissDelete() {
        _state.update { it.copy(confirmDeleteId = null) }
    }

    fun confirmDelete() {
        val id = _state.value.confirmDeleteId ?: return
        _state.update { it.copy(confirmDeleteId = null) }
        viewModelScope.launch {
            repository.deleteById(id)
            loadDateMarkers()
            refreshList()
            showSnackbar("Komplain berhasil dihapus")
        }
    }

    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            _state.update { it.copy(snackbarMessage = message) }
            kotlinx.coroutines.delay(2500L)
            _state.update { it.copy(snackbarMessage = null) }
        }
    }

    private fun refreshList() {
        val (start, end) = _state.value.getPeriodRange()
        viewModelScope.launch {
            repository.getByDateRange(start, end)
                .collect { list ->
                    _state.update { it.copy(complaints = list) }
                }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[CreationExtras.Application] as ComplaintApplication
                val repository = application.repository
                ComplaintViewModel(repository)
            }
        }
    }
}
