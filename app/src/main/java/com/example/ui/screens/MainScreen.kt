package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.FinanceViewModel
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

// Helper to format Vietnamese Currency
fun formatVnd(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(amount).replace("₫", "₫").trim()
}

// Helper to format date
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: FinanceViewModel) {
    var activeTab by remember { mutableStateOf(0) }
    
    // Database states
    val transactions by viewModel.transactions.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val savingGoals by viewModel.savingGoals.collectAsState()
    val subscriptions by viewModel.subscriptions.collectAsState()
    
    // Calculated statistics
    val balance by viewModel.totalBalance.collectAsState()
    val incomeVal by viewModel.totalIncome.collectAsState()
    val expenseVal by viewModel.totalExpense.collectAsState()
    val wastedVal by viewModel.totalWasted.collectAsState()
    val categorySpend by viewModel.categoryExpenseSummary.collectAsState()
    val subCostFlow by viewModel.totalSubscriptionCost.collectAsState()

    // Dialog sheets states
    var showAddTxDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddSubDialog by remember { mutableStateOf(false) }
    var showAddBudgetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Chào buổi tối em,",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Nguyễn Minh",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                actions = {
                    // NM Avatar that also seeds sample data when tapped
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                            .clickable { viewModel.seedSampleDataIfEmpty() }
                            .testTag("nm_avatar_seed"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "NM",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.testTag("app_top_bar")
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Default.Payments, contentDescription = "Dòng Tiền") },
                    label = { Text("Dòng Tiền", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.secondary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_tab_cashflow")
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Default.Savings, contentDescription = "Tích Lũy") },
                    label = { Text("Tích Lũy", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.secondary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_tab_savings")
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Default.Block, contentDescription = "Tránh Thất Thoát") },
                    label = { Text("Mất Tiền Oan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.secondary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_tab_anti_waste")
                )
                NavigationBarItem(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    icon = { Icon(Icons.Default.Lightbulb, contentDescription = "Mẹo & Kế Hoạch") },
                    label = { Text("Mẹo & Kế Hoạch", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.secondary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.testTag("nav_tab_plan")
                )
            }
        },
        floatingActionButton = {
            if (activeTab == 0) {
                FloatingActionButton(
                    onClick = { showAddTxDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_transaction_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm giao dịch")
                }
            } else if (activeTab == 1) {
                FloatingActionButton(
                    onClick = { showAddGoalDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_saving_goal_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm hũ tiết kiệm")
                }
            } else if (activeTab == 2) {
                FloatingActionButton(
                    onClick = { showAddSubDialog = true },
                    containerColor = WarningAmber,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_sub_fab")
                ) {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "Thêm định kỳ")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activeTab) {
                0 -> CashflowTab(
                    transactions = transactions,
                    balance = balance,
                    income = incomeVal,
                    expense = expenseVal,
                    wasted = wastedVal,
                    categorySpend = categorySpend,
                    budgets = budgets,
                    onDeleteTx = { viewModel.deleteTransaction(it) },
                    onAddBudget = { showAddBudgetDialog = true }
                )
                1 -> SavingsTab(
                    goals = savingGoals,
                    onAdjustProgress = { goal, amt -> viewModel.adjustSavingGoalProgress(goal, amt) },
                    onDeleteGoal = { viewModel.deleteSavingGoal(it) }
                )
                2 -> WasteBlockerTab(
                    subscriptions = subscriptions,
                    subCost = subCostFlow,
                    totalWasted = wastedVal,
                    onToggleSub = { viewModel.toggleSubscriptionActive(it) },
                    onDeleteSub = { viewModel.deleteSubscription(it) }
                )
                3 -> TipsAndPlanTab(
                    budgets = budgets,
                    categorySpend = categorySpend,
                    onDeleteBudget = { viewModel.deleteBudget(it) },
                    onAddBudget = { showAddBudgetDialog = true }
                )
            }
        }
    }

    // --- DIALOGS ---
    if (showAddTxDialog) {
        AddTransactionDialog(
            onDismiss = { showAddTxDialog = false },
            onSave = { amount, isIncome, category, note, isLoss, lossType ->
                viewModel.addTransaction(amount, isIncome, category, note, isLoss, lossType)
                showAddTxDialog = false
            }
        )
    }

    if (showAddGoalDialog) {
        AddSavingGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSave = { name, target, initial ->
                viewModel.addSavingGoal(name, target, initial)
                showAddGoalDialog = false
            }
        )
    }

    if (showAddSubDialog) {
        AddSubscriptionDialog(
            onDismiss = { showAddSubDialog = false },
            onSave = { name, amount, cycle, day, cat ->
                viewModel.addSubscription(name, amount, cycle, day, cat)
                showAddSubDialog = false
            }
        )
    }

    if (showAddBudgetDialog) {
        AddBudgetDialog(
            onDismiss = { showAddBudgetDialog = false },
            onSave = { category, limit ->
                viewModel.setBudget(category, limit)
                showAddBudgetDialog = false
            }
        )
    }
}

