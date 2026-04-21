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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hoshiyomix.complaintlogbook.data.local.ComplaintStatus
import com.hoshiyomix.complaintlogbook.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val viewModel: ComplaintViewModel = viewModel(factory = ComplaintViewModel.Factory)
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }

    // Show snackbar when message changes
    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    // Delete confirmation dialog
    if (state.confirmDeleteId != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text("Hapus Komplain") },
            text = { Text("Apakah Anda yakin ingin menghapus komplain ini? Tindakan ini tidak dapat dibatalkan.") },
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

    // Date Picker Dialog — IMPL-004
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDate.timeInMillis
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Complaint Logbook",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Logbook Komplain Tamu",
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
                Icon(Icons.Default.Add, contentDescription = "Tambah komplain")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Period Navigation Bar with tap-to-show DatePicker ── IMPL-001
            PeriodNavBar(
                label = state.periodLabel,
                onToday = viewModel::goToday,
                onDateTap = { showDatePicker = true }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Period View Tabs ──
            PeriodViewTabs(
                selected = state.periodView,
                onSelect = viewModel::setPeriodView
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Stats Counters — 5 counters, tap-to-filter ── IMPL-004
            StatsRow(
                totalCount = state.complaints.size,
                activeCount = state.activeCount,
                pendingCount = state.pendingCount,
                notCompletedCount = state.notCompletedCount,
                completedCount = state.completedCount,
                selectedFilter = state.statusFilter,
                onFilterTap = viewModel::setStatusFilter
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Complaint count label ──
            Text(
                "${state.filteredComplaints.size} komplain" +
                    when (state.statusFilter) {
                        StatusFilter.ACTIVE -> " (aktif)"
                        StatusFilter.PENDING -> " (tertunda)"
                        StatusFilter.NOT_COMPLETED -> " (tidak selesai)"
                        StatusFilter.COMPLETED -> " (selesai)"
                        StatusFilter.ALL -> ""
                    },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Complaint List or Empty State ── IMPL-004 (centered empty state)
            if (state.filteredComplaints.isEmpty()) {
                EmptyState(statusFilter = state.statusFilter)
            } else {
                state.filteredComplaints.forEach { complaint ->
                    ComplaintItemCard(
                        complaint = complaint,
                        onCycleStatus = { viewModel.cycleStatus(complaint) },
                        onDelete = { viewModel.requestDelete(complaint.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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

// ── Period Navigation Bar — tappable date + prominent "Hari Ini" ──
@Composable
private fun PeriodNavBar(
    label: String,
    onToday: () -> Unit,
    onDateTap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Row 1: Tappable date label
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDateTap() }
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CalendarToday,
                contentDescription = "Pilih tanggal",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Row 2: Prominent "Hari Ini" reset button — IMPL-002
        OutlinedButton(
            onClick = onToday,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                contentColor = MaterialTheme.colorScheme.primary
            ),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            Icon(
                Icons.Default.Today,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Hari Ini",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
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
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(12.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PeriodView.entries.forEach { view ->
            Button(
                onClick = { onSelect(view) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (view == selected)
                        MaterialTheme.colorScheme.primary
                    else Color.Transparent,
                    contentColor = if (view == selected)
                        MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.outline
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

// ── Stats Row — 5 counters, tappable to filter ── IMPL-004
@Composable
private fun StatsRow(
    totalCount: Int,
    activeCount: Int,
    pendingCount: Int,
    notCompletedCount: Int,
    completedCount: Int,
    selectedFilter: StatusFilter,
    onFilterTap: (StatusFilter) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Total, Aktif, Tertunda
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterableStatCard(
                modifier = Modifier.weight(1f),
                value = totalCount,
                label = "Total",
                color = MaterialTheme.colorScheme.onSurface,
                isSelected = selectedFilter == StatusFilter.ALL,
                onTap = { onFilterTap(StatusFilter.ALL) }
            )
            FilterableStatCard(
                modifier = Modifier.weight(1f),
                value = activeCount,
                label = "Aktif",
                color = MaterialTheme.colorScheme.primary,
                isSelected = selectedFilter == StatusFilter.ACTIVE,
                onTap = { onFilterTap(StatusFilter.ACTIVE) }
            )
            FilterableStatCard(
                modifier = Modifier.weight(1f),
                value = pendingCount,
                label = "Tertunda",
                color = Color(0xFFFF9800),
                isSelected = selectedFilter == StatusFilter.PENDING,
                onTap = { onFilterTap(StatusFilter.PENDING) }
            )
        }
        // Row 2: Tidak Selesai, Selesai
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterableStatCard(
                modifier = Modifier.weight(1f),
                value = notCompletedCount,
                label = "Tidak Selesai",
                color = Color(0xFFE53935),
                isSelected = selectedFilter == StatusFilter.NOT_COMPLETED,
                onTap = { onFilterTap(StatusFilter.NOT_COMPLETED) }
            )
            FilterableStatCard(
                modifier = Modifier.weight(1f),
                value = completedCount,
                label = "Selesai",
                color = Color(0xFF4CAF50),
                isSelected = selectedFilter == StatusFilter.COMPLETED,
                onTap = { onFilterTap(StatusFilter.COMPLETED) }
            )
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
            containerColor = if (isSelected)
                color.copy(alpha = 0.12f)
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder() else null
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) color else color
            )
            Text(
                label,
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun EmptyState(statusFilter: StatusFilter) {
    // IMPL-004: Centered empty state
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                when (statusFilter) {
                    StatusFilter.ACTIVE -> Icons.Default.CheckCircle
                    StatusFilter.PENDING -> Icons.Default.Schedule
                    StatusFilter.NOT_COMPLETED -> Icons.Default.AssignmentTurnedIn
                    StatusFilter.COMPLETED -> Icons.Default.Build
                    StatusFilter.ALL -> Icons.Default.Inbox
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                when (statusFilter) {
                    StatusFilter.ACTIVE -> "Semua komplain sudah ditangani!"
                    StatusFilter.PENDING -> "Tidak ada komplain tertunda"
                    StatusFilter.NOT_COMPLETED -> "Semua komplain sudah selesai!"
                    StatusFilter.COMPLETED -> "Belum ada komplain yang selesai"
                    StatusFilter.ALL -> "Tidak ada komplain di periode ini"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}
