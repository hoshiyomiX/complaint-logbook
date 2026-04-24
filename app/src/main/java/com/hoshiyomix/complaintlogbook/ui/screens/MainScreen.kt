package com.hoshiyomix.complaintlogbook.ui.screens

import java.util.Calendar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoshiyomix.complaintlogbook.data.local.ComplaintEntity
import com.hoshiyomix.complaintlogbook.data.local.ComplaintStatus
import com.hoshiyomix.complaintlogbook.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val viewModel: ComplaintViewModel = viewModel(factory = ComplaintViewModel.Factory)
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    // Schedule dialog state for Tertunda
    var scheduleTarget by remember { mutableStateOf<ComplaintEntity?>(null) }

    // Show snackbar when message changes
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    // Delete confirmation dialog
    if (state.confirmDeleteId != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text("Hapus Task") },
            text = { Text("Apakah Anda yakin ingin menghapus task ini? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDelete) {
                    Text("Batal")
                }
            }
        )
    }

    // Date Picker Dialog — IMPL-007: selectableDates blocks future dates
    if (showDatePicker) {
        // Calculate today's end-of-day as the maximum selectable date
        val todayEndOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= todayEndOfDay
            }
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDate.timeInMillis,
            selectableDates = selectableDates
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val cal = Calendar.getInstance()
                            cal.timeInMillis = millis
                            viewModel.selectDate(cal)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ── Schedule Dialog for Tertunda ── IMPL-002
    if (scheduleTarget != null) {
        ScheduleDialog(
            onDismiss = { scheduleTarget = null },
            onConfirm = { scheduledAtMillis ->
                scheduleTarget?.let { complaint ->
                    viewModel.updateStatus(complaint, ComplaintStatus.TERTUNDA, scheduledAtMillis)
                }
                scheduleTarget = null
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Melasti Dream",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Engineering Tasklist",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleAddSheet(true) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            PeriodNavBar(
                label = state.periodLabel,
                periodView = state.periodView,
                isToday = state.isToday,
                onToday = viewModel::goToday,
                onDateTap = { showDatePicker = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PeriodViewTabs(
                selected = state.periodView,
                onSelect = viewModel::setPeriodView
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Stats Counters — 5 counters ── IMPL-002
            StatsRow(
                totalCount = state.complaints.size,
                belumDikerjakanCount = state.belumDikerjakanCount,
                tertundaCount = state.tertundaCount,
                tidakSelesaiCount = state.tidakSelesaiCount,
                selesaiCount = state.selesaiCount,
                selectedFilter = state.statusFilter,
                onFilterTap = viewModel::setStatusFilter
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Complaint List with section headers or Empty State ── IMPL-003
            if (state.filteredComplaints.isEmpty()) {
                EmptyState(statusFilter = state.statusFilter)
            } else {
                val groups = state.groupedComplaints
                val showHeaders = groups.size > 1
                groups.forEach { (header, complaints) ->
                    if (showHeaders) {
                        DateSectionHeader(
                            label = header,
                            count = complaints.size
                        )
                    }
                    complaints.forEach { complaint ->
                        ComplaintItemCard(
                            complaint = complaint,
                            onChangeStatus = { newStatus ->
                                if (newStatus == ComplaintStatus.TERTUNDA) {
                                    scheduleTarget = complaint
                                } else {
                                    viewModel.updateStatus(complaint, newStatus)
                                }
                            },
                            onDelete = { viewModel.requestDelete(complaint.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Add Complaint Bottom Sheet
    if (state.isAddSheetOpen) {
        AddComplaintSheet(
            onDismiss = { viewModel.toggleAddSheet(false) },
            onSubmit = { room, category, desc ->
                viewModel.addComplaint(room, category, desc)
                viewModel.toggleAddSheet(false)
            }
        )
    }
}

// ── Schedule Dialog for Tertunda ── IMPL-002
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleDialog(
    onDismiss: () -> Unit,
    onConfirm: (scheduledAtMillis: Long) -> Unit
) {
    var mode by remember { mutableStateOf("duration") } // "duration" or "fixed"
    var selectedDuration by remember { mutableIntStateOf(30) } // minutes
    var selectedHour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    val timePickerState = rememberTimePickerState(
        initialHour = selectedHour,
        initialMinute = selectedMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFFF9800))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Atur Waktu Tunda", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                // Mode selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = mode == "duration",
                        onClick = { mode = "duration" },
                        label = { Text("Durasi") }
                    )
                    FilterChip(
                        selected = mode == "fixed",
                        onClick = {
                            mode = "fixed"
                            selectedHour = timePickerState.hour
                            selectedMinute = timePickerState.minute
                        },
                        label = { Text("Jam Tetap") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (mode == "duration") {
                    Text("Tunda selama:", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Duration presets
                    val durations = listOf(15 to "15 mnt", 30 to "30 mnt", 60 to "1 jam", 120 to "2 jam", 240 to "4 jam", 480 to "8 jam")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        durations.forEach { (mins, label) ->
                            FilterChip(
                                selected = selectedDuration == mins,
                                onClick = { selectedDuration = mins },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                } else {
                    Text("Tunda sampai jam:", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(12.dp))
                    TimePicker(state = timePickerState)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val scheduledAt = if (mode == "duration") {
                        System.currentTimeMillis() + (selectedDuration.toLong() * 60_000L)
                    } else {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        cal.set(Calendar.MINUTE, timePickerState.minute)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        // If the time is in the past today, schedule for tomorrow
                        if (cal.timeInMillis <= System.currentTimeMillis()) {
                            cal.add(Calendar.DAY_OF_MONTH, 1)
                        }
                        cal.timeInMillis
                    }
                    onConfirm(scheduledAt)
                }
            ) {
                Text("Tunda")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// ── Period Navigation Bar ── IMPL-008/009/011
@Composable
private fun PeriodNavBar(
    label: String,
    periodView: PeriodView,
    isToday: Boolean,
    onToday: () -> Unit,
    onDateTap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: Period label (enlarged) + Reset button (only when not today)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Period label — enlarged display text
            Text(
                text = label,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // Reset to today — only shown when selected date is NOT today
            if (!isToday) {
                OutlinedButton(
                    onClick = onToday,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Set kembali ke hari ini", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Row 2: Pilih tanggal — full-width pill-shaped card button
        Card(
            onClick = onDateTap,
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = "Pilih tanggal",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Pilih tanggal",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun PeriodViewTabs(selected: PeriodView, onSelect: (PeriodView) -> Unit) {
    val labels = mapOf(
        PeriodView.DAY to "Harian",
        PeriodView.WEEK to "Mingguan",
        PeriodView.MONTH to "Bulanan",
        PeriodView.YEAR to "Tahunan"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PeriodView.entries.forEach { view ->
            Button(
                onClick = { onSelect(view) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (view == selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (view == selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(labels[view] ?: "", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ── Stats Row — Total (left, 2-row span) + 2x2 status grid (right) ── IMPL-010
@Composable
private fun StatsRow(
    totalCount: Int,
    belumDikerjakanCount: Int,
    tertundaCount: Int,
    tidakSelesaiCount: Int,
    selesaiCount: Int,
    selectedFilter: StatusFilter,
    onFilterTap: (StatusFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Left: Total — spans 2 rows vertically
        FilterableStatCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            value = totalCount, label = "Total",
            color = MaterialTheme.colorScheme.onSurface,
            isSelected = selectedFilter == StatusFilter.ALL,
            onTap = { onFilterTap(StatusFilter.ALL) }
        )

        // Right: 2x2 grid of status cards
        Column(
            modifier = Modifier.weight(2f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterableStatCard(Modifier.weight(1f), belumDikerjakanCount, "Belum",
                    MaterialTheme.colorScheme.primary, selectedFilter == StatusFilter.BELUM_DIKERJAKAN,
                    { onFilterTap(StatusFilter.BELUM_DIKERJAKAN) })
                FilterableStatCard(Modifier.weight(1f), tertundaCount, "Tertunda",
                    Color(0xFFFF9800), selectedFilter == StatusFilter.TERTUNDA,
                    { onFilterTap(StatusFilter.TERTUNDA) })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterableStatCard(Modifier.weight(1f), selesaiCount, "Selesai",
                    Color(0xFF4CAF50), selectedFilter == StatusFilter.SELESAI,
                    { onFilterTap(StatusFilter.SELESAI) })
                FilterableStatCard(Modifier.weight(1f), tidakSelesaiCount, "Tidak Selesai",
                    Color(0xFFE53935), selectedFilter == StatusFilter.TIDAK_SELESAI,
                    { onFilterTap(StatusFilter.TIDAK_SELESAI) })
            }
        }
    }
}

@Composable
private fun FilterableStatCard(
    modifier: Modifier = Modifier,
    value: Int,
    label: String,
    color: Color,
    isSelected: Boolean,
    onTap: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onTap() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder() else null
    ) {
        if (label == "Total") {
            // Total card — centered vertical layout (number on top, label below)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(value.toString(), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
                Spacer(modifier = Modifier.height(4.dp))
                Text(label, fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface)
            }
        } else {
            // Status cards — number top-left, label bottom-right
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 64.dp)
                    .padding(10.dp)
            ) {
                // Number — top-left
                Text(
                    value.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.align(Alignment.TopStart)
                )
                // Label — bottom-right
                Text(
                    label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) color else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }
}

// ── Date Section Header for grouped lists ── IMPL-003
@Composable
private fun DateSectionHeader(label: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = "$count",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun EmptyState(statusFilter: StatusFilter) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                when (statusFilter) {
                    StatusFilter.BELUM_DIKERJAKAN -> Icons.Default.HourglassTop
                    StatusFilter.TERTUNDA -> Icons.Default.Schedule
                    StatusFilter.TIDAK_SELESAI -> Icons.Default.Cancel
                    StatusFilter.SELESAI -> Icons.Default.CheckCircle
                    StatusFilter.ALL -> Icons.Default.Inbox
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                when (statusFilter) {
                    StatusFilter.BELUM_DIKERJAKAN -> "Tidak ada task yang belum dikerjakan"
                    StatusFilter.TERTUNDA -> "Tidak ada task tertunda"
                    StatusFilter.TIDAK_SELESAI -> "Tidak ada task yang tidak selesai"
                    StatusFilter.SELESAI -> "Belum ada task yang selesai"
                    StatusFilter.ALL -> "Tidak ada task di periode ini"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}
