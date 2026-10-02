package com.example.data.model

data class HabitWithProgress(
    val habit: HabitEntity,
    val todayLog: HabitLogEntity?,
    val isCompletedToday: Boolean,
    val currentValue: Int,
    val targetValue: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCompletions: Int,
    val completionRate30Days: Float
)
