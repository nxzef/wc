package com.nxzef.wc.presentation.screens.quotes

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.usecase.quotes.GetQuotesByLeadIdUseCase
import com.nxzef.wc.domain.usecase.quotes.SendQuoteUseCase
import com.nxzef.wc.domain.usecase.quotes.UpdateQuoteStatusUseCase
import com.nxzef.wc.shared.model.SendQuoteRequest
import com.nxzef.wc.shared.model.UpdateQuoteStatusRequest
import com.nxzef.wc.shared.util.AppResult
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class QuoteViewModel(
    private val getQuotesByLeadIdUseCase: GetQuotesByLeadIdUseCase,
    private val sendQuoteUseCase: SendQuoteUseCase,
    private val updateQuoteStatusUseCase: UpdateQuoteStatusUseCase
) : ViewModel() {

    private val _state = mutableStateOf(QuoteContract.State())
    val state: State<QuoteContract.State> = _state

    private val _uiEvent = MutableSharedFlow<QuoteContract.UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onAction(action: QuoteContract.Action) {
        when (action) {
            is QuoteContract.Action.LoadQuotes -> loadQuotes(action.leadId, action.clientName, action.clientEmail)
            is QuoteContract.Action.AttachPdf -> attachPdf(action.path, action.name, action.bytes)
            is QuoteContract.Action.OnAmountChange ->
                _state.value = _state.value.copy(
                    amountInput = action.value.filter { it.isDigit() || it == '.' }
                )
            is QuoteContract.Action.OnNotesChange ->
                _state.value = _state.value.copy(notesInput = action.value)
            is QuoteContract.Action.SendQuote -> sendQuote()
            is QuoteContract.Action.UpdateStatus -> updateStatus(action.id, action.status)
        }
    }

    private fun loadQuotes(leadId: String, clientName: String, clientEmail: String) {
        _state.value = _state.value.copy(
            isLoading = true,
            leadId = leadId,
            clientName = clientName,
            clientEmail = clientEmail
        )
        viewModelScope.launch {
            when (val result = getQuotesByLeadIdUseCase(leadId)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(quotes = result.data, isLoading = false)
                }
                is AppResult.Failure -> {
                    _state.value = _state.value.copy(error = "Failed to load quotes.", isLoading = false)
                }
                is AppResult.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun attachPdf(path: String, name: String, bytes: ByteArray) {
        _state.value = _state.value.copy(
            selectedFilePath = path,
            selectedFileName = name,
            selectedFileBytes = bytes
        )
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun sendQuote() {
        val current = _state.value
        val bytes = current.selectedFileBytes ?: return
        val fileName = current.selectedFileName ?: return
        val amount = current.amountInput.toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            viewModelScope.launch {
                _uiEvent.emit(QuoteContract.UiEvent.ShowError("Enter a valid quote amount"))
            }
            return
        }
        _state.value = current.copy(isSending = true)
        viewModelScope.launch {
            val fileBase64 = Base64.encode(bytes)
            val request = SendQuoteRequest(
                leadId = current.leadId,
                clientEmail = current.clientEmail,
                fileBase64 = fileBase64,
                fileName = fileName,
                totalAmount = amount,
                notes = current.notesInput.trim().takeIf { it.isNotEmpty() }
            )
            when (val result = sendQuoteUseCase(request)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(
                        isSending = false,
                        selectedFilePath = null,
                        selectedFileName = null,
                        selectedFileBytes = null,
                        amountInput = "",
                        notesInput = ""
                    )
                    _uiEvent.emit(QuoteContract.UiEvent.QuoteSent(current.clientEmail))
                    loadQuotes(current.leadId, current.clientName, current.clientEmail)
                }
                is AppResult.Failure -> {
                    _state.value = _state.value.copy(isSending = false)
                    _uiEvent.emit(
                        QuoteContract.UiEvent.ShowError("Failed to send quote. Please try again.")
                    )
                }
                is AppResult.Loading -> {}
            }
        }
    }

    private fun updateStatus(id: String, status: com.nxzef.wc.shared.model.QuoteStatus) {
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val request = UpdateQuoteStatusRequest(status)
            when (val result = updateQuoteStatusUseCase(id, request)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false)
                    loadQuotes(_state.value.leadId, _state.value.clientName, _state.value.clientEmail)
                }
                is AppResult.Failure -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _uiEvent.emit(
                        QuoteContract.UiEvent.ShowError("Failed to update quote status.")
                    )
                }
                is AppResult.Loading -> {}
            }
        }
    }
}
