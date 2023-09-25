package com.example.pomodoro2.models
import java.io.Serializable

data class MealInfo(
    val calories: Int,
    val carbs: Int,
    val fat: Int,
    val mealimgurl: String,
    val mealname: String,
    val protein: Int,
    val recipes: List<Recipe>
) : Serializable

