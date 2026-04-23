package com.hoshiyomix.complaintlogbook.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoshiyomix.complaintlogbook.data.local.ComplaintEntity
import com.hoshiyomix.complaintlogbook.data.local.ComplaintStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ComplaintItemCard(
    complaint: ComplaintEntity,
    onChangeStatus: (newStatus: Int) -> Unit,
    onDelete: () -> Unit
) {
    val (categoryBg, categoryFg) = categoryColorFor(complaint.category)
    val categoryIcon = categoryIconFor(complaint.category)
    val (statusColor, statusIcon, statusLabel) = statusInfoFor(complaint.status)

    val timeFormat = SimpleDateFormat("HH:mm, dd MMM yyyy", Locale("id", "ID"))
    val scheduleFormat = SimpleDateFormat("HH:mm, dd MMM", Locale("id", "ID"))

    val isSelesai = complaint.status == ComplaintStatus.SELESAI
    val isTidakSelesai = complaint.status == ComplaintStatus.TIDAK_SELESAI

    // Status menu state — IMPL-003
    var showStatusMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelesai)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // ── Status button with dropdown menu ── IMPL-003
            Box {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(statusColor.copy(alpha = 0.15f))
                            .clickable { showStatusMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            statusIcon,
                            contentDescription = statusLabel,
                            modifier = Modifier.size(18.dp),
                            tint = statusColor
                        )
                    }
                    Text(
                        statusLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // ── Status dropdown menu ── IMPL-003
                DropdownMenu(
                    expanded = showStatusMenu,
                    onDismissRequest = { showStatusMenu = false }
                ) {
                    StatusMenuItem(
                        icon = Icons.Default.Pending,
                        label = "Belum Dikerjakan",
                        color = MaterialTheme.colorScheme.primary,
                        isSelected = complaint.status == ComplaintStatus.BELUM_DIKERJAKAN,
                        onClick = {
                            onChangeStatus(ComplaintStatus.BELUM_DIKERJAKAN)
                            showStatusMenu = false
                        }
                    )
                    StatusMenuItem(
                        icon = Icons.Default.Schedule,
                        label = "Tertunda",
                        color = Color(0xFFFF9800),
                        isSelected = complaint.status == ComplaintStatus.TERTUNDA,
                        onClick = {
                            onChangeStatus(ComplaintStatus.TERTUNDA)
                            showStatusMenu = false
                        }
                    )
                    StatusMenuItem(
                        icon = Icons.Default.CheckCircle,
                        label = "Selesai",
                        color = Color(0xFF4CAF50),
                        isSelected = complaint.status == ComplaintStatus.SELESAI,
                        onClick = {
                            onChangeStatus(ComplaintStatus.SELESAI)
                            showStatusMenu = false
                        }
                    )
                    StatusMenuItem(
                        icon = Icons.Default.Cancel,
                        label = "Tidak Selesai",
                        color = Color(0xFFE53935),
                        isSelected = complaint.status == ComplaintStatus.TIDAK_SELESAI,
                        onClick = {
                            onChangeStatus(ComplaintStatus.TIDAK_SELESAI)
                            showStatusMenu = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Room badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            "Villa ${complaint.roomNumber}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Category badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                categoryIcon, contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = categoryFg
                            )
                            Text(
                                complaint.category,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = categoryFg
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    complaint.description,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = if (isSelesai || isTidakSelesai)
                        MaterialTheme.colorScheme.outline
                    else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isSelesai) TextDecoration.LineThrough else null
                )

                Spacer(modifier = Modifier.height(4.dp))

                // ── Created time ──
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule, contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        timeFormat.format(Date(complaint.createdAt)),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    complaint.completedAt?.let {
                        Text("\u2192", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Icon(
                            Icons.Default.CheckCircle, contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Text(
                            timeFormat.format(Date(it)),
                            fontSize = 11.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }

                // ── Schedule display for Tertunda ── IMPL-003
                complaint.scheduledAt?.let { scheduled ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFF9800).copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Alarm,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color(0xFFFF9800)
                            )
                            val isOverdue = scheduled <= System.currentTimeMillis()
                            Text(
                                if (isOverdue) "Waktu tunda lewat: ${scheduleFormat.format(Date(scheduled))}"
                                else "Ditunda sampai: ${scheduleFormat.format(Date(scheduled))}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isOverdue) Color(0xFFE53935) else Color(0xFFFF9800)
                            )
                        }
                    }
                }
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete, contentDescription = "Hapus",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun StatusMenuItem(
    icon: ImageVector,
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = color)
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                )
            }
        },
        onClick = onClick,
        trailingIcon = {
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = color)
            }
        }
    )
}

