package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectById(id: Long): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectByIdDirect(id: Long): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("SELECT COUNT(*) FROM projects")
    suspend fun getProjectCount(): Int
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY dueDate ASC")
    fun getTasksByProject(projectId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status != 'Completed' ORDER BY dueDate ASC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Long): Flow<TaskEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTaskCount(): Int
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY date DESC, staffName ASC")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY staffName ASC")
    fun getAttendanceByDate(date: String): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<AttendanceEntity>)

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)

    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date AND status = 'Present'")
    suspend fun getPresentCountByDate(date: String): Int
}

@Dao
interface StaffDao {
    @Query("SELECT * FROM staff WHERE isActive = 1 ORDER BY name ASC")
    fun getAllStaff(): Flow<List<StaffEntity>>

    @Query("SELECT * FROM staff WHERE id = :id")
    fun getStaffById(id: Long): Flow<StaffEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffEntity): Long

    @Update
    suspend fun updateStaff(staff: StaffEntity)

    @Delete
    suspend fun deleteStaff(staff: StaffEntity)

    @Query("SELECT COUNT(*) FROM staff WHERE isActive = 1")
    suspend fun getStaffCount(): Int
}

@Dao
interface MaterialDao {
    @Query("SELECT * FROM materials ORDER BY name ASC")
    fun getAllMaterials(): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE currentQuantity <= minThreshold ORDER BY name ASC")
    fun getLowStockMaterials(): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE id = :id")
    fun getMaterialById(id: Long): Flow<MaterialEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MaterialEntity): Long

    @Update
    suspend fun updateMaterial(material: MaterialEntity)

    @Delete
    suspend fun deleteMaterial(material: MaterialEntity)

    @Query("SELECT COUNT(*) FROM materials")
    suspend fun getMaterialCount(): Int
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC, createdAt DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE projectId = :projectId ORDER BY date DESC")
    fun getExpensesByProject(projectId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    fun getExpenseById(id: Long): Flow<ExpenseEntity?>

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenseAmount(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT COUNT(*) FROM expenses")
    suspend fun getExpenseCount(): Int
}

@Dao
interface ScannedBillDao {
    @Query("SELECT * FROM scanned_bills ORDER BY createdAt DESC")
    fun getAllScannedBills(): Flow<List<ScannedBillEntity>>

    @Query("SELECT * FROM scanned_bills WHERE projectId = :projectId ORDER BY createdAt DESC")
    fun getScannedBillsByProject(projectId: Long): Flow<List<ScannedBillEntity>>

    @Query("SELECT * FROM scanned_bills WHERE id = :id")
    fun getScannedBillById(id: Long): Flow<ScannedBillEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScannedBill(bill: ScannedBillEntity): Long

    @Delete
    suspend fun deleteScannedBill(bill: ScannedBillEntity)

    @Query("DELETE FROM scanned_bills WHERE id = :id")
    suspend fun deleteScannedBillById(id: Long)

    @Query("SELECT COUNT(*) FROM scanned_bills")
    suspend fun getScannedBillCount(): Int
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE id = :id")
    fun getReportById(id: Long): Flow<ReportEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Delete
    suspend fun deleteReport(report: ReportEntity)
}

@Dao
interface AiChatDao {
    @Query("SELECT * FROM ai_chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<AiChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AiChatMessageEntity): Long

    @Query("DELETE FROM ai_chat_messages")
    suspend fun clearMessages()
}
