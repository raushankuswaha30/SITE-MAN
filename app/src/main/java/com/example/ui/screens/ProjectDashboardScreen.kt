package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.ProjectEntity
import com.example.data.local.ReportEntity
import com.example.data.local.TaskEntity
import com.example.data.model.AppLanguage
import com.example.ui.components.StatusBadge
import com.example.ui.components.UniversalAiFillButton
import com.example.ui.theme.*
import com.example.ui.util.Localization
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDashboardScreen(
    projects: List<ProjectEntity>,
    currentLanguage: AppLanguage,
    onProjectClick: (Long) -> Unit,
    onAddProjectClick: () -> Unit,
    onEditProjectClick: (ProjectEntity) -> Unit,
    onDeleteProjectClick: (ProjectEntity) -> Unit,
    onOpenAiFill: () -> Unit,
    onScanBillClick: () -> Unit = {},
    onSaveReport: (ReportEntity) -> Unit = {},
    onSaveTask: (TaskEntity) -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var isGridView by remember { mutableStateOf(true) }
    var showVoiceDictationDialog by remember { mutableStateOf(false) }

    // Status counts
    val ongoingProjects = projects.filter { it.status.equals("Ongoing", ignoreCase = true) }
    val planningProjects = projects.filter { it.status.equals("Planning", ignoreCase = true) }
    val completedProjects = projects.filter { it.status.equals("Completed", ignoreCase = true) }
    val onHoldProjects = projects.filter { it.status.equals("On Hold", ignoreCase = true) }

    // Summary calculations for ongoing / active portfolio
    val ongoingBudget = ongoingProjects.sumOf { it.budget }
    val ongoingSpent = ongoingProjects.sumOf { it.spentAmount }
    val ongoingAvgProgress = if (ongoingProjects.isNotEmpty()) ongoingProjects.map { it.progressPercent }.average().toInt() else 0
    val ongoingTotalCrew = ongoingProjects.sumOf { it.teamCount }
    val ongoingAvgHealth = if (ongoingProjects.isNotEmpty()) ongoingProjects.map { it.healthScore }.average().toInt() else 100

    val filteredProjects = projects.filter { project ->
        val matchesSearch = project.name.contains(searchQuery, ignoreCase = true) ||
                project.clientName.contains(searchQuery, ignoreCase = true) ||
                project.location.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedStatusFilter) {
            "Ongoing" -> project.status.equals("Ongoing", ignoreCase = true)
            "Planning" -> project.status.equals("Planning", ignoreCase = true)
            "Completed" -> project.status.equals("Completed", ignoreCase = true)
            "On Hold" -> project.status.equals("On Hold", ignoreCase = true)
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Voice Dictation FAB
                SmallFloatingActionButton(
                    onClick = { showVoiceDictationDialog = true },
                    containerColor = SkyBlueAccent,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("voice_dictation_fab")
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Dictate Report or Task")
                }

                // Add Project FAB
                FloatingActionButton(
                    onClick = onAddProjectClick,
                    containerColor = DarkBluePrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_project_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Project")
                }
            }
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = if (isGridView) GridCells.Adaptive(minSize = 165.dp) else GridCells.Fixed(1),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("project_dashboard_grid_screen"),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Section: Title, Quick Actions & Layout Toggle
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Project Dashboard",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${ongoingProjects.size} Ongoing Sites • ${projects.size} Total Projects",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Voice Dictation Action
                            IconButton(
                                onClick = { showVoiceDictationDialog = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SkyBlueAccent.copy(alpha = 0.15f))
                                    .testTag("header_voice_dictate_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Dictate",
                                    tint = SkyBlueAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // CameraX Bill Scan Action
                            IconButton(
                                onClick = onScanBillClick,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen.copy(alpha = 0.15f))
                                    .testTag("header_scan_bill_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Scan Bill",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Grid / List toggle
                            IconButton(
                                onClick = { isGridView = !isGridView },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                    contentDescription = "Toggle View",
                                    tint = DarkBluePrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            UniversalAiFillButton(
                                label = "AI New",
                                onClick = onOpenAiFill
                            )
                        }
                    }

                    // Ongoing Projects Summary Hero Card
                    OngoingSummaryHeroCard(
                        ongoingCount = ongoingProjects.size,
                        totalCount = projects.size,
                        totalBudget = ongoingBudget,
                        totalSpent = ongoingSpent,
                        avgProgress = ongoingAvgProgress,
                        totalCrew = ongoingTotalCrew,
                        avgHealth = ongoingAvgHealth,
                        onScanBillClick = onScanBillClick,
                        onVoiceDictateClick = { showVoiceDictationDialog = true }
                    )
                }
            }

            // Status Distribution Chart (Recharts / Progress Summary Chart)
            item(span = { GridItemSpan(maxLineSpan) }) {
                ProjectStatusDistributionChart(
                    totalProjects = projects.size,
                    ongoingCount = ongoingProjects.size,
                    planningCount = planningProjects.size,
                    completedCount = completedProjects.size,
                    onHoldCount = onHoldProjects.size,
                    selectedFilter = selectedStatusFilter,
                    onSelectFilter = { selectedStatusFilter = it }
                )
            }

            // Search Bar & Filter Chips
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_dashboard_search_input"),
                        placeholder = { Text("Search by site name, client, or location...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SkyBlueAccent) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Filter Chips Row with dynamic status counts
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val filters = listOf(
                            FilterOption("All", projects.size),
                            FilterOption("Ongoing", ongoingProjects.size),
                            FilterOption("Planning", planningProjects.size),
                            FilterOption("Completed", completedProjects.size),
                            FilterOption("On Hold", onHoldProjects.size)
                        )
                        items(filters) { option ->
                            FilterChip(
                                selected = selectedStatusFilter == option.label,
                                onClick = { selectedStatusFilter = option.label },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(option.label)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            color = if (selectedStatusFilter == option.label) DarkBluePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = "${option.count}",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (selectedStatusFilter == option.label) Color.White else TextMuted
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }

            // Projects Grid Items
            if (filteredProjects.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apartment,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Projects Found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "No construction projects match '$selectedStatusFilter' filter.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else {
                items(filteredProjects, key = { it.id }) { project ->
                    if (isGridView) {
                        ProjectGridCard(
                            project = project,
                            onClick = { onProjectClick(project.id) },
                            onEdit = { onEditProjectClick(project) },
                            onDelete = { onDeleteProjectClick(project) }
                        )
                    } else {
                        ProjectListCard(
                            project = project,
                            onClick = { onProjectClick(project.id) },
                            onEdit = { onEditProjectClick(project) },
                            onDelete = { onDeleteProjectClick(project) }
                        )
                    }
                }
            }
        }
    }

    // Voice Dictation Modal Dialog for Site Reports & Task Updates
    if (showVoiceDictationDialog) {
        VoiceToTextDictationDialog(
            projects = projects,
            onDismiss = { showVoiceDictationDialog = false },
            onSaveReport = { report ->
                onSaveReport(report)
                showVoiceDictationDialog = false
                Toast.makeText(context, "Site Report Saved Successfully!", Toast.LENGTH_SHORT).show()
            },
            onSaveTask = { task ->
                onSaveTask(task)
                showVoiceDictationDialog = false
                Toast.makeText(context, "Task Update Saved Successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

private data class FilterOption(val label: String, val count: Int)

/**
 * Visual Progress Summary Chart for Project Status Distribution (Recharts-inspired design).
 */
@Composable
fun ProjectStatusDistributionChart(
    totalProjects: Int,
    ongoingCount: Int,
    planningCount: Int,
    completedCount: Int,
    onHoldCount: Int,
    selectedFilter: String,
    onSelectFilter: (String) -> Unit
) {
    val ongoingPct = if (totalProjects > 0) (ongoingCount * 100f / totalProjects) else 0f
    val planningPct = if (totalProjects > 0) (planningCount * 100f / totalProjects) else 0f
    val completedPct = if (totalProjects > 0) (completedCount * 100f / totalProjects) else 0f
    val onHoldPct = if (totalProjects > 0) (onHoldCount * 100f / totalProjects) else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_status_distribution_chart"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title & Status Metric Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = DarkBluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Project Status Distribution",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "$totalProjects Projects",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DarkBluePrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Multi-segment Horizontal Stacked Progress Bar (Custom Recharts visualizer)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
            ) {
                val totalWidth = size.width
                val barHeight = size.height

                if (totalProjects == 0) {
                    drawRoundRect(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        topLeft = Offset.Zero,
                        size = Size(totalWidth, barHeight),
                        cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
                    )
                } else {
                    var currentX = 0f
                    val gap = 3.dp.toPx()

                    // Ongoing segment (Green)
                    if (ongoingCount > 0) {
                        val segWidth = (ongoingPct / 100f) * totalWidth
                        drawRoundRect(
                            color = SuccessGreen,
                            topLeft = Offset(currentX, 0f),
                            size = Size(segWidth - (if (totalProjects > ongoingCount) gap else 0f), barHeight),
                            cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
                        )
                        currentX += segWidth
                    }

                    // Planning segment (Sky Blue)
                    if (planningCount > 0) {
                        val segWidth = (planningPct / 100f) * totalWidth
                        drawRoundRect(
                            color = SkyBlueAccent,
                            topLeft = Offset(currentX, 0f),
                            size = Size(segWidth - (if (totalProjects > planningCount) gap else 0f), barHeight),
                            cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
                        )
                        currentX += segWidth
                    }

                    // Completed segment (Amber/Gold)
                    if (completedCount > 0) {
                        val segWidth = (completedPct / 100f) * totalWidth
                        drawRoundRect(
                            color = AmberGold,
                            topLeft = Offset(currentX, 0f),
                            size = Size(segWidth - (if (totalProjects > completedCount) gap else 0f), barHeight),
                            cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
                        )
                        currentX += segWidth
                    }

                    // On Hold segment (Muted Slate / Red)
                    if (onHoldCount > 0) {
                        val segWidth = (onHoldPct / 100f) * totalWidth
                        drawRoundRect(
                            color = ErrorRed,
                            topLeft = Offset(currentX, 0f),
                            size = Size(segWidth, barHeight),
                            cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Status Legend & Percentages
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusMetricLegendItem(
                    label = "Active / Ongoing",
                    count = ongoingCount,
                    percent = ongoingPct.toInt(),
                    color = SuccessGreen,
                    isSelected = selectedFilter == "Ongoing",
                    onClick = { onSelectFilter(if (selectedFilter == "Ongoing") "All" else "Ongoing") }
                )

                StatusMetricLegendItem(
                    label = "Planning",
                    count = planningCount,
                    percent = planningPct.toInt(),
                    color = SkyBlueAccent,
                    isSelected = selectedFilter == "Planning",
                    onClick = { onSelectFilter(if (selectedFilter == "Planning") "All" else "Planning") }
                )

                StatusMetricLegendItem(
                    label = "Completed",
                    count = completedCount,
                    percent = completedPct.toInt(),
                    color = AmberGold,
                    isSelected = selectedFilter == "Completed",
                    onClick = { onSelectFilter(if (selectedFilter == "Completed") "All" else "Completed") }
                )

                StatusMetricLegendItem(
                    label = "On Hold",
                    count = onHoldCount,
                    percent = onHoldPct.toInt(),
                    color = ErrorRed,
                    isSelected = selectedFilter == "On Hold",
                    onClick = { onSelectFilter(if (selectedFilter == "On Hold") "All" else "On Hold") }
                )
            }
        }
    }
}

@Composable
private fun StatusMetricLegendItem(
    label: String,
    count: Int,
    percent: Int,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) color.copy(alpha = 0.12f) else Color.Transparent,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, color) else null,
        modifier = Modifier.padding(horizontal = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$count sites",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextMuted
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = TextMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun OngoingSummaryHeroCard(
    ongoingCount: Int,
    totalCount: Int,
    totalBudget: Double,
    totalSpent: Double,
    avgProgress: Int,
    totalCrew: Int,
    avgHealth: Int,
    onScanBillClick: () -> Unit = {},
    onVoiceDictateClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ONGOING SITES SUMMARY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = SkyBlueLight
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$ongoingCount Active Sites",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                // Health Score Pill
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = SuccessGreenLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$avgHealth% Health",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreenLight
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metric Counters Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Ongoing Budget", style = MaterialTheme.typography.labelSmall, color = SkyBlueLight)
                    Text(
                        text = "₹${(totalBudget / 100000).toInt()}L",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = AmberGold
                        )
                    )
                }
                Column {
                    Text("Total Spent", style = MaterialTheme.typography.labelSmall, color = SkyBlueLight)
                    Text(
                        text = "₹${(totalSpent / 100000).toInt()}L",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    )
                }
                Column {
                    Text("Active Crew", style = MaterialTheme.typography.labelSmall, color = SkyBlueLight)
                    Text(
                        text = "$totalCrew Workers",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = SkyBlueLight
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Average Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Average Ongoing Completion",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "$avgProgress%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AmberGold
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (avgProgress / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AmberGold,
                trackColor = Color.White.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Fast Actions Bar inside Hero: Voice Dictate & Camera Scan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onVoiceDictateClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlueAccent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dictate Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = onScanBillClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White.copy(alpha = 0.18f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan Bill OCR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Voice-to-Text Dictation Dialog with real-time Android SpeechRecognizer and Microphone permission flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceToTextDictationDialog(
    projects: List<ProjectEntity>,
    onDismiss: () -> Unit,
    onSaveReport: (ReportEntity) -> Unit,
    onSaveTask: (TaskEntity) -> Unit
) {
    val context = LocalContext.current
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (!granted) {
            Toast.makeText(context, "Microphone permission is required to dictate site updates", Toast.LENGTH_SHORT).show()
        }
    }

    var dictationMode by remember { mutableStateOf("Report") } // "Report" or "Task"
    var transcribedText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var selectedProject by remember { mutableStateOf(projects.firstOrNull() ?: ProjectEntity(name = "General Site", clientName = "Client", location = "Site A", budget = 100000.0, startDate = "2026-08-01", targetCompletion = "2026-12-31")) }
    var taskPriority by remember { mutableStateOf("Medium") }
    var taskAssignedTo by remember { mutableStateOf("Site Engineer") }

    // Pulsating animation for mic
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Cleanup SpeechRecognizer on dispose
    DisposableEffect(Unit) {
        onDispose {
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun startListening() {
        if (!hasMicPermission) {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Toast.makeText(context, "Speech Recognition not available on this device", Toast.LENGTH_SHORT).show()
            // Provide simulated prompt template
            if (transcribedText.isBlank()) {
                transcribedText = if (dictationMode == "Report") {
                    "Site inspection done at Sector 4. Foundation slab casting completed for Block B. 18 workers present on site."
                } else {
                    "Inspect reinforcement bars on 2nd floor before concrete pour tomorrow at 9 AM."
                }
            }
            return
        }

        try {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Dictate site report or task update...")
            }

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    isListening = false
                }
                override fun onError(error: Int) {
                    isListening = false
                }
                override fun onResults(results: Bundle?) {
                    isListening = false
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val speech = matches[0]
                        transcribedText = if (transcribedText.isBlank()) speech else "$transcribedText $speech"
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val speech = matches[0]
                        if (speech.isNotBlank()) {
                            transcribedText = speech
                        }
                    }
                }
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            recognizer.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            isListening = false
            Toast.makeText(context, "Error starting voice recognition: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // ignore
        }
        isListening = false
    }

    AlertDialog(
        onDismissRequest = {
            stopListening()
            onDismiss()
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = SkyBlueAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Voice-to-Text Dictation",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Target Mode Selector: Site Report vs Task Update
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { dictationMode = "Report" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (dictationMode == "Report") DarkBluePrimary else Color.Transparent,
                            contentColor = if (dictationMode == "Report") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        elevation = null,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Site Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { dictationMode = "Task" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (dictationMode == "Task") DarkBluePrimary else Color.Transparent,
                            contentColor = if (dictationMode == "Task") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        elevation = null,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Task Update", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Project Selector
                if (projects.isNotEmpty()) {
                    Text(
                        text = "Target Project: ${selectedProject.name}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = SkyBlueAccent
                    )
                }

                // Central Microphone Button & Pulsing status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(72.dp)
                                .scale(if (isListening) pulseScale else 1f)
                                .clip(CircleShape)
                                .background(if (isListening) ErrorRed else SkyBlueAccent)
                                .clickable {
                                    if (isListening) stopListening() else startListening()
                                }
                                .testTag("mic_record_button")
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Mic",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isListening) "Listening... Speak your site update clearly" else "Tap microphone to speak",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isListening) FontWeight.Bold else FontWeight.Normal,
                                color = if (isListening) ErrorRed else TextMuted
                            )
                        )
                    }
                }

                // Transcribed Text Editor Field
                OutlinedTextField(
                    value = transcribedText,
                    onValueChange = { transcribedText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("dictation_transcription_input"),
                    placeholder = {
                        Text(
                            if (dictationMode == "Report")
                                "Dictated site report appears here... (e.g. 'Completed 5th floor brickwork, 24 workers present')"
                            else
                                "Dictated task update appears here... (e.g. 'Check plumbing leakage in Block A before painting')"
                        )
                    },
                    label = { Text("Transcribed Site Text") },
                    trailingIcon = {
                        if (transcribedText.isNotEmpty()) {
                            IconButton(onClick = { transcribedText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick preset templates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AssistChip(
                        onClick = {
                            transcribedText = if (dictationMode == "Report")
                                "Daily Site Log: Casting of column C14 completed. 15 masons on duty. Weather clear."
                            else
                                "Verify electrical wiring in basement section B before 5 PM."
                        },
                        label = { Text("Quick Template", fontSize = 10.sp) },
                        leadingIcon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(12.dp)) }
                    )

                    AssistChip(
                        onClick = {
                            transcribedText = "Safety inspection passed. Scaffolding tightened on exterior wall."
                        },
                        label = { Text("Safety Log", fontSize = 10.sp) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (transcribedText.isBlank()) {
                        Toast.makeText(context, "Please speak or enter text first", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                    if (dictationMode == "Report") {
                        val report = ReportEntity(
                            title = "Site Report: ${selectedProject.name}",
                            type = "Site Daily Report",
                            generatedDate = todayStr,
                            summaryText = transcribedText,
                            recommendationsText = "Logged via SiteMan Voice Dictation on ${selectedProject.name}.",
                            totalBudget = selectedProject.budget,
                            totalExpense = selectedProject.spentAmount,
                            attendanceRate = 96,
                            completedTasks = 12,
                            pendingTasks = 3
                        )
                        onSaveReport(report)
                    } else {
                        val task = TaskEntity(
                            projectId = selectedProject.id,
                            projectName = selectedProject.name,
                            title = transcribedText.take(60),
                            description = transcribedText,
                            assignedTo = taskAssignedTo,
                            dueDate = todayStr,
                            priority = taskPriority,
                            status = "In Progress",
                            progress = 50,
                            category = "Civil Work"
                        )
                        onSaveTask(task)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkBluePrimary),
                modifier = Modifier.testTag("save_dictation_btn")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save ${dictationMode}")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                stopListening()
                onDismiss()
            }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProjectGridCard(
    project: ProjectEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("project_grid_card_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Top Row: Status badge & Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = project.status)

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Overview") },
                            leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMenu = false
                                onClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Project") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp), tint = SkyBlueAccent) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Site", color = ErrorRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = ErrorRed) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Project Title & Location
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = project.location,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Progress & Health Ring Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Circular Progress Indicator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(46.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { (project.progressPercent / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxSize(),
                        color = when {
                            project.progressPercent >= 75 -> SuccessGreen
                            project.progressPercent >= 30 -> SkyBlueAccent
                            else -> AmberGold
                        },
                        strokeWidth = 4.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = StrokeCap.Round
                    )
                    Text(
                        text = "${project.progressPercent}%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp
                        )
                    )
                }

                // Health & Crew column
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = if (project.healthScore >= 80) SuccessGreen.copy(alpha = 0.12f) else AmberGold.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = if (project.healthScore >= 80) SuccessGreen else AmberGold,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${project.healthScore}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (project.healthScore >= 80) SuccessGreen else AmberGold
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${project.teamCount} Crew",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Budget vs Spent mini metric
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Budget: ₹${(project.budget / 100000).toInt()}L",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextMuted
                )
                Text(
                    text = "Spent: ₹${(project.spentAmount / 100000).toInt()}L",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = DarkBlueSecondary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Target Date badge
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Due: ${project.targetCompletion}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectListCard(
    project: ProjectEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("project_list_card_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${project.clientName} • ${project.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                StatusBadge(project.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Budget & Spend row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Budget", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("₹${(project.budget / 100000).toInt()} Lakhs", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                }
                Column {
                    Text("Spent", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("₹${(project.spentAmount / 100000).toInt()} Lakhs", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = DarkBlueSecondary))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Health Score", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("${project.healthScore}%", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = SuccessGreen))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Progress", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text("${project.progressPercent}%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SkyBlueAccent))
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { project.progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = SkyBlueAccent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextMuted)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${project.teamCount} Crew", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextMuted)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Target: ${project.targetCompletion}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SkyBlueAccent, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
