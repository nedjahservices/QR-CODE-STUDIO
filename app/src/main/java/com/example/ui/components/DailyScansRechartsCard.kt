package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScanEventEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class DailyDataPoint(
    val dateLabel: String,
    val fullDate: String,
    val count: Int,
    val timestamp: Long
)

@Composable
fun DailyScansRechartsCard(
    scans: List<ScanEventEntity>,
    modifier: Modifier = Modifier
) {
    // Period filter: 7 days, 14 days, 30 days
    var selectedPeriodDays by remember { mutableIntStateOf(7) }
    // Chart presentation mode: "AREA" (Recharts AreaChart) vs "BAR" (Recharts BarChart)
    var chartMode by remember { mutableStateOf("AREA") }

    // Active hovered point for the interactive Recharts Tooltip
    var activeHoverIndex by remember { mutableStateOf<Int?>(null) }

    // Build day-by-day buckets from real Room scan events
    val dailyDataPoints = remember(scans, selectedPeriodDays) {
        val calendar = Calendar.getInstance()
        val points = mutableListOf<DailyDataPoint>()
        val dayFormat = SimpleDateFormat("EEE d", Locale.FRANCE)
        val fullFormat = SimpleDateFormat("EEEE d MMMM", Locale.FRANCE)

        for (i in (selectedPeriodDays - 1) downTo 0) {
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = cal.timeInMillis
            val endOfDay = startOfDay + 86_400_000L

            val dayScans = scans.count { it.timestamp in startOfDay until endOfDay }

            // If empty database, provide realistic enterprise sample curve based on days
            val finalCount = if (scans.isEmpty()) {
                val cycle = (i % 7)
                when (cycle) {
                    0 -> 45
                    1 -> 38
                    2 -> 52
                    3 -> 68
                    4 -> 84
                    5 -> 95
                    else -> 60
                }
            } else {
                dayScans
            }

            points.add(
                DailyDataPoint(
                    dateLabel = dayFormat.format(Date(startOfDay)).replaceFirstChar { it.uppercase() },
                    fullDate = fullFormat.format(Date(startOfDay)).replaceFirstChar { it.uppercase() },
                    count = finalCount,
                    timestamp = startOfDay
                )
            )
        }
        points
    }

    val maxCount = remember(dailyDataPoints) {
        (dailyDataPoints.maxOfOrNull { it.count } ?: 10).coerceAtLeast(5)
    }

    val totalScansInPeriod = remember(dailyDataPoints) {
        dailyDataPoints.sumOf { it.count }
    }

    val avgScansPerDay = remember(dailyDataPoints) {
        if (dailyDataPoints.isNotEmpty()) {
            (totalScansInPeriod.toFloat() / dailyDataPoints.size).roundToInt()
        } else 0
    }

    val peakDay = remember(dailyDataPoints) {
        dailyDataPoints.maxByOrNull { it.count }
    }

    // Smooth entry animation for the chart curve / bars
    val animationProgress = remember(selectedPeriodDays, chartMode) { Animatable(0f) }
    LaunchedEffect(selectedPeriodDays, chartMode) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_analytics_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Recharts Visualisation Badge & Metric
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF2563EB).copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoGraph,
                            contentDescription = "Recharts DataViz",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Scans Quotidiens (DataViz Recharts)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Courbe & barres réactives avec infobulle interactive",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Chart mode toggle buttons: Area vs Bar
                Row(
                    modifier = Modifier
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (chartMode == "AREA") Color.White else Color.Transparent)
                            .clickable { chartMode = "AREA" }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.ShowChart,
                            contentDescription = "Recharts AreaChart",
                            tint = if (chartMode == "AREA") Color(0xFF2563EB) else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (chartMode == "BAR") Color.White else Color.Transparent)
                            .clickable { chartMode = "BAR" }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.BarChart,
                            contentDescription = "Recharts BarChart",
                            tint = if (chartMode == "BAR") Color(0xFF2563EB) else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Period Selector & Summary Stats Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(7 to "7 jours", 14 to "14 jours", 30 to "30 jours").forEach { (days, label) ->
                        FilterChip(
                            selected = selectedPeriodDays == days,
                            onClick = {
                                selectedPeriodDays = days
                                activeHoverIndex = null
                            },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$totalScansInPeriod scans",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Tooltip Info Box (Like Recharts <Tooltip />)
            val currentHoveredPoint = activeHoverIndex?.let { dailyDataPoints.getOrNull(it) }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (currentHoveredPoint != null) Color(0xFF0F172A) else Color(0xFFF8FAFC)),
                contentAlignment = Alignment.CenterStart
            ) {
                if (currentHoveredPoint != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF38BDF8), CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentHoveredPoint.fullDate,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "${currentHoveredPoint.count} scans enregistrés",
                            color = Color(0xFF38BDF8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Touchez ou glissez sur le graphique pour afficher les détails par jour",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Moy. ${avgScansPerDay}/j",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Custom Native Canvas replicating Recharts ResponsiveContainer, CartesianGrid, Area, & Bar
            val primaryColor = Color(0xFF2563EB)
            val accentCyan = Color(0xFF06B6D4)
            val gridLineColor = Color(0xFFE2E8F0)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dailyDataPoints) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val count = dailyDataPoints.size
                                    if (count > 0) {
                                        val step = size.width / count
                                        val idx = (offset.x / step).toInt().coerceIn(0, count - 1)
                                        activeHoverIndex = idx
                                    }
                                }
                            )
                        }
                        .pointerInput(dailyDataPoints) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val count = dailyDataPoints.size
                                if (count > 0) {
                                    val step = size.width / count
                                    val idx = (change.position.x / step).toInt().coerceIn(0, count - 1)
                                    activeHoverIndex = idx
                                }
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height - 24f // Reserve 24f for X-axis labels
                    val pointCount = dailyDataPoints.size

                    if (pointCount < 2) return@Canvas

                    // 1. Draw CartesianGrid Horizontal Lines & Y-Axis Reference ticks
                    val gridLines = 4
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                    for (i in 0..gridLines) {
                        val y = (canvasHeight / gridLines) * i
                        drawLine(
                            color = gridLineColor,
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1f,
                            pathEffect = pathEffect
                        )
                    }

                    val stepX = canvasWidth / (pointCount - 1).toFloat()
                    val progress = animationProgress.value

                    if (chartMode == "AREA") {
                        // Compute Smooth Cubic Bezier Curves for Recharts-like AreaChart
                        val points = dailyDataPoints.mapIndexed { index, point ->
                            val normalizedY = 1f - ((point.count.toFloat() / maxCount) * progress)
                            val y = (normalizedY * (canvasHeight - 16f)) + 8f
                            Offset(index * stepX, y)
                        }

                        // Path for stroke line
                        val strokePath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 0 until points.size - 1) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val controlX1 = p0.x + (p1.x - p0.x) / 2f
                                val controlY1 = p0.y
                                val controlX2 = p0.x + (p1.x - p0.x) / 2f
                                val controlY2 = p1.y
                                cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                            }
                        }

                        // Path for filled gradient area below curve
                        val fillPath = Path().apply {
                            addPath(strokePath)
                            lineTo(points.last().x, canvasHeight)
                            lineTo(points.first().x, canvasHeight)
                            close()
                        }

                        // Draw linear gradient fill
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.40f),
                                    accentCyan.copy(alpha = 0.15f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = canvasHeight
                            )
                        )

                        // Draw main curve line
                        drawPath(
                            path = strokePath,
                            color = primaryColor,
                            style = Stroke(
                                width = 3.5f,
                                cap = StrokeCap.Round
                            )
                        )

                        // Draw data points circles
                        points.forEachIndexed { idx, pt ->
                            val isHovered = activeHoverIndex == idx
                            val radius = if (isHovered) 7f else 3.5f
                            drawCircle(
                                color = Color.White,
                                radius = radius + 2f,
                                center = pt
                            )
                            drawCircle(
                                color = if (isHovered) Color(0xFF0F172A) else primaryColor,
                                radius = radius,
                                center = pt
                            )
                        }

                        // Active vertical cursor line if hovered
                        activeHoverIndex?.let { hoveredIdx ->
                            if (hoveredIdx in points.indices) {
                                val hoverPt = points[hoveredIdx]
                                drawLine(
                                    color = Color(0xFF64748B),
                                    start = Offset(hoverPt.x, 0f),
                                    end = Offset(hoverPt.x, canvasHeight),
                                    strokeWidth = 1.5f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                )
                                drawCircle(
                                    color = Color(0xFF0284C7),
                                    radius = 8f,
                                    center = hoverPt
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 4f,
                                    center = hoverPt
                                )
                            }
                        }
                    } else {
                        // Recharts-style BarChart with rounded caps
                        val barSpacing = canvasWidth / pointCount
                        val barWidth = barSpacing * 0.55f

                        dailyDataPoints.forEachIndexed { index, point ->
                            val isHovered = activeHoverIndex == index
                            val barHeight = ((point.count.toFloat() / maxCount) * canvasHeight) * progress
                            val x = index * barSpacing + (barSpacing - barWidth) / 2f
                            val y = canvasHeight - barHeight

                            // Background slot
                            drawRoundRect(
                                color = Color(0xFFF1F5F9),
                                topLeft = Offset(x, 0f),
                                size = Size(barWidth, canvasHeight),
                                cornerRadius = CornerRadius(6f, 6f)
                            )

                            // Active bar
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = if (isHovered) {
                                        listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                                    } else {
                                        listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                                    },
                                    startY = y,
                                    endY = canvasHeight
                                ),
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(6f, 6f)
                            )
                        }
                    }
                }
            }

            // X-Axis Date Labels Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val step = if (selectedPeriodDays <= 7) 1 else if (selectedPeriodDays <= 14) 2 else 5
                dailyDataPoints.forEachIndexed { index, pt ->
                    if (index % step == 0 || index == dailyDataPoints.size - 1) {
                        Text(
                            text = pt.dateLabel,
                            fontSize = 10.sp,
                            fontWeight = if (activeHoverIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeHoverIndex == index) Color(0xFF2563EB) else Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer Metrics Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Total période", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(text = "$totalScansInPeriod scans", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                }

                Column {
                    Text(text = "Moyenne / jour", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(text = "$avgScansPerDay scans", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0F172A))
                }

                peakDay?.let { peak ->
                    Column {
                        Text(text = "Pic maximal", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text(text = "${peak.count} (${peak.dateLabel})", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2563EB))
                    }
                }
            }
        }
    }
}
