package com.hoshiyomix.complaintlogbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoshiyomix.complaintlogbook.data.local.ComplaintEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ComplaintItemCard(
    complaint: ComplaintEntity,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when (complaint.category) {
        "AC" -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
        "Lampu" -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        "Kebersihan" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "Air" -> Color(0xFFE1F5FE) to Color(0xFF0277BD)
        "TV / WiFi" -> Color(0xFFF3E5F5) to Color(0xFF7B1FA2)
        else -> Color(0xFFF5F5F5) to Color(0xFF616161)
    }

    val categoryIcon = when (complaint.category) {
        "AC" -> Icons.Default.AcUnit
        "Lampu" -> Icons.Default.Lightbulb
        "Kebersihan" -> Icons.Default.CleaningServices
        "Air" -> Icons.Default.WaterDrop
        "TV / WiFi" -> Icons.Default.Tv
        else -> Icons.Default.MoreHoriz
    }

    val timeFormat = SimpleDateFormat("HH:mm, dd MMM yyyy", Locale("id", "ID"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (complaint.isCompleted)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Checkbox
            Checkbox(
                checked = complaint.isCompleted,
                onCheckedChange = { onToggleComplete() },
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

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
                            "Kmr ${complaint.roomNumber}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Category badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryColor.first
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                categoryIcon, contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = categoryColor.second
                            )
                            Text(
                                complaint.category,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = categoryColor.second
                            )
                        }
                    }

                    if (complaint.isCompleted) {
                        Icon(
                            Icons.Default.CheckCircle, contentDescription = "Selesai",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF4CAF50)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    complaint.description,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = if (complaint.isCompleted)
                        MaterialTheme.colorScheme.outline
                    else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (complaint.isCompleted) TextDecoration.LineThrough else null
                )

                Spacer(modifier = Modifier.height(4.dp))

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
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    complaint.completedAt?.let {
                        Text("\u2192", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        Icon(
                            Icons.Default.CheckCircle, contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Text(
                            timeFormat.format(Date(it)),
                            fontSize = 10.sp,
                            color = Color(0xFF4CAF50)
                        )
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
