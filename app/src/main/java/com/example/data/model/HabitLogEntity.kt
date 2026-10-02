package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habit_logs",
    indices = [Index(value = ["habitId", "date"], unique = true)]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String, // format: "YYYY-MM-DD"
    val currentValue: Int = 0,
    val isCompleted: Boolean = false,
    val note: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