@Composable
private fun statusInfoFor(status: Int): Triple<Color, ImageVector, String> {
    return when (status) {
        ComplaintStatus.BELUM_DIKERJAKAN -> Triple(
            MaterialTheme.colorScheme.primary,
            Icons.Default.Pending,
            "Belum"
        )
        ComplaintStatus.TERTUNDA -> Triple(
            Color(0xFFFF9800),
            Icons.Default.Schedule,
            "Tunda"
        )
        ComplaintStatus.SELESAI -> Triple(
            Color(0xFF4CAF50),
            Icons.Default.CheckCircle,
            "Selesai"
        )
        ComplaintStatus.TIDAK_SELESAI -> Triple(
            Color(0xFFE53935),
            Icons.Default.Cancel,
            "Batal"
        )
        else -> Triple(
            Color.Gray,
            Icons.Default.Info,
            "?"
        )
    }
}

private fun categoryColorFor(category: String): Pair<Color, Color> {
    return when (category) {
        "AC"               -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        "Lampu"            -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        "Air"              -> Color(0xFFE1F5FE) to Color(0xFF0277BD)
        "Shower"           -> Color(0xFFE0F7FA) to Color(0xFF00838F)
        "Toilet"           -> Color(0xFFFCE4EC) to Color(0xFFC62828)
        "Washtafel / Sink" -> Color(0xFFE0F2F1) to Color(0xFF00695C)
        "Exhaust Fan"      -> Color(0xFFF3E5F5) to Color(0xFF6A1B9A)
        "Stove"            -> Color(0xFFFBE9E7) to Color(0xFFBF360C)
        "Fridge"           -> Color(0xFFE8EAF6) to Color(0xFF283593)
        "Door"             -> Color(0xFFEFEBE9) to Color(0xFF4E342E)
        "Chair"            -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        "Table"            -> Color(0xFFF1F8E9) to Color(0xFF33691E)
        "SDB"              -> Color(0xFFECEFF1) to Color(0xFF37474F)
        "Wadrobe"          -> Color(0xFFF9FBE7) to Color(0xFF827717)
        else               -> Color(0xFFF5F5F5) to Color(0xFF616161)
    }
}

private fun categoryIconFor(category: String): ImageVector {
    return when (category) {
        "AC"               -> Icons.Default.AcUnit
        "Lampu"            -> Icons.Default.Lightbulb
        "Air"              -> Icons.Default.WaterDrop
        "Shower"           -> Icons.Default.Bathtub
        "Toilet"           -> Icons.Default.Wc
        "Washtafel / Sink" -> Icons.Default.Plumbing
        "Exhaust Fan"      -> Icons.Default.Air
        "Stove"            -> Icons.Default.LocalFireDepartment
        "Fridge"           -> Icons.Default.Kitchen
        "Door"             -> Icons.Default.DoorFront
        "Chair"            -> Icons.Default.Chair
        "Table"            -> Icons.Default.TableRestaurant
        "SDB"              -> Icons.Default.Lock
        "Wadrobe"          -> Icons.Default.Checkroom
        else               -> Icons.Default.MoreHoriz
    }
}
