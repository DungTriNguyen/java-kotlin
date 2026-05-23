package com.example.data

import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val financeDao: FinanceDao) {
    val allTransactions: Flow<List<TransactionEntity>> = financeDao.getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = financeDao.getAllBudgets()
    val allSavingGoals: Flow<List<SavingGoalEntity>> = financeDao.getAllSavingGoals()
    val allSubscriptions: Flow<List<SubscriptionEntity>> = financeDao.getAllSubscriptions()

    suspend fun insertTransaction(transaction: TransactionEntity) {
        financeDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Int) {
        financeDao.deleteTransaction(id)
    }

    suspend fun insertBudget(budget: BudgetEntity) {
        financeDao.insertBudget(budget)
    }

    suspend fun deleteBudget(category: String) {
        financeDao.deleteBudget(category)
    }

    suspend fun insertSavingGoal(goal: SavingGoalEntity) {
        financeDao.insertSavingGoal(goal)
    }

    suspend fun deleteSavingGoal(id: Int) {
        financeDao.deleteSavingGoal(id)
    }

    suspend fun insertSubscription(subscription: SubscriptionEntity) {
        financeDao.insertSubscription(subscription)
    }

    suspend fun deleteSubscription(id: Int) {
        financeDao.deleteSubscription(id)
    }
}
