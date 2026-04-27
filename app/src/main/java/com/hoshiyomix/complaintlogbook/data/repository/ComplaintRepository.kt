package com.hoshiyomix.complaintlogbook.data.repository

import android.util.Log
import com.hoshiyomix.complaintlogbook.data.local.ComplaintDao
import com.hoshiyomix.complaintlogbook.data.local.ComplaintEntity
import com.hoshiyomix.complaintlogbook.data.local.DateMarkerTuple
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

private const val TAG = "ComplaintRepo"

class ComplaintRepository(private val dao: ComplaintDao) {

    fun getAll(): Flow<List<ComplaintEntity>> =
        dao.getAll().catch { e ->
            Log.e(TAG, "Error observing all complaints", e)
            emit(emptyList())
        }

    fun getByDateRange(start: Long, end: Long): Flow<List<ComplaintEntity>> =
        dao.getByDateRange(start, end).catch { e ->
            Log.e(TAG, "Error observing complaints by date range", e)
            emit(emptyList())
        }

    suspend fun getDateMarkers(): List<DateMarkerTuple> {
        return try {
            dao.getDateMarkers()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading date markers", e)
            emptyList()
        }
    }

    suspend fun insert(complaint: ComplaintEntity): Long {
        return try {
            dao.insert(complaint)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting complaint", e)
            -1L
        }
    }

    suspend fun update(complaint: ComplaintEntity) {
        try {
            dao.update(complaint)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating complaint", e)
        }
    }

    suspend fun deleteById(id: Long) {
        try {
            dao.deleteById(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting complaint", e)
        }
    }
}
