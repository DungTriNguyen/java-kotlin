package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    // --- Database Streams ---
    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingGoals: StateFlow<List<SavingGoalEntity>> = repository.allSavingGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subscriptions: StateFlow<List<SubscriptionEntity>> = repository.allSubscriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Calculated State Flows ---
    val totalBalance: StateFlow<Double> = transactions.map { list ->
        list.sumOf { if (it.isIncome) it.amount else -it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalIncome: StateFlow<Double> = transactions.map { list ->
        list.filter { it.isIncome }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = transactions.map { list ->
        list.filter { !it.isIncome }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalWasted: StateFlow<Double> = transactions.map { list ->
        list.filter { !it.isIncome && it.isLossRisk }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val categoryExpenseSummary: StateFlow<Map<String, Double>> = transactions.map { list ->
        list.filter { !it.isIncome }
            .groupBy { it.category }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val totalSubscriptionCost: StateFlow<Double> = subscriptions.map { list ->
        list.filter { it.isActive }.sumOf {
            when (it.billingCycle) {
                "Hàng tuần" -> it.amount * 4
                "Hàng năm" -> it.amount / 12
                else -> it.amount
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- Operations ---
    fun addTransaction(
        amount: Double,
        isIncome: Boolean,
        category: String,
        note: String,
        isLossRisk: Boolean = false,
        lossRiskType: String = ""
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    amount = amount,
                    isIncome = isIncome,
                    category = category,
                    note = note,
                    timestamp = System.currentTimeMillis(),
                    isLossRisk = isLossRisk,
                    lossRiskType = lossRiskType
                )
            )
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun addSavingGoal(name: String, targetAmount: Double, initialSaved: Double = 0.0) {
        viewModelScope.launch {
            repository.insertSavingGoal(
                SavingGoalEntity(
                    name = name,
                    targetAmount = targetAmount,
                    savedAmount = initialSaved,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun adjustSavingGoalProgress(goal: SavingGoalEntity, addedAmount: Double) {
        viewModelScope.launch {
            val newAmount = (goal.savedAmount + addedAmount).coerceAtLeast(0.0).coerceAtMost(goal.targetAmount)
            repository.insertSavingGoal(goal.copy(savedAmount = newAmount))
            
            // Log as transaction as well to record the cashflow for statistics
            if (addedAmount != 0.0) {
                val isDeposit = addedAmount > 0
                val amountVal = kotlin.math.abs(addedAmount)
                repository.insertTransaction(
                    TransactionEntity(
                        amount = amountVal,
                        isIncome = !isDeposit, // Depositing is outgoing spending, withdrawing is incoming
                        category = "Tích Lũy",
                        note = "${if (isDeposit) "Gửi vào" else "Rút từ"} hũ: ${goal.name}",
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun deleteSavingGoal(id: Int) {
        viewModelScope.launch {
            repository.deleteSavingGoal(id)
        }
    }

    fun addSubscription(name: String, amount: Double, cycle: String, day: Int, cat: String) {
        viewModelScope.launch {
            repository.insertSubscription(
                SubscriptionEntity(
                    name = name,
                    amount = amount,
                    billingCycle = cycle,
                    renewalDay = day,
                    category = cat,
                    isActive = true
                )
            )
        }
    }

    fun toggleSubscriptionActive(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.insertSubscription(subscription.copy(isActive = !subscription.isActive))
        }
    }

    fun deleteSubscription(id: Int) {
        viewModelScope.launch {
            repository.deleteSubscription(id)
        }
    }

    fun setBudget(category: String, limit: Double) {
        viewModelScope.launch {
            repository.insertBudget(BudgetEntity(category, limit))
        }
    }

    fun deleteBudget(category: String) {
        viewModelScope.launch {
            repository.deleteBudget(category)
        }
    }

    // Generate Mock Data on first launch to help low-to-middle income users understand the app instantly
    fun seedSampleDataIfEmpty() {
        viewModelScope.launch {
            // Check if db is empty before seeding
            transactions.take(1).collect { list ->
                if (list.isEmpty()) {
                    // Pre-populate some general budgets for low/mid income helpers
                    repository.insertBudget(BudgetEntity("Ăn uống", 3000000.0))
                    repository.insertBudget(BudgetEntity("Đi lại", 500000.0))
                    repository.insertBudget(BudgetEntity("Nhà cửa", 2500000.0))
                    repository.insertBudget(BudgetEntity("Hóa đơn", 1000000.0))

                    // Some logical transactions
                    repository.insertTransaction(
                        TransactionEntity(
                            amount = 9000000.0,
                            isIncome = true,
                            category = "Thu nhập",
                            note = "Lương tháng này",
                            timestamp = System.currentTimeMillis() - 86400000 * 5
                        )
                    )
                    repository.insertTransaction(
                        TransactionEntity(
                            amount = 2300000.0,
                            isIncome = false,
                            category = "Nhà cửa",
                            note = "Tiền phòng trọ + nước",
                            timestamp = System.currentTimeMillis() - 86400000 * 4
                        )
                    )
                    repository.insertTransaction(
                        TransactionEntity(
                            amount = 120000.0,
                            isIncome = false,
                            category = "Ăn uống",
                            note = "Đi chợ nấu ăn 3 ngày",
                            timestamp = System.currentTimeMillis() - 86400000 * 3
                        )
                    )
                    // Sample waste transaction
                    repository.insertTransaction(
                        TransactionEntity(
                            amount = 150000.0,
                            isIncome = false,
                            category = "Giải trí",
                            note = "Trà sữa & đồ ăn vặt ngẫu hứng",
                            timestamp = System.currentTimeMillis() - 86400000 * 2,
                            isLossRisk = true,
                            lossRiskType = "Chi tiêu tùy ý"
                        )
                    )
                    // Subscription example that leaks money
                    repository.insertSubscription(
                        SubscriptionEntity(
                            name = "Gói xem phim Netflix phụ",
                            amount = 180000.0,
                            billingCycle = "Hàng tháng",
                            renewalDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + 2, // Renewing in 2 days!
                            category = "Giải trí",
                            isActive = true
                        )
                    )
                    repository.insertSubscription(
                        SubscriptionEntity(
                            name = "Phí SMS Banking không dùng",
                            amount = 11000.0,
                            billingCycle = "Hàng tháng",
                            renewalDay = 5,
                            category = "Phí tài khoản",
                            isActive = true
                        )
                    )
                    // Sample saving goal
                    repository.insertSavingGoal(
                        SavingGoalEntity(
                            name = "Quỹ khẩn cấp (Phòng ốm đau/Thất nghiệp)",
                            targetAmount = 5000000.0,
                            savedAmount = 1500000.0
                        )
                    )
                }
            }
        }
    }
}

class FinanceViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
