package com.nxzef.wc.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nxzef.wc.presentation.theme.WCTheme
import com.nxzef.wc.shared.model.BookingStatus
import com.nxzef.wc.shared.model.LeadSource
import com.nxzef.wc.shared.model.QuoteStatus

@Composable
fun StatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun QuoteStatusBadge(status: QuoteStatus, modifier: Modifier = Modifier) {
    val color = when (status) {
        QuoteStatus.DRAFT -> WCTheme.colors.quoteDraft
        QuoteStatus.SENT -> WCTheme.colors.quoteSent
        QuoteStatus.ACCEPTED -> WCTheme.colors.quoteAccepted
        QuoteStatus.REJECTED -> WCTheme.colors.quoteRejected
    }
    StatusBadge(text = status.name, color = color, modifier = modifier)
}

@Composable
fun BookingStatusBadge(status: BookingStatus, modifier: Modifier = Modifier) {
    val color = when (status) {
        BookingStatus.BOOKED -> WCTheme.colors.statusBooked
        BookingStatus.SHOOT_DONE -> WCTheme.colors.statusShootDone
        BookingStatus.EDITING -> WCTheme.colors.statusEditing
        BookingStatus.DELIVERED -> WCTheme.colors.statusDelivered
        BookingStatus.CLOSED -> WCTheme.colors.statusClosed
    }
    StatusBadge(text = status.name.replace("_", " "), color = color, modifier = modifier)
}

@Composable
fun LeadStatusBadge(status: String, modifier: Modifier = Modifier) {
    val color = when (status) {
        "NEW" -> WCTheme.colors.statusNew
        "CONTACTED" -> WCTheme.colors.statusContacted
        "NEGOTIATING" -> WCTheme.colors.statusNegotiating
        "WON" -> WCTheme.colors.statusWon
        "LOST" -> WCTheme.colors.statusLost
        "BOOKED" -> WCTheme.colors.statusBooked
        "SHOOT_DONE" -> WCTheme.colors.statusShootDone
        "EDITING" -> WCTheme.colors.statusEditing
        "DELIVERED" -> WCTheme.colors.statusDelivered
        "CLOSED" -> WCTheme.colors.statusClosed
        else -> MaterialTheme.colorScheme.outline
    }
    StatusBadge(text = status, color = color, modifier = modifier)
}

@Composable
fun LeadSourceBadge(source: LeadSource, modifier: Modifier = Modifier) {
    val color = when (source) {
        LeadSource.INSTAGRAM -> WCTheme.colors.sourceInstagram
        LeadSource.FACEBOOK -> WCTheme.colors.sourceFacebook
        LeadSource.GOOGLE -> WCTheme.colors.sourceGoogle
        LeadSource.REFERRAL -> WCTheme.colors.sourceReferral
        LeadSource.WALK_IN -> WCTheme.colors.sourceWalkIn
        LeadSource.OTHER -> WCTheme.colors.sourceOther
    }
    StatusBadge(text = source.name.replace("_", " "), color = color, modifier = modifier)
}

@Composable
fun InvoiceStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        "FULLY PAID" -> WCTheme.colors.invoicePaid
        "DEPOSIT PAID" -> WCTheme.colors.invoicePartial
        else -> WCTheme.colors.invoiceUnpaid
    }
    StatusBadge(text = status, color = color, modifier = modifier)
}
