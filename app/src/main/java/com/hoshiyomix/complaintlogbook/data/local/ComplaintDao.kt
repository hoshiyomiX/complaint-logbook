package com.hoshiyomix.complaintlogbook.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ComplaintDao {

    @Query("SELECT * FROM complaints ORDER BY isCompleted ASC, createdAt DESC")
    fun getAll(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE createdAt BETWEEN :start AND :end ORDER BY isCompleted ASC, createdAt DESC")
    fun getByDateRange(start: Long, end: Long): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE id = :id")
    suspend fun getById(id: Long): ComplaintEntity?

    @Insert
    suspend fun insert(complaint: ComplaintEntity): Long

    @Update
    suspend fun update(complaint: ComplaintEntity)

    @Delete
    suspend fun delete(complaint: ComplaintEntity)

    @Query("SELECT * FROM complaints ORDER BY createdAt ASC")
    suspend fun getAllSync(): List<ComplaintEntity>

    @Query("SELECT createdAt, isCompleted FROM complaints")
    suspend fun getDateMarkers(): List<DateMarkerTuple>

    @Query("DELETE FROM complaints WHERE id = :id")
    suspend fun deleteById(id: Long)
}
