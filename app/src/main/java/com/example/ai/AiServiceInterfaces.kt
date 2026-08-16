package com.example.ai

import android.graphics.Bitmap
import com.example.data.model.AiExtractedFields
import com.example.data.model.AiSuggestion
import com.example.data.model.ProjectHealthDetails

interface AiTextService {
    suspend fun generateText(prompt: String, systemInstruction: String? = null): Result<String>
    suspend fun parseIntentAndEntities(rawInput: String, targetEntity: String): Result<AiExtractedFields>
}

interface AiVoiceService {
    suspend fun processVoiceText(transcription: String, currentScreen: String): Result<AiExtractedFields>
}

interface AiDocumentService {
    suspend fun processPdfDocument(fileName: String, documentText: String, formType: String): Result<AiExtractedFields>
}

interface AiImageService {
    suspend fun analyzeConstructionImage(bitmap: Bitmap, prompt: String): Result<String>
}

interface AiOcrService {
    suspend fun scanBillInvoice(bitmap: Bitmap?, rawText: String?): Result<AiExtractedFields>
}

interface AiAutoFillService {
    suspend fun autoFillForm(input: String, mode: String, targetForm: String, imageBitmap: Bitmap? = null): Result<AiExtractedFields>
}

interface AiSuggestionService {
    suspend fun getContextualSuggestions(screenName: String, contextData: Map<String, Any>): List<AiSuggestion>
    suspend fun calculateProjectHealth(projectProgress: Int, budgetUtilization: Double, overdueTasks: Int, lowMaterials: Int, attendancePct: Int): ProjectHealthDetails
}

interface AiReportService {
    suspend fun generateManagementReport(period: String, statsSummary: String): Result<Pair<String, String>>
}

interface AiValidationService {
    fun validateFormFields(fields: AiExtractedFields, targetForm: String): List<String>
}
