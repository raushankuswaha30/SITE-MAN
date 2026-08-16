package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.*
import com.example.data.model.AiExtractedFields
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.ui.components.SiteManTopBar
import com.example.ui.components.UniversalAiModalSheet
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.util.Localization
import com.example.viewmodel.MainViewModel

enum class AppScreen {
    SPLASH,
    LOGIN,
    SIGNUP,
    ROLE_SELECT,
    LANGUAGE_SELECT,
    MAIN_DASHBOARD,
    PROJECTS,
    PROJECT_DETAIL,
    TASKS,
    ATTENDANCE,
    MATERIALS,
    EXPENSES,
    BILL_SCANNER,
    STAFF,
    REPORTS,
    AI_CHAT,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkMode) {
                SiteManApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteManApp(viewModel: MainViewModel) {
    // ViewModel States
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val attendance by viewModel.attendance.collectAsStateWithLifecycle()
    val staff by viewModel.staff.collectAsStateWithLifecycle()
    val materials by viewModel.materials.collectAsStateWithLifecycle()
    val lowStockMaterials by viewModel.lowStockMaterials.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val scannedBills by viewModel.scannedBills.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpenseAmount.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val weatherInfo by viewModel.weatherInfo.collectAsStateWithLifecycle()
    val aiSuggestions by viewModel.aiSuggestions.collectAsStateWithLifecycle()
    val projectHealth by viewModel.projectHealth.collectAsStateWithLifecycle()
    val isAiProcessing by viewModel.isAiProcessing.collectAsStateWithLifecycle()

    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val isAiSuggestionsEnabled by viewModel.isAiSuggestionsEnabled.collectAsStateWithLifecycle()
    val isVoiceInputEnabled by viewModel.isVoiceInputEnabled.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val userPhone by viewModel.userPhone.collectAsStateWithLifecycle()

    // Navigation and Modal States
    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
    var selectedProjectIdForDetail by remember { mutableStateOf<Long?>(null) }

    // Dialogs & Sheets State
    var isUniversalAiModalOpen by remember { mutableStateOf(false) }
    var universalAiTargetForm by remember { mutableStateOf("Project") }

    var showAddProjectDialog by remember { mutableStateOf(false) }
    var editingProject by remember { mutableStateOf<ProjectEntity?>(null) }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskEntity?>(null) }

    var showMarkAttendanceDialog by remember { mutableStateOf(false) }

    var showAddMaterialDialog by remember { mutableStateOf(false) }
    var editingMaterial by remember { mutableStateOf<MaterialEntity?>(null) }

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }
    var initialExpenseFieldsForDialog by remember { mutableStateOf<AiExtractedFields?>(null) }

    var showAddStaffDialog by remember { mutableStateOf(false) }
    var editingStaff by remember { mutableStateOf<StaffEntity?>(null) }

    var showMoreMenuSheet by remember { mutableStateOf(false) }

    // Routing Logic
    when (currentScreen) {
        AppScreen.SPLASH -> {
            SplashScreen(
                onSplashFinished = {
                    currentScreen = if (isLoggedIn) AppScreen.MAIN_DASHBOARD else AppScreen.LOGIN
                }
            )
        }

        AppScreen.LOGIN -> {
            LoginScreen(
                onLoginSuccess = {
                    viewModel.setLoginState(true)
                    currentScreen = AppScreen.MAIN_DASHBOARD
                },
                onNavigateToSignUp = { currentScreen = AppScreen.SIGNUP },
                onSelectRoleClick = { currentScreen = AppScreen.ROLE_SELECT },
                onSelectLanguageClick = { currentScreen = AppScreen.LANGUAGE_SELECT },
                selectedRole = currentRole,
                selectedLanguage = currentLanguage
            )
        }

        AppScreen.SIGNUP -> {
            SignUpScreen(
                onSignUpSuccess = {
                    viewModel.setLoginState(true)
                    currentScreen = AppScreen.MAIN_DASHBOARD
                },
                onNavigateToLogin = { currentScreen = AppScreen.LOGIN }
            )
        }

        AppScreen.ROLE_SELECT -> {
            RoleSelectionScreen(
                currentRole = currentRole,
                onRoleSelected = { viewModel.selectRole(it) },
                onDone = {
                    currentScreen = if (isLoggedIn) AppScreen.MAIN_DASHBOARD else AppScreen.LOGIN
                }
            )
        }

        AppScreen.LANGUAGE_SELECT -> {
            LanguageSelectionScreen(
                currentLanguage = currentLanguage,
                onLanguageSelected = { viewModel.selectLanguage(it) },
                onDone = {
                    currentScreen = if (isLoggedIn) AppScreen.MAIN_DASHBOARD else AppScreen.LOGIN
                }
            )
        }

        AppScreen.PROJECT_DETAIL -> {
            val project = projects.find { it.id == selectedProjectIdForDetail } ?: projects.firstOrNull()
            ProjectDetailScreen(
                project = project,
                onBack = { currentScreen = AppScreen.PROJECTS },
                onEdit = {
                    editingProject = project
                    showAddProjectDialog = true
                }
            )
        }

        AppScreen.BILL_SCANNER -> {
            BillScannerCameraScreen(
                projects = projects,
                scannedBills = scannedBills,
                onSaveExpense = { expense ->
                    viewModel.saveExpense(expense)
                },
                onSaveScannedBill = { bill ->
                    viewModel.saveScannedBill(bill)
                },
                onDeleteScannedBill = { billId ->
                    viewModel.deleteScannedBillById(billId)
                },
                onOpenAddExpenseWithFields = { fields ->
                    initialExpenseFieldsForDialog = fields
                    editingExpense = null
                    showAddExpenseDialog = true
                    currentScreen = AppScreen.EXPENSES
                },
                onBack = { currentScreen = AppScreen.EXPENSES },
                onScanImageWithGemini = { bitmap, onComplete ->
                    viewModel.scanBillWithGemini(bitmap, onComplete)
                }
            )
        }

        else -> {
            // Main App Shell with persistent TopBar & BottomNavigationBar
            Scaffold(
                topBar = {
                    val title = when (currentScreen) {
                        AppScreen.MAIN_DASHBOARD -> "SITE MAN"
                        AppScreen.PROJECTS -> Localization.tr("projects", currentLanguage)
                        AppScreen.TASKS -> Localization.tr("tasks", currentLanguage)
                        AppScreen.ATTENDANCE -> Localization.tr("attendance", currentLanguage)
                        AppScreen.MATERIALS -> Localization.tr("materials", currentLanguage)
                        AppScreen.EXPENSES -> Localization.tr("expenses", currentLanguage)
                        AppScreen.STAFF -> Localization.tr("staff", currentLanguage)
                        AppScreen.REPORTS -> Localization.tr("reports", currentLanguage)
                        AppScreen.AI_CHAT -> "SITE MAN AI Assistant"
                        AppScreen.SETTINGS -> "Settings"
                        else -> "SITE MAN"
                    }

                    SiteManTopBar(
                        title = title,
                        currentRole = currentRole,
                        currentLanguage = currentLanguage,
                        onLanguageClick = { currentScreen = AppScreen.LANGUAGE_SELECT },
                        onRoleClick = { currentScreen = AppScreen.ROLE_SELECT },
                        onAiChatClick = { currentScreen = AppScreen.AI_CHAT }
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.MAIN_DASHBOARD,
                            onClick = { currentScreen = AppScreen.MAIN_DASHBOARD },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                            label = { Text("Dashboard", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("nav_item_dashboard")
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.PROJECTS,
                            onClick = { currentScreen = AppScreen.PROJECTS },
                            icon = { Icon(Icons.Default.Apartment, contentDescription = "Projects") },
                            label = { Text("Projects", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("nav_item_projects")
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.TASKS,
                            onClick = { currentScreen = AppScreen.TASKS },
                            icon = { Icon(Icons.Default.Assignment, contentDescription = "Tasks") },
                            label = { Text("Tasks", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("nav_item_tasks")
                        )
                        NavigationBarItem(
                            selected = currentScreen == AppScreen.ATTENDANCE,
                            onClick = { currentScreen = AppScreen.ATTENDANCE },
                            icon = { Icon(Icons.Default.Fingerprint, contentDescription = "Attendance") },
                            label = { Text("Attendance", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("nav_item_attendance")
                        )
                        NavigationBarItem(
                            selected = currentScreen in listOf(
                                AppScreen.MATERIALS,
                                AppScreen.EXPENSES,
                                AppScreen.STAFF,
                                AppScreen.REPORTS,
                                AppScreen.SETTINGS,
                                AppScreen.AI_CHAT
                            ),
                            onClick = { showMoreMenuSheet = true },
                            icon = { Icon(Icons.Default.Menu, contentDescription = "More") },
                            label = { Text("More", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("nav_item_more")
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentScreen) {
                        AppScreen.MAIN_DASHBOARD -> {
                            DashboardScreen(
                                currentRole = currentRole,
                                currentLanguage = currentLanguage,
                                weatherInfo = weatherInfo,
                                projectHealth = projectHealth,
                                aiSuggestions = aiSuggestions,
                                projects = projects,
                                tasks = tasks,
                                lowStockMaterials = lowStockMaterials,
                                staffCount = staff.size,
                                presentCount = attendance.count { it.status == "Present" || it.status == "Late" },
                                totalExpense = totalExpense ?: 0.0,
                                onNavigateToProjects = { currentScreen = AppScreen.PROJECTS },
                                onNavigateToTasks = { currentScreen = AppScreen.TASKS },
                                onNavigateToAttendance = { currentScreen = AppScreen.ATTENDANCE },
                                onNavigateToMaterials = { currentScreen = AppScreen.MATERIALS },
                                onNavigateToExpenses = { currentScreen = AppScreen.EXPENSES },
                                onNavigateToReports = { currentScreen = AppScreen.REPORTS },
                                onNavigateToStaff = { currentScreen = AppScreen.STAFF },
                                onOpenAiFill = { targetForm ->
                                    universalAiTargetForm = targetForm
                                    isUniversalAiModalOpen = true
                                },
                                onProjectClick = { projectId ->
                                    selectedProjectIdForDetail = projectId
                                    currentScreen = AppScreen.PROJECT_DETAIL
                                }
                            )
                        }

                        AppScreen.PROJECTS -> {
                            ProjectsScreen(
                                projects = projects,
                                currentLanguage = currentLanguage,
                                onProjectClick = { projectId ->
                                    selectedProjectIdForDetail = projectId
                                    currentScreen = AppScreen.PROJECT_DETAIL
                                },
                                onAddProjectClick = {
                                    editingProject = null
                                    showAddProjectDialog = true
                                },
                                onEditProjectClick = { project ->
                                    editingProject = project
                                    showAddProjectDialog = true
                                },
                                onDeleteProjectClick = { project ->
                                    viewModel.deleteProject(project)
                                },
                                onOpenAiFill = {
                                    universalAiTargetForm = "Project"
                                    isUniversalAiModalOpen = true
                                },
                                onScanBillClick = {
                                    currentScreen = AppScreen.BILL_SCANNER
                                },
                                onSaveReport = { report ->
                                    viewModel.saveReport(report)
                                },
                                onSaveTask = { task ->
                                    viewModel.saveTask(task)
                                }
                            )
                        }

                        AppScreen.TASKS -> {
                            TasksScreen(
                                tasks = tasks,
                                projects = projects,
                                currentLanguage = currentLanguage,
                                onAddTaskClick = {
                                    editingTask = null
                                    showAddTaskDialog = true
                                },
                                onEditTaskClick = { task ->
                                    editingTask = task
                                    showAddTaskDialog = true
                                },
                                onDeleteTaskClick = { task ->
                                    viewModel.deleteTask(task)
                                },
                                onStatusChange = { task, newStatus ->
                                    viewModel.updateTaskStatus(task, newStatus)
                                },
                                onOpenAiFill = {
                                    universalAiTargetForm = "Task"
                                    isUniversalAiModalOpen = true
                                }
                            )
                        }

                        AppScreen.ATTENDANCE -> {
                            AttendanceScreen(
                                attendanceList = attendance,
                                staffList = staff,
                                currentLanguage = currentLanguage,
                                onMarkAttendanceClick = { showMarkAttendanceDialog = true },
                                onOpenAiVoiceAttendance = {
                                    universalAiTargetForm = "Attendance"
                                    isUniversalAiModalOpen = true
                                }
                            )
                        }

                        AppScreen.MATERIALS -> {
                            MaterialsScreen(
                                materials = materials,
                                currentLanguage = currentLanguage,
                                onAddMaterialClick = {
                                    editingMaterial = null
                                    showAddMaterialDialog = true
                                },
                                onEditMaterialClick = { material ->
                                    editingMaterial = material
                                    showAddMaterialDialog = true
                                },
                                onDeleteMaterialClick = { material ->
                                    viewModel.deleteMaterial(material)
                                },
                                onOpenAiFill = {
                                    universalAiTargetForm = "Material"
                                    isUniversalAiModalOpen = true
                                }
                            )
                        }

                        AppScreen.EXPENSES -> {
                            ExpensesScreen(
                                expenses = expenses,
                                scannedBills = scannedBills,
                                projects = projects,
                                currentLanguage = currentLanguage,
                                onAddExpenseClick = {
                                    editingExpense = null
                                    initialExpenseFieldsForDialog = null
                                    showAddExpenseDialog = true
                                },
                                onEditExpenseClick = { expense ->
                                    editingExpense = expense
                                    initialExpenseFieldsForDialog = null
                                    showAddExpenseDialog = true
                                },
                                onDeleteExpenseClick = { expense ->
                                    viewModel.deleteExpense(expense)
                                },
                                onDeleteScannedBill = { billId ->
                                    viewModel.deleteScannedBillById(billId)
                                },
                                onScanBillClick = {
                                    currentScreen = AppScreen.BILL_SCANNER
                                }
                            )
                        }

                        AppScreen.STAFF -> {
                            StaffScreen(
                                staffList = staff,
                                currentLanguage = currentLanguage,
                                onAddStaffClick = {
                                    editingStaff = null
                                    showAddStaffDialog = true
                                },
                                onEditStaffClick = { member ->
                                    editingStaff = member
                                    showAddStaffDialog = true
                                },
                                onDeleteStaffClick = { member ->
                                    viewModel.deleteStaff(member)
                                },
                                onOpenAiFill = {
                                    universalAiTargetForm = "Staff"
                                    isUniversalAiModalOpen = true
                                }
                            )
                        }

                        AppScreen.REPORTS -> {
                            ReportsScreen(
                                reports = reports,
                                isGenerating = isAiProcessing,
                                currentLanguage = currentLanguage,
                                onGenerateReport = { reportType ->
                                    viewModel.generateReport(reportType)
                                }
                            )
                        }

                        AppScreen.AI_CHAT -> {
                            AiChatScreen(
                                messages = chatMessages,
                                isProcessing = isAiProcessing,
                                currentLanguage = currentLanguage,
                                onSelectLanguage = { lang -> viewModel.selectLanguage(lang) },
                                onSendMessage = { text, lang -> viewModel.sendChatMessage(text, lang) },
                                onClearChat = { viewModel.clearChatHistory() },
                                onBack = { currentScreen = AppScreen.MAIN_DASHBOARD }
                            )
                        }

                        AppScreen.SETTINGS -> {
                            SettingsScreen(
                                currentRole = currentRole,
                                currentLanguage = currentLanguage,
                                isDarkMode = isDarkMode,
                                isAiSuggestionsEnabled = isAiSuggestionsEnabled,
                                isVoiceInputEnabled = isVoiceInputEnabled,
                                userName = userName,
                                userPhone = userPhone,
                                onRoleClick = { currentScreen = AppScreen.ROLE_SELECT },
                                onLanguageClick = { currentScreen = AppScreen.LANGUAGE_SELECT },
                                onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                                onToggleAiSuggestions = { viewModel.toggleAiSuggestions(it) },
                                onToggleVoiceInput = { viewModel.toggleVoiceInput(it) },
                                onResetData = { viewModel.resetAllData() },
                                onLogout = {
                                    viewModel.setLoginState(false)
                                    currentScreen = AppScreen.LOGIN
                                }
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    // "More" Navigation Sheet
    if (showMoreMenuSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreMenuSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Construction Operations Hub",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MoreMenuGridCard(
                        title = "Materials Stock",
                        subtitle = "${materials.size} Items tracked",
                        icon = Icons.Default.Inventory2,
                        color = AmberGold,
                        modifier = Modifier.weight(1f)
                    ) {
                        currentScreen = AppScreen.MATERIALS
                        showMoreMenuSheet = false
                    }
                    MoreMenuGridCard(
                        title = "Expenses & Bills",
                        subtitle = "₹${((totalExpense ?: 0.0) / 100000).toInt()}L Logged",
                        icon = Icons.Default.ReceiptLong,
                        color = SkyBlueAccent,
                        modifier = Modifier.weight(1f)
                    ) {
                        currentScreen = AppScreen.EXPENSES
                        showMoreMenuSheet = false
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MoreMenuGridCard(
                        title = "Staff & Wages",
                        subtitle = "${staff.size} Active crew",
                        icon = Icons.Default.Groups,
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    ) {
                        currentScreen = AppScreen.STAFF
                        showMoreMenuSheet = false
                    }
                    MoreMenuGridCard(
                        title = "AI Site Reports",
                        subtitle = "${reports.size} Reports ready",
                        icon = Icons.Default.Assessment,
                        color = DarkBlueSecondary,
                        modifier = Modifier.weight(1f)
                    ) {
                        currentScreen = AppScreen.REPORTS
                        showMoreMenuSheet = false
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MoreMenuGridCard(
                        title = "AI Chat Assistant",
                        subtitle = "Voice & text queries",
                        icon = Icons.Default.AutoAwesome,
                        color = SkyBlueAccent,
                        modifier = Modifier.weight(1f)
                    ) {
                        currentScreen = AppScreen.AI_CHAT
                        showMoreMenuSheet = false
                    }
                    MoreMenuGridCard(
                        title = "Settings & Profile",
                        subtitle = "Preferences, role & DB",
                        icon = Icons.Default.Settings,
                        color = TextMuted,
                        modifier = Modifier.weight(1f)
                    ) {
                        currentScreen = AppScreen.SETTINGS
                        showMoreMenuSheet = false
                    }
                }
            }
        }
    }

    // Universal AI Autofill Modal Sheet
    if (isUniversalAiModalOpen) {
        UniversalAiModalSheet(
            targetForm = universalAiTargetForm,
            isProcessing = isAiProcessing,
            onDismiss = { isUniversalAiModalOpen = false },
            onApplyFields = { extracted ->
                when (universalAiTargetForm.lowercase()) {
                    "project" -> {
                        editingProject = ProjectEntity(
                            name = extracted.projectName ?: "New Construction Project",
                            clientName = extracted.clientName ?: "Client",
                            location = extracted.location ?: "Raipur",
                            budget = extracted.budget ?: 2500000.0,
                            spentAmount = 0.0,
                            startDate = extracted.startDate ?: "2026-09-01",
                            targetCompletion = extracted.targetCompletion ?: "2027-05-01",
                            status = "Ongoing",
                            notes = extracted.summary
                        )
                        showAddProjectDialog = true
                    }
                    "task" -> {
                        editingTask = TaskEntity(
                            projectId = 1L,
                            projectName = extracted.projectName ?: "Green Valley Apartment",
                            title = extracted.taskTitle ?: "Construction Site Task",
                            description = extracted.summary,
                            assignedTo = extracted.assignedTo ?: "Ramesh Kumar",
                            dueDate = extracted.dueDate ?: "2026-08-20",
                            priority = extracted.priority ?: "High",
                            status = "To Do"
                        )
                        showAddTaskDialog = true
                    }
                    "material" -> {
                        editingMaterial = MaterialEntity(
                            name = extracted.materialName ?: "OPC 53 Cement",
                            category = "Cement & Aggregates",
                            currentQuantity = extracted.quantity ?: 50.0,
                            unit = extracted.unit ?: "Bags",
                            minThreshold = 20.0,
                            unitPrice = extracted.unitPrice ?: 380.0,
                            supplierName = extracted.supplier ?: "UltraTech Supplies Ltd."
                        )
                        showAddMaterialDialog = true
                    }
                    "expense" -> {
                        editingExpense = ExpenseEntity(
                            projectId = 1L,
                            projectName = "Green Valley Apartment",
                            title = extracted.expenseTitle ?: "Site Material Expense",
                            category = extracted.expenseCategory ?: "Materials",
                            amount = extracted.amount ?: 19000.0,
                            gstAmount = extracted.gstAmount ?: 3420.0,
                            vendorName = extracted.vendorName ?: "UltraTech Supplies Ltd.",
                            invoiceNumber = extracted.invoiceNumber ?: "INV-2026-0891",
                            date = extracted.invoiceDate ?: "2026-08-14"
                        )
                        showAddExpenseDialog = true
                    }
                    "staff" -> {
                        editingStaff = StaffEntity(
                            name = extracted.staffName ?: "Suresh Yadav",
                            phone = extracted.staffPhone ?: "+91 97130 98765",
                            role = extracted.staffRole ?: "Head Mason",
                            department = "Civil",
                            dailySalary = extracted.dailyWage ?: 950.0
                        )
                        showAddStaffDialog = true
                    }
                    "attendance" -> {
                        viewModel.markAttendance(
                            staffId = 1L,
                            staffName = extracted.staffName ?: "Ramesh Kumar",
                            status = "Present",
                            location = "Site Main Entrance - Voice Verified",
                            notes = extracted.summary
                        )
                    }
                }
            },
            onProcessInput = { input, mode, bitmap, onResult ->
                viewModel.processUniversalAiInput(input, mode, universalAiTargetForm, bitmap, onResult)
            }
        )
    }

    // Add / Edit Project Dialog
    if (showAddProjectDialog) {
        AddEditProjectDialog(
            projectToEdit = editingProject,
            onDismiss = { showAddProjectDialog = false },
            onSave = { project ->
                viewModel.saveProject(project) {
                    showAddProjectDialog = false
                }
            },
            onOpenAiFill = { onResult ->
                universalAiTargetForm = "Project"
                isUniversalAiModalOpen = true
            }
        )
    }

    // Add / Edit Task Dialog
    if (showAddTaskDialog) {
        AddEditTaskDialog(
            taskToEdit = editingTask,
            projects = projects,
            staffList = staff,
            onDismiss = { showAddTaskDialog = false },
            onSave = { task ->
                viewModel.saveTask(task) {
                    showAddTaskDialog = false
                }
            },
            onOpenAiFill = { onResult ->
                universalAiTargetForm = "Task"
                isUniversalAiModalOpen = true
            }
        )
    }

    // Mark Attendance Dialog
    if (showMarkAttendanceDialog) {
        MarkAttendanceDialog(
            staffList = staff,
            onDismiss = { showMarkAttendanceDialog = false },
            onSave = { staffId, staffName, status, isLate, isOvertime, otHours, notes ->
                viewModel.markAttendance(
                    staffId = staffId,
                    staffName = staffName,
                    status = status,
                    isLate = isLate,
                    isOvertime = isOvertime,
                    otHours = otHours,
                    notes = notes
                )
                showMarkAttendanceDialog = false
            }
        )
    }

    // Add / Edit Material Dialog
    if (showAddMaterialDialog) {
        AddEditMaterialDialog(
            materialToEdit = editingMaterial,
            onDismiss = { showAddMaterialDialog = false },
            onSave = { material ->
                viewModel.saveMaterial(material) {
                    showAddMaterialDialog = false
                }
            },
            onOpenAiFill = { onResult ->
                universalAiTargetForm = "Material"
                isUniversalAiModalOpen = true
            }
        )
    }

    // Add / Edit Expense Dialog
    if (showAddExpenseDialog) {
        AddEditExpenseDialog(
            expenseToEdit = editingExpense,
            initialFields = initialExpenseFieldsForDialog,
            projects = projects,
            onDismiss = {
                showAddExpenseDialog = false
                initialExpenseFieldsForDialog = null
            },
            onSave = { expense ->
                viewModel.saveExpense(expense) {
                    showAddExpenseDialog = false
                    initialExpenseFieldsForDialog = null
                }
            },
            onOpenAiFill = { onResult ->
                universalAiTargetForm = "Expense"
                isUniversalAiModalOpen = true
            },
            onScanBillWithCamera = {
                showAddExpenseDialog = false
                currentScreen = AppScreen.BILL_SCANNER
            }
        )
    }

    // Add / Edit Staff Dialog
    if (showAddStaffDialog) {
        AddEditStaffDialog(
            staffToEdit = editingStaff,
            onDismiss = { showAddStaffDialog = false },
            onSave = { member ->
                viewModel.saveStaff(member) {
                    showAddStaffDialog = false
                }
            },
            onOpenAiFill = { onResult ->
                universalAiTargetForm = "Staff"
                isUniversalAiModalOpen = true
            }
        )
    }
}

@Composable
private fun MoreMenuGridCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("more_menu_$title"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}
