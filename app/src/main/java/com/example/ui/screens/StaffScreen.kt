package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.example.data.local.StaffEntity
import com.example.data.model.AiExtractedFields
import com.example.data.model.AppLanguage
import com.example.ui.components.UniversalAiFillButton
import com.example.ui.theme.*
import com.example.ui.util.Localization

@Composable
fun StaffScreen(
    staffList: List<StaffEntity>,
    currentLanguage: AppLanguage,
    onAddStaffClick: () -> Unit,
    onEditStaffClick: (StaffEntity) -> Unit,
    onDeleteStaffClick: (StaffEntity) -> Unit,
    onOpenAiFill: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredStaff = staffList.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.role.contains(searchQuery, ignoreCase = true) ||
                it.department.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddStaffClick,
                containerColor = SuccessGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_staff_fab")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Staff")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("staff_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Localization.tr("staff", currentLanguage),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${staffList.size} Active Crew & Supervisory Engineers",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    UniversalAiFillButton(
                        label = "AI Add Staff",
                        onClick = onOpenAiFill
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search staff by name or role...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (filteredStaff.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No staff members found", color = TextMuted)
                    }
                }
            } else {
                items(filteredStaff) { staff ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("staff_card_${staff.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(SkyBlueContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Engineering, contentDescription = null, tint = SkyBlueAccent)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = staff.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${staff.role} • ${staff.assignedProjectName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SkyBlueAccent
                                    )
                                    Text(
                                        text = "Phone: ${staff.phone} • Daily Wage: ₹${staff.dailySalary.toInt()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }

                            Row {
                                IconButton(onClick = { onEditStaffClick(staff) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SkyBlueAccent, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { onDeleteStaffClick(staff) }, modifier = Modifier.size(28.dp)) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditStaffDialog(
    staffToEdit: StaffEntity?,
    onDismiss: () -> Unit,
    onSave: (StaffEntity) -> Unit,
    onOpenAiFill: (onResult: (AiExtractedFields) -> Unit) -> Unit
) {
    var name by remember { mutableStateOf(staffToEdit?.name ?: "") }
    var phone by remember { mutableStateOf(staffToEdit?.phone ?: "+91 ") }
    var role by remember { mutableStateOf(staffToEdit?.role ?: "Site Supervisor") }
    var department by remember { mutableStateOf(staffToEdit?.department ?: "Civil") }
    var dailySalaryStr by remember { mutableStateOf(staffToEdit?.dailySalary?.toInt()?.toString() ?: "850") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (staffToEdit == null) "Register Site Staff" else "Edit Staff Member",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                UniversalAiFillButton(
                    label = "AI Fill",
                    onClick = {
                        onOpenAiFill { fields ->
                            fields.staffName?.let { name = it }
                            fields.staffPhone?.let { phone = it }
                            fields.staffRole?.let { role = it }
                            fields.dailyWage?.let { dailySalaryStr = it.toInt().toString() }
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
                    label = { Text("Full Name *") },
                    placeholder = { Text("e.g. Ramesh Kumar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("staff_name_input")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Mobile Number *") },
                    placeholder = { Text("+91 98271 XXXXX") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Job Role / Skill *") },
                    placeholder = { Text("e.g. Site Supervisor, Mason, Civil Engineer") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("Trade") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = dailySalaryStr,
                        onValueChange = { dailySalaryStr = it },
                        label = { Text("Daily Wage (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val salary = dailySalaryStr.toDoubleOrNull() ?: 800.0
                    val entity = staffToEdit?.copy(
                        name = name.ifBlank { "Staff Member" },
                        phone = phone,
                        role = role.ifBlank { "Worker" },
                        department = department,
                        dailySalary = salary
                    ) ?: StaffEntity(
                        name = name.ifBlank { "Staff Member" },
                        phone = phone,
                        role = role.ifBlank { "Worker" },
                        department = department,
                        dailySalary = salary
                    )
                    onSave(entity)
                },
                modifier = Modifier.testTag("save_staff_dialog_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = DarkBluePrimary)
            ) {
                Text("Save Staff", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
