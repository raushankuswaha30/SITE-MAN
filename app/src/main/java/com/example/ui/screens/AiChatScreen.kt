package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.AiChatMessageEntity
import com.example.data.model.AppLanguage
import com.example.ui.theme.*
import com.example.ui.util.Localization
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    messages: List<AiChatMessageEntity>,
    isProcessing: Boolean,
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onSendMessage: (String, AppLanguage) -> Unit,
    onClearChat: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // TTS state
    var isTtsReady by remember { mutableStateOf(false) }
    var currentlySpeakingMessageId by remember { mutableStateOf<Long?>(null) }
    var autoSpeakEnabled by remember { mutableStateOf(true) }
    var isListeningVoice by remember { mutableStateOf(false) }
    var voiceError by remember { mutableStateOf<String?>(null) }

    // TTS instance
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }

    fun getLocaleForLanguage(lang: AppLanguage): Locale {
        return try {
            when (lang) {
                AppLanguage.ENGLISH -> Locale.US
                AppLanguage.HINDI, AppLanguage.BHOJPURI -> Locale("hi", "IN")
                AppLanguage.SPANISH -> Locale("es", "ES")
                AppLanguage.FRENCH -> Locale.FRENCH
                AppLanguage.GERMAN -> Locale.GERMAN
                AppLanguage.ARABIC -> Locale("ar")
                AppLanguage.BENGALI -> Locale("bn", "IN")
                AppLanguage.MARATHI -> Locale("mr", "IN")
                AppLanguage.TELUGU -> Locale("te", "IN")
                AppLanguage.TAMIL -> Locale("ta", "IN")
                AppLanguage.GUJARATI -> Locale("gu", "IN")
                AppLanguage.KANNADA -> Locale("kn", "IN")
                AppLanguage.PUNJABI -> Locale("pa", "IN")
                AppLanguage.MALAYALAM -> Locale("ml", "IN")
                AppLanguage.ODIA -> Locale("or", "IN")
                AppLanguage.URDU -> Locale("ur", "PK")
                AppLanguage.PORTUGUESE -> Locale("pt", "PT")
                AppLanguage.RUSSIAN -> Locale("ru", "RU")
                AppLanguage.CHINESE -> Locale.SIMPLIFIED_CHINESE
                AppLanguage.JAPANESE -> Locale.JAPANESE
            }
        } catch (e: Exception) {
            Locale.getDefault()
        }
    }

    DisposableEffect(context) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                try {
                    val locale = getLocaleForLanguage(currentLanguage)
                    tts?.language = locale
                } catch (e: Exception) {
                    // Ignore locale fallback
                }
            }
        }
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                currentlySpeakingMessageId = null
            }
            override fun onError(utteranceId: String?) {
                currentlySpeakingMessageId = null
            }
        })
        ttsInstance = tts

        onDispose {
            tts?.stop()
            tts?.shutdown()
            ttsInstance = null
        }
    }

    // Update TTS language whenever language changes
    LaunchedEffect(currentLanguage, isTtsReady) {
        if (isTtsReady && ttsInstance != null) {
            try {
                val locale = getLocaleForLanguage(currentLanguage)
                ttsInstance?.language = locale
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun speakText(text: String, messageId: Long) {
        if (ttsInstance != null && isTtsReady) {
            if (currentlySpeakingMessageId == messageId) {
                ttsInstance?.stop()
                currentlySpeakingMessageId = null
            } else {
                ttsInstance?.stop()
                currentlySpeakingMessageId = messageId
                try {
                    ttsInstance?.language = getLocaleForLanguage(currentLanguage)
                } catch (e: Exception) {
                    // fallback
                }
                // Strip markdown styling for cleaner speech
                val cleanText = text.replace(Regex("[*#_`~]"), "")
                ttsInstance?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "msg_$messageId")
            }
        }
    }

    fun stopSpeaking() {
        ttsInstance?.stop()
        currentlySpeakingMessageId = null
    }

    // Auto-speak latest AI response if enabled
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
            val latest = messages.last()
            if (latest.sender.equals("ai", ignoreCase = true) && autoSpeakEnabled && isTtsReady) {
                speakText(latest.message, latest.id)
            }
        }
    }

    // Speech Recognizer setup
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            voiceError = "Speech recognition not available on this device"
            return
        }
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListeningVoice = true
                    voiceError = null
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListeningVoice = false
                }
                override fun onError(error: Int) {
                    isListeningVoice = false
                }
                override fun onResults(results: Bundle?) {
                    isListeningVoice = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val spokenText = matches[0]
                        textInput = spokenText
                        onSendMessage(spokenText, currentLanguage)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage.localeTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, currentLanguage.localeTag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, currentLanguage.localeTag)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak in ${currentLanguage.displayName} (${currentLanguage.nativeName})")
        }
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            isListeningVoice = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {}
        isListeningVoice = false
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListening()
        } else {
            voiceError = "Microphone permission required for voice input"
        }
    }

    // Dynamic Quick Questions according to language
    val quickQuestions = remember(currentLanguage) {
        when (currentLanguage) {
            AppLanguage.HINDI, AppLanguage.BHOJPURI -> listOf(
                "इस महीने कुल कितना खर्च हुआ?",
                "कौन सा सामान कम हो रहा है?",
                "आज की हाजिरी का विवरण दिखाओ",
                "ग्रीन वैली प्रोजेक्ट की स्थिति क्या है?",
                "रमेश के लिए फाउंडेशन का नया टास्क बनाओ",
                "सीमेंट और सरिया की सुरक्षा गाइडलाइन बताएं"
            )
            AppLanguage.SPANISH -> listOf(
                "¿Cuánto gastamos este mes?",
                "¿Qué material se está agotando?",
                "Mostrar asistencia de hoy",
                "Crear tarea urgente para cimientos",
                "¿Cuál es el estado de Green Valley?"
            )
            AppLanguage.FRENCH -> listOf(
                "Combien avons-nous dépensé ce mois-ci?",
                "Quel matériau est en rupture de stock?",
                "Afficher le résumé des présences",
                "Créer une tâche pour les fondations",
                "Quel est l'état du chantier?"
            )
            AppLanguage.GERMAN -> listOf(
                "Wie viel haben wir diesen Monat ausgegeben?",
                "Welches Material geht zur Neige?",
                "Heutige Anwesenheit anzeigen",
                "Neue Aufgabe für Fundament erstellen"
            )
            AppLanguage.MARATHI -> listOf(
                "या महिन्यात एकूण किती खर्च झाला?",
                "कोणते साहित्य कमी पडत आहे?",
                "आजची हजेरी दाखवा",
                "रमेशसाठी नवीन कामाची नोंद करा"
            )
            AppLanguage.BENGALI -> listOf(
                "এই মাসে মোট কত খরচ হয়েছে?",
                "কোন উপাদানটি কম আছে?",
                "আজকের উপস্থিতির সারসংক্ষেপ দেখাও",
                "নতুন কাজের টাস্ক তৈরি করুন"
            )
            AppLanguage.TELUGU -> listOf(
                "ఈ నెలలో ఎంత ఖర్చు చేశాము?",
                "ఏ మెటీరియల్ తక్కువగా ఉంది?",
                "ఈ రోజు హాజరు వివరాలు చూపించు",
                "కొత్త టాస్క్ సృష్టించండి"
            )
            AppLanguage.TAMIL -> listOf(
                "இந்த மாதம் எவ்வளவு செலவு செய்தோம்?",
                "எந்த பொருள் குறைவாக உள்ளது?",
                "இன்றைய வருகை விவரத்தைக் காட்டு",
                "புதிய பணியை உருவாக்குங்கள்"
            )
            AppLanguage.ARABIC -> listOf(
                "كم أنفقنا هذا الشهر؟",
                "ما هي المواد التي قاربت على النفاد؟",
                "عرض ملخص الحضور اليومي",
                "إنشاء مهمة جديدة للأساسات"
            )
            else -> listOf(
                "How much did we spend this month?",
                "Which material is running low?",
                "Show today's attendance summary",
                "What is the health score of Green Valley?",
                "Create task for Ramesh to finish foundation",
                "Give safety tips for heavy machinery"
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(SkyBlueAccent, DarkBluePrimary)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AmberGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = Localization.tr("app_title", currentLanguage) + " AI",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SuccessGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Google AI • Fast",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                            Text(
                                text = "Speaks in ${currentLanguage.displayName} (${currentLanguage.nativeName})",
                                style = MaterialTheme.typography.labelSmall,
                                color = SkyBlueAccent
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Auto-Speak Toggle
                    IconButton(
                        onClick = {
                            autoSpeakEnabled = !autoSpeakEnabled
                            if (!autoSpeakEnabled) stopSpeaking()
                        }
                    ) {
                        Icon(
                            imageVector = if (autoSpeakEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Auto-Speak",
                            tint = if (autoSpeakEnabled) AmberGold else TextMuted
                        )
                    }
                    IconButton(onClick = onClearChat) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Clear History")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("ai_chat_screen")
        ) {
            // Horizontal Language Switcher Chip Row
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Language",
                        tint = DarkBluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${Localization.tr("language", currentLanguage)}:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(AppLanguage.values()) { lang ->
                            val isSelected = currentLanguage == lang
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectLanguage(lang) },
                                label = {
                                    Text(
                                        text = lang.nativeName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DarkBluePrimary,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Audio Speaking Visual Banner
            AnimatedVisibility(
                visible = currentlySpeakingMessageId != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = AmberGold.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val infiniteTransition = rememberInfiniteTransition(label = "audio_pulse")
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(500, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "scale"
                            )
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Speaking",
                                tint = DarkBluePrimary,
                                modifier = Modifier.size((18 * scale).dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${Localization.tr("ai_speaking", currentLanguage)} (${currentLanguage.displayName})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = DarkBluePrimary
                            )
                        }
                        TextButton(
                            onClick = { stopSpeaking() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp), tint = ErrorRed)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Localization.tr("stop_speaking", currentLanguage), color = ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(DarkBluePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "SITE MAN AI Copilot",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Ask any construction, budget, attendance, material, or site question in ${currentLanguage.displayName} (${currentLanguage.nativeName}). Answers are delivered with ultra-fast Google AI and spoken aloud.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                items(messages) { msg ->
                    val isUser = msg.sender.equals("user", ignoreCase = true)
                    val isSpeakingThis = currentlySpeakingMessageId == msg.id

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        if (!isUser) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(DarkBluePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = AmberGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
                            Card(
                                modifier = Modifier
                                    .widthIn(max = 310.dp)
                                    .testTag("chat_bubble_${msg.id}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) DarkBluePrimary else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 4.dp,
                                    bottomEnd = if (isUser) 4.dp else 16.dp
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                border = if (isSpeakingThis) CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(AmberGold, SkyBlueAccent))) else null
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = msg.message,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 21.sp
                                        )
                                    )

                                    if (!isUser) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { speakText(msg.message, msg.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isSpeakingThis) Icons.Default.StopCircle else Icons.Default.VolumeUp,
                                                    contentDescription = "Speak Response",
                                                    tint = if (isSpeakingThis) AmberGold else SkyBlueAccent,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isProcessing) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 40.dp, top = 4.dp, bottom = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = SkyBlueAccent
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Localization.tr("ai_thinking", currentLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Quick Question Chips
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = Localization.tr("quick_questions_title", currentLanguage),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextMuted
                        )
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickQuestions) { q ->
                            SuggestionChip(
                                onClick = {
                                    onSendMessage(q, currentLanguage)
                                },
                                label = {
                                    Text(
                                        text = q,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                )
                            )
                        }
                    }
                }
            }

            // Listening Voice Overlay Indicator
            AnimatedVisibility(visible = isListeningVoice) {
                Surface(
                    color = DarkBluePrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = AmberGold,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Listening in ${currentLanguage.displayName} (${currentLanguage.nativeName})...",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        TextButton(onClick = { stopListening() }) {
                            Text("Done", color = AmberGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voice input mic button
                    IconButton(
                        onClick = {
                            if (isListeningVoice) {
                                stopListening()
                            } else {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    startListening()
                                } else {
                                    recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListeningVoice) ErrorRed.copy(alpha = 0.2f)
                                else SkyBlueAccent.copy(alpha = 0.15f)
                            )
                            .testTag("ai_chat_mic_btn")
                    ) {
                        Icon(
                            imageVector = if (isListeningVoice) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = Localization.tr("voice_input", currentLanguage),
                            tint = if (isListeningVoice) ErrorRed else DarkBluePrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_chat_input"),
                        placeholder = {
                            Text(
                                "${Localization.tr("ask_ai", currentLanguage)}...",
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                val msg = textInput
                                textInput = ""
                                onSendMessage(msg, currentLanguage)
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(DarkBluePrimary)
                            .testTag("ai_chat_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
