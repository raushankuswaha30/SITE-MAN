package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val clientName: String,
    val location: String,
    val budget: Double,
    val spentAmount: Double = 0.0,
    val startDate: String,
    val targetCompletion: String,
    val status: String = "Ongoing", // Planning, Ongoing, Completed, On Hold
    val progressPercent: Int = 15,
    val healthScore: Int = 85,
    val notes: String = "",
    val teamCount: Int = 12,
    val documentsCount: Int = 3,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val projectName: String,
    val title: String,
    val description: String = "",
    val assignedTo: String,
    val dueDate: String,
    val priority: String = "Medium", // Urgent, High, Medium, Low
    val status: String = "To Do", // To Do, In Progress, Review, Completed
    val progress: Int = 0,
    val category: String = "Civil Work",
    val isOverdue: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val staffId: Long,
    val staffName: String,
    val date: String, // YYYY-MM-DD
    val checkInTime: String = "09:00 AM",
    val checkOutTime: String? = null,
    val status: String = "Present", // Present, Absent, Half Day, Overtime, Late
    val locationAddress: String = "Site A - Raipur Sector 4",
    val selfieUri: String? = null,
    val isLate: Boolean = false,
    val isOvertime: Boolean = false,
    val overtimeHours: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "staff")
data class StaffEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val role: String, // Site Supervisor, Mason, Civil Engineer, Electrician, Carpenter, Labor
    val department: String = "Operations",
    val dailySalary: Double = 800.0,
    val emergencyContact: String = "",
    val idCardNumber: String = "",
    val assignedProjectId: Long = 1,
    val assignedProjectName: String = "Green Valley Residency",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long = 1,
    val projectName: String = "Green Valley Residency",
    val name: String,
    val category: String = "Cement & Aggregates", // Steel, Brick & Block, Plumbing, Electrical, Paint
    val currentQuantity: Double,
    val unit: String = "Bags", // Bags, Tons, Sq.ft, Pcs, Liters, Meters
    val minThreshold: Double = 20.0,
    val unitPrice: Double = 380.0,
    val supplierName: String = "UltraTech Supplies Ltd.",
    val supplierPhone: String = "+91 98765 43210",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long = 1,
    val projectName: String = "Green Valley Residency",
    val title: String,
    val category: String = "Materials", // Materials, Labor, Equipment, Fuel, Subcontractor, Utilities, Misc
    val amount: Double,
    val gstAmount: Double = 0.0,
    val vendorName: String = "",
    val invoiceNumber: String = "",
    val date: String, // YYYY-MM-DD
    val receiptUri: String? = null,
    val status: String = "Approved", // Pending, Approved, Rejected
    val approvedBy: String = "Admin",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scanned_bills")
data class ScannedBillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vendorName: String,
    val totalAmount: Double,
    val gstAmount: Double = 0.0,
    val date: String, // YYYY-MM-DD
    val invoiceNumber: String = "",
    val category: String = "Materials",
    val projectId: Long = 1,
    val projectName: String = "Green Valley Apartment",
    val summary: String = "",
    val confidence: Int = 96,
    val imageUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String, // Project, Attendance, Expense, Material, Management Summary
    val generatedDate: String,
    val summaryText: String,
    val recommendationsText: String,
    val totalBudget: Double = 0.0,
    val totalExpense: Double = 0.0,
    val attendanceRate: Int = 94,
    val completedTasks: Int = 12,
    val pendingTasks: Int = 4,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_chat_messages")
data class AiChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "user" or "ai"
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String? = null, // "create_task", "create_project", "check_material", "add_expense"
    val actionPayloadJson: String? = null
)
