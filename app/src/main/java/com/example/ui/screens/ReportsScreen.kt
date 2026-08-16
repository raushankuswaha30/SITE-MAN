package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.local.ReportEntity
import com.example.data.model.AppLanguage
import com.example.ui.components.UniversalAiFillButton
import com.example.ui.theme.*
import com.example.ui.util.Localization

@Composable
fun ReportsScreen(
    reports: List<ReportEntity>,
    isGenerating: Boolean,
    currentLanguage: AppLanguage,
    onGenerateReport: (String) -> Unit
) {
    var selectedReportForView by remember { mutableStateOf<ReportEntity?>(null) }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("reports_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = Localization.tr("reports", currentLanguage),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "AI-Synthesized Construction Insights & PDF Export",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            // Quick Generate Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkBluePrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Generate Executive Summary",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AmberGold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Synthesizes budgets, attendance logs, overdue milestones, material reorder triggers, and GST ledgers into a presentation-ready report.",
                            style = MaterialTheme.typography.bodySmall.copy(color = SkyBlueLight)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onGenerateReport("Management Summary") },
                            modifier = Modifier.fillMaxWidth().testTag("generate_report_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                            enabled = !isGenerating
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = DarkBluePrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Synthesizing...", color = DarkBluePrimary, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = DarkBluePrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate Mid-Month Report", color = DarkBluePrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Generated Reports & Archives",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (reports.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No reports generated yet.", color = TextMuted)
                    }
                }
            } else {
                items(reports) { rep ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("report_card_${rep.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
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
                                        text = rep.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${rep.type} • Generated ${rep.generatedDate}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                                IconButton(onClick = { selectedReportForView = rep }) {
                                    Icon(Icons.Default.Visibility, contentDescription = "View", tint = SkyBlueAccent)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = rep.summaryText,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 3,
                                color = TextDark
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = { selectedReportForView = rep },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("View & Export PDF", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // View Report Dialog
    selectedReportForView?.let { rep ->
        AlertDialog(
            onDismissRequest = { selectedReportForView = null },
            title = {
                Text(rep.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Executive Summary", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = DarkBluePrimary))
                    Text(rep.summaryText, style = MaterialTheme.typography.bodySmall, lineHeight = 20.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Actionable Recommendations", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = SuccessGreen))
                    Text(rep.recommendationsText, style = MaterialTheme.typography.bodySmall, lineHeight = 20.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedReportForView = null },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkBluePrimary)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
