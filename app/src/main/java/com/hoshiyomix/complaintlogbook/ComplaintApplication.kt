package com.hoshiyomix.complaintlogbook

import android.app.Application
import com.hoshiyomix.complaintlogbook.data.local.AppDatabase
import com.hoshiyomix.complaintlogbook.data.repository.ComplaintRepository

class ComplaintApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ComplaintRepository(database.complaintDao()) }
}
