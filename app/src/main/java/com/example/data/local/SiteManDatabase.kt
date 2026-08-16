package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProjectEntity::class,
        TaskEntity::class,
        AttendanceEntity::class,
        StaffEntity::class,
        MaterialEntity::class,
        ExpenseEntity::class,
        ScannedBillEntity::class,
        ReportEntity::class,
        AiChatMessageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SiteManDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun staffDao(): StaffDao
    abstract fun materialDao(): MaterialDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun scannedBillDao(): ScannedBillDao
    abstract fun reportDao(): ReportDao
    abstract fun aiChatDao(): AiChatDao

    companion object {
        @Volatile
        private var INSTANCE: SiteManDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SiteManDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SiteManDatabase::class.java,
                    "siteman_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }
    }
}

suspend fun populateInitialData(db: SiteManDatabase) {
    val projectDao = db.projectDao()
    val taskDao = db.taskDao()
    val staffDao = db.staffDao()
    val attendanceDao = db.attendanceDao()
    val materialDao = db.materialDao()
    val expenseDao = db.expenseDao()
    val scannedBillDao = db.scannedBillDao()
    val reportDao = db.reportDao()
    val aiChatDao = db.aiChatDao()

    // 1. Projects
    val p1 = ProjectEntity(
        id = 1,
        name = "Green Valley Apartment",
        clientName = "ABC Construction & Realty",
        location = "Raipur, Sector 4",
        budget = 2500000.0,
        spentAmount = 845000.0,
        startDate = "2026-09-01",
        targetCompletion = "2027-05-01",
        status = "Ongoing",
        progressPercent = 38,
        healthScore = 88,
        notes = "Foundation and 2nd floor column reinforcement in progress.",
        teamCount = 18,
        documentsCount = 6
    )
    val p2 = ProjectEntity(
        id = 2,
        name = "Apex Commercial Complex",
        clientName = "Apex Mega Retailers",
        location = "Bhilai East Industrial Area",
        budget = 6500000.0,
        spentAmount = 5200000.0,
        startDate = "2026-03-15",
        targetCompletion = "2026-11-30",
        status = "Ongoing",
        progressPercent = 78,
        healthScore = 76,
        notes = "Electrical & HVAC ducting work phase.",
        teamCount = 24,
        documentsCount = 12
    )
    val p3 = ProjectEntity(
        id = 3,
        name = "Skyline Luxury Villa #4",
        clientName = "Dr. S. Verma",
        location = "Naya Raipur Lakeview",
        budget = 1200000.0,
        spentAmount = 310000.0,
        startDate = "2026-07-10",
        targetCompletion = "2027-01-20",
        status = "Planning",
        progressPercent = 15,
        healthScore = 95,
        notes = "Architectural blueprint approved. Excavation scheduled.",
        teamCount = 8,
        documentsCount = 4
    )
    projectDao.insertProject(p1)
    projectDao.insertProject(p2)
    projectDao.insertProject(p3)

    // 2. Staff
    val s1 = StaffEntity(1, "Ramesh Kumar", "+91 98271 23456", "ramesh@siteman.com", "Site Supervisor", "Civil", 1200.0, "+91 94252 11223", "ID-SUP-101", 1, "Green Valley Apartment")
    val s2 = StaffEntity(2, "Suresh Yadav", "+91 97130 98765", "suresh.y@siteman.com", "Head Mason", "Masonry", 950.0, "+91 98261 44556", "ID-MAS-204", 1, "Green Valley Apartment")
    val s3 = StaffEntity(3, "Rahul Sharma", "+91 99812 34567", "rahul.s@siteman.com", "Civil Engineer", "Engineering", 1500.0, "+91 91310 99887", "ID-ENG-045", 2, "Apex Commercial Complex")
    val s4 = StaffEntity(4, "Amit Patel", "+91 94060 12345", "amit.p@siteman.com", "Electrician Lead", "Electrical", 900.0, "+91 98263 77889", "ID-ELE-302", 2, "Apex Commercial Complex")
    val s5 = StaffEntity(5, "Vikram Singh", "+91 96172 67890", "vikram@siteman.com", "Safety Officer", "HSE", 1100.0, "+91 99264 55667", "ID-SAF-012", 1, "Green Valley Apartment")
    staffDao.insertStaff(s1)
    staffDao.insertStaff(s2)
    staffDao.insertStaff(s3)
    staffDao.insertStaff(s4)
    staffDao.insertStaff(s5)

    // 3. Attendance for today
    val today = "2026-08-14"
    attendanceDao.insertAll(listOf(
        AttendanceEntity(1, 1, "Ramesh Kumar", today, "08:45 AM", null, "Present", "Green Valley Gate A", null, false, false, 0.0),
        AttendanceEntity(2, 2, "Suresh Yadav", today, "09:35 AM", null, "Late", "Green Valley Gate A", null, true, false, 0.0, "Traffic on bridge"),
        AttendanceEntity(3, 3, "Rahul Sharma", today, "08:50 AM", null, "Present", "Apex Site Tower B", null, false, false, 0.0),
        AttendanceEntity(4, 4, "Amit Patel", today, "09:00 AM", null, "Present", "Apex Site Tower B", null, false, true, 2.0, "Overtime requested for DB wiring"),
        AttendanceEntity(5, 5, "Vikram Singh", today, "09:10 AM", null, "Present", "Green Valley Gate A", null, false, false, 0.0)
    ))

    // 4. Tasks
    taskDao.insertTask(TaskEntity(1, 1, "Green Valley Apartment", "Foundation concrete curing - Block A", "Ensure water sprinkler active twice daily", "Ramesh Kumar", "2026-08-18", "High", "In Progress", 60, "Civil"))
    taskDao.insertTask(TaskEntity(2, 1, "Green Valley Apartment", "Procure 100 bags of OPC 53 Grade Cement", "Required before Friday column casting", "Suresh Yadav", "2026-08-16", "Urgent", "To Do", 0, "Procurement"))
    taskDao.insertTask(TaskEntity(3, 2, "Apex Commercial Complex", "Main Electrical Distribution Panel installation", "Inspect grounding and 3-phase wiring", "Amit Patel", "2026-08-20", "High", "In Progress", 40, "Electrical"))
    taskDao.insertTask(TaskEntity(4, 2, "Apex Commercial Complex", "HVAC duct pressure testing", "Submit test certificate to client architect", "Rahul Sharma", "2026-08-14", "Urgent", "Review", 90, "HVAC", isOverdue = true))
    taskDao.insertTask(TaskEntity(5, 3, "Skyline Luxury Villa #4", "Soil Bearing Capacity (SBC) Test verification", "Review lab report from NIT Raipur", "Rahul Sharma", "2026-08-22", "Medium", "To Do", 0, "Engineering"))

    // 5. Materials
    materialDao.insertMaterial(MaterialEntity(1, 1, "Green Valley Apartment", "OPC 53 Cement", "Cement & Aggregates", 18.0, "Bags", 50.0, 380.0, "UltraTech Supplies Ltd.", "+91 98765 43210"))
    materialDao.insertMaterial(MaterialEntity(2, 1, "Green Valley Apartment", "TMT Steel Rebars (16mm)", "Steel", 2.4, "Tons", 5.0, 58000.0, "Jindal Steel & Power", "+91 98261 11223"))
    materialDao.insertMaterial(MaterialEntity(3, 1, "Green Valley Apartment", "River Sand (Coarse)", "Cement & Aggregates", 120.0, "Cu.Ft", 100.0, 48.0, "Mahanadi Sand Co.", "+91 94250 88990"))
    materialDao.insertMaterial(MaterialEntity(4, 2, "Apex Commercial Complex", "PVC Conduit Pipes (25mm)", "Electrical", 85.0, "Meters", 30.0, 45.0, "Finolex Electricals", "+91 97130 55443"))
    materialDao.insertMaterial(MaterialEntity(5, 2, "Apex Commercial Complex", "Gypsum Board False Ceiling Sheets", "Finishing", 350.0, "Sq.ft", 100.0, 95.0, "Saint-Gobain Gyproc", "+91 99810 66778"))

    // 6. Expenses
    expenseDao.insertExpense(ExpenseEntity(1, 1, "Green Valley Apartment", "UltraTech OPC Cement 50 Bags", "Materials", 19000.0, 3420.0, "UltraTech Supplies Ltd.", "INV-2026-0891", "2026-08-12", null, "Approved", "Admin", "Received at Site Gate 1"))
    expenseDao.insertExpense(ExpenseEntity(2, 1, "Green Valley Apartment", "JCB Excavator 8 hrs rental + Diesel", "Equipment", 9600.0, 0.0, "Chhattishgarh Heavy Equipment", "VCH-4412", "2026-08-10", null, "Approved", "Supervisor", "Ground levelling Block B"))
    expenseDao.insertExpense(ExpenseEntity(3, 2, "Apex Commercial Complex", "Electrical Switchgear Panel & Cables", "Materials", 145000.0, 26100.0, "Schneider Electric Agency", "SCH-9981", "2026-08-08", null, "Approved", "Admin", "3-phase board delivery"))
    expenseDao.insertExpense(ExpenseEntity(4, 1, "Green Valley Apartment", "Weekly Mason & Helper Daily Wages", "Labor", 48000.0, 0.0, "Direct Labor Payroll", "PAY-W32", "2026-08-07", null, "Approved", "Accountant", "12 workers 6 days"))

    // 6b. Scanned Bills
    scannedBillDao.insertScannedBill(
        ScannedBillEntity(
            id = 1,
            vendorName = "UltraTech Supplies Ltd.",
            totalAmount = 19000.0,
            gstAmount = 3420.0,
            date = "2026-08-12",
            invoiceNumber = "INV-2026-0891",
            category = "Materials",
            projectId = 1,
            projectName = "Green Valley Apartment",
            summary = "Extracted 50 Bags OPC 53 Cement delivery voucher with 18% GST.",
            confidence = 98
        )
    )
    scannedBillDao.insertScannedBill(
        ScannedBillEntity(
            id = 2,
            vendorName = "Schneider Electric Agency",
            totalAmount = 145000.0,
            gstAmount = 26100.0,
            date = "2026-08-08",
            invoiceNumber = "SCH-9981",
            category = "Materials",
            projectId = 2,
            projectName = "Apex Commercial Complex",
            summary = "Extracted 3-Phase Main Distribution Switchgear Panel invoice.",
            confidence = 95
        )
    )
    scannedBillDao.insertScannedBill(
        ScannedBillEntity(
            id = 3,
            vendorName = "Chhattishgarh Heavy Equipment",
            totalAmount = 9600.0,
            gstAmount = 0.0,
            date = "2026-08-10",
            invoiceNumber = "VCH-4412",
            category = "Equipment",
            projectId = 1,
            projectName = "Green Valley Apartment",
            summary = "Extracted JCB excavator daily rental & fuel slip.",
            confidence = 92
        )
    )

    // 7. Initial Report
    reportDao.insertReport(ReportEntity(
        id = 1,
        title = "August 2026 Mid-Month Construction Summary",
        type = "Management Summary",
        generatedDate = "2026-08-14",
        summaryText = "Overall construction progress across 3 sites stands at an average of 43%. Green Valley foundation curing is on track. Apex Commercial electrical rough-ins are progressing rapidly. Total spend ₹6,35,000 against allocated monthly budget of ₹8,00,000.",
        recommendationsText = "1. Immediate purchase order recommended for OPC Cement (current stock: 18 bags, threshold: 50 bags).\n2. Expedite HVAC pressure test certificate on Apex Commercial.\n3. Monitor Suresh Yadav's arrival punctuality.",
        totalBudget = 10200000.0,
        totalExpense = 6355000.0,
        attendanceRate = 96,
        completedTasks = 18,
        pendingTasks = 5
    ))

    // 8. AI Chat intro message
    aiChatDao.insertMessage(AiChatMessageEntity(
        sender = "ai",
        message = "Namaste! I am your SITE MAN AI Assistant. 🏗️\n\nI can help you create projects, scan bills, auto-fill forms with voice 🎤, check low-stock materials, track attendance, and generate project summaries.\n\nTry saying: 'Create project Green Valley', 'Show low stock materials', or 'Add 50 bags cement'."
    ))
}
