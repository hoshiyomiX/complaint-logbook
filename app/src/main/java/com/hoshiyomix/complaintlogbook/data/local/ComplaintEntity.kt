package com.hoshiyomix.complaintlogbook.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Status constants for complaint workflow */
object ComplaintStatus {
    const val ACTIVE = 0      // Aktif — newly reported, being worked on
    const val PENDING = 1     // Tertunda — deferred / waiting for parts
    const val COMPLETED = 2   // Selesai — resolved
}

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomNumber: String,
    val category: String,
    val description: String,
    val status: Int = ComplaintStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
