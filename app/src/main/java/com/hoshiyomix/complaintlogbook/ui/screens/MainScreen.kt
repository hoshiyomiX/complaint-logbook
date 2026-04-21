package com.hoshiyomix.complaintlogbook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.hoshiyomix.complaintlogbook.ui.components.CalendarGrid
import com.hoshiyomix.complaintlogbook.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val viewModel: ComplaintViewModel = viewModel(factory = ComplaintViewModel.Factory)
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val selectedTab = remember { mutableIntStateOf(0) }

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
        ) {
            // Tab Row — IMPL-001
            val tabs = listOf("Kalender" to Icons.Default.CalendarMonth, "Daftar" to Icons.Default.List)
            TabRow(
                selectedTabIndex = selectedTab.intValue,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            ) {
                tabs.forEachIndexed { index, (title, icon) ->
                    Tab(
                        selected = selectedTab.intValue == index,
                        onClick = { selectedTab.intValue = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(icon, contentDescription = title, modifier = Modifier.size(18.dp))
                                Text(title, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            }
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Tab Content — IMPL-001
            when (selectedTab.intValue) {
                0 -> CalendarTabContent(state, viewModel)
                1 -> TaskListTabContent(state, viewModel)
            }
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

// IMPL-001: Calendar tab — period view, nav bar, calendar, stats
@Composable
private fun CalendarTabContent(state: UiState, viewModel: ComplaintViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Period View Tabs
        PeriodViewTabs(
            selected = state.periodView,
            onSelect = viewModel::setPeriodView
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation bar
        PeriodNavBar(
            label = state.periodLabel,
            onPrev = { viewModel.navigatePeriod(-1) },
            onNext = { viewModel.navigatePeriod(1) },
            onToday = viewModel::goToday
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Calendar
        CalendarGrid(
            calendarMonth = state.calendarMonth,
            selectedDate = state.selectedDate,
            dateMarkers = state.dateMarkers,
            onDaySelected = viewModel::selectDate,
            onMonthChanged = viewModel::setCalendarMonth
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Stats
        StatsRow(
            total = state.complaints.size,
            active = state.activeCount,
            completed = state.completedCount
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}

// IMPL-001: Task list tab — status filter, complaint count, complaint list
@Composable
private fun TaskListTabContent(state: UiState, viewModel: ComplaintViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Period summary label
        PeriodNavBar(
            label = state.periodLabel,
            onPrev = { viewModel.navigatePeriod(-1) },
            onNext = { viewModel.navigatePeriod(1) },
            onToday = viewModel::goToday
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Stats
        StatsRow(
            total = state.complaints.size,
            active = state.activeCount,
            completed = state.completedCount
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Status Filter
        StatusFilterTabs(
            selected = state.statusFilter,
            onSelect = viewModel::setStatusFilter
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Complaint count label
        Text(
            "${state.filteredComplaints.size} komplain" +
                if (state.statusFilter != StatusFilter.ALL)
                    " (${if (state.statusFilter == StatusFilter.ACTIVE) "aktif" else "selesai"})"
                else "",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Complaint list
        if (state.filteredComplaints.isEmpty()) {
            EmptyState(statusFilter = state.statusFilter)
        } else {
            state.filteredComplaints.forEach { complaint ->
                ComplaintItemCard(
                    complaint = complaint,
                    onToggleComplete = { viewModel.toggleComplete(complaint) },
                    onDelete = { viewModel.requestDelete(complaint.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
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

@Composable
private fun PeriodNavBar(label: String, onPrev: () -> Unit, onNext: () -> Unit, onToday: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrev, modifier = Modifier.size(36.dp)) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Prev")
        }
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = onToday) {
            Text("Hari Ini", fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
        IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next")
        }
    }
}

@Composable
private fun StatsRow(total: Int, active: Int, completed: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(modifier = Modifier.weight(1f), value = total, label = "Total", color = MaterialTheme.colorScheme.onSurface)
        StatCard(modifier = Modifier.weight(1f), value = active, label = "Aktif", color = MaterialTheme.colorScheme.primary)
        StatCard(modifier = Modifier.weight(1f), value = completed, label = "Selesai", color = Color(0xFF4CAF50))
    }
}

@Composable
private fun StatCard(modifier: Modifier, value: Int, label: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value.toString(), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun StatusFilterTabs(selected: StatusFilter, onSelect: (StatusFilter) -> Unit) {
    val labels = mapOf(
        StatusFilter.ALL to "Semua",
        StatusFilter.ACTIVE to "Aktif",
        StatusFilter.COMPLETED to "Selesai"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StatusFilter.entries.forEach { filter ->
            Button(
                onClick = { onSelect(filter) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (filter == selected)
                        MaterialTheme.colorScheme.onSurface
                    else Color.Transparent,
                    contentColor = if (filter == selected)
                        MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 6.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(labels[filter] ?: "", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun EmptyState(statusFilter: StatusFilter) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                if (statusFilter == StatusFilter.ACTIVE) Icons.Default.CheckCircle
                else Icons.Default.Build,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (statusFilter == StatusFilter.ACTIVE) "Semua komplain sudah selesai!"
                else "Tidak ada komplain di periode ini",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
