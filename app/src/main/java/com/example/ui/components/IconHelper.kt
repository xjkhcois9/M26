package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.ModeEdit
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

data class HabitIconItem(
    val name: String,
    val labelAr: String,
    val icon: ImageVector
)

object IconHelper {
    val availableIcons: List<HabitIconItem> = listOf(
        HabitIconItem("water_drop", "شرب ماء", Icons.Filled.WaterDrop),
        HabitIconItem("fitness", "رياضة ولياقة", Icons.Filled.FitnessCenter),
        HabitIconItem("book", "قراءة وتعلم", Icons.Filled.Book),
        HabitIconItem("self_improvement", "تأمل وسكينة", Icons.Filled.SelfImprovement),
        HabitIconItem("directions_walk", "مشي وخطوات", Icons.Filled.DirectionsWalk),
        HabitIconItem("directions_run", "جري وركض", Icons.Filled.DirectionsRun),
        HabitIconItem("nights_stay", "نوم مبكر", Icons.Filled.NightsStay),
        HabitIconItem("wb_sunny", "استيقاظ مبكر", Icons.Filled.WbSunny),
        HabitIconItem("favorite", "صحة وقلب", Icons.Filled.Favorite),
        HabitIconItem("psychology", "تركيز وإنتاجية", Icons.Filled.Psychology),
        HabitIconItem("work", "عمل ودراسة", Icons.Filled.Work),
        HabitIconItem("lightbulb", "إبداع وتفكير", Icons.Filled.Lightbulb),
        HabitIconItem("star", "عادة مميزة", Icons.Filled.Star),
        HabitIconItem("timer", "إدارة الوقت", Icons.Filled.Timer),
        HabitIconItem("check_circle", "إنجاز عام", Icons.Filled.CheckCircle),
        HabitIconItem("local_drink", "مشروبات صحية", Icons.Filled.LocalDrink)
    )

    fun getIcon(name: String): ImageVector {
        return availableIcons.find { it.name == name }?.icon ?: Icons.Filled.CheckCircle
    }
}
