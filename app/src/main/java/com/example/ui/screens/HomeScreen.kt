package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.HabitWithProgress
import com.example.data.model.TimeOfDay
import com.example.ui.components.CalendarStrip
import com.example.ui.components.CircularProgressRing
import com.example.ui.components.HabitCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HabitViewModel,
    onNavigateToAddHabit: () -> Unit,
    onNavigateToHabitDetail: (Long) -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habitsWithProgress by viewModel.habitsWithProgress.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedTimeOfDay by viewModel.selectedTimeOfDay.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showSearchBox by remember { mutableStateOf(false) }
    var noteDialogHabitId by remember { mutableStateOf<Long?>(null) }
    var noteText by remember { mutableStateOf("") }

    // Completion metrics
    val totalCount = habitsWithProgress.size
    val completedCount = habitsWithProgress.count { it.isCompletedToday }
    val progressRatio = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val progressPercentage = (progressRatio * 100).toInt()

    val categories = listOf("الكل", "الصحة والرشاقة", "التعلم والقراءة", "الروحانيات والسكينة", "الإنتاجية", "العناية الشخصية", "عام")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "عاداتي",
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "✨ اليومية",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSearchBox = !showSearchBox },
                        modifier = Modifier.testTag("action_search")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Habits"
                        )
                    }
                    IconButton(
                        onClick = onNavigateToStats,
                        modifier = Modifier.testTag("action_statistics")
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Statistics",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("action_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddHabit,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Habit") },
                text = { Text("عادة جديدة", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_habit")
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Search field
            if (showSearchBox) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("ابحث في عاداتك...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("search_input")
                    )
                }
            }

            // Calendar Strip
            item {
                CalendarStrip(
                    selectedDate = selectedDate,
                    onDateSelected = { viewModel.setSelectedDate(it) },
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            // Daily Progress Hero Banner Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        PrimaryIndigo.copy(alpha = 0.10f)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when {
                                        progressPercentage == 100 && totalCount > 0 -> "رائع جداً! أتممت كل أهدافك اليوم 🎉"
                                        progressPercentage >= 50 -> "استمر! أنت في منتصف الطريق نحو هدفك 💪"
                                        else -> "بداية موفقة! خطوة واحدة تصنع فارقاً 🌟"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$completedCount من $totalCount عادات مكتملة ($progressPercentage%)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            CircularProgressRing(
                                progress = progressRatio,
                                size = 68.dp,
                                strokeWidth = 7.dp,
                                progressColor = MaterialTheme.colorScheme.primary,
                                centerContent = {
                                    Text(
                                        text = "$progressPercentage%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // Filter Chips (Time of day & Categories)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Time of Day Filter
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedTimeOfDay == null,
                                onClick = { viewModel.setSelectedTimeOfDay(null) },
                                label = { Text("كل الأوقات") },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedTimeOfDay == TimeOfDay.MORNING,
                                onClick = {
                                    viewModel.setSelectedTimeOfDay(if (selectedTimeOfDay == TimeOfDay.MORNING) null else TimeOfDay.MORNING)
                                },
                                label = { Text("🌅 الصباح") },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedTimeOfDay == TimeOfDay.AFTERNOON,
                                onClick = {
                                    viewModel.setSelectedTimeOfDay(if (selectedTimeOfDay == TimeOfDay.AFTERNOON) null else TimeOfDay.AFTERNOON)
                                },
                                label = { Text("☀️ الظهيرة") },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedTimeOfDay == TimeOfDay.EVENING,
                                onClick = {
                                    viewModel.setSelectedTimeOfDay(if (selectedTimeOfDay == TimeOfDay.EVENING) null else TimeOfDay.EVENING)
                                },
                                label = { Text("🌙 المساء") },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Category Filter
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val isSelected = (selectedCategory == null && category == "الكل") || selectedCategory == category
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setSelectedCategory(if (category == "الكل") null else category)
                                },
                                label = { Text(category) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // Habits Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "قائمة العادات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${habitsWithProgress.size} عادات",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Habits List
            if (habitsWithProgress.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "🌱",
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "لا توجد عادات تطابق هذا الفلتر",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "اضغط على زر '+ عادة جديدة' لإضافة أول عادة في روتينك",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onNavigateToAddHabit,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("إضافة عادة الآن")
                            }
                        }
                    }
                }
            } else {
                items(habitsWithProgress, key = { it.habit.id }) { item ->
                    HabitCard(
                        habitWithProgress = item,
                        onToggleCheck = { viewModel.toggleHabit(item.habit) },
                        onIncrement = { viewModel.updateProgress(item.habit, 1) },
                        onDecrement = { viewModel.updateProgress(item.habit, -1) },
                        onClick = { onNavigateToHabitDetail(item.habit.id) },
                        onAddNoteClick = {
                            noteDialogHabitId = item.habit.id
                            noteText = item.todayLog?.note ?: ""
                        }
                    )
                }
            }
        }
    }

    // Add / Edit Note Dialog
    if (noteDialogHabitId != null) {
        val currentHabitId = noteDialogHabitId!!
        val habitItem = habitsWithProgress.find { it.habit.id == currentHabitId }
        AlertDialog(
            onDismissRequest = { noteDialogHabitId = null },
            title = {
                Text(
                    text = "ملاحظة: ${habitItem?.habit?.title ?: ""}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "أضف يومياتك أو شعورك بعد ممارسة هذه العادة اليوم:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = { Text("مثال: شعرت بنشاط كبير بعد التمرين اليوم...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("habit_note_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveHabitNote(currentHabitId, noteText)
                        noteDialogHabitId = null
                    },
                    modifier = Modifier.testTag("save_note_button")
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteDialogHabitId = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
