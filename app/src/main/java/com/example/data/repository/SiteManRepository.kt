package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow

class SiteManRepository(private val database: SiteManDatabase) {
    // Projects
    val allProjects: Flow<List<ProjectEntity>> = database.projectDao().getAllProjects()
    fun getProject(id: Long): Flow<ProjectEntity?> = database.projectDao().getProjectById(id)
    suspend fun insertProject(project: ProjectEntity): Long = database.projectDao().insertProject(project)
    suspend fun updateProject(project: ProjectEntity) = database.projectDao().updateProject(project)
    suspend fun deleteProject(project: ProjectEntity) = database.projectDao().deleteProject(project)

    // Tasks
    val allTasks: Flow<List<TaskEntity>> = database.taskDao().getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = database.taskDao().getPendingTasks()
    fun getTasksByProject(projectId: Long): Flow<List<TaskEntity>> = database.taskDao().getTasksByProject(projectId)
    suspend fun insertTask(task: TaskEntity): Long = database.taskDao().insertTask(task)
    suspend fun updateTask(task: TaskEntity) = database.taskDao().updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = database.taskDao().deleteTask(task)

    // Attendance
    val allAttendance: Flow<List<AttendanceEntity>> = database.attendanceDao().getAllAttendance()
    fun getAttendanceForDate(date: String): Flow<List<AttendanceEntity>> = database.attendanceDao().getAttendanceByDate(date)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long = database.attendanceDao().insertAttendance(attendance)
    suspend fun updateAttendance(attendance: AttendanceEntity) = database.attendanceDao().updateAttendance(attendance)

    // Staff
    val allStaff: Flow<List<StaffEntity>> = database.staffDao().getAllStaff()
    suspend fun insertStaff(staff: StaffEntity): Long = database.staffDao().insertStaff(staff)
    suspend fun updateStaff(staff: StaffEntity) = database.staffDao().updateStaff(staff)
    suspend fun deleteStaff(staff: StaffEntity) = database.staffDao().deleteStaff(staff)

    // Materials
    val allMaterials: Flow<List<MaterialEntity>> = database.materialDao().getAllMaterials()
    val lowStockMaterials: Flow<List<MaterialEntity>> = database.materialDao().getLowStockMaterials()
    suspend fun insertMaterial(material: MaterialEntity): Long = database.materialDao().insertMaterial(material)
    suspend fun updateMaterial(material: MaterialEntity) = database.materialDao().updateMaterial(material)
    suspend fun deleteMaterial(material: MaterialEntity) = database.materialDao().deleteMaterial(material)

    // Expenses
    val allExpenses: Flow<List<ExpenseEntity>> = database.expenseDao().getAllExpenses()
    fun getExpensesForProject(projectId: Long): Flow<List<ExpenseEntity>> = database.expenseDao().getExpensesByProject(projectId)
    val totalExpenseAmount: Flow<Double?> = database.expenseDao().getTotalExpenseAmount()
    suspend fun insertExpense(expense: ExpenseEntity): Long = database.expenseDao().insertExpense(expense)
    suspend fun updateExpense(expense: ExpenseEntity) = database.expenseDao().updateExpense(expense)
    suspend fun deleteExpense(expense: ExpenseEntity) = database.expenseDao().deleteExpense(expense)

    // Scanned Bills (Room persistence)
    val allScannedBills: Flow<List<ScannedBillEntity>> = database.scannedBillDao().getAllScannedBills()
    fun getScannedBillsForProject(projectId: Long): Flow<List<ScannedBillEntity>> = database.scannedBillDao().getScannedBillsByProject(projectId)
    suspend fun insertScannedBill(bill: ScannedBillEntity): Long = database.scannedBillDao().insertScannedBill(bill)
    suspend fun deleteScannedBill(bill: ScannedBillEntity) = database.scannedBillDao().deleteScannedBill(bill)
    suspend fun deleteScannedBillById(id: Long) = database.scannedBillDao().deleteScannedBillById(id)

    // Reports
    val allReports: Flow<List<ReportEntity>> = database.reportDao().getAllReports()
    suspend fun insertReport(report: ReportEntity): Long = database.reportDao().insertReport(report)
    suspend fun deleteReport(report: ReportEntity) = database.reportDao().deleteReport(report)

    // AI Chat
    val chatMessages: Flow<List<AiChatMessageEntity>> = database.aiChatDao().getAllMessages()
    suspend fun insertChatMessage(message: AiChatMessageEntity): Long = database.aiChatDao().insertMessage(message)
    suspend fun clearChatMessages() = database.aiChatDao().clearMessages()

    suspend fun resetDatabase() {
        populateInitialData(database)
    }
}
