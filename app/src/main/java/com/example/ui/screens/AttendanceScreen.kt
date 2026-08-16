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
import com.example.data.local.AttendanceEntity
import com.example.data.local.StaffEntity
import com.example.data.model.AppLanguage
import com.example.ui.components.StatusBadge
import com.example.ui.components.UniversalAiFillButton
import com.example.ui.theme.*
import com.example.ui.util.Localization

@Composable
fun AttendanceScreen(
    attendanceList: List<AttendanceEntity>,
    staffList: List<StaffEntity>,
    currentLanguage: AppLanguage,
    onMarkAttendanceClick: () -> Unit,
    onOpenAiVoiceAttendance: () -> Unit
) {
    val presentCount = attendanceList.count { it.status == "Present" || it.status == "Late" }
    val lateCount = attendanceList.count { it.isLate }
    val otCount = attendanceList.count { it.isOvertime }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onMarkAttendanceClick,
                containerColor = SuccessGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("mark_attendance_fab")
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = "Mark Attendance")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("attendance_screen"),
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
                            text = Localization.tr("attendance", currentLanguage),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Real-Time GPS & Facial Verification",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    UniversalAiFillButton(
                        label = "Voice Mark",
                        onClick = onOpenAiVoiceAttendance
                    )
                }
            }

            // Stats summary card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        AttendanceStatItem("Total Crew", "${staffList.size.coerceAtLeast(5)}", DarkBluePrimary)
                        AttendanceStatItem("Present", "$presentCount", SuccessGreen)
                        AttendanceStatItem("Late", "$lateCount", WarningOrange)
                        AttendanceStatItem("Overtime", "$otCount", SkyBlueAccent)
                    }
                }
            }

            // Site Geofence & Verification Status
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreenLight.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.GpsFixed, contentDescription = null, tint = SuccessGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Active Geofence: Site Gate A & Tower B",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SuccessGreen)
                            )
                            Text(
                                text = "Automated radius 200m • GPS Timestamped",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // Attendance list
            if (attendanceList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No attendance marked for today yet.", color = TextMuted)
                    }
                }
            } else {
                items(attendanceList) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("attendance_item_${item.id}"),
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
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(SkyBlueContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = SkyBlueAccent
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = item.staffName,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "In: ${item.checkInTime} • ${item.locationAddress}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                    if (item.notes.isNotBlank()) {
                                        Text(
                                            text = "Note: ${item.notes}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = WarningOrange)
                                        )
                                    }
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                StatusBadge(item.status)
                                if (item.isOvertime) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "+${item.overtimeHours}h OT",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SkyBlueAccent
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = color))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkAttendanceDialog(
    staffList: List<StaffEntity>,
    onDismiss: () -> Unit,
    onSave: (staffId: Long, staffName: String, status: String, isLate: Boolean, isOvertime: Boolean, otHours: Double, notes: String) -> Unit
) {
    var selectedStaff by remember { mutableStateOf(staffList.firstOrNull()?.name ?: "Ramesh Kumar") }
    var selectedStaffId by remember { mutableStateOf(staffList.firstOrNull()?.id ?: 1L) }
    var status by remember { mutableStateOf("Present") }
    var isLate by remember { mutableStateOf(false) }
    var isOvertime by remember { mutableStateOf(false) }
    var otHoursStr by remember { mutableStateOf("2.0") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Mark Site Attendance", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Select Worker / Staff Member:", style = MaterialTheme.typography.labelMedium)

                staffList.forEach { staff ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedStaffId == staff.id,
                            onClick = {
                                selectedStaffId = staff.id
                                selectedStaff = staff.name
                            }
                        )
                        Text("${staff.name} (${staff.role})", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Divider()

                Text("Attendance Status:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Present", "Late", "Overtime", "Half Day", "Absent").forEach { s ->
                        FilterChip(
                            selected = status == s,
                            onClick = {
                                status = s
                                isLate = (s == "Late")
                                isOvertime = (s == "Overtime")
                            },
                            label = { Text(s, fontSize = 11.sp) }
                        )
                    }
                }

                if (isOvertime) {
                    OutlinedTextField(
                        value = otHoursStr,
                        onValueChange = { otHoursStr = it },
                        label = { Text("Overtime Hours") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Reason") },
                    placeholder = { Text("e.g. Traffic on bridge / DB Wiring OT") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val otVal = otHoursStr.toDoubleOrNull() ?: 0.0
                    onSave(selectedStaffId, selectedStaff, status, isLate, isOvertime, otVal, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("Check In Staff", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
