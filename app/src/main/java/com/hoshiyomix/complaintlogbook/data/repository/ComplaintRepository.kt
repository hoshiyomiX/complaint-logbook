package com.hoshiyomix.complaintlogbook.data.repository

import com.hoshiyomix.complaintlogbook.data.local.ComplaintDao
import com.hoshiyomix.complaintlogbook.data.local.ComplaintEntity
import com.hoshiyomix.complaintlogbook.data.local.DateMarkerTuple
import kotlinx.coroutines.flow.Flow

class ComplaintRepository(private val dao: ComplaintDao) {

    fun getAll(): Flow<List<ComplaintEntity>> = dao.getAll()

    fun getByDateRange(start: Long, end: Long): Flow<List<ComplaintEntity>> =
        dao.getByDateRange(start, end)

    suspend fun getDateMarkers(): List<DateMarkerTuple> = dao.getDateMarkers()

    suspend fun insert(complaint: ComplaintEntity): Long = dao.insert(complaint)

    suspend fun update(complaint: ComplaintEntity) = dao.update(complaint)

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}
