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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ExpenseEntity
import com.example.data.local.ProjectEntity
import com.example.data.local.ScannedBillEntity
import com.example.data.model.AiExtractedFields
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
fun ExpensesScreen(
    expenses: List<ExpenseEntity>,
    scannedBills: List<ScannedBillEntity> = emptyList(),
    projects: List<ProjectEntity>,
    currentLanguage: AppLanguage,
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (ExpenseEntity) -> Unit,
    onDeleteExpenseClick: (ExpenseEntity) -> Unit,
    onDeleteScannedBill: (Long) -> Unit = {},
    onScanBillClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All Expenses, 1: Stored Scanned Bills (Room DB)
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var billSearchQuery by remember { mutableStateOf("") }
    var selectedBillForDetail by remember { mutableStateOf<ScannedBillEntity?>(null) }

    val totalAmount = expenses.sumOf { it.amount }
    val totalGst = expenses.sumOf { it.gstAmount }
    val totalScannedBillAmount = scannedBills.sumOf { it.totalAmount }

    val filteredExpenses = if (selectedCategoryFilter == "All") {
        expenses
    } else {
        expenses.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
    }

    val filteredScannedBills = scannedBills.filter { bill ->
        if (billSearchQuery.isBlank()) true
        else {
            bill.vendorName.contains(billSearchQuery, ignoreCase = true) ||
            bill.date.contains(billSearchQuery, ignoreCase = true) ||
            bill.invoiceNumber.contains(billSearchQuery, ignoreCase = true) ||
            bill.projectName.contains(billSearchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpenseClick,
                containerColor = DarkBluePrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_expense_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("expenses_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Scan Bill OCR action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Localization.tr("expenses", currentLanguage),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Site Cash & Room DB Bill Store",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    Button(
                        onClick = onScanBillClick,
                        colors = ButtonDefaults.buttonColors(containerColor = SkyBlueAccent),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("open_scanner_btn")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan Bill OCR", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total Construction Spend", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹${"%,.2f".format(totalAmount)}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = DarkBluePrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "GST Input Credit: ₹${"%,.2f".format(totalGst)}",
                                style = MaterialTheme.typography.labelSmall.copy(color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                "${scannedBills.size} Bills in Room DB",
                                style = MaterialTheme.typography.labelSmall.copy(color = SkyBlueAccent, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // CameraX Bill Scan Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onScanBillClick() }
                        .testTag("scan_bill_camera_banner"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(16.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(SkyBlueAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "CameraX Bill & Receipt OCR",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Snap a bill with camera — stores vendor, total amount & date in Room DB",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SkyBlueAccent)
                    }
                }
            }

            // Tabs: All Expenses vs Stored Scanned Bills (Room DB)
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = DarkBluePrimary,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text("All Expenses (${expenses.size})", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp), tint = SkyBlueAccent)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Room Bills (${scannedBills.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                }
            }

            if (selectedTab == 0) {
                // Category Filter Chips
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val categories = listOf("All", "Materials", "Equipment", "Labor", "Fuel", "Subcontractor")
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategoryFilter == cat,
                                onClick = { selectedCategoryFilter = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }

                // Expenses List
                if (filteredExpenses.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No expenses in this category", color = TextMuted)
                        }
                    }
                } else {
                    items(filteredExpenses) { exp ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("expense_card_${exp.id}"),
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
                                            text = exp.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${exp.projectName} • ${exp.category}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SkyBlueAccent
                                        )
                                    }
                                    Text(
                                        text = "₹${"%,.0f".format(exp.amount)}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = DarkBluePrimary
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        if (exp.vendorName.isNotBlank()) {
                                            Text("Vendor: ${exp.vendorName}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                        }
                                        if (exp.invoiceNumber.isNotBlank()) {
                                            Text("Invoice: ${exp.invoiceNumber} • Date: ${exp.date}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        } else {
                                            Text("Date: ${exp.date}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        }
                                    }
                                    StatusBadge(exp.status)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(onClick = { onEditExpenseClick(exp) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SkyBlueAccent, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = { onDeleteExpenseClick(exp) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ErrorRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Stored Scanned Bills (Room Database View)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = billSearchQuery,
                            onValueChange = { billSearchQuery = it },
                            placeholder = { Text("Search by vendor, date (YYYY-MM-DD), or invoice...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SkyBlueAccent) },
                            trailingIcon = {
                                if (billSearchQuery.isNotBlank()) {
                                    IconButton(onClick = { billSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_stored_bills_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Stored in Room Database: ${filteredScannedBills.size} bills",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Text(
                                text = "Total: ₹${"%,.0f".format(totalScannedBillAmount)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = DarkBluePrimary)
                            )
                        }
                    }
                }

                if (filteredScannedBills.isEmpty()) {
                    item {
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
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No Stored Bills Found",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Scan or upload a bill to extract vendor name, total amount, and date into Room DB.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onScanBillClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlueAccent)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Scan Now")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredScannedBills) { bill ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedBillForDetail = bill }
                                .testTag("scanned_bill_card_${bill.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Header: Vendor Name & Total Amount
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(SkyBlueAccent.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Storefront, contentDescription = null, tint = SkyBlueAccent, modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = bill.vendorName,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "${bill.projectName} • ${bill.category}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${"%,.0f".format(bill.totalAmount)}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = DarkBluePrimary
                                            )
                                        )
                                        if (bill.gstAmount > 0) {
                                            Text(
                                                text = "+GST ₹${"%,.0f".format(bill.gstAmount)}",
                                                style = MaterialTheme.typography.labelSmall.copy(color = SuccessGreen)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Key details pill row: Date, Invoice #, Confidence
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Extracted Date Pill
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(12.dp), tint = TextMuted)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(bill.date, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium))
                                        }
                                    }

                                    // Invoice Number Pill
                                    if (bill.invoiceNumber.isNotBlank()) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(12.dp), tint = TextMuted)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(bill.invoiceNumber, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.weight(1f))

                                    // Room DB Stored Badge
                                    Surface(
                                        color = SuccessGreen.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(12.dp), tint = SuccessGreen)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Room DB", style = MaterialTheme.typography.labelSmall.copy(color = SuccessGreen, fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }

                                if (bill.summary.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = bill.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        maxLines = 2
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Footer Actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Saved: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(bill.createdAt))}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = TextMuted
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { selectedBillForDetail = bill },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Visibility, contentDescription = "View Details", tint = SkyBlueAccent, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { onDeleteScannedBill(bill.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete from Room DB", tint = ErrorRed, modifier = Modifier.size(16.dp))
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

    // Detail Dialog for Stored Bill
    selectedBillForDetail?.let { bill ->
        ScannedBillDetailDialog(
            bill = bill,
            onDismiss = { selectedBillForDetail = null },
            onDelete = {
                onDeleteScannedBill(bill.id)
                selectedBillForDetail = null
            }
        )
    }
}

@Composable
fun ScannedBillDetailDialog(
    bill: ScannedBillEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = SkyBlueAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stored Bill Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vendor Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Vendor / Merchant Name", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(bill.vendorName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Total Bill Amount", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            "₹${"%,.2f".format(bill.totalAmount)}",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold, color = DarkBluePrimary)
                        )
                    }
                }

                // Grid Details
                DetailRowItem(label = "Date Extracted", value = bill.date, icon = Icons.Default.CalendarToday)
                if (bill.invoiceNumber.isNotBlank()) {
                    DetailRowItem(label = "Invoice Number", value = bill.invoiceNumber, icon = Icons.Default.Receipt)
                }
                if (bill.gstAmount > 0) {
                    DetailRowItem(label = "GST / Tax Input", value = "₹${"%,.2f".format(bill.gstAmount)}", icon = Icons.Default.AccountBalance)
                }
                DetailRowItem(label = "Category", value = bill.category, icon = Icons.Default.Category)
                DetailRowItem(label = "Project Assigned", value = bill.projectName, icon = Icons.Default.Apartment)
                DetailRowItem(label = "Extraction Confidence", value = "${bill.confidence}% (Gemini AI Vision)", icon = Icons.Default.AutoAwesome)

                if (bill.summary.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("AI OCR Notes & Summary", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(bill.summary, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                }

                Text(
                    text = "Persisted securely in local Room Database table 'scanned_bills'",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = SuccessGreen)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DarkBluePrimary)
            ) {
                Text("Close")
            }
        },
        dismissButton = {
            TextButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete Bill", color = ErrorRed)
            }
        }
    )
}

@Composable
private fun DetailRowItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = SkyBlueAccent, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseDialog(
    expenseToEdit: ExpenseEntity?,
    initialFields: AiExtractedFields? = null,
    projects: List<ProjectEntity>,
    onDismiss: () -> Unit,
    onSave: (ExpenseEntity) -> Unit,
    onOpenAiFill: (onResult: (AiExtractedFields) -> Unit) -> Unit,
    onScanBillWithCamera: (() -> Unit)? = null
) {
    var title by remember {
        mutableStateOf(
            expenseToEdit?.title
                ?: initialFields?.expenseTitle
                ?: if (!initialFields?.vendorName.isNullOrBlank()) "${initialFields?.vendorName} - Bill" else ""
        )
    }
    var category by remember { mutableStateOf(expenseToEdit?.category ?: initialFields?.expenseCategory ?: "Materials") }
    var amountStr by remember { mutableStateOf(expenseToEdit?.amount?.toInt()?.toString() ?: initialFields?.amount?.toInt()?.toString() ?: "19000") }
    var gstStr by remember { mutableStateOf(expenseToEdit?.gstAmount?.toInt()?.toString() ?: initialFields?.gstAmount?.toInt()?.toString() ?: "3420") }
    var vendorName by remember { mutableStateOf(expenseToEdit?.vendorName ?: initialFields?.vendorName ?: "UltraTech Supplies Ltd.") }
    var invoiceNumber by remember { mutableStateOf(expenseToEdit?.invoiceNumber ?: initialFields?.invoiceNumber ?: "INV-2026-0891") }
    var date by remember { mutableStateOf(expenseToEdit?.date ?: initialFields?.invoiceDate ?: "2026-08-14") }
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
                    text = if (expenseToEdit == null) "Log Site Expense" else "Edit Expense",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (onScanBillWithCamera != null) {
                        IconButton(
                            onClick = onScanBillWithCamera,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera Scanner", tint = SkyBlueAccent, modifier = Modifier.size(20.dp))
                        }
                    }
                    UniversalAiFillButton(
                        label = "AI Fill",
                        onClick = {
                            onOpenAiFill { fields ->
                                fields.expenseTitle?.let { title = it }
                                fields.expenseCategory?.let { category = it }
                                fields.amount?.let { amountStr = it.toInt().toString() }
                                fields.gstAmount?.let { gstStr = it.toInt().toString() }
                                fields.vendorName?.let { vendorName = it }
                                fields.invoiceNumber?.let { invoiceNumber = it }
                                fields.invoiceDate?.let { date = it }
                            }
                        }
                    )
                }
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
                    label = { Text("Expense Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = vendorName,
                    onValueChange = { vendorName = it },
                    label = { Text("Vendor / Merchant Name *") },
                    leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = SkyBlueAccent) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Amount (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = gstStr,
                        onValueChange = { gstStr = it },
                        label = { Text("GST (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = invoiceNumber,
                        onValueChange = { invoiceNumber = it },
                        label = { Text("Invoice #") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Materials, Labor, Fuel, Equipment)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Project Selector
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedProject,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Project") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        projects.forEach { proj ->
                            DropdownMenuItem(
                                text = { Text(proj.name) },
                                onClick = {
                                    selectedProject = proj.name
                                    selectedProjectId = proj.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 19000.0
                    val gst = gstStr.toDoubleOrNull() ?: 0.0
                    val newExp = (expenseToEdit?.copy(
                        title = title.ifBlank { "$vendorName - Bill" },
                        category = category,
                        amount = amount,
                        gstAmount = gst,
                        vendorName = vendorName,
                        invoiceNumber = invoiceNumber,
                        date = date,
                        projectId = selectedProjectId,
                        projectName = selectedProject
                    ) ?: ExpenseEntity(
                        projectId = selectedProjectId,
                        projectName = selectedProject,
                        title = title.ifBlank { "$vendorName - Bill" },
                        category = category,
                        amount = amount,
                        gstAmount = gst,
                        vendorName = vendorName,
                        invoiceNumber = invoiceNumber,
                        date = date
                    ))
                    onSave(newExp)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkBluePrimary)
            ) {
                Text("Save to Room DB")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
