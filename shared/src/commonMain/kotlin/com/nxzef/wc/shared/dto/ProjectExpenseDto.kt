package com.nxzef.wc.shared.dto

import com.nxzef.wc.shared.model.ProjectExpense
import kotlinx.serialization.Serializable

@Serializable
data class ProjectExpenseDto(
    val id: String,
    val bookingId: String,
    val category: String,
    val description: String? = null,
    val estimatedAmount: Double = 0.0,
    val actualAmount: Double,
    val expenseDate: String,
    val addedBy: String? = null,
    val paymentMethod: String? = null,
    val notes: String? = null,
    val createdAt: String
)

fun ProjectExpenseDto.toDomain(): ProjectExpense = ProjectExpense(
    id = id, bookingId = bookingId, category = category,
    description = description, estimatedAmount = estimatedAmount,
    actualAmount = actualAmount, expenseDate = expenseDate,
    addedBy = addedBy, paymentMethod = paymentMethod,
    notes = notes, createdAt = createdAt
)

fun ProjectExpense.toDto(): ProjectExpenseDto = ProjectExpenseDto(
    id = id, bookingId = bookingId, category = category,
    description = description, estimatedAmount = estimatedAmount,
    actualAmount = actualAmount, expenseDate = expenseDate,
    addedBy = addedBy, paymentMethod = paymentMethod,
    notes = notes, createdAt = createdAt
)
