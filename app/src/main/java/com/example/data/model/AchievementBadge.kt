package com.example.data.model

data class AchievementBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val requiredCount: Int,
    val currentProgress: Int,
    val isUnlocked: Boolean,
    val unlockedDate: String? = null
)

object BadgesCatalog {
    fun calculateBadges(
        totalCompletions: Int,
        maxStreak: Int,
        activeHabitsCount: Int,
        habitLogsCount: Int
    ): List<AchievementBadge> {
        return listOf(
            AchievementBadge(
                id = "first_step",
                title = "الخطوة الأولى",
                description = "سجل أول إنجاز لعادة بنجاح",
                iconEmoji = "🌱",
                requiredCount = 1,
                currentProgress = totalCompletions.coerceAtMost(1),
                isUnlocked = totalCompletions >= 1
            ),
            AchievementBadge(
                id = "streak_3",
                title = "شعلة الحماس",
                description = "حافظ على سلسلة إنجاز لمدة 3 أيام متتالية",
                iconEmoji = "🔥",
                requiredCount = 3,
                currentProgress = maxStreak.coerceAtMost(3),
                isUnlocked = maxStreak >= 3
            ),
            AchievementBadge(
                id = "streak_7",
                title = "أسبوع الإصرار",
                description = "استمر في عاداتك لمدة 7 أيام متتالية دون انقطاع",
                iconEmoji = "⚡",
                requiredCount = 7,
                currentProgress = maxStreak.coerceAtMost(7),
                isUnlocked = maxStreak >= 7
            ),
            AchievementBadge(
                id = "streak_21",
                title = "بناء العادة (21 يوماً)",
                description = "رسّخ العادات بعمق عبر 21 يوماً من الاستمرارية",
                iconEmoji = "🏆",
                requiredCount = 21,
                currentProgress = maxStreak.coerceAtMost(21),
                isUnlocked = maxStreak >= 21
            ),
            AchievementBadge(
                id = "completions_25",
                title = "المثابر النشيط",
                description = "أكمل 25 جلسة عادة بالمجمل",
                iconEmoji = "🎯",
                requiredCount = 25,
                currentProgress = totalCompletions.coerceAtMost(25),
                isUnlocked = totalCompletions >= 25
            ),
            AchievementBadge(
                id = "completions_100",
                title = "نادي المئة 💎",
                description = "حقّق 100 إنجاز في سجل عاداتك الكلي",
                iconEmoji = "👑",
                requiredCount = 100,
                currentProgress = totalCompletions.coerceAtMost(100),
                isUnlocked = totalCompletions >= 100
            ),
            AchievementBadge(
                id = "habits_created_5",
                title = "مهندس الروتين",
                description = "أنشئ 5 عادات إيجابية منتظمة في حياتك",
                iconEmoji = "🌟",
                requiredCount = 5,
                currentProgress = activeHabitsCount.coerceAtMost(5),
                isUnlocked = activeHabitsCount >= 5
            )
        )
    }
}