// ==========================================
// 1. CASHFLOW TAB
// ==========================================
@Composable
fun CashflowTab(
    transactions: List<TransactionEntity>,
    balance: Double,
    income: Double,
    expense: Double,
    wasted: Double,
    categorySpend: Map<String, Double>,
    budgets: List<BudgetEntity>,
    onDeleteTx: (Int) -> Unit,
    onAddBudget: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("cashflow_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Balance Widget Card (Natural Tones Visual Anchor)
        item {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("natural_balance_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                ) {
                    // Decorative organic background shape
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 30.dp, y = (-30).dp)
                            .size(140.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                                shape = CircleShape
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "TỔNG SỐ DƯ HIỆN TẠI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatVnd(balance),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dynamic Saving Ratio and Helper report
                        val savingsRatio = if (income > 0) ((income - expense) / income * 100).toInt() else 0
                        val savingsRatioClamped = savingsRatio.coerceIn(-100, 100)
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF285030).copy(alpha = 0.6f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (savingsRatio >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = "Xu hướng",
                                        tint = if (savingsRatio >= 0) TertiaryGold else Color(0xFFFFB4AB),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (savingsRatio >= 0) "Tích lũy dương: +$savingsRatio%" else "Thâm hụt chi: $savingsRatio%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (savingsRatio >= 20) "Đạt chuẩn an toàn" else "Khuyến nghị tích lũy >20%",
                                        fontSize = 9.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            
                            // Interactive report action trigger
                            Button(
                                onClick = onAddBudget, // Open prompt to configure dynamic budget
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = PrimaryGreen
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("report_budget_quick_btn")
                            ) {
                                Text("Đặt mục tiêu", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }

        // Secondary break-out metric capsules (Income/Expense Details)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceNeutral),
                    border = BorderStroke(1.dp, BorderDividerLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(IncomeGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = "Thu nhập",
                                tint = IncomeGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Thu Nhập",
                                fontSize = 11.sp,
                                color = SoftGrayText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                formatVnd(income),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = IncomeGreen
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceNeutral),
                    border = BorderStroke(1.dp, BorderDividerLight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ExpenseRed.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = "Chi phi",
                                tint = ExpenseRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Đã Chi",
                                fontSize = 11.sp,
                                color = SoftGrayText,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                formatVnd(expense),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ExpenseRed
                            )
                        }
                    }
                }
            }
        }

        // Leaks Alert if there is a risk of wasted capital
        if (wasted > 0) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.15f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Lãng phí",
                            tint = ExpenseRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Phát hiện khoản chi lãng phí tự động: ${formatVnd(wasted)}! Hãy xem mục 'Mất Tiền Oan'.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }
                }
            }
        }

        // Budget Warnings & Alert Card
        item {
            BudgetAlertSection(categorySpend = categorySpend, budgets = budgets, onAddBudget = onAddBudget)
        }

        // Ngân sách chi tiêu progress cards (Natural Tones style)
        if (budgets.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Ngân sách chi tiêu",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Điều chỉnh",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { onAddBudget() }
                            .testTag("adjust_budget_anchor_label")
                    )
                }
            }

            items(budgets) { budget ->
                val spent = categorySpend[budget.category] ?: 0.0
                val ratio = if (budget.limitAmount > 0) spent / budget.limitAmount else 0.0
                val ratioClamped = ratio.coerceIn(0.0, 1.0).toFloat()
                val remaining = (budget.limitAmount - spent).coerceAtLeast(0.0)
                
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceNeutral),
                    border = BorderStroke(1.dp, BorderDividerLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hạng mục: ${budget.category}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (remaining > 0) "Còn ${formatVnd(remaining)}" else "Vượt hạn mức!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (remaining > 0) MaterialTheme.colorScheme.primary else ExpenseRed
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = ratioClamped,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = if (ratio >= 1.0) ExpenseRed else MaterialTheme.colorScheme.primary,
                            trackColor = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bạn đã dùng ${(ratio * 100).toInt()}% ngân sách này (Hạn mức ${formatVnd(budget.limitAmount)}).",
                            fontSize = 10.sp,
                            color = SoftGrayText
                        )
                    }
                }
            }
        }

        // Transactions List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GIAO DỊCH GẦN ĐÂY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
            }
        }

        // Transactions list or empty state
        if (transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "Chưa có giao dịch",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Chưa có giao dịch nào được ghi chép.",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bấm nút '+' ở góc dưới để bắt đầu ghi sổ.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                TransactionRowItem(tx = tx, onDelete = { onDeleteTx(tx.id) })
            }
        }
    }
}

