package com.nxzef.wc.presentation.screens.quotes

import com.nxzef.wc.shared.model.Quote
import com.nxzef.wc.shared.model.QuoteStatus
import com.nxzef.wc.shared.model.CreateQuoteItemRequest

class QuoteContract {
    data class State(
        val quotes: List<Quote> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val leadId: String = ""
    )

    sealed class Action {
        data class LoadQuotes(val leadId: String) : Action()
        data class UpdateStatus(val id: String, val status: QuoteStatus) : Action()
        data class CreateQuote(val notes: String, val items: List<CreateQuoteItemRequest>) : Action()
    }

    sealed class UiEvent {
        data class ShowError(val message: String) : UiEvent()
        object QuoteCreated : UiEvent()
    }
}
