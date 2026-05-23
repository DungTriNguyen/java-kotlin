package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val isIncome: Boolean,
    val category: String,
    val note: String,
    val timestamp: Long,
    val isLossRisk: Boolean = false,
    val lossRiskType: String = "" // "Chi tiêu tùy ý", "Trắc ẩn bẫy tiêu dùng", "Phí đăng ký ẩn", "Khác"
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val category: String,
    val limitAmount: Double
)

@Entity(tableName = "saving_goals")
data class SavingGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val billingCycle: String = "Hàng tháng", // "Hàng tháng", "Hàng tuần", "Hàng năm"
    val renewalDay: Int = 1, // Day of the month/week this renews
    val category: String = "Giải trí", // "Giải trí", "Viễn thông", "Phí tài khoản", "Khác"
    val isActive: Boolean = true
)