@Composable
fun TransactionRowItem(tx: TransactionEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tx_item_${tx.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon with Circle background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (tx.isIncome) IncomeGreen.copy(alpha = 0.12f)
                        else if (tx.isLossRisk) WarningAmber.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryIcon(tx.category, tx.isIncome),
                    contentDescription = tx.category,
                    tint = if (tx.isIncome) IncomeGreen
                    else if (tx.isLossRisk) WarningAmber
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Transaction Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (tx.note.isNotEmpty()) tx.note else tx.category,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (tx.isLossRisk) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(WarningAmber.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Lãng Phí",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningAmber
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = tx.category,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatDate(tx.timestamp),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            // Amount & Delete Button
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${if (tx.isIncome) "+" else "-"}${formatVnd(tx.amount)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = if (tx.isIncome) IncomeGreen else if (tx.isLossRisk) WarningAmber else ExpenseRed
                )
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Xóa",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onDelete() }
                        .testTag("delete_tx_${tx.id}")
                )
            }
        }
    }
}

@Composable
fun BudgetAlertSection(
    categorySpend: Map<String, Double>,
    budgets: List<BudgetEntity>,
    onAddBudget: () -> Unit
) {
    if (budgets.isEmpty()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Mẹo hạn mức",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Thiết lập hạn mức chi tiêu?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Đặt ngân sách tối đa cho Ăn uống, Giải trí... để tự động cảnh báo vung tay quá trán.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAddBudget,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("setup_budget_btn")
                ) {
                    Text("Đặt ngay", fontSize = 11.sp)
                }
            }
        }
    } else {
        // Evaluate alerts
        val activeAlerts = remember(categorySpend, budgets) {
            budgets.mapNotNull { budget ->
                val spent = categorySpend[budget.category] ?: 0.0
                val ratio = if (budget.limitAmount > 0) spent / budget.limitAmount else 0.0
                if (ratio >= 0.8) {
                    Triple(budget.category, spent, budget.limitAmount)
                } else {
                    null
                }
            }
        }

        if (activeAlerts.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.08f)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(WarningAmber.copy(alpha = 0.4f), WarningAmber.copy(alpha = 0.4f)))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Nguy cơ quá hạn mức",
                            tint = WarningAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CẢNH BÁO VƯỢT HẠN MỨC",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = WarningAmber,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    activeAlerts.forEach { (cat, spent, limit) ->
                        val ratio = spent / limit
                        val isOver = spent > limit
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Mục '$cat'",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "${if (isOver) "Đã vượt" else "Đã dùng"} ${(ratio * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOver) ExpenseRed else WarningAmber
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            LinearProgressIndicator(
                                progress = ratio.toFloat().coerceAtMost(1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = if (isOver) ExpenseRed else WarningAmber,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Chi: ${formatVnd(spent)} / Hạn mức: ${formatVnd(limit)}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. SAVINGS TAB
// ==========================================
@Composable
fun SavingsTab(
    goals: List<SavingGoalEntity>,
    onAdjustProgress: (SavingGoalEntity, Double) -> Unit,
    onDeleteGoal: (Int) -> Unit
) {
    var goalToAdjust by remember { mutableStateOf<SavingGoalEntity?>(null) }
    var adjustAmountStr by remember { mutableStateOf("") }
    var isDepositMode by remember { mutableStateOf(true) } // True = deposit, False = withdraw

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("savings_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Explanatory Motivation banner for lower-income savers
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = "Tích lũy",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "LẬP HŨ TÍCH LŨY (ENVELOPES)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Với thu nhập trung bình-thấp, chìa khóa vàng là chia nhỏ tiền vào các hũ ảo riêng biệt để tránh vô tình tiêu lạm vào quỹ quan trọng (Tiền nhà, Sự cố khẩn cấp, Đi học).",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Summary metric
        item {
            val totalSavedVal = goals.sumOf { it.savedAmount }
            val totalGoalTargetVal = goals.sumOf { it.targetAmount }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tổng tiền đang tích lũy",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = formatVnd(totalSavedVal),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = IncomeGreen
                        )
                    }
                    Text(
                        text = "Mục tiêu: " + formatVnd(totalGoalTargetVal),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }

        // List of Saving Goals or empty
        if (goals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = "Chưa có hũ nào",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Bạn chưa lập hũ tích lũy nào.",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bấm nút '+' ở góc dưới để tạo hũ mới (ví dụ: 'Quỹ Dự Phòng Khẩn Cấp')",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(goals, key = { it.id }) { goal ->
                GoalRowItem(
                    goal = goal,
                    onAdjustClick = {
                        goalToAdjust = goal
                        adjustAmountStr = ""
                        isDepositMode = true
                    },
                    onDelete = { onDeleteGoal(goal.id) }
                )
            }
        }
    }

    // Adjust Cashflow Dialog
    if (goalToAdjust != null) {
        val goal = goalToAdjust!!
        Dialog(onDismissRequest = { goalToAdjust = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("adjust_saving_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ĐIỀU CHỈNH HŨ TIỀN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = goal.name,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Mode Selection Segment
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            .padding(4.dp)
                    ) {
                        Button(
                            onClick = { isDepositMode = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_deposit_mode"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDepositMode) IncomeGreen else Color.Transparent,
                                contentColor = if (isDepositMode) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            elevation = null,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Gửi thêm tiền", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { isDepositMode = false },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_withdraw_mode"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isDepositMode) ExpenseRed else Color.Transparent,
                                contentColor = if (!isDepositMode) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            elevation = null,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Rút tiền ra", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = adjustAmountStr,
                        onValueChange = { adjustAmountStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Số tiền (VND)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("adjust_amount_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { goalToAdjust = null },
                            modifier = Modifier.testTag("adjust_cancel_btn")
                        ) {
                            Text("Hủy bỏ")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amountVal = adjustAmountStr.toDoubleOrNull() ?: 0.0
                                if (amountVal > 0.0) {
                                    val factor = if (isDepositMode) amountVal else -amountVal
                                    onAdjustProgress(goal, factor)
                                }
                                goalToAdjust = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDepositMode) IncomeGreen else ExpenseRed
                            ),
                            modifier = Modifier.testTag("adjust_confirm_btn")
                        ) {
                            Text("Thực Hiện")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalRowItem(
    goal: SavingGoalEntity,
    onAdjustClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("goal_item_${goal.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("delete_goal_${goal.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Xóa hũ",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress estimation
            val progress = if (goal.targetAmount > 0) (goal.savedAmount / goal.targetAmount).toFloat() else 0f
            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (progress >= 1f) TertiaryColor else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = formatVnd(goal.savedAmount),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = if (progress >= 1f) TertiaryColor else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = " / " + formatVnd(goal.targetAmount),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    Text(
                        text = "Tiến trình: ${(progress * 100).toInt()}% " + if (progress >= 1f) "🎉 Đạt mục tiêu!" else "",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (progress >= 1f) TertiaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Button(
                    onClick = onAdjustClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("adjust_goal_btn_${goal.id}")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Sửa", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nạp/Rút", fontSize = 11.sp)
                }
            }
        }
    }
}

// ==========================================
// 3. WASTE BLOCKER / SUB TRACKER TAB
// ==========================================
@Composable
fun WasteBlockerTab(
    subscriptions: List<SubscriptionEntity>,
    subCost: Double,
    totalWasted: Double,
    onToggleSub: (SubscriptionEntity) -> Unit,
    onDeleteSub: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("waste_blocker_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Red theme alert card for "Prevent lost money flow"
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(WarningAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = "Tránh thất thoát",
                                tint = WarningAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "CHẶN CÁC KÊNH MẤT TIỀN OAN",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Với thu nhập trung bình/thấp, tiền rò rỉ âm thầm từ các dịch vụ tự động đăng ký gia hạn, gói mạng không dùng hết, hoặc phí thẻ ngân hàng ẩn chính là nguyên nhân làm cạn kiệt túi tiền của bạn. Hãy ghi chép chúng tại đây để chặn ngay!",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Risk indicators cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Tiền rò rỉ hàng tháng", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatVnd(subCost), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = WarningAmber)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Từ các đăng ký định kỳ", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Đã mất do tùy ý/vặt", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(formatVnd(totalWasted), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = ExpenseRed)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Tổng ăn vặt & mua sắm tuỳ ý", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                }
            }
        }

        // Subscriptions List Header
        item {
            Text(
                text = "CÁC GÓI ĐỊNH KỲ CẦN QUAN SÁT",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                letterSpacing = 0.5.sp
            )
        }

        // Active Subscription rows
        if (subscriptions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Chưa có danh sách",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Chưa lưu đăng ký định kỳ nào.",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bấm nút chuông '+' màu cam để thêm (ví dụ: Phí tin nhắn ngân hàng, Gói điện thoại, Netflix)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(subscriptions, key = { it.id }) { sub ->
                SubscriptionRowItem(
                    sub = sub,
                    onToggleActive = { onToggleSub(sub) },
                    onDelete = { onDeleteSub(sub.id) }
                )
            }
        }

        // Important Tips tailored for lower-income risk avoidance
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💡 CẢNH BÁO BẪY TÀI CHÍNH VIỆT NAM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = WarningAmber
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val traps = listOf(
                        "**Gói 3G/4G tự gia hạn**: Đăng ký thử 1 ngày/1 tuần rồi quên hủy sẽ trừ âm thầm tiền tài khoản sim liên tục.",
                        "**Phí thẻ ngân hàng chồng chéo**: Dùng 3-4 thẻ khác nhau làm phát sinh phí quản lý tài khoản định kỳ (11k - 50k/tháng/thẻ). Hãy đóng bớt chỉ giữ 1-2 thẻ.",
                        "**Gia hạn thử nghiệm free**: Rất nhiều ứng dụng dụ xài thử 7 ngày rồi tự động đăng ký gói năm lên tới vài trăm nghìn đồng.",
                        "**Bẫy Mua Trước Trả Sau**: Trông có vẻ nhẹ nhàng chỉ vài chục nghìn/tháng nhưng khiến bạn lạm chi dễ dàng, tích tiểu thành đại tạo gánh nặng nợ."
                    )
                    
                    traps.forEach { trap ->
                        val parts = trap.split("**")
                        Text(
                            text = buildString {
                                if (parts.size >= 3) {
                                    append("• ")
                                    append(parts[1])
                                    append(": ")
                                    append(parts[2])
                                } else {
                                    append(trap)
                                }
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 4.dp),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionRowItem(
    sub: SubscriptionEntity,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    // Check if renew date is nearby (suppose today is day of month)
    val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val isNear = sub.isActive && (sub.renewalDay - currentDay in 0..3)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sub_item_${sub.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNear) WarningAmber.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = sub.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (sub.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    if (isNear) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(WarningAmber)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("SẮP THU PHÍ", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${sub.category} • Ngày thu phí: Hàng tháng, ngày ${sub.renewalDay}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                if (isNear) {
                    Text(
                        text = "⚠️ Hãy hủy dịch vụ ngay nếu bạn không còn thực sự dùng cần để tránh lãng phí!",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber,
                        lineHeight = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatVnd(sub.amount),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = if (sub.isActive) WarningAmber else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = sub.isActive,
                        onCheckedChange = { onToggleActive() },
                        modifier = Modifier
                            .scale(0.7f)
                            .testTag("toggle_sub_${sub.id}")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("delete_sub_${sub.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Hủy gia đoạn",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// Extension to scale switch smaller
fun Modifier.scale(scale: Float): Modifier = this.then(
    android.graphics.Matrix().run {
        postScale(scale, scale)
        // Adjust bounds
        Modifier
    }
)

// ==========================================
// 4. SMART BUDGET & 50/30/20 CALCULATOR TAB
// ==========================================
@Composable
fun TipsAndPlanTab(
    budgets: List<BudgetEntity>,
    categorySpend: Map<String, Double>,
    onDeleteBudget: (String) -> Unit,
    onAddBudget: () -> Unit
) {
    // 50/30/20 Calculator state
    var incomeStr by remember { mutableStateOf("8000000") } // Average Vietnamese worker salary default
    val incomeVal = incomeStr.toDoubleOrNull() ?: 0.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tips_and_plan_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vietnamese localized 50/30/20 interactive tools
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🧮 CHIA THU NHẬP THEO QUY TẮC SÁNG SUỐT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nhập thu nhập hàng tháng để hệ thống tự động phân bổ nguồn hạn mức an toàn tối đa cho từng nhóm.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = incomeStr,
                        onValueChange = { incomeStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Mức thu nhập hàng tháng của bạn (VND)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = "VND Icon") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("formula_income_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val needs = incomeVal * 0.50
                    val wants = incomeVal * 0.30
                    val saves = incomeVal * 0.20

                    // Circular breakdown simple visualizer (using linear rows)
                    FormulaProgressRow(
                        title = "1. Thiết yếu (Needs) - 50%",
                        amount = needs,
                        percent = 0.5f,
                        color = InfoBlue,
                        desc = "Nhà trọ, ăn uống, điện nước, xăng xe, thuốc men bảo hiểm cố định..."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FormulaProgressRow(
                        title = "2. Chi tiêu tùy ý (Wants) - 30%",
                        amount = wants,
                        percent = 0.3f,
                        color = WarningAmber,
                        desc = "Cà phê gặp bạn, ăn uống ngoài tiệm bún ngon, mua sắm áo quần mới..."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FormulaProgressRow(
                        title = "3. Tích lũy (Savings/Debts) - 20%",
                        amount = saves,
                        percent = 0.2f,
                        color = IncomeGreen,
                        desc = "Gửi vào hũ tiết kiệm phòng khẩn cấp, hoặc trả các nợ cũ dứt điểm..."
                    )
                }
            }
        }

        // Active Budgets settings panel
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LẬP HẠN MỨC CHO TỪNG DANH MỤC",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )
                Button(
                    onClick = onAddBudget,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("add_budget_limit_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Thêm hạn mức", modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Thiết lập", fontSize = 11.sp)
                }
            }
        }

        if (budgets.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Chưa thiết lập hạn mức nào.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        } else {
            items(budgets, key = { it.category }) { budget ->
                val spent = categorySpend[budget.category] ?: 0.0
                val ratio = if (budget.limitAmount > 0) spent / budget.limitAmount else 0.0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_setting_${budget.category}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Mục '${budget.category}'",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Hạn mức: " + formatVnd(budget.limitAmount),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Đã tiêu: ${formatVnd(spent)} (${(ratio * 100).toInt()}%)",
                                fontSize = 11.sp,
                                color = if (spent > budget.limitAmount) ExpenseRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = { onDeleteBudget(budget.category) },
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("delete_budget_${budget.category}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Xóa hạn mức",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Actionable savings practices list card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "Lời khuyên",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "💡 5 THÓI QUEN CỨU CÁNH CHO THU NHẬP THẤP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val instructions = listOf(
                        "**Nấu ăn mang đi**: Tự nấu mang đi giúp tiết kiệm tối thiểu 1.000.000 đ/tháng so với ăn bún cơm ngoài hàng ngày.",
                        "**Quy tắc 24h hoãn mua**: Nếu cực kỳ muốn mua một áo quần mới hoặc thiết bị điện tử, hãy hoãn lại đúng 24h. 80% ham muốn sẽ tự biến mất sau một giấc ngủ ngon.",
                        "**Ghi chép từng VND lẻ**: Từng cốc chè 15k, tiền gửi xe 5k gom lại làm ngốn tài sản cực lớn. Sổ Dòng Tiền của Hệ thống giúp bạn kiểm soát hoàn toàn những khoản này.",
                        "**Trích lập tiết kiệm đầu ngày**: Ngay khi có lương, lập tức gửi 10-20% thẳng vào hũ tiết kiệm khóa ảo trước, phần còn lại mới chia chi tiêu. Không bao giờ đợi chi tiêu xong thừa mới tiết kiệm!"
                    )

                    instructions.forEach { instr ->
                        val pieces = instr.split("**")
                        Text(
                            text = buildString {
                                if (pieces.size >= 3) {
                                    append("✔ ")
                                    append(pieces[1])
                                    append(": ")
                                    append(pieces[2])
                                } else {
                                    append(instr)
                                }
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FormulaProgressRow(
    title: String,
    amount: Double,
    percent: Float,
    color: Color,
    desc: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(formatVnd(amount), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = percent,
            color = color,
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(desc, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), lineHeight = 13.sp)
    }
}

// ==========================================
// HELPERS & DIALOG COMPONENT IMPLEMENTATIONS
// ==========================================

fun getCategoryIcon(category: String, isIncome: Boolean): ImageVector {
    if (isIncome) return Icons.Default.Payments
    return when (category) {
        "Ăn uống" -> Icons.Default.Restaurant
        "Nhà cửa" -> Icons.Default.Home
        "Đi lại" -> Icons.Default.DirectionsCar
        "Hóa đơn" -> Icons.Default.Receipt
        "Giải trí" -> Icons.Default.Mood
        "Sức khỏe" -> Icons.Default.MedicalServices
        "Học tập" -> Icons.Default.School
        "Tích Lũy" -> Icons.Default.Savings
        else -> Icons.Default.Category
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (amount: Double, isIncome: Boolean, category: String, note: String, isLossRisk: Boolean, lossType: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("") }
    var isLossRisk by remember { mutableStateOf(false) }
    var selectedLossType by remember { mutableStateOf("Chi tiêu tùy ý") }

    val categoriesIncome = listOf("Thu nhập", "Lương bổng", "Thưởng", "Khác")
    val categoriesExpense = listOf("Ăn uống", "Nhà cửa", "Đi lại", "Hóa đơn", "Giải trí", "Sức khỏe", "Học tập", "Khác")

    LaunchedEffect(isIncome) {
        selectedCategory = if (isIncome) categoriesIncome.first() else categoriesExpense.first()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_tx_dialog")
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "GHI CHÉP THU / CHI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                // Income / Expense selector chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            .padding(4.dp)
                    ) {
                        Button(
                            onClick = { isIncome = false },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tx_is_expense_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isIncome) ExpenseRed else Color.Transparent,
                                contentColor = if (!isIncome) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            elevation = null,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("KHOẢN CHI (-)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { isIncome = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tx_is_income_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isIncome) IncomeGreen else Color.Transparent,
                                contentColor = if (isIncome) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            elevation = null,
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("KHOẢN THU (+)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Amount Text Field
                item {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Số tiền (VND)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tx_amount_input")
                    )
                }

                // Note Text Field
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Ghi chú / Nhãn riêng") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tx_note_input")
                    )
                }

                // Category selection
                item {
                    Text("Danh mục:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val currentCategoryList = if (isIncome) categoriesIncome else categoriesExpense
                        currentCategoryList.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                modifier = Modifier.testTag("chip_cat_$cat")
                            )
                        }
                    }
                }

                // Special feature: Flagging as financial loss warning risk for low/middle budgets
                if (!isIncome) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Thất thoát tiền",
                                            tint = WarningAmber,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = "Là khoản chi tùy ý / bẫy lãng phí?",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = WarningAmber
                                            )
                                            Text(
                                                text = "Ví dụ: ăn vặt ngẫu hứng, trà sữa, quên hủy gia hạn định kỳ",
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                    }
                                    Switch(
                                        checked = isLossRisk,
                                        onCheckedChange = { isLossRisk = it },
                                        modifier = Modifier
                                            .scale(0.8f)
                                            .testTag("tx_is_loss_switch")
                                    )
                                }

                                if (isLossRisk) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Phân loại nguyên nhân:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    val lossTypes = listOf("Chi tiêu tùy ý", "Phí quên hủy", "Bị mua hớ/đắt", "Khác")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        lossTypes.forEach { type ->
                                            val selected = selectedLossType == type
                                            ElevatedFilterChip(
                                                selected = selected,
                                                onClick = { selectedLossType = type },
                                                label = { Text(type, fontSize = 9.sp) },
                                                modifier = Modifier.testTag("chip_loss_$type")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Buttons Save & Cancel
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss, modifier = Modifier.testTag("tx_cancel_btn")) {
                            Text("Bỏ qua")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amtNum = amountStr.toDoubleOrNull() ?: 0.0
                                if (amtNum > 0.0) {
                                    onSave(
                                        amtNum,
                                        isIncome,
                                        selectedCategory,
                                        note,
                                        if (isIncome) false else isLossRisk,
                                        if (isIncome) "" else (if (isLossRisk) selectedLossType else "")
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isIncome) IncomeGreen else if (isLossRisk) WarningAmber else MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("tx_save_btn")
                        ) {
                            Text("Ghi vào Sổ")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddSavingGoalDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, target: Double, initial: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("") }
    var initialStr by remember { mutableStateOf("") }

    val popularSavingNames = listOf("Quỹ Dự Phòng Sự Cố", "Gửi gia đình", "Sửa xe/Bảo dưỡng máy", "Đóng học", "Mua Sắm Tết")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_goal_dialog")
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "TẠO HŨ TÍCH LŨY MỚI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Tên hũ (mục tiêu tích lũy)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_name_input")
                    )
                }

                // Autocomplete chip recommend list
                item {
                    Text("Gợi ý tên hũ thiết yếu:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        popularSavingNames.take(3).forEach { pName ->
                            SuggestionChip(
                                onClick = { name = pName },
                                label = { Text(pName, fontSize = 9.sp) },
                                modifier = Modifier.testTag("suggest_name_$pName")
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = targetStr,
                        onValueChange = { targetStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Mục tiêu tiền (VND)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_target_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = initialStr,
                        onValueChange = { initialStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Số tiền đã tích trữ sẵn (nếu có)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("goal_initial_input")
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss, modifier = Modifier.testTag("goal_cancel_btn")) {
                            Text("Bỏ qua")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val targetAmount = targetStr.toDoubleOrNull() ?: 0.0
                                val initialAmount = initialStr.toDoubleOrNull() ?: 0.0
                                if (name.isNotEmpty() && targetAmount > 0.0) {
                                    onSave(name, targetAmount, initialAmount)
                                }
                            },
                            modifier = Modifier.testTag("goal_save_btn")
                        ) {
                            Text("Tạo Hũ Thôi")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddSubscriptionDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Double, cycle: String, day: Int, cat: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var renewalDayStr by remember { mutableStateOf("1") }
    var selectedCategory by remember { mutableStateOf("Giải trí") }

    val categories = listOf("Viễn thông", "Giải trí", "Phí tài khoản", "Khác")
    val currentDayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    val popularSubsRecommendations = listOf(
        SubscriptionEntity(0, "Mạng Gói Tháng 3G/4G", 90000.0, "Hàng tháng", 15, "Viễn thông", true),
        SubscriptionEntity(0, "Phí Quản Lý Thẻ Ngân Hàng", 11000.0, "Hàng tháng", 5, "Phí tài khoản", true),
        SubscriptionEntity(0, "Yotube Premium Đáng Mộ", 79000.0, "Hàng tháng", 25, "Giải trí", true)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("add_sub_dialog")
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "THEO DÕI ĐĂNG KÝ GIA HẠN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = WarningAmber,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Tên khoản gia hạn định kỳ") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sub_name_input")
                    )
                }

                // Autocomp suggested row
                item {
                    Text("Mẫu định kỳ rò rỉ phổ biến nhất:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        popularSubsRecommendations.forEach { recsub ->
                            SuggestionChip(
                                onClick = {
                                    name = recsub.name
                                    amountStr = recsub.amount.toInt().toString()
                                    renewalDayStr = recsub.renewalDay.toString()
                                    selectedCategory = recsub.category
                                },
                                label = { Text(recsub.name, fontSize = 9.sp) },
                                modifier = Modifier.testTag("suggest_sub_${recsub.id}")
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Mức phí thu một chu kỳ (VND)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sub_amount_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = renewalDayStr,
                        onValueChange = { renewalDayStr = it.filter { c -> c.isDigit() } },
                        label = { Text("Hạn thu phí (Ngày hàng tháng: 1 - 31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sub_day_input")
                    )
                }

                item {
                    Text("Mục phân loại:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val selected = selectedCategory == cat
                            FilterChip(
                                selected = selected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                modifier = Modifier.testTag("chip_sub_cat_$cat")
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss, modifier = Modifier.testTag("sub_cancel_btn")) {
                            Text("Bỏ qua")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amountVal = amountStr.toDoubleOrNull() ?: 0.0
                                val renewalDayVal = renewalDayStr.toIntOrNull()?.coerceIn(1, 31) ?: 1
                                if (name.isNotEmpty() && amountVal > 0.0) {
                                    onSave(name, amountVal, "Hàng tháng", renewalDayVal, selectedCategory)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                            modifier = Modifier.testTag("sub_save_btn")
                        ) {
                            Text("Lưu & Theo dõi", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onSave: (category: String, limit: Double) -> Unit
) {
    var category by remember { mutableStateOf("Ăn uống") }
    var limitStr by remember { mutableStateOf("") }

    val categories = listOf("Ăn uống", "Nhà cửa", "Đi lại", "Hóa đơn", "Giải trí", "Sức khỏe", "Học tập", "Khác")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("add_budget_dialog")
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "LẬP HẠN MỨC TIÊU DÙNG",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { limitStr = it.filter { c -> c.isDigit() } },
                    label = { Text("Số tiền giới hạn tối đa (VND)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_limit_input")
                )

                Text("Áp dụng cho danh mục:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val selected = category == cat
                        FilterChip(
                            selected = selected,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            modifier = Modifier.testTag("chip_budget_$cat")
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.testTag("budget_cancel_btn")) {
                        Text("Bỏ qua")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val limitAmount = limitStr.toDoubleOrNull() ?: 0.0
                            if (limitAmount > 0.0) {
                                onSave(category, limitAmount)
                            }
                        },
                        modifier = Modifier.testTag("budget_save_btn")
                    ) {
                        Text("Thiết Lập")
                    }
                }
            }
        }
    }
}
