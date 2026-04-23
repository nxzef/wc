package com.nxzef.wc.presentation.screens.quotes

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nxzef.wc.domain.usecase.quotes.CreateQuoteUseCase
import com.nxzef.wc.domain.usecase.quotes.GetQuotesByLeadIdUseCase
import com.nxzef.wc.domain.usecase.quotes.UpdateQuoteStatusUseCase
import com.nxzef.wc.shared.model.CreateQuoteRequest
import com.nxzef.wc.shared.model.UpdateQuoteStatusRequest
import com.nxzef.wc.shared.util.AppResult
import com.nxzef.wc.shared.model.CreateQuoteItemRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class QuoteViewModel(
    private val getQuotesByLeadIdUseCase: GetQuotesByLeadIdUseCase,
    private val createQuoteUseCase: CreateQuoteUseCase,
    private val updateQuoteStatusUseCase: UpdateQuoteStatusUseCase
) : ViewModel() {

    private val _state = mutableStateOf(QuoteContract.State())
    val state: State<QuoteContract.State> = _state

    private val _uiEvent = MutableSharedFlow<QuoteContract.UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun onAction(action: QuoteContract.Action) {
        when (action) {
            is QuoteContract.Action.LoadQuotes -> loadQuotes(action.leadId)
            is QuoteContract.Action.CreateQuote -> createQuote(action.notes, action.items)
            is QuoteContract.Action.UpdateStatus -> updateStatus(action.id, action.status)
        }
    }

    private fun loadQuotes(leadId: String) {
        _state.value = _state.value.copy(isLoading = true, leadId = leadId)
        viewModelScope.launch {
            when (val result = getQuotesByLeadIdUseCase(leadId)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(quotes = result.data, isLoading = false)
                }
                is AppResult.Failure -> {
                    _state.value = _state.value.copy(
                        error = result.exception.message,
                        isLoading = false
                    )
                }
                is AppResult.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }

    private fun createQuote(notes: String, items: List<CreateQuoteItemRequest>) {
        val leadId = _state.value.leadId
        _state.value = _state.value.copy(isLoading = true)
        viewModelScope.launch {
            val request = CreateQuoteRequest(leadId = leadId, notes = notes, items = items)
            when (val result = createQuoteUseCase(request)) {
                is AppResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _uiEvent.emit(QuoteContract.UiEvent.QuoteCreated)
                    loadQuotes(leadId)
                }
                is AppResult.Failure -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _uiEvent.emit(QuoteContract.UiEvent.ShowError(result.exception.message ?: "Failed to create quote"))
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
                    loadQuotes(_state.value.leadId)
                }
                is AppResult.Failure -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _uiEvent.emit(QuoteContract.UiEvent.ShowError(result.exception.message ?: "Failed to update status"))
                }
                is AppResult.Loading -> {}
            }
        }
    }
}
