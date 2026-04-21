package com.hoshiyomix.complaintlogbook.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.hoshiyomix.complaintlogbook.ComplaintApplication
import com.hoshiyomix.complaintlogbook.data.local.ComplaintEntity
import com.hoshiyomix.complaintlogbook.data.local.ComplaintStatus
import com.hoshiyomix.complaintlogbook.data.local.DateMarkerTuple
import com.hoshiyomix.complaintlogbook.data.repository.ComplaintRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class PeriodView { DAY, WEEK, MONTH, YEAR }

enum class StatusFilter { ALL, ACTIVE, PENDING, NOT_COMPLETED, COMPLETED }

data class DateMarkerInfo(
    val activeCount: Int = 0,
    val pendingCount: Int = 0,
    val completedCount: Int = 0
)

data class UiState(
    val complaints: List<ComplaintEntity> = emptyList(),
    val dateMarkers: Map<String, DateMarkerInfo> = emptyMap(),
    val selectedDate: Calendar = Calendar.getInstance(),
    val periodView: PeriodView = PeriodView.DAY,
    val statusFilter: StatusFilter = StatusFilter.ALL,
    val isAddSheetOpen: Boolean = false,
    val snackbarMessage: String? = null,
    val confirmDeleteId: Long? = null
) {
    val filteredComplaints: List<ComplaintEntity>
        get() = when (statusFilter) {
            StatusFilter.ALL -> complaints
            StatusFilter.ACTIVE -> complaints.filter { it.status == ComplaintStatus.ACTIVE }
            StatusFilter.PENDING -> complaints.filter { it.status == ComplaintStatus.PENDING }
            StatusFilter.NOT_COMPLETED -> complaints.filter { it.status != ComplaintStatus.COMPLETED }
            StatusFilter.COMPLETED -> complaints.filter { it.status == ComplaintStatus.COMPLETED }
        }

    val periodLabel: String
        get() {
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
                PeriodView.MONTH -> SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(selectedDate.time)
                PeriodView.YEAR -> "${selectedDate[Calendar.YEAR]}"
            }
        }

    val activeCount: Int get() = complaints.count { it.status == ComplaintStatus.ACTIVE }
    val pendingCount: Int get() = complaints.count { it.status == ComplaintStatus.PENDING }
    val notCompletedCount: Int get() = complaints.count { it.status != ComplaintStatus.COMPLETED }
    val completedCount: Int get() = complaints.count { it.status == ComplaintStatus.COMPLETED }

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
            val map = mutableMapOf<String, DateMarkerInfo>()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            for (m in markers) {
                val key = sdf.format(Date(m.createdAt))
                val info = map.getOrPut(key) { DateMarkerInfo() }
                map[key] = when (m.status) {
                    ComplaintStatus.ACTIVE -> info.copy(activeCount = info.activeCount + 1)
                    ComplaintStatus.PENDING -> info.copy(pendingCount = info.pendingCount + 1)
                    ComplaintStatus.COMPLETED -> info.copy(completedCount = info.completedCount + 1)
                    else -> info
                }
            }
            _state.update { it.copy(dateMarkers = map) }
        }
    }

    fun selectDate(date: Calendar) {
        _state.update { it.copy(selectedDate = date.clone() as Calendar) }
        refreshList()
    }

    fun setPeriodView(view: PeriodView) {
        _state.update { it.copy(periodView = view) }
        refreshList()
    }

    fun setStatusFilter(filter: StatusFilter) {
        _state.update { it.copy(statusFilter = filter) }
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

    fun cycleStatus(complaint: ComplaintEntity) {
        viewModelScope.launch {
            val nextStatus = when (complaint.status) {
                ComplaintStatus.ACTIVE -> ComplaintStatus.PENDING
                ComplaintStatus.PENDING -> ComplaintStatus.COMPLETED
                ComplaintStatus.COMPLETED -> ComplaintStatus.ACTIVE
                else -> ComplaintStatus.ACTIVE
            }
            val updated = complaint.copy(
                status = nextStatus,
                completedAt = if (nextStatus == ComplaintStatus.COMPLETED) System.currentTimeMillis() else null
            )
            repository.update(updated)
            loadDateMarkers()
            refreshList()
            showSnackbar(
                when (nextStatus) {
                    ComplaintStatus.ACTIVE -> "Komplain dikembalikan ke aktif"
                    ComplaintStatus.PENDING -> "Komplain ditandai tertunda"
                    ComplaintStatus.COMPLETED -> "Komplain ditandai selesai"
                    else -> "Status diperbarui"
                }
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
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ComplaintApplication
                val repository = application.repository
                ComplaintViewModel(repository)
            }
        }
    }
}
