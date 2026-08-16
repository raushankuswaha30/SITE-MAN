package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MaterialEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.TaskEntity
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.util.Localization

@Composable
fun DashboardScreen(
    currentRole: UserRole,
    currentLanguage: AppLanguage,
    weatherInfo: WeatherInfo,
    projectHealth: ProjectHealthDetails,
    aiSuggestions: List<AiSuggestion>,
    projects: List<ProjectEntity>,
    tasks: List<TaskEntity>,
    lowStockMaterials: List<MaterialEntity>,
    staffCount: Int,
    presentCount: Int,
    totalExpense: Double,
    onNavigateToProjects: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToMaterials: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToStaff: () -> Unit,
    onOpenAiFill: (targetForm: String) -> Unit,
    onProjectClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Welcome & Role Context Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = DarkBluePrimary
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Welcome, Vikramaditya",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Role: ${currentRole.title}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = SkyBlueLight
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AmberGold)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PRO SITE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DarkBluePrimary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickStatsPill("Projects", "${projects.size}", SkyBlueLight)
                        QuickStatsPill("Workforce", "$presentCount/$staffCount", SuccessGreenLight)
                        QuickStatsPill("Expense", "₹${(totalExpense / 100000).toInt()}L", AmberGoldLight)
                        QuickStatsPill("Pending", "${tasks.filter { it.status != "Completed" }.size}", WarningOrangeLight)
                    }
                }
            }
        }

        // 2. Weather & Construction Safety Card
        item {
            WeatherWidgetCard(weather = weatherInfo)
        }

        // 3. AI Insights & Actionable Recommendations Carousel
        if (aiSuggestions.isNotEmpty()) {
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SkyBlueAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Insights & Recommendations",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "${aiSuggestions.size} Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = SkyBlueAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(aiSuggestions) { sug ->
                            Card(
                                modifier = Modifier
                                    .width(280.dp)
                                    .testTag("ai_suggestion_${sug.id}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        PriorityBadge(sug.urgency)
                                        Icon(
                                            imageVector = when (sug.category) {
                                                "material" -> Icons.Default.Inventory
                                                "task" -> Icons.Default.Assignment
                                                "attendance" -> Icons.Default.AccessTime
                                                else -> Icons.Default.AccountBalanceWallet
                                            },
                                            contentDescription = null,
                                            tint = SkyBlueAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = sug.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = sug.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedButton(
                                        onClick = {
                                            when (sug.category) {
                                                "material" -> onNavigateToMaterials()
                                                "task" -> onNavigateToTasks()
                                                "attendance" -> onNavigateToAttendance()
                                                else -> onNavigateToExpenses()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(sug.actionText, style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Project Health Gauge
        item {
            ProjectHealthGauge(health = projectHealth)
        }

        // 5. Quick Actions Hub
        item {
            Column {
                Text(
                    text = Localization.tr("quick_actions", currentLanguage),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        title = "AI Fill",
                        icon = Icons.Default.AutoAwesome,
                        color = SkyBlueAccent,
                        modifier = Modifier.weight(1f)
                    ) {
                        onOpenAiFill("Project")
                    }
                    QuickActionButton(
                        title = "Scan Bill",
                        icon = Icons.Default.QrCodeScanner,
                        color = AmberGold,
                        modifier = Modifier.weight(1f)
                    ) {
                        onOpenAiFill("Expense")
                    }
                    QuickActionButton(
                        title = "Attendance",
                        icon = Icons.Default.Fingerprint,
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigateToAttendance()
                    }
                    QuickActionButton(
                        title = "Add Task",
                        icon = Icons.Default.AddTask,
                        color = DarkBluePrimary,
                        modifier = Modifier.weight(1f)
                    ) {
                        onOpenAiFill("Task")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        title = "Add Expense",
                        icon = Icons.Default.ReceiptLong,
                        color = DarkBlueSecondary,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigateToExpenses()
                    }
                    QuickActionButton(
                        title = "Materials",
                        icon = Icons.Default.Inventory2,
                        color = AmberGold,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigateToMaterials()
                    }
                    QuickActionButton(
                        title = "Reports",
                        icon = Icons.Default.Assessment,
                        color = SkyBlueAccent,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigateToReports()
                    }
                    QuickActionButton(
                        title = "Staff",
                        icon = Icons.Default.Groups,
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigateToStaff()
                    }
                }
            }
        }

        // 6. Low Stock Alert Banner (if any)
        if (lowStockMaterials.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = WarningOrangeLight.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(WarningOrange, AmberGold)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = WarningOrange,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Low Stock Alert: ${lowStockMaterials.first().name}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextDark
                                )
                                Text(
                                    text = "${lowStockMaterials.first().currentQuantity} ${lowStockMaterials.first().unit} remaining (Min: ${lowStockMaterials.first().minThreshold})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                        Button(
                            onClick = onNavigateToMaterials,
                            colors = ButtonDefaults.buttonColors(containerColor = WarningOrange),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Reorder", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // 7. Ongoing Projects Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.tr("ongoing_projects", currentLanguage),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onNavigateToProjects) {
                    Text("View All (${projects.size})", color = SkyBlueAccent)
                }
            }
        }

        items(projects.take(2)) { project ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProjectClick(project.id) }
                    .testTag("dashboard_project_${project.id}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Budget: ₹${(project.budget / 100000).toInt()}L",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "${project.progressPercent}% Completed",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SkyBlueAccent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { project.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SkyBlueAccent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // 8. Pending Tasks preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.tr("pending_tasks", currentLanguage),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onNavigateToTasks) {
                    Text("View Tasks (${tasks.size})", color = SkyBlueAccent)
                }
            }
        }

        items(tasks.filter { it.status != "Completed" }.take(2)) { task ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToTasks() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Assigned: ${task.assignedTo} • Due: ${task.dueDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    PriorityBadge(task.priority)
                }
            }
        }
    }
}

@Composable
private fun QuickStatsPill(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = color))
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp))
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("quick_action_$title"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
