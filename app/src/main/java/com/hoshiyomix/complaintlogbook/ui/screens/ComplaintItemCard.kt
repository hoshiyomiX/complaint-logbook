package com.hoshiyomix.complaintlogbook.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoshiyomix.complaintlogbook.data.local.ComplaintEntity
import com.hoshiyomix.complaintlogbook.data.local.ComplaintStatus
import java.text.SimpleDateFormat
import java.util.*

/**
 * A card that groups all tasks for the same villa number.
 * Shows villa number on the left, tasks stacked in the center, merged status on the right.
 */
@Composable
fun VillaTaskCard(
    complaints: List<ComplaintEntity>,
    expandedTaskId: Long?,
    onExpand: (Long?) -> Unit,
    onChangeStatus: (ComplaintEntity, Int) -> Unit,
    onDelete: (Long) -> Unit
) {
    val roomNumber = complaints.first().roomNumber

    // Count statuses for merged indicator
    val activeStatusCount = complaints.count {
        it.status != ComplaintStatus.SELESAI && it.status != ComplaintStatus.TIDAK_SELESAI
    }
    val allDone = complaints.all { it.status == ComplaintStatus.SELESAI }
    val allFailed = complaints.all { it.status == ComplaintStatus.TIDAK_SELESAI }

    // Merged status color/icon for the right side
    val mergedStatusColor = when {
        allDone -> Color(0xFF4CAF50)
        allFailed -> Color(0xFFE53935)
        activeStatusCount > 0 -> MaterialTheme.colorScheme.primary
        else -> Color(0xFF757575)
    }
    val mergedStatusIcon = when {
        allDone -> Icons.Default.CheckCircle
        allFailed -> Icons.Default.Cancel
        else -> Icons.Default.List
    }
    val mergedStatusLabel = "${complaints.size} task"

    val cardBg = if (allDone)
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    else MaterialTheme.colorScheme.surface

    Card(
        onClick = {
            // If no task expanded, expand first; if already expanded, collapse
            onExpand(if (expandedTaskId != null && complaints.any { it.id == expandedTaskId }) null else complaints.first().id)
        },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column {
            // ── Main content: Villa | Tasks summary | Status ──
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ── Left: Villa label + number ──
                Column(
                    modifier = Modifier.widthIn(max = 72.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "Villa",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF546E7A)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        roomNumber,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // ── Vertical separator ──
                VerticalDivider(
                    modifier = Modifier
                        .height(48.dp)
                        .padding(horizontal = 10.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )

                // ── Center: Stacked tasks ──
                Column(modifier = Modifier.weight(1f)) {
                    complaints.forEachIndexed { index, complaint ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 6.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        }
                        TaskContent(complaint = complaint)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // ── Right: Merged status indicator ──
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(mergedStatusColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            mergedStatusIcon,
                            contentDescription = mergedStatusLabel,
                            modifier = Modifier.size(20.dp),
                            tint = mergedStatusColor
                        )
                    }
                    Text(
                        mergedStatusLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = mergedStatusColor,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // ── Expandable options for each task ──
            complaints.forEach { complaint ->
                val isExpanded = expandedTaskId == complaint.id

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Show which task this is
                        val (catBg, catFg) = categoryColorFor(complaint.category)
                        val catIcon = categoryIconFor(complaint.category)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = catBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(catIcon, contentDescription = null, modifier = Modifier.size(10.dp), tint = catFg)
                                Text(complaint.category, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = catFg)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            complaint.description,
                            fontSize = 13.sp,
                            color = Color(0xFF37474F),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatusActionChip(
                                icon = Icons.Default.HourglassTop, label = "Belum",
                                color = MaterialTheme.colorScheme.primary,
                                isSelected = complaint.status == ComplaintStatus.BELUM_DIKERJAKAN,
                                onClick = { onChangeStatus(complaint, ComplaintStatus.BELUM_DIKERJAKAN); onExpand(null) }
                            )
                            StatusActionChip(
                                icon = Icons.Default.Schedule, label = "Tunda",
                                color = Color(0xFFFF9800),
                                isSelected = complaint.status == ComplaintStatus.TERTUNDA,
                                onClick = { onChangeStatus(complaint, ComplaintStatus.TERTUNDA); onExpand(null) }
                            )
                            StatusActionChip(
                                icon = Icons.Default.CheckCircle, label = "Selesai",
                                color = Color(0xFF4CAF50),
                                isSelected = complaint.status == ComplaintStatus.SELESAI,
                                onClick = { onChangeStatus(complaint, ComplaintStatus.SELESAI); onExpand(null) }
                            )
                            StatusActionChip(
                                icon = Icons.Default.Cancel, label = "Gagal",
                                color = Color(0xFFE53935),
                                isSelected = complaint.status == ComplaintStatus.TIDAK_SELESAI,
                                onClick = { onChangeStatus(complaint, ComplaintStatus.TIDAK_SELESAI); onExpand(null) }
                            )
                            StatusActionChip(
                                icon = Icons.Default.Delete, label = "Hapus",
                                color = MaterialTheme.colorScheme.error,
                                isSelected = false,
                                onClick = { onDelete(complaint.id); onExpand(null) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskContent(complaint: ComplaintEntity) {
    val (categoryBg, categoryFg) = categoryColorFor(complaint.category)
    val categoryIcon = categoryIconFor(complaint.category)
    val (statusColor, statusIcon, statusLabel) = statusInfoFor(complaint.status)

    val isSelesai = complaint.status == ComplaintStatus.SELESAI
    val isTidakSelesai = complaint.status == ComplaintStatus.TIDAK_SELESAI

    val timeFormat = remember { SimpleDateFormat("HH:mm, dd MMM yyyy", Locale("id", "ID")) }
    val scheduleFormat = remember { SimpleDateFormat("HH:mm, dd MMM", Locale("id", "ID")) }

    // Category badge
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = categoryBg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                categoryIcon, contentDescription = null,
                modifier = Modifier.size(10.dp),
                tint = categoryFg
            )
            Text(
                complaint.category,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = categoryFg
            )
        }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Description
    Text(
        complaint.description,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        color = if (isSelesai || isTidakSelesai) Color(0xFF78909C) else Color(0xFF37474F),
        textDecoration = if (isSelesai) TextDecoration.LineThrough else null
    )

    Spacer(modifier = Modifier.height(40.dp))

    // Time row
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            Icons.Default.Schedule, contentDescription = null,
            modifier = Modifier.size(11.dp),
            tint = Color(0xFF546E7A)
        )
        Text(
            timeFormat.format(Date(complaint.createdAt)),
            fontSize = 11.sp,
            color = Color(0xFF546E7A)
        )

        complaint.completedAt?.let {
            Text("\u2192", fontSize = 11.sp, color = Color(0xFF546E7A))
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

    // Schedule display for Tertunda
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

    // Per-task status badge (compact, inline)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Icon(
            statusIcon, contentDescription = statusLabel,
            modifier = Modifier.size(12.dp),
            tint = statusColor
        )
        Text(
            statusLabel,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = statusColor
        )
    }
}

@Composable
private fun StatusActionChip(
    icon: ImageVector,
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                onClick = onClick,
                indication = ripple(bounded = true, radius = 24.dp),
                interactionSource = remember { MutableInteractionSource() }
            )
            .background(
                if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = if (isSelected) color else Color(0xFF546E7A)
        )
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) color else Color(0xFF546E7A)
        )
    }
}

@Composable
private fun statusInfoFor(status: Int): Triple<Color, ImageVector, String> {
    return when (status) {
        ComplaintStatus.BELUM_DIKERJAKAN -> Triple(
            MaterialTheme.colorScheme.primary,
            Icons.Default.HourglassTop,
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
            "Gagal"
        )
        else -> Triple(
            Color(0xFF757575),
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
        "Floor Drain"      -> Color(0xFFE0F2F1) to Color(0xFF00695C)
        "Curtain"          -> Color(0xFFFCE4EC) to Color(0xFFAD1457)
        "Sofa"             -> Color(0xFFEFEBE9) to Color(0xFF6D4C41)
        "Electrical / MCB" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        "Stairs"           -> Color(0xFFECEFF1) to Color(0xFF455A64)
        "Wall"             -> Color(0xFFF5F5F5) to Color(0xFF757575)
        "Jetspray"         -> Color(0xFFE1F5FE) to Color(0xFF0288D1)
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
        "Floor Drain"      -> Icons.Default.Plumbing
        "Curtain"          -> Icons.Default.Blinds
        "Sofa"             -> Icons.Default.Weekend
        "Electrical / MCB" -> Icons.Default.ElectricalServices
        "Stairs"           -> Icons.Default.Stairs
        "Wall"             -> Icons.Default.ViewInAr
        "Jetspray"         -> Icons.Default.WaterDrop
        else               -> Icons.Default.MoreHoriz
    }
}
