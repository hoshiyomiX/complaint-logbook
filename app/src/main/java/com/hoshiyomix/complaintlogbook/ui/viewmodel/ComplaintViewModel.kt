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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class PeriodView { DAY, WEEK, MONTH, YEAR }

enum class StatusFilter { ALL, BELUM_DIKERJAKAN, TERTUNDA, TIDAK_SELESAI, SELESAI }

data class DateMarkerInfo(
    val belumDikerjakanCount: Int = 0,
    val tertundaCount: Int = 0,
    val selesaiCount: Int = 0,
    val tidakSelesaiCount: Int = 0
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
            StatusFilter.BELUM_DIKERJAKAN -> complaints.filter { it.status == ComplaintStatus.BELUM_DIKERJAKAN }
            StatusFilter.TERTUNDA -> complaints.filter { it.status == ComplaintStatus.TERTUNDA }
            StatusFilter.TIDAK_SELESAI -> complaints.filter { it.status == ComplaintStatus.TIDAK_SELESAI }
            StatusFilter.SELESAI -> complaints.filter { it.status == ComplaintStatus.SELESAI }
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

    val belumDikerjakanCount: Int get() = complaints.count { it.status == ComplaintStatus.BELUM_DIKERJAKAN }
    val tertundaCount: Int get() = complaints.count { it.status == ComplaintStatus.TERTUNDA }
    val tidakSelesaiCount: Int get() = complaints.count { it.status == ComplaintStatus.TIDAK_SELESAI }
    val selesaiCount: Int get() = complaints.count { it.status == ComplaintStatus.SELESAI }

    /** Group filtered complaints by date section headers depending on period view.
     *  DAY → no grouping (single key = periodLabel)
     *  WEEK → group by day ("Senin, 21 Apr")
     *  MONTH → group by week number ("Minggu 1", "Minggu 2", …)
     *  YEAR → group by month ("Januari", "Februari", …)
     */
    val groupedComplaints: LinkedHashMap<String, List<ComplaintEntity>>
        get() {
            val result = LinkedHashMap<String, List<ComplaintEntity>>()
            val items = filteredComplaints
            if (items.isEmpty()) return result

            when (periodView) {
                PeriodView.DAY -> {
                    result[periodLabel] = items
                }
                PeriodView.WEEK -> {
                    val sdf = SimpleDateFormat("EEEE, dd MMM", Locale("id", "ID"))
                    items.groupBy { sdf.format(Date(it.createdAt)) }
                        .forEach { (key, list) -> result[key] = list }
                }
                PeriodView.MONTH -> {
                    val sdf = SimpleDateFormat("dd MMM", Locale("id", "ID"))
                    val cal = Calendar.getInstance()
                    items.groupBy {
                        cal.timeInMillis = it.createdAt
                        val weekOfMonth = cal.get(Calendar.WEEK_OF_MONTH)
                        "Minggu $weekOfMonth"
                    }.forEach { (key, list) -> result[key] = list }
                }
                PeriodView.YEAR -> {
                    val sdf = SimpleDateFormat("MMMM", Locale("id", "ID"))
                    items.groupBy { sdf.format(Date(it.createdAt)) }
                        .forEach { (key, list) -> result[key] = list }
                }
            }
            return result
        }

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

    private var listCollectionJob: Job? = null

    init {
        loadDateMarkers()
        refreshList()
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
                    ComplaintStatus.BELUM_DIKERJAKAN -> info.copy(belumDikerjakanCount = info.belumDikerjakanCount + 1)
                    ComplaintStatus.TERTUNDA -> info.copy(tertundaCount = info.tertundaCount + 1)
                    ComplaintStatus.SELESAI -> info.copy(selesaiCount = info.selesaiCount + 1)
                    ComplaintStatus.TIDAK_SELESAI -> info.copy(tidakSelesaiCount = info.tidakSelesaiCount + 1)
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

    fun goToday() {
        val today = Calendar.getInstance()
        _state.update { it.copy(selectedDate = today) }
        loadDateMarkers()
        refreshList()
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
            showSnackbar("Task berhasil ditambahkan")
        }
    }

    /** Update status — for Tertunda, pass scheduledAt millis; otherwise null */
    fun updateStatus(complaint: ComplaintEntity, newStatus: Int, scheduledAt: Long? = null) {
        viewModelScope.launch {
            val updated = complaint.copy(
                status = newStatus,
                completedAt = if (newStatus == ComplaintStatus.SELESAI) System.currentTimeMillis() else null,
                scheduledAt = if (newStatus == ComplaintStatus.TERTUNDA) scheduledAt else null
            )
            repository.update(updated)
            loadDateMarkers()
            refreshList()
            showSnackbar(
                when (newStatus) {
                    ComplaintStatus.BELUM_DIKERJAKAN -> "Task dikembalikan ke Belum Dikerjakan"
                    ComplaintStatus.TERTUNDA -> "Task ditandai Tertunda"
                    ComplaintStatus.SELESAI -> "Task ditandai Selesai"
                    ComplaintStatus.TIDAK_SELESAI -> "Task ditandai Tidak Selesai"
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
            showSnackbar("Task berhasil dihapus")
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
        // Cancel previous collection to avoid overlapping Flow collectors
        listCollectionJob?.cancel()
        val (start, end) = _state.value.getPeriodRange()
        listCollectionJob = viewModelScope.launch {
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
