package com.hoshiyomix.complaintlogbook.ui.components

import androidx.compose.foundation.background
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
        Column(modifier = Modifier.padding(12.dp)) {
            // Month header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onMonthChanged(-1) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Prev month")
                }
                Text(
                    text = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(calendarMonth.time),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = { onMonthChanged(1) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Weekday headers — Monday-first to match grid offset logic
            val dayLabels = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
            Row(modifier = Modifier.fillMaxWidth()) {
                dayLabels.forEach { label ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Day cells
            val days = getCalendarDays(calendarMonth)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            Column {
                days.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { day ->
                            val dateStr = sdf.format(day.time)
                            val markers = dateMarkers[dateStr]
                            val hasComplaint = markers != null && (markers[0] + markers[1] > 0)
                            val isSelected = sameDay(day, selectedDate)
                            val isToday = sameDay(day, today)
                            val isCurrentMonth = sameMonth(day, calendarMonth)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isSelected -> MaterialTheme.colorScheme.primary
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable { onDaySelected(day) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${day[Calendar.DAY_OF_MONTH]}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isToday -> MaterialTheme.colorScheme.primary
                                            isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                                            else -> MaterialTheme.colorScheme.outline
                                        }
                                    )

                                    if (hasComplaint) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            if (markers != null && markers[0] > 0) {
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
                                            if (markers != null && markers[1] > 0) {
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

            // Legend
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(16.dp))
                LegendDot(color = MaterialTheme.colorScheme.primary, label = "Aktif")
                Spacer(modifier = Modifier.width(16.dp))
                LegendDot(color = Color(0xFF4CAF50), label = "Selesai")
                Spacer(modifier = Modifier.width(16.dp))
                LegendDot(color = MaterialTheme.colorScheme.outline, label = "Hari ini", isRing = true)
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String, isRing: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .then(
                    if (isRing) Modifier
                        .background(Color.Transparent)
                        .then(
                            Modifier.background(
                                color = Color.Transparent,
                                shape = CircleShape
                            )
                        )
                    else Modifier.background(color)
                )
        )
        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
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

    // 6 rows to cover all cases
    for (i in 0 until 42) {
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
