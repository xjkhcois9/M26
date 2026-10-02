package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitType
import com.example.data.model.TimeOfDay
import com.example.ui.components.IconHelper
import com.example.ui.theme.HabitColors
import com.example.ui.theme.PrimaryGreen
import com.example.ui.viewmodel.HabitViewModel

data class HabitTemplate(
    val title: String,
    val description: String,
    val category: String,
    val colorHex: Long,
    val iconName: String,
    val type: HabitType,
    val targetValue: Int,
    val unit: String,
    val timeOfDay: TimeOfDay
)

val SuggestedTemplates = listOf(
    HabitTemplate("شرب الماء", "شرب 8 أكواب ماء يومياً لترطيب الجسم", "الصحة والرشاقة", 0xFF06B6D4, "water_drop", HabitType.TARGET_COUNT, 8, "كوب", TimeOfDay.ANYTIME),
    HabitTemplate("قراءة كتاب", "قراءة 20 صفحة أو 20 دقيقة يومياً", "التعلم والقراءة", 0xFF6366F1, "book", HabitType.TARGET_COUNT, 20, "صفحة", TimeOfDay.EVENING),
    HabitTemplate("رياضة وتمارين", "30 دقيقة نشاط بدني ولياقة", "الصحة والرشاقة", 0xFFF59E0B, "fitness", HabitType.TARGET_COUNT, 30, "دقيقة", TimeOfDay.MORNING),
    HabitTemplate("صلاة وتأمل وأذكار", "المحافظة على السكينة اليومية", "الروحانيات والسكينة", 0xFF10B981, "self_improvement", HabitType.YES_NO, 1, "مرة", TimeOfDay.MORNING),
    HabitTemplate("المشي 8000 خطوة", "تحقيق معدل حركة يومي ممتاز", "الصحة والرشاقة", 0xFFEC4899, "directions_walk", HabitType.TARGET_COUNT, 8000, "خطوة", TimeOfDay.AFTERNOON),
    HabitTemplate("النوم قبل 11 مساءً", "نوم صحي ومنتظم للاستيقاظ بنشاط", "العناية الشخصية", 0xFF8B5CF6, "nights_stay", HabitType.YES_NO, 1, "مرة", TimeOfDay.EVENING),
    HabitTemplate("برمجة وتطوير ذات", "ساعة لتطوير مهاراتي البرمجية", "الإنتاجية", 0xFF3B82F6, "work", HabitType.TARGET_COUNT, 60, "دقيقة", TimeOfDay.ANYTIME)
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditHabitScreen(
    habitId: Long?,
    viewModel: HabitViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditMode = habitId != null && habitId > 0

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("الصحة والرشاقة") }
    var colorHex by remember { mutableLongStateOf(HabitColors.first()) }
    var iconName by remember { mutableStateOf("check_circle") }
    var habitType by remember { mutableStateOf(HabitType.YES_NO) }
    var targetValueStr by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("مرة") }
    var timeOfDay by remember { mutableStateOf(TimeOfDay.ANYTIME) }
    var targetDays by remember { mutableStateOf("1,2,3,4,5,6,7") }
    var reminderTime by remember { mutableStateOf<String?>(null) }
    var isTitleError by remember { mutableStateOf(false) }

    // If edit mode, load existing data
    if (isEditMode) {
        val existingHabit by viewModel.getHabitFlow(habitId!!).collectAsState(initial = null)
        LaunchedEffect(existingHabit) {
            existingHabit?.let { h ->
                title = h.title
                description = h.description
                category = h.category
                colorHex = h.colorHex
                iconName = h.iconName
                habitType = if (h.habitType == HabitType.TARGET_COUNT.name) HabitType.TARGET_COUNT else HabitType.YES_NO
                targetValueStr = h.targetValue.toString()
                unit = h.unit
                timeOfDay = try { TimeOfDay.valueOf(h.timeOfDay) } catch (_: Exception) { TimeOfDay.ANYTIME }
                targetDays = h.targetDays
                reminderTime = h.reminderTime
            }
        }
    }

    val categories = listOf("الصحة والرشاقة", "التعلم والقراءة", "الروحانيات والسكينة", "الإنتاجية", "العناية الشخصية", "المالية", "عام")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "تعديل العادة" else "إضافة عادة جديدة",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Quick suggested templates (only on add mode)
            if (!isEditMode) {
                item {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "قوالب مقترحة لبدء سريع",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(SuggestedTemplates) { t ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(t.colorHex).copy(alpha = 0.12f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            title = t.title
                                            description = t.description
                                            category = t.category
                                            colorHex = t.colorHex
                                            iconName = t.iconName
                                            habitType = t.type
                                            targetValueStr = t.targetValue.toString()
                                            unit = t.unit
                                            timeOfDay = t.timeOfDay
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = IconHelper.getIcon(t.iconName),
                                            contentDescription = null,
                                            tint = Color(t.colorHex),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = t.title,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Habit Name & Description Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "معلومات العادة الأساسية",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                isTitleError = false
                            },
                            label = { Text("اسم العادة *") },
                            placeholder = { Text("مثال: شرب الماء، قراءة كتاب...") },
                            isError = isTitleError,
                            supportingText = {
                                if (isTitleError) Text("الرجاء كتابة اسم للعادة")
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_habit_title")
                        )

                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("الوصف أو الهدف (اختياري)") },
                            placeholder = { Text("مثال: لتصفية الذهن، صحة أفضل...") },
                            maxLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("input_habit_description")
                        )
                    }
                }
            }

            // Category Selection
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "التصنيف",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // Habit Type (Yes/No vs Target Count)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "نوع الهدف اليومي",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = habitType == HabitType.YES_NO,
                                onClick = {
                                    habitType = HabitType.YES_NO
                                    targetValueStr = "1"
                                    unit = "مرة"
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) {
                                Text("إنجاز فوري (نعم/لا)")
                            }

                            SegmentedButton(
                                selected = habitType == HabitType.TARGET_COUNT,
                                onClick = {
                                    habitType = HabitType.TARGET_COUNT
                                    if (targetValueStr == "1") targetValueStr = "8"
                                    if (unit == "مرة") unit = "كوب"
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) {
                                Text("هدف رقمي (عداد)")
                            }
                        }

                        if (habitType == HabitType.TARGET_COUNT) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = targetValueStr,
                                    onValueChange = { targetValueStr = it.filter { ch -> ch.isDigit() } },
                                    label = { Text("الهدف المطلوب") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("input_target_value")
                                )

                                OutlinedTextField(
                                    value = unit,
                                    onValueChange = { unit = it },
                                    label = { Text("الوحدة") },
                                    placeholder = { Text("كوب، دقيقة، صفحة...") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f).testTag("input_target_unit")
                                )
                            }
                        }
                    }
                }
            }

            // Time of Day
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "وقت الممارسة المفضل",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple(TimeOfDay.ANYTIME, "أي وقت", "🌟"),
                            Triple(TimeOfDay.MORNING, "الصباح", "🌅"),
                            Triple(TimeOfDay.AFTERNOON, "الظهيرة", "☀️"),
                            Triple(TimeOfDay.EVENING, "المساء", "🌙")
                        ).forEach { (tod, label, emoji) ->
                            val isSelected = timeOfDay == tod
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { timeOfDay = tod }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(emoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Color Picker
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "اختر لون العادة",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(HabitColors) { hex ->
                            val c = Color(hex)
                            val isSelected = colorHex == hex
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { colorHex = hex }
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Icon Picker
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "اختر أيقونة العادة",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconHelper.availableIcons.forEach { iconItem ->
                            val isSelected = iconName == iconItem.name
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) Color(colorHex).copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { iconName = iconItem.name }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .border(
                                            width = if (isSelected) 2.dp else 0.dp,
                                            color = if (isSelected) Color(colorHex) else Color.Transparent,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                ) {
                                    Icon(
                                        imageVector = iconItem.icon,
                                        contentDescription = iconItem.labelAr,
                                        tint = if (isSelected) Color(colorHex) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Save Action Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            isTitleError = true
                        } else {
                            val parsedTarget = targetValueStr.toIntOrNull() ?: 1
                            viewModel.saveHabit(
                                id = habitId ?: 0L,
                                title = title,
                                description = description,
                                category = category,
                                colorHex = colorHex,
                                iconName = iconName,
                                habitType = habitType,
                                targetValue = parsedTarget,
                                unit = unit,
                                timeOfDay = timeOfDay,
                                targetDays = targetDays,
                                reminderTime = reminderTime
                            )
                            onNavigateBack()
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(colorHex)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_save_habit")
                ) {
                    Text(
                        text = if (isEditMode) "حفظ التعديلات" else "إنشاء العادة",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
