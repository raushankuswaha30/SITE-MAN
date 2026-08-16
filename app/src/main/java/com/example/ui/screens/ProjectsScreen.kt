package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProjectEntity
import com.example.data.model.AiExtractedFields
import com.example.data.model.AppLanguage
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.UniversalAiFillButton
import com.example.ui.theme.*
import com.example.ui.util.Localization

@Composable
fun ProjectsScreen(
    projects: List<ProjectEntity>,
    currentLanguage: AppLanguage,
    onProjectClick: (Long) -> Unit,
    onAddProjectClick: () -> Unit,
    onEditProjectClick: (ProjectEntity) -> Unit,
    onDeleteProjectClick: (ProjectEntity) -> Unit,
    onOpenAiFill: () -> Unit,
    onScanBillClick: () -> Unit = {},
    onSaveReport: (com.example.data.local.ReportEntity) -> Unit = {},
    onSaveTask: (com.example.data.local.TaskEntity) -> Unit = {}
) {
    ProjectDashboardScreen(
        projects = projects,
        currentLanguage = currentLanguage,
        onProjectClick = onProjectClick,
        onAddProjectClick = onAddProjectClick,
        onEditProjectClick = onEditProjectClick,
        onDeleteProjectClick = onDeleteProjectClick,
        onOpenAiFill = onOpenAiFill,
        onScanBillClick = onScanBillClick,
        onSaveReport = onSaveReport,
        onSaveTask = onSaveTask
    )
}

@Composable
fun ProjectDetailScreen(
    project: ProjectEntity?,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    if (project == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Project not found")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("project_detail_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Project Overview",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SkyBlueAccent)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Client: ${project.clientName} • ${project.location}",
                            style = MaterialTheme.typography.bodySmall.copy(color = SkyBlueLight)
                        )
                    }
                    StatusBadge(project.status)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Budget", style = MaterialTheme.typography.labelSmall, color = SkyBlueLight)
                        Text("₹${(project.budget / 100000).toInt()} Lakhs", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AmberGold))
                    }
                    Column {
                        Text("Total Spent", style = MaterialTheme.typography.labelSmall, color = SkyBlueLight)
                        Text("₹${(project.spentAmount / 100000).toInt()} Lakhs", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White))
                    }
                    Column {
                        Text("Health Score", style = MaterialTheme.typography.labelSmall, color = SkyBlueLight)
                        Text("${project.healthScore}%", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SuccessGreenLight))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Completion Progress (${project.progressPercent}%)", style = MaterialTheme.typography.labelSmall, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { project.progressPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AmberGold,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Key Timeline details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Project Timeline & Notes", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Start Date", color = TextMuted)
                    Text(project.startDate, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Target Completion", color = TextMuted)
                    Text(project.targetCompletion, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Divider()
                Spacer(modifier = Modifier.height(10.dp))
                Text("Site Notes & Blueprint Scope:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (project.notes.isNotBlank()) project.notes else "No additional notes specified for this site.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProjectDialog(
    projectToEdit: ProjectEntity?,
    onDismiss: () -> Unit,
    onSave: (ProjectEntity) -> Unit,
    onOpenAiFill: (onResult: (AiExtractedFields) -> Unit) -> Unit
) {
    var name by remember { mutableStateOf(projectToEdit?.name ?: "") }
    var clientName by remember { mutableStateOf(projectToEdit?.clientName ?: "") }
    var location by remember { mutableStateOf(projectToEdit?.location ?: "Raipur") }
    var budgetStr by remember { mutableStateOf(projectToEdit?.budget?.toInt()?.toString() ?: "2500000") }
    var startDate by remember { mutableStateOf(projectToEdit?.startDate ?: "2026-09-01") }
    var targetCompletion by remember { mutableStateOf(projectToEdit?.targetCompletion ?: "2027-05-01") }
    var status by remember { mutableStateOf(projectToEdit?.status ?: "Ongoing") }
    var notes by remember { mutableStateOf(projectToEdit?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (projectToEdit == null) "New Construction Project" else "Edit Project",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                UniversalAiFillButton(
                    label = "AI Fill",
                    onClick = {
                        onOpenAiFill { fields ->
                            fields.projectName?.let { name = it }
                            fields.clientName?.let { clientName = it }
                            fields.location?.let { location = it }
                            fields.budget?.let { budgetStr = it.toInt().toString() }
                            fields.startDate?.let { startDate = it }
                            fields.targetCompletion?.let { targetCompletion = it }
                        }
                    }
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name *") },
                    placeholder = { Text("e.g. Green Valley Apartment") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("project_name_input")
                )

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Client / Developer Name *") },
                    placeholder = { Text("e.g. ABC Construction") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Site Location *") },
                    placeholder = { Text("e.g. Raipur Sector 4") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = budgetStr,
                    onValueChange = { budgetStr = it },
                    label = { Text("Project Budget (₹) *") },
                    placeholder = { Text("e.g. 2500000") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = targetCompletion,
                        onValueChange = { targetCompletion = it },
                        label = { Text("Target Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Scope of Work") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val budgetVal = budgetStr.toDoubleOrNull() ?: 1000000.0
                    val entity = projectToEdit?.copy(
                        name = name.ifBlank { "New Project Site" },
                        clientName = clientName.ifBlank { "Client" },
                        location = location.ifBlank { "Raipur" },
                        budget = budgetVal,
                        startDate = startDate,
                        targetCompletion = targetCompletion,
                        status = status,
                        notes = notes
                    ) ?: ProjectEntity(
                        name = name.ifBlank { "New Project Site" },
                        clientName = clientName.ifBlank { "Client" },
                        location = location.ifBlank { "Raipur" },
                        budget = budgetVal,
                        spentAmount = 0.0,
                        startDate = startDate,
                        targetCompletion = targetCompletion,
                        status = status,
                        notes = notes
                    )
                    onSave(entity)
                },
                modifier = Modifier.testTag("save_project_dialog_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBluePrimary)
            ) {
                Text("Save Project", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
