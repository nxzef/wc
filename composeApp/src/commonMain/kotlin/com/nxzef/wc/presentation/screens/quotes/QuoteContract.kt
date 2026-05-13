package com.nxzef.wc.presentation.screens.quotes

import com.nxzef.wc.shared.model.Quote
import com.nxzef.wc.shared.model.QuoteStatus

class QuoteContract {
    data class State(
        val quotes: List<Quote> = emptyList(),
        val isLoading: Boolean = false,
        val isSending: Boolean = false,
        val error: String? = null,
        val leadId: String = "",
        val clientName: String = "",
        val clientEmail: String = "",
        val selectedFilePath: String? = null,
        val selectedFileName: String? = null,
        val selectedFileBytes: ByteArray? = null,
        val amountInput: String = "",
        val notesInput: String = ""
    )

    sealed class Action {
        data class LoadQuotes(
            val leadId: String,
            val clientName: String,
            val clientEmail: String
        ) : Action()
        data class AttachPdf(val path: String, val name: String, val bytes: ByteArray) : Action()
        data class OnAmountChange(val value: String) : Action()
        data class OnNotesChange(val value: String) : Action()
        object SendQuote : Action()
        data class UpdateStatus(val id: String, val status: QuoteStatus) : Action()
    }

    sealed class UiEvent {
        data class ShowError(val message: String) : UiEvent()
        data class QuoteSent(val email: String) : UiEvent()
    }
}
