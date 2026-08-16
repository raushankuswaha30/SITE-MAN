package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProjectEntity
import com.example.data.local.StaffEntity
import com.example.data.local.TaskEntity
import com.example.data.model.AiExtractedFields
import com.example.data.model.AppLanguage
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.UniversalAiFillButton
import com.example.ui.theme.*
import com.example.ui.util.Localization

@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    projects: List<ProjectEntity>,
    currentLanguage: AppLanguage,
    onAddTaskClick: () -> Unit,
    onEditTaskClick: (TaskEntity) -> Unit,
    onDeleteTaskClick: (TaskEntity) -> Unit,
    onStatusChange: (TaskEntity, String) -> Unit,
    onOpenAiFill: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }

    val filteredTasks = tasks.filter {
        val matchesSearch = it.title.contains(searchQuery, ignoreCase = true) ||
                it.assignedTo.contains(searchQuery, ignoreCase = true) ||
                it.projectName.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedStatusFilter) {
            "To Do" -> it.status == "To Do"
            "In Progress" -> it.status == "In Progress"
            "Review" -> it.status == "Review"
            "Completed" -> it.status == "Completed"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                containerColor = DarkBluePrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("tasks_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Localization.tr("tasks", currentLanguage),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${tasks.count { it.status != "Completed" }} Tasks Active",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    UniversalAiFillButton(
                        label = "AI Add Task",
                        onClick = onOpenAiFill
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().testTag("tasks_search_input"),
                    placeholder = { Text("Search task or assigned crew...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Status Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filters = listOf("All", "To Do", "In Progress", "Review", "Completed")
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedStatusFilter == filter,
                            onClick = { selectedStatusFilter = filter },
                            label = { Text(filter) }
                        )
                    }
                }
            }

            // Tasks List
            if (filteredTasks.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No tasks found", color = TextMuted)
                    }
                }
            } else {
                items(filteredTasks) { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_card_${task.id}"),
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
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Checkbox(
                                        checked = task.status == "Completed",
                                        onCheckedChange = { checked ->
                                            onStatusChange(task, if (checked) "Completed" else "In Progress")
                                        }
                                    )
                                    Column {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = "${task.projectName} • ${task.category}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SkyBlueAccent
                                        )
                                    }
                                }
                                PriorityBadge(task.priority)
                            }

                            if (task.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    modifier = Modifier.padding(start = 48.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 48.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextMuted)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(task.assignedTo, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(14.dp), tint = if (task.isOverdue) ErrorRed else TextMuted)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = task.dueDate,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = if (task.isOverdue) ErrorRed else TextMuted,
                                            fontWeight = if (task.isOverdue) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { onEditTaskClick(task) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SkyBlueAccent, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { onDeleteTaskClick(task) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskDialog(
    taskToEdit: TaskEntity?,
    projects: List<ProjectEntity>,
    staffList: List<StaffEntity>,
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit,
    onOpenAiFill: (onResult: (AiExtractedFields) -> Unit) -> Unit
) {
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var assignedTo by remember { mutableStateOf(taskToEdit?.assignedTo ?: (staffList.firstOrNull()?.name ?: "Ramesh Kumar")) }
    var dueDate by remember { mutableStateOf(taskToEdit?.dueDate ?: "2026-08-20") }
    var priority by remember { mutableStateOf(taskToEdit?.priority ?: "Medium") }
    var status by remember { mutableStateOf(taskToEdit?.status ?: "To Do") }
    var selectedProject by remember { mutableStateOf(projects.firstOrNull()?.name ?: "Green Valley Apartment") }
    var selectedProjectId by remember { mutableStateOf(projects.firstOrNull()?.id ?: 1L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (taskToEdit == null) "Create Site Task" else "Edit Task",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                UniversalAiFillButton(
                    label = "AI Fill",
                    onClick = {
                        onOpenAiFill { fields ->
                            fields.taskTitle?.let { title = it }
                            fields.assignedTo?.let { assignedTo = it }
                            fields.dueDate?.let { dueDate = it }
                            fields.priority?.let { priority = it }
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
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    placeholder = { Text("e.g. Concrete curing Block A") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input")
                )

                OutlinedTextField(
                    value = assignedTo,
                    onValueChange = { assignedTo = it },
                    label = { Text("Assigned Crew Member *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (YYYY-MM-DD) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Priority:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Urgent", "High", "Medium", "Low").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Checklist") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val task = taskToEdit?.copy(
                        title = title.ifBlank { "Construction Task" },
                        description = description,
                        assignedTo = assignedTo,
                        dueDate = dueDate,
                        priority = priority,
                        status = status
                    ) ?: TaskEntity(
                        projectId = selectedProjectId,
                        projectName = selectedProject,
                        title = title.ifBlank { "Construction Task" },
                        description = description,
                        assignedTo = assignedTo,
                        dueDate = dueDate,
                        priority = priority,
                        status = status
                    )
                    onSave(task)
                },
                modifier = Modifier.testTag("save_task_dialog_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBluePrimary)
            ) {
                Text("Save Task", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
