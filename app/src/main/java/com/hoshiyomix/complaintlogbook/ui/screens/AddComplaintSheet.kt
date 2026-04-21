package com.hoshiyomix.complaintlogbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val CATEGORIES = listOf("AC", "Lampu", "Kebersihan", "Air", "TV / WiFi", "Lainnya")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddComplaintSheet(
    onDismiss: () -> Unit,
    onSubmit: (roomNumber: String, category: String, description: String) -> Unit
) {
    var roomNumber by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var roomError by remember { mutableStateOf(false) }
    var descError by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Tambah Komplain Tamu",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Room number
            OutlinedTextField(
                value = roomNumber,
                onValueChange = {
                    roomNumber = it
                    roomError = false
                },
                label = { Text("Nomor Kamar") },
                placeholder = { Text("Contoh: 101") },
                isError = roomError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category label
            Text(
                "Kategori Komplain",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Custom category selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CATEGORIES.forEach { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat, fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    descError = false
                },
                label = { Text("Keterangan Detail") },
                placeholder = { Text("Jelaskan keluhan tamu secara detail...") },
                isError = descError,
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Submit button
            Button(
                onClick = {
                    when {
                        roomNumber.isBlank() -> roomError = true
                        category.isBlank() -> {}
                        description.isBlank() -> descError = true
                        else -> onSubmit(roomNumber.trim(), category, description.trim())
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Komplain", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
