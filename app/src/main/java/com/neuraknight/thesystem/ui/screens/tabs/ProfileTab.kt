package com.neuraknight.thesystem.ui.screens.tabs

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.neuraknight.thesystem.data.models.DaySnapshot
import com.neuraknight.thesystem.ui.screens.SettingsActivity
import com.neuraknight.thesystem.ui.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

@Composable
fun ProfileTab(viewModel: MainViewModel) {
    val user = viewModel.appData.user
    val settings = viewModel.appData.settings
    val context = LocalContext.current

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.reloadData()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.profileImg.isNotEmpty() && File(user.profileImg).exists()) {
                            var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                            LaunchedEffect(user.profileImg) {
                                bitmap = withContext(Dispatchers.IO) {
                                    android.graphics.BitmapFactory.decodeFile(user.profileImg)
                                }
                            }
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap!!.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = user.name.uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = " LVL ${user.level} ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "[${user.rank}]",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatChip(icon = Icons.Default.LocalFireDepartment, label = "${user.streak}", subLabel = "Streak")
                        StatChip(icon = Icons.Default.ConfirmationNumber, label = "${user.passcards}", subLabel = "Passes")
                        StatChip(icon = if (user.gender == "female") Icons.Default.Female else Icons.Default.Male, label = if (user.gender == "female") "F" else "M", subLabel = "Gender")
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            }

            item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "EXPERIENCE",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${user.totalXp.roundToInt()} XP",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (user.xpProgress / user.xpNeeded).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${user.xpProgress.roundToInt()} / ${user.xpNeeded.roundToInt()} to next level",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Text(
                text = "ATTRIBUTES",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ability Points",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = " ${user.stats.AP} AP ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    StatUpgradeRow("STR", user.stats.STR.roundToInt(), user.stats.AP > 0) { viewModel.upgradeStat("STR") }
                    StatUpgradeRow("AGI", user.stats.AGI.roundToInt(), user.stats.AP > 0) { viewModel.upgradeStat("AGI") }
                    StatUpgradeRow("VIT", user.stats.VIT.roundToInt(), user.stats.AP > 0) { viewModel.upgradeStat("VIT") }
                    StatUpgradeRow("END", user.stats.END.roundToInt(), user.stats.AP > 0) { viewModel.upgradeStat("END") }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Button(
                onClick = {
                    val intent = Intent(context, SettingsActivity::class.java)
                    settingsLauncher.launch(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Settings", fontWeight = FontWeight.Bold)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            ProgressSection(history = viewModel.appData.history)
        }
    }
}

@Composable
fun StatChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, subLabel: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun StatUpgradeRow(name: String, value: Int, canUpgrade: Boolean, onUpgrade: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onUpgrade,
                enabled = canUpgrade,
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ProgressSection(history: List<DaySnapshot>) {
    if (history.isEmpty()) return

    var selectedRange by remember { mutableStateOf(14) }
    val ranges = listOf(7, 14, 30, 90)

    val filtered = remember(history, selectedRange) {
        history.takeLast(selectedRange)
    }

    Text(
        text = "PROGRESS",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ranges.forEach { range ->
            FilterChip(
                selected = selectedRange == range,
                onClick = { selectedRange = range },
                label = { Text("${range}d") }
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    LineChartCard(title = "Total XP", data = filtered.map { it.totalXp })
    LineChartCard(title = "Level", data = filtered.map { it.level.toDouble() })
    LineChartCard(title = "Streak", data = filtered.map { it.streak.toDouble() })
    ColumnChartCard(
        title = "Quest Completion",
        data = filtered.map { if (it.questCompleted) 1.0 else 0.0 }
    )
    MultiLineChartCard(
        title = "Attributes",
        seriesData = listOf(
            filtered.map { it.stats.STR },
            filtered.map { it.stats.AGI },
            filtered.map { it.stats.VIT },
            filtered.map { it.stats.END }
        ),
        seriesLabels = listOf("STR", "AGI", "VIT", "END"),
        seriesColors = listOf(
            Color(0xFFE53935),
            Color(0xFF1E88E5),
            Color(0xFF43A047),
            Color(0xFFFB8C00)
        )
    )
}

@Composable
fun LineChartCard(title: String, data: List<Double>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (data.isNotEmpty()) {
                CanvasLineChart(
                    data = data,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    lineColor = MaterialTheme.colorScheme.primary
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ColumnChartCard(title: String, data: List<Double>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (data.isNotEmpty()) {
                CanvasColumnChart(
                    data = data,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
fun MultiLineChartCard(
    title: String,
    seriesData: List<List<Double>>,
    seriesLabels: List<String>,
    seriesColors: List<Color>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                seriesLabels.forEachIndexed { i, label ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    seriesColors.getOrElse(i) { MaterialTheme.colorScheme.primary },
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            val hasData = seriesData.isNotEmpty() && seriesData.all { it.isNotEmpty() }
            if (hasData) {
                CanvasMultiLineChart(
                    seriesData = seriesData,
                    seriesColors = seriesColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
fun CanvasLineChart(
    data: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.isEmpty()) return

    val minVal = data.min()
    val maxVal = data.max()
    val range = (maxVal - minVal).coerceAtLeast(1.0)
    val paint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 28f
            isAntiAlias = true
        }
    }
    val gridColor = Color.White.copy(alpha = 0.1f)

    Canvas(modifier = modifier.padding(start = 4.dp, end = 4.dp, bottom = 20.dp)) {
        val w = size.width
        val h = size.height
        val stepX = w / (data.size - 1).coerceAtLeast(1)

        // Grid lines
        for (i in 0..4) {
            val y = h * i / 4
            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 0.5.dp.toPx())
        }

        // Line path
        val path = Path()
        data.forEachIndexed { i, value ->
            val x = i * stepX
            val y = h - ((value - minVal) / range * h).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(path, color = lineColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        // Area fill
        val fillPath = Path().apply {
            addPath(path)
            lineTo((data.size - 1) * stepX, h)
            lineTo(0f, h)
            close()
        }
        drawPath(fillPath, color = lineColor.copy(alpha = 0.1f))

        // Data points
        data.forEachIndexed { i, value ->
            val x = i * stepX
            val y = h - ((value - minVal) / range * h).toFloat()
            drawCircle(color = lineColor, radius = 2.5.dp.toPx(), center = Offset(x, y))
        }

        // Min/Max labels
        drawContext.canvas.nativeCanvas.drawText(
            "${maxVal.toInt()}",
            0f,
            24f,
            paint
        )
        drawContext.canvas.nativeCanvas.drawText(
            "${minVal.toInt()}",
            0f,
            h + 40f,
            paint
        )
    }
}

@Composable
fun CanvasColumnChart(
    data: List<Double>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val gridColor = Color.White.copy(alpha = 0.08f)

    Canvas(modifier = modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp)) {
        val w = size.width
        val h = size.height
        val n = data.size
        val barW = (w / n) * 0.6f
        val gap = (w / n) * 0.4f

        data.forEachIndexed { i, value ->
            val x = i * (barW + gap) + gap / 2
            val barH = value.toFloat() * h
            val color = if (value > 0.5) Color(0xFF43A047) else Color(0xFFE53935)
            drawRect(
                color = color,
                topLeft = Offset(x, h - barH),
                size = androidx.compose.ui.geometry.Size(barW, barH)
            )
        }
    }
}

@Composable
fun CanvasMultiLineChart(
    seriesData: List<List<Double>>,
    seriesColors: List<Color>,
    modifier: Modifier = Modifier
) {
    if (seriesData.isEmpty() || seriesData.first().isEmpty()) return

    var globalMin = Double.MAX_VALUE
    var globalMax = Double.MIN_VALUE
    seriesData.forEach { series ->
        series.forEach { v ->
            if (v < globalMin) globalMin = v
            if (v > globalMax) globalMax = v
        }
    }
    val range = (globalMax - globalMin).coerceAtLeast(1.0)
    val gridColor = Color.White.copy(alpha = 0.08f)
    val defaultColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp)) {
        val w = size.width
        val h = size.height
        val n = seriesData.first().size
        val stepX = w / (n - 1).coerceAtLeast(1)

        // Grid
        for (i in 0..4) {
            val y = h * i / 4
            drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 0.5.dp.toPx())
        }

        seriesData.forEachIndexed { si, series ->
            val color = seriesColors.getOrElse(si) { defaultColor }
            val path = Path()
            series.forEachIndexed { i, value ->
                val x = i * stepX
                val y = h - ((value - globalMin) / range * h).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}