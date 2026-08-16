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
import com.example.data.local.MaterialEntity
import com.example.data.model.AiExtractedFields
import com.example.data.model.AppLanguage
import com.example.ui.components.UniversalAiFillButton
import com.example.ui.theme.*
import com.example.ui.util.Localization

@Composable
fun MaterialsScreen(
    materials: List<MaterialEntity>,
    currentLanguage: AppLanguage,
    onAddMaterialClick: () -> Unit,
    onEditMaterialClick: (MaterialEntity) -> Unit,
    onDeleteMaterialClick: (MaterialEntity) -> Unit,
    onOpenAiFill: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = materials.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.supplierName.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMaterialClick,
                containerColor = AmberGold,
                contentColor = DarkBluePrimary,
                modifier = Modifier.testTag("add_material_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Material")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("materials_screen"),
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
                            text = Localization.tr("materials", currentLanguage),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${materials.size} Items Tracked in Site Store",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    UniversalAiFillButton(
                        label = "AI Add Stock",
                        onClick = onOpenAiFill
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search materials, cement, steel, pipes...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (filtered.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No materials found", color = TextMuted)
                    }
                }
            } else {
                items(filtered) { mat ->
                    val isLow = mat.currentQuantity <= mat.minThreshold
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("material_card_${mat.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLow) WarningOrangeLight.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                        ),
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
                                        text = mat.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${mat.category} • Supplier: ${mat.supplierName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                                if (isLow) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(WarningOrange)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "LOW STOCK",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Current Stock", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(
                                        text = "${mat.currentQuantity} ${mat.unit}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLow) WarningOrange else SuccessGreen
                                        )
                                    )
                                }
                                Column {
                                    Text("Safety Threshold", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("${mat.minThreshold} ${mat.unit}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Unit Rate", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text("₹${mat.unitPrice.toInt()} / ${mat.unit}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { onEditMaterialClick(mat) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SkyBlueAccent, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { onDeleteMaterialClick(mat) }, modifier = Modifier.size(28.dp)) {
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
fun AddEditMaterialDialog(
    materialToEdit: MaterialEntity?,
    onDismiss: () -> Unit,
    onSave: (MaterialEntity) -> Unit,
    onOpenAiFill: (onResult: (AiExtractedFields) -> Unit) -> Unit
) {
    var name by remember { mutableStateOf(materialToEdit?.name ?: "") }
    var category by remember { mutableStateOf(materialToEdit?.category ?: "Cement & Aggregates") }
    var quantityStr by remember { mutableStateOf(materialToEdit?.currentQuantity?.toInt()?.toString() ?: "50") }
    var unit by remember { mutableStateOf(materialToEdit?.unit ?: "Bags") }
    var minThresholdStr by remember { mutableStateOf(materialToEdit?.minThreshold?.toInt()?.toString() ?: "20") }
    var unitPriceStr by remember { mutableStateOf(materialToEdit?.unitPrice?.toInt()?.toString() ?: "380") }
    var supplierName by remember { mutableStateOf(materialToEdit?.supplierName ?: "UltraTech Supplies Ltd.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (materialToEdit == null) "Add Inventory Material" else "Edit Material",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                UniversalAiFillButton(
                    label = "AI Fill",
                    onClick = {
                        onOpenAiFill { fields ->
                            fields.materialName?.let { name = it }
                            fields.quantity?.let { quantityStr = it.toInt().toString() }
                            fields.unit?.let { unit = it }
                            fields.supplier?.let { supplierName = it }
                            fields.unitPrice?.let { unitPriceStr = it.toInt().toString() }
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
                    label = { Text("Material Name *") },
                    placeholder = { Text("e.g. OPC 53 Cement") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("material_name_input")
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Quantity *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minThresholdStr,
                        onValueChange = { minThresholdStr = it },
                        label = { Text("Min Safety Stock") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unitPriceStr,
                        onValueChange = { unitPriceStr = it },
                        label = { Text("Unit Price (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text("Supplier / Vendor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityStr.toDoubleOrNull() ?: 50.0
                    val minT = minThresholdStr.toDoubleOrNull() ?: 20.0
                    val price = unitPriceStr.toDoubleOrNull() ?: 380.0

                    val entity = materialToEdit?.copy(
                        name = name.ifBlank { "Cement / Rebar" },
                        category = category,
                        currentQuantity = qty,
                        unit = unit,
                        minThreshold = minT,
                        unitPrice = price,
                        supplierName = supplierName
                    ) ?: MaterialEntity(
                        name = name.ifBlank { "Cement / Rebar" },
                        category = category,
                        currentQuantity = qty,
                        unit = unit,
                        minThreshold = minT,
                        unitPrice = price,
                        supplierName = supplierName
                    )
                    onSave(entity)
                },
                modifier = Modifier.testTag("save_material_dialog_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
            ) {
                Text("Save Material", fontWeight = FontWeight.Bold, color = DarkBluePrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
