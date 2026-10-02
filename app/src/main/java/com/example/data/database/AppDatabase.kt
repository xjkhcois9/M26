package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.HabitDao
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.HabitType
import com.example.data.model.TimeOfDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(
    entities = [HabitEntity::class, HabitLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_tracker_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.habitDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: HabitDao) {
                val now = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()

                // Initial popular habits
                val habit1 = HabitEntity(
                    id = 1,
                    title = "شرب 8 أكواب ماء",
                    description = "الحفاظ على ترطيب الجسم والصحة العامة طوال اليوم",
                    category = "الصحة والرشاقة",
                    colorHex = 0xFF06B6D4, // Cyan
                    iconName = "water_drop",
                    habitType = HabitType.TARGET_COUNT.name,
                    targetValue = 8,
                    unit = "كوب",
                    timeOfDay = TimeOfDay.ANYTIME.name,
                    createdAt = now - 14 * 86400000L
                )

                val habit2 = HabitEntity(
                    id = 2,
                    title = "قراءة كتاب مفيد (20 دقيقة)",
                    description = "تغذية العقل وتوسيع المدارك بالقراءة اليومية المنتظمة",
                    category = "التعلم والقراءة",
                    colorHex = 0xFF6366F1, // Indigo
                    iconName = "book",
                    habitType = HabitType.TARGET_COUNT.name,
                    targetValue = 20,
                    unit = "دقيقة",
                    timeOfDay = TimeOfDay.EVENING.name,
                    createdAt = now - 14 * 86400000L
                )

                val habit3 = HabitEntity(
                    id = 3,
                    title = "ممارسة الرياضة الصباحية",
                    description = "تمارين رياضية خفيفة أو إطالة لبدء اليوم بنشاط",
                    category = "الصحة والرشاقة",
                    colorHex = 0xFFF59E0B, // Amber
                    iconName = "fitness",
                    habitType = HabitType.YES_NO.name,
                    targetValue = 1,
                    unit = "مرة",
                    timeOfDay = TimeOfDay.MORNING.name,
                    createdAt = now - 14 * 86400000L
                )

                val habit4 = HabitEntity(
                    id = 4,
                    title = "أذكار وتأمل صباحي",
                    description = "صفاء ذهني وسكينة قلبية لبدء اليوم بطاقة إيجابية",
                    category = "الروحانيات والسكينة",
                    colorHex = 0xFF10B981, // Emerald Green
                    iconName = "self_improvement",
                    habitType = HabitType.YES_NO.name,
                    targetValue = 1,
                    unit = "مرة",
                    timeOfDay = TimeOfDay.MORNING.name,
                    createdAt = now - 14 * 86400000L
                )

                val habit5 = HabitEntity(
                    id = 5,
                    title = "المشي 6000 خطوة",
                    description = "نشاط حركي للحفاظ على اللياقة وصحة القلب",
                    category = "الصحة والرشاقة",
                    colorHex = 0xFFEC4899, // Rose Pink
                    iconName = "directions_walk",
                    habitType = HabitType.TARGET_COUNT.name,
                    targetValue = 6000,
                    unit = "خطوة",
                    timeOfDay = TimeOfDay.AFTERNOON.name,
                    createdAt = now - 14 * 86400000L
                )

                dao.insertHabit(habit1)
                dao.insertHabit(habit2)
                dao.insertHabit(habit3)
                dao.insertHabit(habit4)
                dao.insertHabit(habit5)

                // Insert some past days logs so streak & chart shows realistic momentum
                for (daysAgo in 6 downTo 1) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                    val dateStr = dateFormat.format(cal.time)

                    // Logs for water
                    dao.insertOrUpdateLog(
                        HabitLogEntity(
                            habitId = 1,
                            date = dateStr,
                            currentValue = 8,
                            isCompleted = true,
                            note = "أنجزت الهدف كاملاً 💧"
                        )
                    )

                    // Logs for reading
                    dao.insertOrUpdateLog(
                        HabitLogEntity(
                            habitId = 2,
                            date = dateStr,
                            currentValue = 20,
                            isCompleted = true,
                            note = "قراءة ممتازة ومفيدة"
                        )
                    )

                    // Logs for morning exercise (completed 4 out of 5 days)
                    if (daysAgo != 3) {
                        dao.insertOrUpdateLog(
                            HabitLogEntity(
                                habitId = 3,
                                date = dateStr,
                                currentValue = 1,
                                isCompleted = true
                            )
                        )
                    }

                    // Logs for meditation
                    dao.insertOrUpdateLog(
                        HabitLogEntity(
                            habitId = 4,
                            date = dateStr,
                            currentValue = 1,
                            isCompleted = true
                        )
                    )

                    // Logs for walk
                    dao.insertOrUpdateLog(
                        HabitLogEntity(
                            habitId = 5,
                            date = dateStr,
                            currentValue = if (daysAgo % 2 == 0) 6500 else 6000,
                            isCompleted = true
                        )
                    )
                }

                // Today partial logs
                val todayStr = dateFormat.format(calendar.time)
                dao.insertOrUpdateLog(
                    HabitLogEntity(
                        habitId = 1,
                        date = todayStr,
                        currentValue = 5,
                        isCompleted = false
                    )
                )
                dao.insertOrUpdateLog(
                    HabitLogEntity(
                        habitId = 4,
                        date = todayStr,
                        currentValue = 1,
                        isCompleted = true
                    )
                )
            }
        }
    }
}
