package com.example.workoutlogger.ux.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workoutlogger.viewmodel.WorkoutViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.chart.line.LineChart
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.util.Locale
import kotlin.math.roundToInt

// DESIGN SYSTEM COLORS
val AppBg = Color(0xFF0D1526)
val SurfaceCard = Color(0xFF1E2A4A)
val AccentBlue = Color(0xFF3B82F6)
val AccentGold = Color(0xFFF59E0B)
val AccentGreen = Color(0xFF10B981)
val AccentPurple = Color(0xFF8B5CF6)
val TextPrimary = Color.White
val TextMuted = Color(0xFF64748B)

data class WorkoutSession(
    val date: String,
    val weight: Float,
    val volume: Int,
    val reps: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: WorkoutViewModel,
    weightUnit: String
) {
    val exercises by viewModel.exercises.collectAsState(initial = emptyList())
    val exerciseNames = remember(exercises) { exercises.map { it.name }.distinct().sorted() }

    var selectedExercise by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf("All") }
    var selectedMetric by remember { mutableStateOf("Max Weight") }
    var screenAlpha by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(exerciseNames) {
        if (exerciseNames.isNotEmpty() && selectedExercise.isEmpty()) {
            selectedExercise = exerciseNames.first()
        }
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = screenAlpha,
        animationSpec = tween(durationMillis = 800),
        label = "fade_in"
    )

    LaunchedEffect(Unit) {
        screenAlpha = 1f
    }

    // Filtered Real Data
    val filteredData = remember(exercises, selectedExercise, selectedRange) {
        val filtered = exercises.filter { it.name == selectedExercise }.sortedBy { it.date }
        
        // Implement range filtering
        val now = System.currentTimeMillis()
        val rangeMillis = when (selectedRange) {
            "1W" -> 7 * 24 * 60 * 60 * 1000L
            "1M" -> 30 * 24 * 60 * 60 * 1000L
            "3M" -> 90 * 24 * 60 * 60 * 1000L
            else -> Long.MAX_VALUE
        }
        
        val finalFiltered = if (selectedRange == "All") filtered else {
            filtered.filter { now - it.date <= rangeMillis }
        }

        finalFiltered.map { 
            val displayWeight = if (weightUnit == "lbs") (it.weight * 2.20462f) else it.weight
            WorkoutSession(
                date = java.text.SimpleDateFormat("dd MMM", Locale.getDefault()).format(java.util.Date(it.date)),
                weight = displayWeight,
                volume = (it.sets * it.reps * it.weight).toInt(), // volume remains in kg for logic
                reps = it.reps
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg)
            .alpha(animatedAlpha)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. HEADER ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .background(AccentBlue.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            .border(1.dp, AccentBlue.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (selectedExercise.isNotEmpty()) "$selectedExercise 🏋" else "Select Exercise",
                            fontSize = 12.sp,
                            color = AccentBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2. EXERCISE SELECTOR (Functional)
            item {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2540)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f))
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedExercise.ifEmpty { "Select Exercise" },
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TextMuted
                            )
                        }
                    }

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(SurfaceCard)
                    ) {
                        exerciseNames.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name, color = TextPrimary) },
                                onClick = {
                                    selectedExercise = name
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 3. METRIC TOGGLE
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(SurfaceCard, RoundedCornerShape(14.dp))
                        .padding(4.dp)
                ) {
                    MetricButton(
                        label = "Max Weight",
                        isSelected = selectedMetric == "Max Weight",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedMetric = "Max Weight" }
                    )
                    MetricButton(
                        label = "Volume",
                        isSelected = selectedMetric == "Volume",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedMetric = "Volume" }
                    )
                }
            }

            // 4. RANGE CHIPS ROW
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("1W", "1M", "3M", "All").forEach { range ->
                        RangeChip(
                            label = range,
                            isSelected = selectedRange == range,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedRange = range }
                        )
                    }
                }
            }

            // 5. CHART SECTION
            item {
                if (filteredData.isNotEmpty()) {
                    ChartCard(filteredData, selectedMetric)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(SurfaceCard, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No data for this exercise", color = TextMuted)
                    }
                }
            }

            // 6. PERSONAL RECORDS
            item {
                Text(
                    text = "PERSONAL RECORDS",
                    fontSize = 10.sp,
                    color = TextMuted,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                val maxWeight = filteredData.maxOfOrNull { it.weight } ?: 0f
                PersonalRecordCard(
                    title = "Max Weight",
                    value = "${if (maxWeight % 1.0f == 0f) maxWeight.toInt().toString() else String.format(java.util.Locale.getDefault(), "%.1f", maxWeight)} $weightUnit",
                    date = filteredData.find { it.weight == maxWeight }?.date ?: "N/A",
                    icon = Icons.Default.EmojiEvents,
                    iconTint = AccentGold,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                val maxVolume = filteredData.maxOfOrNull { it.volume } ?: 0
                val displayMaxVolume = if (weightUnit == "lbs") (maxVolume * 2.20462f) else maxVolume.toFloat()
                val volumeValue = if (displayMaxVolume % 1.0f == 0f) displayMaxVolume.toInt().toString() else String.format(java.util.Locale.getDefault(), "%.1f", displayMaxVolume)
                PersonalRecordCard(
                    title = "Max Volume",
                    value = "$volumeValue $weightUnit",
                    date = filteredData.find { it.volume == maxVolume }?.date ?: "N/A",
                    icon = Icons.Default.EmojiEvents,
                    iconTint = AccentBlue,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 7. INSIGHTS SECTION
            item {
                Text(
                    text = "INSIGHTS",
                    fontSize = 10.sp,
                    color = TextMuted,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                if (filteredData.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val lastSession = filteredData.last()
                        val est1RM = (lastSession.weight * (1 + lastSession.reps / 30f)).roundToInt()
                        
                        // Delta calculation from second to last session
                        val prevSession = filteredData.getOrNull(filteredData.size - 2)
                        val weightDelta = if (prevSession != null) lastSession.weight - prevSession.weight else 0f
                        
                        val lastVolume = if (weightUnit == "lbs") (lastSession.volume * 2.20462f) else lastSession.volume.toFloat()
                        val prevVolume = prevSession?.let { if (weightUnit == "lbs") (it.volume * 2.20462f) else it.volume.toFloat() }
                        
                        val volumeDeltaPercent = if (prevVolume != null && prevVolume != 0f) {
                            ((lastVolume - prevVolume) / prevVolume * 100)
                        } else 0f

                        val lastVolumeValue = if (lastVolume % 1.0f == 0f) lastVolume.toInt().toString() else String.format(java.util.Locale.getDefault(), "%.1f", lastVolume)

                        InsightMiniCard(
                            label = "EST. 1RM",
                            value = "$est1RM $weightUnit",
                            delta = "${if (weightDelta >= 0) "+" else ""}${String.format(java.util.Locale.getDefault(), "%.1f", weightDelta)} $weightUnit",
                            isPositive = weightDelta >= 0,
                            borderColor = AccentGold.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        )
                        InsightMiniCard(
                            label = "VOLUME Δ",
                            value = "$lastVolumeValue $weightUnit",
                            delta = "${if (volumeDeltaPercent >= 0) "+" else ""}${String.format(java.util.Locale.getDefault(), "%.1f", volumeDeltaPercent)}%",
                            isPositive = volumeDeltaPercent >= 0,
                            borderColor = AccentBlue.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            item {
                MomentumCard()
            }
            item {
                MilestoneCard()
            }
        }
    }
}

@Composable
fun MetricButton(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) AccentBlue else Color.Transparent,
        label = "metric_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else TextMuted,
        label = "metric_text"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun RangeChip(label: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) AccentBlue.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.07f),
        label = "chip_border"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF63B3ED) else TextMuted,
        label = "chip_text"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) AccentBlue.copy(alpha = 0.05f) else Color.Transparent,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ChartCard(data: List<WorkoutSession>, metric: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (data.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Need at least 2 sessions", color = TextMuted, fontSize = 14.sp)
                        Text("to plot a progress line", color = TextMuted, fontSize = 12.sp)
                    }
                }
            } else {
                val chartData = data.mapIndexed { index, session ->
                    val value = if (metric == "Max Weight") session.weight else session.volume.toFloat()
                    FloatEntry(index.toFloat(), value)
                }
                val entryModel = entryModelOf(chartData)

                Chart(
                    chart = lineChart(
                        lines = listOf(
                            LineChart.LineSpec(
                                lineColor = AccentBlue.toArgb(),
                                lineThicknessDp = 2.5f,
                                lineBackgroundShader = verticalGradient(
                                    arrayOf(AccentBlue.copy(0.3f), AccentBlue.copy(0f))
                                )
                            )
                        )
                    ),
                    model = entryModel,
                    startAxis = rememberStartAxis(
                        label = MaterialTheme.typography.labelSmall.copy(color = TextMuted).toAxisLabelComponent(),
                        guideline = null
                    ),
                    bottomAxis = rememberBottomAxis(
                        valueFormatter = object : AxisValueFormatter<AxisPosition.Horizontal.Bottom> {
                            override fun formatValue(value: Float, chartValues: ChartValues): CharSequence {
                                val index = value.toInt()
                                return if (index >= 0 && index < data.size) data[index].date else ""
                            }
                        },
                        label = MaterialTheme.typography.labelSmall.copy(color = TextMuted).toAxisLabelComponent(),
                        guideline = null
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }
    }
}

@Composable
fun PersonalRecordCard(title: String, value: String, date: String, icon: ImageVector, iconTint: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.07f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconTint.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextMuted, fontSize = 12.sp)
                Text(value, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            }
            Text(date, color = TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
fun InsightMiniCard(label: String, value: String, delta: String, isPositive: Boolean, borderColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier.animateContentSize(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(value, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(vertical = 4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isPositive) "↑" else "↓",
                    color = if (isPositive) AccentGreen else Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(delta, color = if (isPositive) AccentGreen else Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MomentumCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📈", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("On a growth streak!", color = AccentGreen, fontWeight = FontWeight.Bold)
                Text("You've hit a PR in 3 of your last 4 sessions.", color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MilestoneCard() {
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")
    
    LaunchedEffect(Unit) { progress = 0.875f }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AccentPurple.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, AccentPurple.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎯", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Next Milestone", color = AccentPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Text("87%", color = AccentPurple, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("12.5 kg to reach 100 kg", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = AccentPurple,
                trackColor = AccentPurple.copy(alpha = 0.1f)
            )
        }
    }
}

private fun Color.toArgb() = (this.value shr 32).toInt()

@Composable
private fun androidx.compose.ui.text.TextStyle.toAxisLabelComponent() =
    com.patrykandpatrick.vico.compose.component.textComponent(
        color = this.color,
        textSize = this.fontSize
    )
