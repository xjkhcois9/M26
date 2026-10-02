package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HabitType {
    YES_NO,
    TARGET_COUNT
}

enum class TimeOfDay {
    ANYTIME,
    MORNING,
    AFTERNOON,
    EVENING
}

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "عام",
    val colorHex: Long = 0xFF10B981,
    val iconName: String = "check_circle",
    val habitType: String = HabitType.YES_NO.name,
    val targetValue: Int = 1,
    val unit: String = "مرة",
    val timeOfDay: String = TimeOfDay.ANYTIME.name,
    val targetDays: String = "1,2,3,4,5,6,7", // 1 = Monday ... 7 = Sunday
    val reminderTime: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)

typealias Habit = HabitEntity

