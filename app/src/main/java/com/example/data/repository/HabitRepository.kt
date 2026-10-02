package com.example.data.repository

import com.example.data.dao.HabitDao
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.HabitType
import com.example.data.model.HabitWithProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HabitRepository(private val habitDao: HabitDao) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val allActiveHabits: Flow<List<HabitEntity>> = habitDao.getAllActiveHabits()
    val allLogs: Flow<List<HabitLogEntity>> = habitDao.getAllLogs()

    fun getHabitById(id: Long): Flow<HabitEntity?> = habitDao.getHabitById(id)

    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>> = habitDao.getLogsForHabit(habitId)

    fun getHabitsWithProgressForDate(dateStr: String): Flow<List<HabitWithProgress>> {
        return combine(habitDao.getAllActiveHabits(), habitDao.getAllLogs()) { habits, logs ->
            val logsByHabit = logs.groupBy { it.habitId }
            habits.map { habit ->
                val habitLogs = logsByHabit[habit.id] ?: emptyList()
                val targetLog = habitLogs.find { it.date == dateStr }
                val isCompleted = targetLog?.isCompleted == true
                val currentVal = targetLog?.currentValue ?: 0

                val (currStreak, bestStreak) = calculateStreaks(habitLogs, dateStr)
                val totalCompletions = habitLogs.count { it.isCompleted }
                val completionRate30 = calculate30DayRate(habitLogs)

                HabitWithProgress(
                    habit = habit,
                    todayLog = targetLog,
                    isCompletedToday = isCompleted,
                    currentValue = currentVal,
                    targetValue = habit.targetValue,
                    currentStreak = currStreak,
                    bestStreak = bestStreak,
                    totalCompletions = totalCompletions,
                    completionRate30Days = completionRate30
                )
            }
        }
    }

    suspend fun insertHabit(habit: HabitEntity): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: HabitEntity) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: HabitEntity) {
        habitDao.deleteLogsForHabit(habit.id)
        habitDao.deleteHabit(habit)
    }

    suspend fun toggleHabitCompletion(habit: HabitEntity, dateStr: String, note: String? = null) {
        val existingLog = habitDao.getLogForHabitAndDate(habit.id, dateStr)
        if (existingLog != null) {
            val newCompleted = !existingLog.isCompleted
            val newVal = if (newCompleted) habit.targetValue else 0
            val updated = existingLog.copy(
                isCompleted = newCompleted,
                currentValue = newVal,
                note = if (newCompleted) (note ?: existingLog.note) else existingLog.note,
                updatedAt = System.currentTimeMillis()
            )
            habitDao.insertOrUpdateLog(updated)
        } else {
            val newLog = HabitLogEntity(
                habitId = habit.id,
                date = dateStr,
                currentValue = habit.targetValue,
                isCompleted = true,
                note = note,
                updatedAt = System.currentTimeMillis()
            )
            habitDao.insertOrUpdateLog(newLog)
        }
    }

    suspend fun updateHabitProgressValue(habit: HabitEntity, dateStr: String, delta: Int) {
        val existingLog = habitDao.getLogForHabitAndDate(habit.id, dateStr)
        val currentVal = existingLog?.currentValue ?: 0
        val nextVal = (currentVal + delta).coerceAtLeast(0)
        val isDone = nextVal >= habit.targetValue

        val logToSave = existingLog?.copy(
            currentValue = nextVal,
            isCompleted = isDone,
            updatedAt = System.currentTimeMillis()
        ) ?: HabitLogEntity(
            habitId = habit.id,
            date = dateStr,
            currentValue = nextVal,
            isCompleted = isDone,
            updatedAt = System.currentTimeMillis()
        )

        habitDao.insertOrUpdateLog(logToSave)
    }

    suspend fun setHabitLogNote(habitId: Long, dateStr: String, note: String) {
        val existing = habitDao.getLogForHabitAndDate(habitId, dateStr)
        if (existing != null) {
            habitDao.insertOrUpdateLog(existing.copy(note = note, updatedAt = System.currentTimeMillis()))
        } else {
            habitDao.insertOrUpdateLog(
                HabitLogEntity(
                    habitId = habitId,
                    date = dateStr,
                    currentValue = 0,
                    isCompleted = false,
                    note = note
                )
            )
        }
    }

    private fun calculateStreaks(logs: List<HabitLogEntity>, referenceDateStr: String): Pair<Int, Int> {
        if (logs.isEmpty()) return Pair(0, 0)

        val completedDates = logs
            .filter { it.isCompleted }
            .map { it.date }
            .toSet()

        if (completedDates.isEmpty()) return Pair(0, 0)

        val cal = Calendar.getInstance()
        try {
            cal.time = dateFormat.parse(referenceDateStr) ?: Date()
        } catch (e: Exception) {
            cal.time = Date()
        }

        // Current streak from reference date backwards
        var currentStreak = 0
        val refDateKey = dateFormat.format(cal.time)

        // If reference day is completed, count it, else start from yesterday
        if (completedDates.contains(refDateKey)) {
            currentStreak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            // Check yesterday
            val yesterdayCal = cal.clone() as Calendar
            yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayKey = dateFormat.format(yesterdayCal.time)
            if (completedDates.contains(yesterdayKey)) {
                currentStreak++
                cal.add(Calendar.DAY_OF_YEAR, -2)
            } else {
                currentStreak = 0
            }
        }

        if (currentStreak > 0) {
            while (true) {
                val checkKey = dateFormat.format(cal.time)
                if (completedDates.contains(checkKey)) {
                    currentStreak++
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
        }

        // Calculate max streak across all time
        val sortedDates = completedDates.sorted()
        var maxStreak = 0
        var tempStreak = 0
        var prevCal: Calendar? = null

        for (dStr in sortedDates) {
            try {
                val d = dateFormat.parse(dStr) ?: continue
                val curr = Calendar.getInstance().apply { time = d }

                if (prevCal == null) {
                    tempStreak = 1
                } else {
                    val prevPlusOne = (prevCal.clone() as Calendar).apply {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                    if (dateFormat.format(prevPlusOne.time) == dateFormat.format(curr.time)) {
                        tempStreak++
                    } else if (dateFormat.format(prevCal.time) != dateFormat.format(curr.time)) {
                        tempStreak = 1
                    }
                }
                prevCal = curr
                if (tempStreak > maxStreak) {
                    maxStreak = tempStreak
                }
            } catch (_: Exception) {}
        }

        return Pair(currentStreak, maxStreak.coerceAtLeast(currentStreak))
    }

    private fun calculate30DayRate(logs: List<HabitLogEntity>): Float {
        val cal = Calendar.getInstance()
        val completedSet = logs.filter { it.isCompleted }.map { it.date }.toSet()
        var completedIn30Days = 0

        for (i in 0 until 30) {
            val dKey = dateFormat.format(cal.time)
            if (completedSet.contains(dKey)) {
                completedIn30Days++
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        return (completedIn30Days / 30f) * 100f
    }
}
