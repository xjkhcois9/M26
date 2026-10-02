package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarDayItem(
    val dateStr: String,
    val dayNumber: String,
    val dayNameAr: String,
    val monthNameAr: String,
    val isToday: Boolean,
    val isSelected: Boolean
)

@Composable
fun CalendarStrip(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val todayStr = remember { dateFormat.format(Date()) }

    // Generate 21 days (past 14 days, today, next 6 days)
    val days = remember(selectedDate) {
        val list = mutableListOf<CalendarDayItem>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -14)

        val dayNames = arrayOf("الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")
        val monthNames = arrayOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو", "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر")

        for (i in 0..20) {
            val dStr = dateFormat.format(cal.time)
            val dNum = cal.get(Calendar.DAY_OF_MONTH).toString()
            val dayOfWeekIndex = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sunday
            val dName = dayNames[dayOfWeekIndex]
            val mName = monthNames[cal.get(Calendar.MONTH)]

            list.add(
                CalendarDayItem(
                    dateStr = dStr,
                    dayNumber = dNum,
                    dayNameAr = dName,
                    monthNameAr = mName,
                    isToday = dStr == todayStr,
                    isSelected = dStr == selectedDate
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val listState = rememberLazyListState()

    // Scroll to selected item or today on first layout
    LaunchedEffect(selectedDate) {
        val index = days.indexOfFirst { it.dateStr == selectedDate }
        if (index != -1) {
            listState.animateScrollToItem((index - 2).coerceAtLeast(0))
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        // Top header with month name and "Today" button
        val selectedItem = days.find { it.isSelected } ?: days.find { it.isToday }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${selectedItem?.dayNameAr ?: ""}، ${selectedItem?.dayNumber ?: ""} ${selectedItem?.monthNameAr ?: ""}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (selectedDate != todayStr) {
                AssistChip(
                    onClick = { onDateSelected(todayStr) },
                    label = { Text("الرجوع لليوم", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Today",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("today_quick_chip")
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(days, key = { it.dateStr }) { day ->
                val bgCol by animateColorAsState(
                    targetValue = when {
                        day.isSelected -> MaterialTheme.colorScheme.primary
                        day.isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    },
                    label = "day_bg"
                )

                val textColor by animateColorAsState(
                    targetValue = when {
                        day.isSelected -> MaterialTheme.colorScheme.onPrimary
                        day.isToday -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    label = "day_text"
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = bgCol,
                    modifier = Modifier
                        .width(54.dp)
                        .height(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onDateSelected(day.dateStr) }
                        .testTag("calendar_day_${day.dateStr}")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Text(
                            text = day.dayNameAr.take(3),
                            fontSize = 11.sp,
                            fontWeight = if (day.isSelected || day.isToday) FontWeight.Bold else FontWeight.Normal,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day.dayNumber,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (day.isToday && !day.isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        } else {
                            Spacer(modifier = Modifier.size(5.dp))
                        }
                    }
                }
            }
        }
    }
}
