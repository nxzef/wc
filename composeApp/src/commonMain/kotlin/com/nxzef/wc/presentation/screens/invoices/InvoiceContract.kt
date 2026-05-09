package com.nxzef.wc.presentation.screens.invoices

import com.nxzef.wc.shared.model.Booking
import com.nxzef.wc.shared.model.Invoice
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.Receipt

data class InvoiceState(
    val invoices: List<Invoice> = emptyList(),
    val bookings: List<Booking> = emptyList(),
    val leads: List<Lead> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedInvoice: Invoice? = null,
    val receipts: List<Receipt> = emptyList(),
    val isLoadingReceipts: Boolean = false,
    val viewingReceipt: Receipt? = null,
    val notes: String = ""
)

sealed interface InvoiceAction {
    data object LoadInvoices : InvoiceAction
    data class SelectInvoice(val invoice: Invoice) : InvoiceAction
    data object DismissDetail : InvoiceAction
    data class OnNotesChange(val value: String) : InvoiceAction
    data class OnMarkDepositPaid(val invoiceId: String, val date: String) : InvoiceAction
    data class OnMarkFinalPaid(val invoiceId: String, val date: String) : InvoiceAction
    data class ViewReceipt(val receipt: Receipt) : InvoiceAction
    data object DismissReceipt : InvoiceAction
}

sealed interface InvoiceUiEvent {
    data class ShowSnackbar(val message: String) : InvoiceUiEvent
    data object InvoiceCreated : InvoiceUiEvent
    data object PaymentUpdated : InvoiceUiEvent
}
