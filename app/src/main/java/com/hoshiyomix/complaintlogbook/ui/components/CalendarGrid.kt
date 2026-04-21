package com.hoshiyomix.complaintlogbook.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarGrid(
    calendarMonth: Calendar,
    selectedDate: Calendar,
    dateMarkers: Map<String, IntArray>,
    onDaySelected: (Calendar) -> Unit,
    onMonthChanged: (Int) -> Unit
) {
    val today = remember { Calendar.getInstance() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ── Month header with navigation ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChanged(-1) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Bulan sebelumnya"
                    )
                }
                Text(
                    text = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
                        .format(calendarMonth.time)
                        .replaceFirstChar { it.uppercase() },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { onMonthChanged(1) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Bulan berikutnya"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Weekday headers — Monday-first ──
            val dayLabels = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
            Row(modifier = Modifier.fillMaxWidth()) {
                dayLabels.forEach { label ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Day cells ──
            val days = getCalendarDays(calendarMonth)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val weeks = days.chunked(7)

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                weeks.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        week.forEach { day ->
                            val dateStr = sdf.format(day.time)
                            val markers = dateMarkers[dateStr]
                            val hasActive = markers != null && markers[0] > 0
                            val hasCompleted = markers != null && markers[1] > 0
                            val isSelected = sameDay(day, selectedDate)
                            val isToday = sameDay(day, today)
                            val isCurrentMonth = sameMonth(day, calendarMonth)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(CircleShape)
                                    .then(
                                        // Today ring indicator
                                        if (isToday && !isSelected) {
                                            Modifier.border(
                                                width = 1.5.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            )
                                        } else Modifier
                                    )
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            else -> Color.Transparent
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable { onDaySelected(day) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // Day number
                                    Text(
                                        text = "${day[Calendar.DAY_OF_MONTH]}",
                                        fontSize = 13.sp,
                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isToday -> MaterialTheme.colorScheme.primary
                                            isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                        },
                                        textAlign = TextAlign.Center
                                    )

                                    // Marker dots
                                    if (hasActive || hasCompleted) {
                                        Spacer(modifier = Modifier.height(1.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (hasActive) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isSelected) Color.White
                                                            else MaterialTheme.colorScheme.primary
                                                        )
                                                )
                                            }
                                            if (hasCompleted) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isSelected) Color(0xFFFFCC80)
                                                            else Color(0xFF4CAF50)
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Legend ──
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(dotColor = MaterialTheme.colorScheme.primary, label = "Aktif")
                Spacer(modifier = Modifier.width(20.dp))
                LegendItem(dotColor = Color(0xFF4CAF50), label = "Selesai")
                Spacer(modifier = Modifier.width(20.dp))
                LegendRing(color = MaterialTheme.colorScheme.primary, label = "Hari ini")
            }
        }
    }
}

@Composable
private fun LegendItem(dotColor: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun LegendRing(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .border(width = 1.5.dp, color = color, shape = CircleShape)
        )
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
    }
}

private fun getCalendarDays(month: Calendar): List<Calendar> {
    val days = mutableListOf<Calendar>()
    val cal = month.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)

    // Offset to Monday start
    val dayOfWeek = cal[Calendar.DAY_OF_WEEK]
    val offset = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
    cal.add(Calendar.DAY_OF_MONTH, -offset)

    // Calculate the number of rows needed (5 or 6)
    val firstDayOfWeek = (month.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val daysInMonth = firstDayOfWeek.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOffset = if (firstDayOfWeek[Calendar.DAY_OF_WEEK] == Calendar.SUNDAY) 6
                        else firstDayOfWeek[Calendar.DAY_OF_WEEK] - 2
    val totalCells = firstDayOffset + daysInMonth
    val rows = if (totalCells > 35) 6 else 5
    val cellCount = rows * 7

    for (i in 0 until cellCount) {
        days.add((cal.clone() as Calendar))
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return days
}

private fun sameDay(a: Calendar, b: Calendar): Boolean {
    return a[Calendar.YEAR] == b[Calendar.YEAR] &&
            a[Calendar.MONTH] == b[Calendar.MONTH] &&
            a[Calendar.DAY_OF_MONTH] == b[Calendar.DAY_OF_MONTH]
}

private fun sameMonth(a: Calendar, b: Calendar): Boolean {
    return a[Calendar.YEAR] == b[Calendar.YEAR] &&
            a[Calendar.MONTH] == b[Calendar.MONTH]
}
