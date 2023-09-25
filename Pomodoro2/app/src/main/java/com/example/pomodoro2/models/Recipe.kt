package com.example.pomodoro2.models
import java.io.Serializable

data class Recipe(
    val calories: Int,
    val carbs: Int,
    val duration: Int,
    val fat: Int,
    val image_url: String,
    val ingredients: List<String>,
    val name: String,
    val protein: Int,
    val url: String
) : Serializable