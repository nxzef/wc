package com.nxzef.wc.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class WCColors(
    val statusNew: Color,
    val statusContacted: Color,
    val statusNegotiating: Color,
    val statusWon: Color,
    val statusLost: Color,
    val statusBooked: Color,
    val statusShootDone: Color,
    val statusEditing: Color,
    val statusDelivered: Color,
    val statusClosed: Color,
    
    val sourceInstagram: Color,
    val sourceFacebook: Color,
    val sourceGoogle: Color,
    val sourceReferral: Color,
    val sourceWalkIn: Color,
    val sourceOther: Color,

    val quoteDraft: Color,
    val quoteSent: Color,
    val quoteAccepted: Color,
    val quoteRejected: Color,

    val invoicePaid: Color,
    val invoicePartial: Color,
    val invoiceUnpaid: Color
)

val lightWCColors = WCColors(
    statusNew = Color(0xFF0061A4),
    statusContacted = Color(0xFF8B5000),
    statusNegotiating = Color(0xFF8E31B2),
    statusWon = Color(0xFF006D32),
    statusLost = Color(0xFFBA1A1A),
    statusBooked = Color(0xFF0061A4),
    statusShootDone = Color(0xFF006A60),
    statusEditing = Color(0xFF8B5000),
    statusDelivered = Color(0xFF3B6939),
    statusClosed = Color(0xFF006D32),
    
    sourceInstagram = Color(0xFF91004E),
    sourceFacebook = Color(0xFF0056D2),
    sourceGoogle = Color(0xFF006E1C),
    sourceReferral = Color(0xFF7A33AC),
    sourceWalkIn = Color(0xFF825500),
    sourceOther = Color(0xFF4F5F72),

    quoteDraft = Color(0xFF4F5F72),
    quoteSent = Color(0xFF0061A4),
    quoteAccepted = Color(0xFF006D32),
    quoteRejected = Color(0xFFBA1A1A),

    invoicePaid = Color(0xFF006D32),
    invoicePartial = Color(0xFF8B5000),
    invoiceUnpaid = Color(0xFFBA1A1A)
)

val darkWCColors = WCColors(
    statusNew = Color(0xFF9ECAFF),
    statusContacted = Color(0xFFFFB866),
    statusNegotiating = Color(0xFFF7ADFF),
    statusWon = Color(0xFF66DF82),
    statusLost = Color(0xFFFFB4AB),
    statusBooked = Color(0xFF9ECAFF),
    statusShootDone = Color(0xFF53DBC9),
    statusEditing = Color(0xFFFFB866),
    statusDelivered = Color(0xFFA1D39A),
    statusClosed = Color(0xFF66DF82),
    
    sourceInstagram = Color(0xFFFFB1C8),
    sourceFacebook = Color(0xFFB1C5FF),
    sourceGoogle = Color(0xFF76DE74),
    sourceReferral = Color(0xFFE9B3FF),
    sourceWalkIn = Color(0xFFFFB951),
    sourceOther = Color(0xFFB7C7DC),

    quoteDraft = Color(0xFFB7C7DC),
    quoteSent = Color(0xFF9ECAFF),
    quoteAccepted = Color(0xFF66DF82),
    quoteRejected = Color(0xFFFFB4AB),

    invoicePaid = Color(0xFF66DF82),
    invoicePartial = Color(0xFFFFB866),
    invoiceUnpaid = Color(0xFFFFB4AB)
)

val LocalWCColors = staticCompositionLocalOf {
    lightWCColors
}
