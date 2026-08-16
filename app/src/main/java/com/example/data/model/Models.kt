package com.example.data.model

import com.example.data.local.*

enum class UserRole(val title: String, val badge: String, val description: String) {
    OWNER_ADMIN("Owner / Admin", "Owner", "Full control over projects, budgets, payroll, approvals & AI management"),
    SITE_SUPERVISOR("Site Supervisor", "Supervisor", "Manages daily tasks, labor attendance, material issues & site safety"),
    ACCOUNTANT("Accountant", "Finance", "Manages expenses, bill scanning, invoices, salary payouts & financial reports"),
    STAFF("Staff / Worker", "Staff", "Views assigned tasks, personal attendance check-in & site safety alerts")
}

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String, val localeTag: String) {
    ENGLISH("en", "English", "English", "en-US"),
    HINDI("hi", "Hindi", "हिन्दी", "hi-IN"),
    SPANISH("es", "Spanish", "Español", "es-ES"),
    FRENCH("fr", "French", "Français", "fr-FR"),
    GERMAN("de", "German", "Deutsch", "de-DE"),
    ARABIC("ar", "Arabic", "العربية", "ar-SA"),
    BENGALI("bn", "Bengali", "বাংলা", "bn-IN"),
    MARATHI("mr", "Marathi", "मराठी", "mr-IN"),
    TELUGU("te", "Telugu", "తెలుగు", "te-IN"),
    TAMIL("ta", "Tamil", "தமிழ்", "ta-IN"),
    GUJARATI("gu", "Gujarati", "ગુજરાતી", "gu-IN"),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ", "kn-IN"),
    PUNJABI("pa", "Punjabi", "ਪੰਜਾਬੀ", "pa-IN"),
    MALAYALAM("ml", "Malayalam", "മലയാളം", "ml-IN"),
    ODIA("or", "Odia", "ଓଡ଼ିଆ", "or-IN"),
    URDU("ur", "Urdu", "اردو", "ur-PK"),
    BHOJPURI("bho", "Bhojpuri", "भोजपुरी", "hi-IN"),
    PORTUGUESE("pt", "Portuguese", "Português", "pt-PT"),
    RUSSIAN("ru", "Russian", "Русский", "ru-RU"),
    CHINESE("zh", "Chinese", "中文", "zh-CN"),
    JAPANESE("ja", "Japanese", "日本語", "ja-JP")
}

enum class AiInputMode(val title: String, val iconName: String) {
    VOICE("Speak / Voice", "mic"),
    SCAN("Scan Camera", "camera"),
    PDF("Upload PDF", "pdf"),
    FILE("Upload File", "file"),
    TEXT("Enter Text", "text"),
    CLIPBOARD("Paste Text", "paste")
}

data class AiExtractedFields(
    val entityType: String = "", // "Project", "Task", "Material", "Expense", "Attendance", "Staff"
    val confidence: Int = 90,
    val summary: String = "",
    val projectName: String? = null,
    val clientName: String? = null,
    val location: String? = null,
    val budget: Double? = null,
    val startDate: String? = null,
    val targetCompletion: String? = null,
    
    val taskTitle: String? = null,
    val assignedTo: String? = null,
    val dueDate: String? = null,
    val priority: String? = null,
    
    val materialName: String? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val supplier: String? = null,
    val unitPrice: Double? = null,
    
    val expenseTitle: String? = null,
    val expenseCategory: String? = null,
    val amount: Double? = null,
    val gstAmount: Double? = null,
    val vendorName: String? = null,
    val invoiceNumber: String? = null,
    val invoiceDate: String? = null,
    
    val staffName: String? = null,
    val staffRole: String? = null,
    val staffPhone: String? = null,
    val dailyWage: Double? = null,
    
    val missingFields: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

data class ProjectHealthDetails(
    val score: Int = 85,
    val progressStatus: String = "Good",
    val budgetStatus: String = "Normal",
    val overdueTasksCount: Int = 1,
    val lowStockMaterialsCount: Int = 2,
    val attendancePercentage: Int = 94,
    val riskSummary: String = "Budget utilization is on track. 1 critical task overdue."
)

data class AiSuggestion(
    val id: String,
    val title: String,
    val description: String,
    val actionText: String,
    val category: String, // "material", "task", "attendance", "expense", "project"
    val urgency: String = "Medium" // "High", "Medium", "Info"
)

data class WeatherInfo(
    val city: String = "Raipur, CG",
    val tempCelsius: Int = 31,
    val condition: String = "Partly Cloudy",
    val humidity: Int = 68,
    val windSpeedKm: Int = 14,
    val safetyStatus: String = "Safe for Concrete & Masonry Work",
    val isWorkSafe: Boolean = true
)
