package com.nxzef.wc.presentation.screens.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.repository.BookingRepository
import com.nxzef.wc.domain.repository.LeadRepository
import com.nxzef.wc.domain.repository.ReceiptRepository
import com.nxzef.wc.domain.usecase.invoices.GetAllInvoicesUseCase
import com.nxzef.wc.domain.usecase.invoices.UpdatePaymentUseCase
import com.nxzef.wc.shared.model.UpdatePaymentRequest
import com.nxzef.wc.shared.util.onFailure
import com.nxzef.wc.shared.util.onSuccess
import com.nxzef.wc.util.RefreshManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InvoiceViewModel(
    private val getAllInvoicesUseCase: GetAllInvoicesUseCase,
    private val updatePaymentUseCase: UpdatePaymentUseCase,
    private val bookingRepository: BookingRepository,
    private val leadRepository: LeadRepository,
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InvoiceState())
    val state: StateFlow<InvoiceState> = _state.asStateFlow()

    private val _uiEvent = Channel<InvoiceUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private var hasLoadedOnce = false
    private var previousInvoiceCount = -1

    init {
        load()
        collectRefreshTrigger()
    }

    private fun collectRefreshTrigger() {
        viewModelScope.launch {
            RefreshManager.refreshTrigger.collect {
                _state.update { it.copy(isRefreshing = true) }
                load(silent = true)
            }
        }
    }

    private fun load(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) _state.update { it.copy(isLoading = true) }
            val oldCount = previousInvoiceCount
            val invoicesResult = getAllInvoicesUseCase()
            val bookingsResult = bookingRepository.getAll()
            val leadsResult = leadRepository.getAll()
            invoicesResult.onSuccess { invoices ->
                val newCount = invoices.size
                if (hasLoadedOnce && silent && newCount != oldCount) {
                    _uiEvent.send(InvoiceUiEvent.ShowSnackbar("Updated"))
                }
                previousInvoiceCount = newCount
                hasLoadedOnce = true
                _state.update { it.copy(invoices = invoices) }
            }.onFailure {
                if (!silent) {
                    _uiEvent.send(InvoiceUiEvent.ShowSnackbar("Failed to load invoices."))
                }
            }
            bookingsResult.onSuccess { bookings ->
                _state.update { it.copy(bookings = bookings) }
            }
            leadsResult.onSuccess { leads ->
                _state.update { it.copy(leads = leads) }
            }
            _state.update { it.copy(isLoading = false, isRefreshing = false) }
        }
    }

    fun onAction(action: InvoiceAction) {
        when (action) {
            InvoiceAction.LoadInvoices -> load(silent = false)

            is InvoiceAction.OnSearchQueryChange ->
                _state.update { it.copy(searchQuery = action.query) }

            is InvoiceAction.SelectInvoice -> {
                _state.update { it.copy(selectedInvoice = action.invoice, receipts = emptyList()) }
                loadReceipts(action.invoice.id)
            }

            InvoiceAction.DismissDetail ->
                _state.update { it.copy(selectedInvoice = null, receipts = emptyList()) }

            is InvoiceAction.OnNotesChange ->
                _state.update { it.copy(notes = action.value) }

            is InvoiceAction.OnMarkDepositPaid ->
                updatePayment(action.invoiceId, UpdatePaymentRequest(depositPaid = true, depositPaidDate = action.date))

            is InvoiceAction.OnMarkFinalPaid ->
                updatePayment(action.invoiceId, UpdatePaymentRequest(finalPaid = true, finalPaidDate = action.date))

            is InvoiceAction.ViewReceipt ->
                _state.update { it.copy(viewingReceipt = action.receipt) }

            InvoiceAction.DismissReceipt ->
                _state.update { it.copy(viewingReceipt = null) }
        }
    }

    private fun loadReceipts(invoiceId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingReceipts = true) }
            receiptRepository.getByInvoiceId(invoiceId)
                .onSuccess { receipts ->
                    _state.update { it.copy(receipts = receipts, isLoadingReceipts = false) }
                }
                .onFailure {
                    _state.update { it.copy(isLoadingReceipts = false) }
                }
        }
    }

    private fun updatePayment(invoiceId: String, request: UpdatePaymentRequest) {
        viewModelScope.launch {
            updatePaymentUseCase(invoiceId, request)
                .onSuccess { (_, emailSent) ->
                    _uiEvent.send(InvoiceUiEvent.PaymentUpdated(emailSent))
                    RefreshManager.triggerRefresh()
                    load()
                    _state.value.selectedInvoice?.let { loadReceipts(invoiceId) }
                }.onFailure {
                    _uiEvent.send(InvoiceUiEvent.ShowSnackbar("Invalid amount entered."))
                }
        }
    }
}
