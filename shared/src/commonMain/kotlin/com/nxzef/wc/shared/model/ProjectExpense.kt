package com.nxzef.wc.shared.model

import kotlinx.serialization.Serializable

val EXPENSE_CATEGORIES = listOf(
    "Photographer Charges",
    "Videographer Charges",
    "Editor Charges",
    "Colourist Charges",
    "Travel & Fuel",
    "Accommodation",
    "Food & Meals",
    "Album & Printing",
    "Miscellaneous"
)

@Serializable
data class ProjectExpense(
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

@Serializable
data class CreateProjectExpenseRequest(
    val bookingId: String,
    val category: String,
    val description: String? = null,
    val estimatedAmount: Double = 0.0,
    val actualAmount: Double,
    val expenseDate: String,
    val paymentMethod: String? = null,
    val notes: String? = null
)
