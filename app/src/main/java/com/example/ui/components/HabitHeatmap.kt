package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitLogEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class HeatmapDay(
    val dateStr: String,
    val isCompleted: Boolean,
    val valueRatio: Float // 0.0 to 1.0
)

data class HeatmapWeek(
    val weekNumber: Int,
    val days: List<HeatmapDay>
)

@Composable
fun HabitHeatmap(
    logs: List<HabitLogEntity>,
    habitTargetValue: Int,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    numberOfDays: Int = 84, // 12 weeks
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val weeks = remember(logs, habitTargetValue) {
        val completedLogsMap = logs.associateBy { it.date }
        val cal = Calendar.getInstance()
        val daysList = mutableListOf<HeatmapDay>()

        for (i in (numberOfDays - 1) downTo 0) {
            val c = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dStr = dateFormat.format(c.time)
            val log = completedLogsMap[dStr]
            val isDone = log?.isCompleted == true
            val ratio = if (isDone) 1f else if (log != null && habitTargetValue > 0) {
                (log.currentValue.toFloat() / habitTargetValue.toFloat()).coerceIn(0f, 1f)
            } else 0f

            daysList.add(
                HeatmapDay(
                    dateStr = dStr,
                    isCompleted = isDone,
                    valueRatio = ratio
                )
            )
        }

        // Chunk into 7-day columns (weeks)
        daysList.chunked(7).mapIndexed { idx, weekDays ->
            HeatmapWeek(weekNumber = idx, days = weekDays)
        }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "خريطة الالتزام (آخر 12 أسبوعاً)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("أقل", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(accentColor.copy(alpha = 0.4f))
                    )
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(accentColor)
                    )
                    Text("أكثر", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(weeks) { week ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        week.days.forEach { day ->
                            val cellColor = when {
                                day.isCompleted -> accentColor
                                day.valueRatio > 0.5f -> accentColor.copy(alpha = 0.65f)
                                day.valueRatio > 0.0f -> accentColor.copy(alpha = 0.35f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(cellColor)
                            )
                        }
                    }
                }
            }
        }
    }
}
