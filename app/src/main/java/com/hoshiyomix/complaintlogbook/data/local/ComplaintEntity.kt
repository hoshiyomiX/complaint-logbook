package com.hoshiyomix.complaintlogbook.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Status constants for complaint workflow */
object ComplaintStatus {
    const val BELUM_DIKERJAKAN = 0  // Belum Dikerjakan — newly created, not started
    const val SELESAI = 1           // Selesai — completed / resolved
    const val TERTUNDA = 2          // Tertunda — guest requests delay (has scheduledAt)
    const val TIDAK_SELESAI = 3     // Tidak Selesai — cannot be resolved
}

@Entity(
    tableName = "complaints",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["status", "createdAt"])
    ]
)
data class ComplaintEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomNumber: String,
    val category: String,
    val description: String,
    val status: Int = ComplaintStatus.BELUM_DIKERJAKAN,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val scheduledAt: Long? = null  // Only set when status == TERTUNDA
)
