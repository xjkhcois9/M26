package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.AchievementBadge
import com.example.data.model.BadgesCatalog
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.HabitType
import com.example.data.model.HabitWithProgress
import com.example.data.model.TimeOfDay
import com.example.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HabitRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedTimeOfDay = MutableStateFlow<TimeOfDay?>(null)
    val selectedTimeOfDay: StateFlow<TimeOfDay?> = _selectedTimeOfDay.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Shared flow for triggering confetti/celebration animations
    private val _celebrationEvent = MutableSharedFlow<String>()
    val celebrationEvent: SharedFlow<String> = _celebrationEvent.asSharedFlow()

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = HabitRepository(db.habitDao())
    }

    // Dynamic list of habits with progress for selected date
    val habitsWithProgress: StateFlow<List<HabitWithProgress>> = _selectedDate
        .flatMapLatest { dateStr ->
            repository.getHabitsWithProgressForDate(dateStr)
        }
        .combine(_selectedCategory) { habits, category ->
            if (category == null || category == "الكل") habits
            else habits.filter { it.habit.category == category }
        }
        .combine(_selectedTimeOfDay) { habits, timeOfDay ->
            if (timeOfDay == null) habits
            else habits.filter {
                it.habit.timeOfDay == timeOfDay.name || it.habit.timeOfDay == TimeOfDay.ANYTIME.name
            }
        }
        .combine(_searchQuery) { habits, query ->
            if (query.isBlank()) habits
            else habits.filter {
                it.habit.title.contains(query, ignoreCase = true) ||
                it.habit.description.contains(query, ignoreCase = true) ||
                it.habit.category.contains(query, ignoreCase = true)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allLogs: StateFlow<List<HabitLogEntity>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allHabits: StateFlow<List<HabitEntity>> = repository.allActiveHabits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val badges: StateFlow<List<AchievementBadge>> = combine(allHabits, allLogs) { habits, logs ->
        val totalCompletions = logs.count { it.isCompleted }
        val maxStreak = habits.maxOfOrNull { habit ->
            // compute streak
            val hLogs = logs.filter { it.habitId == habit.id }
            val completedDates = hLogs.filter { it.isCompleted }.map { it.date }.toSet()
            completedDates.size
        } ?: 0
        BadgesCatalog.calculateBadges(
            totalCompletions = totalCompletions,
            maxStreak = maxStreak,
            activeHabitsCount = habits.size,
            habitLogsCount = logs.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSelectedDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setSelectedTimeOfDay(timeOfDay: TimeOfDay?) {
        _selectedTimeOfDay.value = timeOfDay
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleHabit(habit: HabitEntity, note: String? = null) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habit, _selectedDate.value, note)
            vibrateDevice(50)
            _celebrationEvent.emit("🎉 أحسنت! عادة مكتملة بنجاح")
        }
    }

    fun updateProgress(habit: HabitEntity, delta: Int) {
        viewModelScope.launch {
            repository.updateHabitProgressValue(habit, _selectedDate.value, delta)
            vibrateDevice(30)
        }
    }

    fun saveHabitNote(habitId: Long, note: String) {
        viewModelScope.launch {
            repository.setHabitLogNote(habitId, _selectedDate.value, note)
        }
    }

    fun saveHabit(
        id: Long = 0,
        title: String,
        description: String,
        category: String,
        colorHex: Long,
        iconName: String,
        habitType: HabitType,
        targetValue: Int,
        unit: String,
        timeOfDay: TimeOfDay,
        targetDays: String,
        reminderTime: String?
    ) {
        viewModelScope.launch {
            val entity = HabitEntity(
                id = id,
                title = title.trim(),
                description = description.trim(),
                category = category,
                colorHex = colorHex,
                iconName = iconName,
                habitType = habitType.name,
                targetValue = targetValue.coerceAtLeast(1),
                unit = unit.trim().ifEmpty { "مرة" },
                timeOfDay = timeOfDay.name,
                targetDays = targetDays,
                reminderTime = reminderTime,
                createdAt = if (id == 0L) System.currentTimeMillis() else System.currentTimeMillis()
            )
            if (id == 0L) {
                repository.insertHabit(entity)
            } else {
                repository.updateHabit(entity)
            }
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun getHabitFlow(id: Long) = repository.getHabitById(id)
    fun getHabitLogsFlow(habitId: Long) = repository.getLogsForHabit(habitId)

    private fun vibrateDevice(durationMs: Long) {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }
}
