package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.util.Localization

@Composable
fun SettingsScreen(
    currentRole: UserRole,
    currentLanguage: AppLanguage,
    isDarkMode: Boolean,
    isAiSuggestionsEnabled: Boolean,
    isVoiceInputEnabled: Boolean,
    userName: String,
    userPhone: String,
    onRoleClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleAiSuggestions: (Boolean) -> Unit,
    onToggleVoiceInput: (Boolean) -> Unit,
    onResetData: () -> Unit,
    onLogout: () -> Unit
) {
    var showResetConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // User Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(SkyBlueAccent.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName.ifBlank { "Vikramaditya Sharma" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = userPhone.ifBlank { "+91 98271 55667" },
                        style = MaterialTheme.typography.bodySmall.copy(color = SkyBlueLight)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AmberGold)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = currentRole.badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DarkBluePrimary,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }
        }

        // Section: Workspace & Preferences
        Text(
            text = "Workspace & Preferences",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SettingsClickableItem(
                    title = "Role-Based Access Control",
                    subtitle = currentRole.title,
                    icon = Icons.Default.Badge,
                    onClick = onRoleClick
                )
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                SettingsClickableItem(
                    title = "Language / भाषा",
                    subtitle = "${currentLanguage.nativeName} (${currentLanguage.displayName})",
                    icon = Icons.Default.Translate,
                    onClick = onLanguageClick
                )
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                SettingsSwitchItem(
                    title = "Dark Theme",
                    subtitle = "Reduce glare during night site inspections",
                    icon = Icons.Default.DarkMode,
                    checked = isDarkMode,
                    onCheckedChange = onToggleDarkMode
                )
            }
        }

        // Section: AI Intelligence & Automation
        Text(
            text = "AI Intelligence & Engine",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SettingsSwitchItem(
                    title = "AI Suggestions & Alerts",
                    subtitle = "Proactive low stock alerts & task reminders",
                    icon = Icons.Default.AutoAwesome,
                    checked = isAiSuggestionsEnabled,
                    onCheckedChange = onToggleAiSuggestions
                )
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                SettingsSwitchItem(
                    title = "Voice Input Support",
                    subtitle = "Natural Hindi/English voice commands",
                    icon = Icons.Default.Mic,
                    checked = isVoiceInputEnabled,
                    onCheckedChange = onToggleVoiceInput
                )
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                SettingsClickableItem(
                    title = "Camera OCR & Receipt Engine",
                    subtitle = "Automated bill scanning enabled",
                    icon = Icons.Default.DocumentScanner,
                    onClick = {}
                )
            }
        }

        // Section: Data & Diagnostics
        Text(
            text = "Data Management",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SettingsClickableItem(
                    title = "Offline Database Sync",
                    subtitle = "Room SQLite local storage active",
                    icon = Icons.Default.CloudDone,
                    onClick = {}
                )
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                SettingsClickableItem(
                    title = "Reset Demo Data",
                    subtitle = "Revert to initial demo construction state",
                    icon = Icons.Default.RestartAlt,
                    iconTint = WarningOrange,
                    onClick = { showResetConfirmation = true }
                )
            }
        }

        // Logout Button
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = ErrorRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out of Session", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Footer Info
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("SITE MAN v1.0.0", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Text("Manage • Build • Succeed", style = MaterialTheme.typography.labelSmall, color = SkyBlueAccent)
        }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Reset Demo Data?") },
            text = { Text("This will restore all projects, materials, tasks, attendance records, and expenses to default sample state.") },
            confirmButton = {
                Button(
                    onClick = {
                        onResetData()
                        showResetConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningOrange)
                ) {
                    Text("Reset All Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsClickableItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color = SkyBlueAccent,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = SkyBlueAccent, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = SkyBlueAccent, checkedTrackColor = SkyBlueContainer)
        )
    }
}
