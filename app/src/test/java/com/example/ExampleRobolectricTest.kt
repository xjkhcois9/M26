package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BadgesCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("متتبع العادات", appName)
  }

  @Test
  fun `test badges calculation`() {
    val badges = BadgesCatalog.calculateBadges(
      totalCompletions = 10,
      maxStreak = 5,
      activeHabitsCount = 3,
      habitLogsCount = 10
    )
    val firstStepBadge = badges.find { it.id == "first_step" }
    val streak3Badge = badges.find { it.id == "streak_3" }
    val streak7Badge = badges.find { it.id == "streak_7" }

    assertTrue(firstStepBadge?.isUnlocked == true)
    assertTrue(streak3Badge?.isUnlocked == true)
    assertTrue(streak7Badge?.isUnlocked == false)
  }
}
