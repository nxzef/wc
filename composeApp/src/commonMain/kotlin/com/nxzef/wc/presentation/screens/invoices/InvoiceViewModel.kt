package com.nxzef.wc.presentation.screens.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.repository.BookingRepository
import com.nxzef.wc.domain.usecase.invoices.GetAllInvoicesUseCase
import com.nxzef.wc.domain.usecase.invoices.UpdatePaymentUseCase
import com.nxzef.wc.shared.model.UpdatePaymentRequest
import com.nxzef.wc.shared.util.onFailure
import com.nxzef.wc.shared.util.onSuccess
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InvoiceViewModel(
    private val getAllInvoicesUseCase: GetAllInvoicesUseCase,
    private val updatePaymentUseCase: UpdatePaymentUseCase,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InvoiceState())
    val state: StateFlow<InvoiceState> = _state.asStateFlow()

    private val _uiEvent = Channel<InvoiceUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val invoicesResult = getAllInvoicesUseCase()
            val bookingsResult = bookingRepository.getAll()
            invoicesResult.onSuccess { invoices ->
                _state.update { it.copy(invoices = invoices) }
            }
            bookingsResult.onSuccess { bookings ->
                _state.update { it.copy(bookings = bookings) }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun onAction(action: InvoiceAction) {
        when (action) {
            InvoiceAction.LoadInvoices -> load()

            is InvoiceAction.SelectInvoice ->
                _state.update {
                    it.copy(selectedInvoice = action.invoice)
                }

            InvoiceAction.DismissDetail ->
                _state.update { it.copy(selectedInvoice = null) }

            is InvoiceAction.OnNotesChange ->
                _state.update { it.copy(notes = action.value) }

            is InvoiceAction.OnMarkDepositPaid ->
                updatePayment(
                    action.invoiceId,
                    UpdatePaymentRequest(
                        depositPaid = true,
                        depositPaidDate = action.date
                    )
                )

            is InvoiceAction.OnMarkFinalPaid ->
                updatePayment(
                    action.invoiceId,
                    UpdatePaymentRequest(
                        finalPaid = true,
                        finalPaidDate = action.date
                    )
                )
        }
    }

    private fun updatePayment(
        invoiceId: String,
        request: UpdatePaymentRequest
    ) {
        viewModelScope.launch {
            updatePaymentUseCase(invoiceId, request)
                .onSuccess {
                    _uiEvent.send(InvoiceUiEvent.PaymentUpdated)
                    load()
                }.onFailure { e ->
                    _uiEvent.send(
                        InvoiceUiEvent.ShowSnackbar(
                            e.message ?: "Failed to update payment"
                        )
                    )
                }
        }
    }
}