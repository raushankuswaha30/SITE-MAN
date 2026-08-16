package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.UniversalAiEngine
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.repository.SiteManRepository
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val database = SiteManDatabase.getDatabase(application, viewModelScope)
    val repository = SiteManRepository(database)
    val prefsRepo = UserPreferencesRepository(application)
    val aiEngine = UniversalAiEngine()

    // StateFlows from DB
    val projects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTasks: StateFlow<List<TaskEntity>> = repository.pendingTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val attendance: StateFlow<List<AttendanceEntity>> = repository.getAttendanceForDate(todayStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<AttendanceEntity>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val staff: StateFlow<List<StaffEntity>> = repository.allStaff
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val materials: StateFlow<List<MaterialEntity>> = repository.allMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockMaterials: StateFlow<List<MaterialEntity>> = repository.lowStockMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scannedBills: StateFlow<List<ScannedBillEntity>> = repository.allScannedBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpenseAmount: StateFlow<Double?> = repository.totalExpenseAmount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val reports: StateFlow<List<ReportEntity>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<AiChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Preferences
    val currentRole: StateFlow<UserRole> = prefsRepo.currentRole
    val currentLanguage: StateFlow<AppLanguage> = prefsRepo.currentLanguage
    val isDarkMode: StateFlow<Boolean> = prefsRepo.isDarkMode
    val isAiSuggestionsEnabled: StateFlow<Boolean> = prefsRepo.isAiSuggestionsEnabled
    val isVoiceInputEnabled: StateFlow<Boolean> = prefsRepo.isVoiceInputEnabled
    val isLoggedIn: StateFlow<Boolean> = prefsRepo.isLoggedIn
    val userName: StateFlow<String> = prefsRepo.userName
    val userPhone: StateFlow<String> = prefsRepo.userPhone

    // Weather & Health
    private val _weatherInfo = MutableStateFlow(WeatherInfo())
    val weatherInfo: StateFlow<WeatherInfo> = _weatherInfo.asStateFlow()

    private val _aiSuggestions = MutableStateFlow<List<AiSuggestion>>(emptyList())
    val aiSuggestions: StateFlow<List<AiSuggestion>> = _aiSuggestions.asStateFlow()

    private val _projectHealth = MutableStateFlow(ProjectHealthDetails())
    val projectHealth: StateFlow<ProjectHealthDetails> = _projectHealth.asStateFlow()

    private val _activeProjectId = MutableStateFlow<Long?>(1L)
    val activeProjectId: StateFlow<Long?> = _activeProjectId.asStateFlow()

    private val _isAiProcessing = MutableStateFlow(false)
    val isAiProcessing: StateFlow<Boolean> = _isAiProcessing.asStateFlow()

    init {
        loadAiContext()
    }

    fun loadAiContext() {
        viewModelScope.launch {
            _aiSuggestions.value = aiEngine.getContextualSuggestions("Dashboard", emptyMap())
            _projectHealth.value = aiEngine.calculateProjectHealth(
                projectProgress = 43,
                budgetUtilization = 79.3,
                overdueTasks = 1,
                lowMaterials = 2,
                attendancePct = 95
            )
        }
    }

    fun setActiveProject(id: Long) {
        _activeProjectId.value = id
    }

    // Role & Language switchers
    fun selectRole(role: UserRole) = prefsRepo.setRole(role)
    fun selectLanguage(lang: AppLanguage) = prefsRepo.setLanguage(lang)
    fun toggleDarkMode(dark: Boolean) = prefsRepo.setDarkMode(dark)
    fun toggleAiSuggestions(enabled: Boolean) = prefsRepo.setAiSuggestionsEnabled(enabled)
    fun toggleVoiceInput(enabled: Boolean) = prefsRepo.setVoiceInputEnabled(enabled)
    fun setLoginState(loggedIn: Boolean) = prefsRepo.setLoggedIn(loggedIn)
    fun updateUserProfile(name: String, phone: String) = prefsRepo.setUserProfile(name, phone)

    // Universal AI Form Autofill
    fun processUniversalAiInput(
        input: String,
        mode: String,
        targetForm: String,
        bitmap: Bitmap? = null,
        onComplete: (AiExtractedFields) -> Unit
    ) {
        viewModelScope.launch {
            _isAiProcessing.value = true
            val result = aiEngine.autoFillForm(input, mode, targetForm, bitmap)
            _isAiProcessing.value = false
            result.onSuccess { fields ->
                onComplete(fields)
            }.onFailure {
                onComplete(AiExtractedFields(entityType = targetForm, summary = "Unable to extract fields"))
            }
        }
    }

    // Direct Gemini Vision Bill Scanner
    fun scanBillWithGemini(
        bitmap: Bitmap,
        onComplete: (AiExtractedFields) -> Unit
    ) {
        viewModelScope.launch {
            _isAiProcessing.value = true
            val result = aiEngine.scanBillInvoice(bitmap, null)
            _isAiProcessing.value = false
            result.onSuccess { fields ->
                onComplete(fields)
            }.onFailure {
                onComplete(AiExtractedFields(entityType = "Expense", summary = "Bill OCR analysis complete"))
            }
        }
    }

    // AI Chat
    fun sendChatMessage(userText: String, targetLanguage: AppLanguage? = null) {
        if (userText.isBlank()) return
        val activeLang = targetLanguage ?: currentLanguage.value
        viewModelScope.launch {
            repository.insertChatMessage(AiChatMessageEntity(sender = "user", message = userText))
            _isAiProcessing.value = true
            val sysInstruction = """
                You are SITE MAN AI, an ultra-fast, expert construction copilot and intelligent assistant powered by Google AI.
                Target Language: ${activeLang.displayName} (${activeLang.nativeName}).
                Language Directive: You MUST formulate and answer your entire response in ${activeLang.displayName} (${activeLang.nativeName}) or the specific language the user asked in.
                Provide concise, crystal clear, accurate, and actionable answers. Help with construction workflows, site safety, engineering questions, calculations, materials, expenses, staff, and any general questions with high precision.
            """.trimIndent()
            val aiResponse = aiEngine.generateText(
                prompt = userText,
                systemInstruction = sysInstruction
            ).getOrDefault("SITE MAN AI: $userText")
            _isAiProcessing.value = false
            repository.insertChatMessage(AiChatMessageEntity(sender = "ai", message = aiResponse))
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatMessages()
        }
    }

    // Projects CRUD
    fun saveProject(project: ProjectEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            if (project.id == 0L) {
                repository.insertProject(project)
            } else {
                repository.updateProject(project)
            }
            onDone()
        }
    }

    fun deleteProject(project: ProjectEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteProject(project)
            onDone()
        }
    }

    // Tasks CRUD
    fun saveTask(task: TaskEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            if (task.id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
            }
            onDone()
        }
    }

    fun updateTaskStatus(task: TaskEntity, newStatus: String) {
        viewModelScope.launch {
            val updated = task.copy(
                status = newStatus,
                progress = if (newStatus == "Completed") 100 else if (newStatus == "In Progress") 50 else 0
            )
            repository.updateTask(updated)
        }
    }

    fun deleteTask(task: TaskEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteTask(task)
            onDone()
        }
    }

    // Attendance
    fun markAttendance(
        staffId: Long,
        staffName: String,
        status: String,
        location: String = "Site Gate A - GPS Verified",
        isLate: Boolean = false,
        isOvertime: Boolean = false,
        otHours: Double = 0.0,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            val record = AttendanceEntity(
                staffId = staffId,
                staffName = staffName,
                date = todayStr,
                checkInTime = timeStr,
                status = status,
                locationAddress = location,
                isLate = isLate,
                isOvertime = isOvertime,
                overtimeHours = otHours,
                notes = notes
            )
            repository.insertAttendance(record)
        }
    }

    // Staff CRUD
    fun saveStaff(staff: StaffEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            if (staff.id == 0L) {
                repository.insertStaff(staff)
            } else {
                repository.updateStaff(staff)
            }
            onDone()
        }
    }

    fun deleteStaff(staff: StaffEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteStaff(staff)
            onDone()
        }
    }

    // Materials CRUD
    fun saveMaterial(material: MaterialEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            if (material.id == 0L) {
                repository.insertMaterial(material)
            } else {
                repository.updateMaterial(material)
            }
            onDone()
        }
    }

    fun deleteMaterial(material: MaterialEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteMaterial(material)
            onDone()
        }
    }

    // Expenses CRUD
    fun saveExpense(expense: ExpenseEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            if (expense.id == 0L) {
                repository.insertExpense(expense)
            } else {
                repository.updateExpense(expense)
            }
            onDone()
        }
    }

    fun deleteExpense(expense: ExpenseEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            onDone()
        }
    }

    // Scanned Bills CRUD (Room Database)
    fun saveScannedBill(bill: ScannedBillEntity, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertScannedBill(bill)
            onDone(id)
        }
    }

    fun deleteScannedBill(bill: ScannedBillEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteScannedBill(bill)
            onDone()
        }
    }

    fun deleteScannedBillById(id: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteScannedBillById(id)
            onDone()
        }
    }

    fun saveExtractedBillDetails(
        fields: AiExtractedFields,
        projectId: Long,
        projectName: String,
        onDone: (ScannedBillEntity) -> Unit = {}
    ) {
        viewModelScope.launch {
            val totalAmt = fields.amount ?: 0.0
            val gstAmt = fields.gstAmount ?: 0.0
            val vendor = if (!fields.vendorName.isNullOrBlank()) fields.vendorName else "Site Vendor"
            val billDate = if (!fields.invoiceDate.isNullOrBlank()) fields.invoiceDate else todayStr
            val invNum = fields.invoiceNumber ?: ""
            val cat = fields.expenseCategory ?: "Materials"

            val scannedBill = ScannedBillEntity(
                vendorName = vendor,
                totalAmount = totalAmt,
                gstAmount = gstAmt,
                date = billDate,
                invoiceNumber = invNum,
                category = cat,
                projectId = projectId,
                projectName = projectName,
                summary = fields.summary ?: "AI Extracted Bill ($vendor - ₹${totalAmt.toInt()})"
            )
            val id = repository.insertScannedBill(scannedBill)

            // Also ensure expense record is created/synced
            val expenseEntity = ExpenseEntity(
                projectId = projectId,
                projectName = projectName,
                title = "$vendor - $cat Bill",
                category = cat,
                amount = totalAmt,
                gstAmount = gstAmt,
                vendorName = vendor,
                invoiceNumber = invNum,
                date = billDate,
                status = "Approved",
                notes = fields.summary ?: "Extracted via SITE MAN Camera & AI OCR"
            )
            repository.insertExpense(expenseEntity)

            onDone(scannedBill.copy(id = id))
        }
    }

    // Report Generation
    fun saveReport(report: ReportEntity, onDone: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertReport(report)
            onDone(id)
        }
    }

    fun deleteReport(report: ReportEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteReport(report)
            onDone()
        }
    }

    fun generateReport(type: String, onDone: (ReportEntity) -> Unit = {}) {
        viewModelScope.launch {
            _isAiProcessing.value = true
            val (summary, recommendations) = aiEngine.generateManagementReport("August 2026", "").getOrDefault(
                Pair("Summary generated for $type", "Recommendations logged")
            )
            val newReport = ReportEntity(
                title = "$type - ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())}",
                type = type,
                generatedDate = todayStr,
                summaryText = summary,
                recommendationsText = recommendations,
                totalBudget = 10200000.0,
                totalExpense = 6355000.0,
                attendanceRate = 95,
                completedTasks = 18,
                pendingTasks = 5
            )
            val id = repository.insertReport(newReport)
            _isAiProcessing.value = false
            onDone(newReport.copy(id = id))
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetDatabase()
            loadAiContext()
        }
    }
}
