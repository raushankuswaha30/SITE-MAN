package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.util.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteManTopBar(
    title: String,
    currentRole: UserRole,
    currentLanguage: AppLanguage,
    onLanguageClick: () -> Unit,
    onRoleClick: () -> Unit,
    onAiChatClick: () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(DarkBluePrimary, SkyBlueAccent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = "SITE MAN Logo",
                        tint = AmberGold,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SkyBlueAccent.copy(alpha = 0.15f))
                                .clickable { onRoleClick() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = currentRole.badge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                ),
                                color = SkyBlueAccent
                            )
                        }
                    }
                    Text(
                        text = Localization.tr("tagline", currentLanguage),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp
                        ),
                        color = TextMuted
                    )
                }
            }
        },
        navigationIcon = {
            navigationIcon?.invoke()
        },
        actions = {
            // Language selector button
            IconButton(
                onClick = onLanguageClick,
                modifier = Modifier.testTag("topbar_language_btn")
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentLanguage.code.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            // AI Assistant action button
            IconButton(
                onClick = onAiChatClick,
                modifier = Modifier.testTag("topbar_ai_chat_btn")
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(SkyBlueAccent, DarkBlueSecondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "SITE MAN AI",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun UniversalAiFillButton(
    modifier: Modifier = Modifier,
    label: String = "AI Fill",
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .testTag("universal_ai_fill_btn")
            .height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DarkBluePrimary,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = AmberGold,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalAiModalSheet(
    targetForm: String,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onApplyFields: (AiExtractedFields) -> Unit,
    onProcessInput: (input: String, mode: String, bitmap: Bitmap?, onResult: (AiExtractedFields) -> Unit) -> Unit
) {
    var selectedMode by remember { mutableStateOf(AiInputMode.VOICE) }
    var rawTextInput by remember { mutableStateOf("") }
    var extractedResult by remember { mutableStateOf<AiExtractedFields?>(null) }
    var isListening by remember { mutableStateOf(false) }

    // Pre-canned Voice/Text sample presets to simulate fast realistic inputs
    val samplePresets = when (targetForm.lowercase()) {
        "project" -> listOf(
            "Create a project named Green Valley Apartment in Raipur. Client is ABC Construction. Budget is 25 lakh. Start date is 1 September 2026 and completion should be within 8 months.",
            "New project Apex Heights in Bhilai. Client Apex Realty. Budget 65 Lakhs. Start Oct 2026."
        )
        "task" -> listOf(
            "Create a task for Ramesh to complete foundation work by Friday with high priority.",
            "Assign Rahul Sharma for electrical panel grounding test due on 20 August with urgent priority."
        )
        "material" -> listOf(
            "Add 50 bags of cement to material stock.",
            "Add 2.5 tons of TMT steel rebars 16mm from Jindal Steel."
        )
        "expense" -> listOf(
            "Paid ₹19,000 for 50 bags cement from UltraTech Supplies Ltd. Invoice #INV-2026-0891 with 18% GST.",
            "JCB excavator 8 hours rental ₹9,600 for ground levelling block B."
        )
        "staff" -> listOf(
            "Add staff member Suresh Yadav, Head Mason, phone +91 97130 98765, daily wage 950.",
            "New civil engineer Rahul Sharma, phone +91 99812 34567, salary 1500 per day."
        )
        else -> listOf(
            "Add 50 bags cement to inventory stock.",
            "Create task foundation curing for Ramesh."
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SkyBlueAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SkyBlueAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SITE MAN AI Auto-Fill",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Target Form: $targetForm",
                            style = MaterialTheme.typography.bodySmall,
                            color = SkyBlueAccent
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input Modes Selector (🎤 Speak, 📷 Scan, 📄 PDF, 📁 File, ✍ Text, 📋 Paste)
            ScrollableTabRow(
                selectedTabIndex = AiInputMode.values().indexOf(selectedMode),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                AiInputMode.values().forEach { mode ->
                    val isSelected = selectedMode == mode
                    Tab(
                        selected = isSelected,
                        onClick = {
                            selectedMode = mode
                            if (mode == AiInputMode.CLIPBOARD && rawTextInput.isBlank()) {
                                rawTextInput = samplePresets.firstOrNull() ?: ""
                            }
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (mode) {
                                        AiInputMode.VOICE -> Icons.Default.Mic
                                        AiInputMode.SCAN -> Icons.Default.QrCodeScanner
                                        AiInputMode.PDF -> Icons.Default.PictureAsPdf
                                        AiInputMode.FILE -> Icons.Default.FolderOpen
                                        AiInputMode.TEXT -> Icons.Default.EditNote
                                        AiInputMode.CLIPBOARD -> Icons.Default.ContentPaste
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) SkyBlueAccent else TextMuted
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) SkyBlueAccent else TextMuted
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mode-specific container
            when (selectedMode) {
                AiInputMode.VOICE -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isListening) "Listening... Speak naturally in English, Hindi, Hinglish" else "Tap microphone to speak instruction",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isListening) ErrorRed else SkyBlueAccent
                                    )
                                    .clickable {
                                        isListening = !isListening
                                        if (!isListening) {
                                            rawTextInput = samplePresets.firstOrNull() ?: ""
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                                    contentDescription = "Microphone",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Or select a quick voice query:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            samplePresets.forEach { preset ->
                                SuggestionChip(
                                    onClick = {
                                        rawTextInput = preset
                                        onProcessInput(preset, "Voice", null) { result ->
                                            extractedResult = result
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = preset,
                                            maxLines = 2,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                AiInputMode.SCAN -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = null,
                                tint = SkyBlueAccent,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Camera OCR & Document Scanner",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Position camera over bills, invoices, cement receipts, or site drawings",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val billText = "UltraTech Cement Tax Invoice #INV-2026-0891 Date: 14 Aug 2026. Vendor: UltraTech Supplies Ltd. Item: OPC 53 Cement 50 Bags @ Rs 380 = Rs 19,000 + GST 18% Rs 3,420 Total Rs 22,420."
                                    rawTextInput = billText
                                    onProcessInput(billText, "Scan Camera", null) { result ->
                                        extractedResult = result
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SkyBlueAccent)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan Sample Construction Bill")
                            }
                        }
                    }
                }
                AiInputMode.PDF, AiInputMode.FILE -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Upload Project Quotation / PDF Doc",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    val docText = "Project Quotation PDF: Green Valley Apartment, Raipur. Client: ABC Construction. Budget: 25,00,000 INR. Scope: 8 months completion."
                                    rawTextInput = docText
                                    onProcessInput(docText, "PDF", null) { result ->
                                        extractedResult = result
                                    }
                                }
                            ) {
                                Icon(Icons.Default.UploadFile, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Extract from 'Quotation_GreenValley.pdf'")
                            }
                        }
                    }
                }
                AiInputMode.TEXT, AiInputMode.CLIPBOARD -> {
                    OutlinedTextField(
                        value = rawTextInput,
                        onValueChange = { rawTextInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("ai_fill_raw_text_input"),
                        label = { Text("Enter or paste natural language instruction") },
                        placeholder = { Text("e.g., Create project Green Valley Apartment in Raipur with budget 25 lakh...") },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Button: Extract & Understand
            if (rawTextInput.isNotBlank() && extractedResult == null) {
                Button(
                    onClick = {
                        onProcessInput(rawTextInput, selectedMode.title, null) { result ->
                            extractedResult = result
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("ai_process_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Analyzing...")
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = AmberGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Extract & Auto-Fill Form", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Extracted Result Preview Card
            extractedResult?.let { result ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreenLight.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(SuccessGreen, SkyBlueAccent)))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI Extracted Data (${result.confidence}% confidence)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessGreen
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = result.summary,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = TextDark
                        )

                        // Highlight missing fields if any
                        if (result.missingFields.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⚠️ Missing info: ${result.missingFields.joinToString(", ")}",
                                style = MaterialTheme.typography.labelSmall.copy(color = WarningOrange)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                onApplyFields(result)
                                onDismiss()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_ai_fill_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Apply & Fill Form", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectHealthGauge(
    health: ProjectHealthDetails,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("project_health_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (health.score >= 80) SuccessGreen.copy(alpha = 0.15f)
                                else if (health.score >= 65) WarningOrange.copy(alpha = 0.15f)
                                else ErrorRed.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = if (health.score >= 80) SuccessGreen else if (health.score >= 65) WarningOrange else ErrorRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Site Health Score",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "AI Real-Time Anomaly & Progress Index",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
                // Health Score Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (health.score >= 80) SuccessGreen
                            else if (health.score >= 65) WarningOrange
                            else ErrorRed
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${health.score}%",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { health.score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (health.score >= 80) SuccessGreen else if (health.score >= 65) WarningOrange else ErrorRed,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HealthMetricPill("Progress", health.progressStatus, Icons.Default.TrendingUp, SkyBlueAccent)
                HealthMetricPill("Budget", health.budgetStatus, Icons.Default.AccountBalanceWallet, AmberGold)
                HealthMetricPill("Tasks", "${health.overdueTasksCount} Overdue", Icons.Default.AssignmentLate, if (health.overdueTasksCount > 0) WarningOrange else SuccessGreen)
                HealthMetricPill("Attendance", "${health.attendancePercentage}%", Icons.Default.People, SuccessGreen)
            }
        }
    }
}

@Composable
private fun HealthMetricPill(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextMuted)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun WeatherWidgetCard(
    weather: WeatherInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("weather_widget_card"),
        colors = CardDefaults.cardColors(
            containerColor = DarkBluePrimary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(SkyBlueAccent.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "Weather",
                        tint = AmberGold,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${weather.tempCelsius}°C • ${weather.city}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "${weather.condition} • Humidity ${weather.humidity}%",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SkyBlueLight
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "✅ ${weather.safetyStatus}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SuccessGreenLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("stat_card_$title"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextMuted
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor
            )
        }
    }
}

@Composable
fun PriorityBadge(priority: String) {
    val (bg, fg) = when (priority.lowercase()) {
        "urgent" -> Pair(ErrorRedLight, ErrorRed)
        "high" -> Pair(WarningOrangeLight, WarningOrange)
        "medium" -> Pair(AmberGoldLight, AmberGold)
        else -> Pair(SuccessGreenLight, SuccessGreen)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = priority,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = fg
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bg, fg) = when (status.lowercase()) {
        "completed", "approved", "present" -> Pair(SuccessGreenLight, SuccessGreen)
        "in progress", "ongoing" -> Pair(SkyBlueContainer, SkyBlueAccent)
        "planning", "pending", "review", "half day" -> Pair(AmberGoldLight, AmberGold)
        "late", "overtime" -> Pair(WarningOrangeLight, WarningOrange)
        else -> Pair(ErrorRedLight, ErrorRed)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = fg
        )
    }
}
