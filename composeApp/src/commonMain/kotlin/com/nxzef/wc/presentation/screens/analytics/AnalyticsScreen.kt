package com.nxzef.wc.presentation.screens.analytics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nxzef.wc.presentation.components.WCTopBar
import com.nxzef.wc.presentation.theme.WCTheme
import com.nxzef.wc.shared.model.DashboardStats
import com.nxzef.wc.shared.model.Lead
import com.nxzef.wc.shared.model.LeadSource
import com.nxzef.wc.shared.model.ProjectPnL
import com.nxzef.wc.shared.util.CurrencyUtils
import com.nxzef.wc.shared.util.DateUtils
import org.koin.compose.viewmodel.koinViewModel

private val CHART_COLORS = listOf(
    Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFFFFC107), Color(0xFFE91E63),
    Color(0xFF9C27B0), Color(0xFFFF5722), Color(0xFF00BCD4), Color(0xFF795548)
)

@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AnalyticsUiEvent.ShowSnackbar -> snackbarState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        topBar = {
            WCTopBar(
                title = "Analytics",
                subtitle = "Business performance overview",
                onBack = onBack
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null -> Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.onAction(AnalyticsAction.Retry) }) {
                        Text("Retry")
                    }
                }
                state.stats != null -> AnalyticsContent(
                    stats = state.stats!!,
                    leads = state.leads,
                    modifier = Modifier.widthIn(max = 1000.dp).fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun AnalyticsContent(
    stats: DashboardStats,
    leads: List<Lead>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Monthly Revenue Line Chart
        item {
            AnalyticsSection("Monthly Revenue") {
                if (stats.monthlyRevenue.isEmpty()) {
                    EmptyChartPlaceholder("No revenue data yet")
                } else {
                    val data = stats.monthlyRevenue.map { it.amount.toFloat() }
                    val labels = stats.monthlyRevenue.map { it.month }
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        AnalyticsLineChart(
                            data = data,
                            labels = labels,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // 2. Lead Source Pie Chart
        item {
            AnalyticsSection("Lead Sources") {
                if (stats.leadsBySource.isEmpty()) {
                    EmptyChartPlaceholder("No lead source data yet")
                } else {
                    LeadSourcePieChart(leadsBySource = stats.leadsBySource)
                }
            }
        }

        // 3. Conversion Funnel
        item {
            AnalyticsSection("Conversion Funnel") {
                ConversionFunnel(leads = leads)
            }
        }

        // 4. Top 5 Projects by Revenue
        item {
            AnalyticsSection("Top Projects by Revenue") {
                val topProjects = stats.projectPnLList.sortedByDescending { it.revenue }.take(5)
                if (topProjects.isEmpty()) {
                    EmptyChartPlaceholder("No project data yet")
                } else {
                    TopProjectsList(projects = topProjects)
                }
            }
        }

        // 5. Monthly Comparison Table
        item {
            AnalyticsSection("Monthly Breakdown") {
                if (stats.monthlyRevenue.isEmpty()) {
                    EmptyChartPlaceholder("No monthly data yet")
                } else {
                    MonthlyComparisonTable(stats = stats)
                }
            }
        }
    }
}

// ── 1. Line Chart ─────────────────────────────────────────────────────────────

@Composable
private fun AnalyticsLineChart(
    data: List<Float>,
    labels: List<String>,
    color: Color
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = Color.Gray.copy(alpha = 0.7f),
        fontSize = 10.sp
    )
    var animPlayed by rememberSaveable { mutableStateOf(false) }
    val progress = remember { Animatable(if (animPlayed) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!animPlayed) {
            progress.animateTo(1f, animationSpec = tween(1200, easing = FastOutSlowInEasing))
            animPlayed = true
        }
    }
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val dotBg = MaterialTheme.colorScheme.surface
    val maxVal = (data.maxOrNull() ?: 1f).coerceAtLeast(1f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val padL = 52.dp.toPx()
        val padB = 28.dp.toPx()
        val cw = size.width - padL
        val ch = size.height - padB
        val spacing = cw / (data.size - 1).coerceAtLeast(1)

        // Grid lines + Y labels
        for (i in 0..4) {
            val y = ch - (i * ch / 4)
            drawLine(gridColor.copy(alpha = 0.25f), Offset(padL, y), Offset(size.width, y), 1.dp.toPx())
            val lbl = textMeasurer.measure(CurrencyUtils.formatINRShort((i * maxVal / 4).toDouble()), labelStyle)
            drawText(lbl, topLeft = Offset(padL - lbl.size.width - 6.dp.toPx(), y - lbl.size.height / 2))
        }

        // X labels
        labels.forEachIndexed { idx, lbl ->
            val x = padL + idx * spacing
            val lay = textMeasurer.measure(lbl, labelStyle)
            drawText(lay, topLeft = Offset(x - lay.size.width / 2f, ch + 6.dp.toPx()))
        }

        if (data.all { it == 0f }) return@Canvas

        val path = Path()
        val fill = Path()
        data.forEachIndexed { idx, v ->
            val x = padL + idx * spacing
            val y = ch - (v / maxVal * ch)
            if (idx == 0) { path.moveTo(x, y); fill.moveTo(x, ch); fill.lineTo(x, y) }
            else { path.lineTo(x, y); fill.lineTo(x, y) }
            if (idx == data.size - 1) { fill.lineTo(x, ch); fill.close() }
        }

        val clip = padL + cw * progress.value
        drawContext.canvas.save()
        drawContext.canvas.clipRect(padL, 0f, clip, size.height)

        drawPath(fill, Brush.verticalGradient(listOf(color.copy(alpha = 0.18f), Color.Transparent)))
        drawPath(path, color, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))

        data.forEachIndexed { idx, v ->
            if (v > 0) {
                val x = padL + idx * spacing
                val y = ch - (v / maxVal * ch)
                if (x <= clip) {
                    drawCircle(color, 4.dp.toPx(), Offset(x, y))
                    drawCircle(dotBg, 2.dp.toPx(), Offset(x, y))
                }
            }
        }
        drawContext.canvas.restore()
    }
}

// ── 2. Lead Source Pie Chart ──────────────────────────────────────────────────

@Composable
private fun LeadSourcePieChart(leadsBySource: Map<String, Int>) {
    val sorted = leadsBySource.entries.sortedByDescending { it.value }
    val total = sorted.sumOf { it.value }.coerceAtLeast(1)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(130.dp)) {
            var start = -90f
            sorted.forEachIndexed { idx, (_, count) ->
                val sweep = (count.toFloat() / total * 360f)
                drawArc(
                    color = CHART_COLORS[idx % CHART_COLORS.size],
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = true,
                    size = Size(size.width, size.height),
                    style = Fill
                )
                start += sweep
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sorted.forEachIndexed { idx, (sourceName, count) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(CHART_COLORS[idx % CHART_COLORS.size], CircleShape)
                    )
                    Text(
                        text = sourceName.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$count (${count * 100 / total}%)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ── 3. Conversion Funnel ─────────────────────────────────────────────────────

@Composable
private fun ConversionFunnel(leads: List<Lead>) {
    if (leads.isEmpty()) {
        EmptyChartPlaceholder("No leads data")
        return
    }

    val total = leads.size
    val inNegotiation = leads.count { it.statusName.lowercase().let { s ->
        s.contains("negotiat") || s.contains("proposal") || s.contains("quote")
    }}
    val won = leads.count { it.statusName.lowercase().let { s ->
        s.contains("won") || s.contains("booked") || s.contains("confirm")
    }}
    val delivered = leads.count { it.statusName.lowercase().let { s ->
        s.contains("deliver") || s.contains("close") || s.contains("complet")
    }}

    val stages = listOf(
        Triple("Total Leads", total, total),
        Triple("Negotiating", inNegotiation, total),
        Triple("Won / Booked", won, total),
        Triple("Delivered", delivered, total)
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceHigh = MaterialTheme.colorScheme.surfaceContainerHighest

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        stages.forEachIndexed { idx, (label, count, base) ->
            val fraction = if (base > 0) count.toFloat() / base else 0f
            val barColor = CHART_COLORS[idx]

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "$count (${(fraction * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(surfaceHigh, MaterialTheme.shapes.extraSmall)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .height(12.dp)
                            .background(barColor, MaterialTheme.shapes.extraSmall)
                    )
                }
            }
        }
    }
}

// ── 4. Top Projects List ──────────────────────────────────────────────────────

@Composable
private fun TopProjectsList(projects: List<ProjectPnL>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        projects.forEachIndexed { idx, pnl ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${idx + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        pnl.eventType,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        DateUtils.formatDisplayDate(pnl.eventDate),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        CurrencyUtils.formatINR(pnl.revenue),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Profit ${CurrencyUtils.formatINRShort(pnl.netProfit)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (pnl.netProfit >= 0) WCTheme.colors.success
                        else MaterialTheme.colorScheme.error
                    )
                }
            }
            if (idx < projects.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 40.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}

// ── 5. Monthly Comparison Table ───────────────────────────────────────────────

@Composable
private fun MonthlyComparisonTable(stats: DashboardStats) {
    val totalRevenue = stats.monthlyRevenue.sumOf { it.amount }.coerceAtLeast(0.01)

    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text("Month", modifier = Modifier.weight(1.5f),
                style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text("Revenue", modifier = Modifier.weight(2f), textAlign = TextAlign.End,
                style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text("Share", modifier = Modifier.weight(1f), textAlign = TextAlign.End,
                style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        stats.monthlyRevenue.forEachIndexed { idx, point ->
            val share = point.amount / totalRevenue * 100
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (idx % 2 == 0) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceContainerLow
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(point.month, modifier = Modifier.weight(1.5f),
                    style = MaterialTheme.typography.bodySmall)
                Text(CurrencyUtils.formatINR(point.amount),
                    modifier = Modifier.weight(2f), textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Text("${share.toInt()}%",
                    modifier = Modifier.weight(1f), textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun AnalyticsSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun EmptyChartPlaceholder(message: String) {
    Box(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}
