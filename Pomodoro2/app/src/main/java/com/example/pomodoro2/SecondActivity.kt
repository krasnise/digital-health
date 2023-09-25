package com.example.pomodoro2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.pomodoro2.models.MealInfo
import com.example.pomodoro2.models.Recipe
import kotlin.math.ceil

class SecondActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.second_activity)

        val scale_item: Float = resources.displayMetrics.density
        val pixels_item: Int = (30 * scale_item + 0.5f).toInt()

        val mealInfo = intent.extras?.get("response") as MealInfo

        val transparentImage: ImageView = findViewById(R.id.transparentImage)
        val gridView: GridView = findViewById(R.id.recipeGridView)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val scrollView: ScrollView = findViewById(R.id.scrollView)


        val mealNameTextView: TextView = findViewById(R.id.mealName)
        val calorieInfoTextView: TextView = findViewById(R.id.calorieInfo)
        val carbsInfoTextView: TextView = findViewById(R.id.carbsInfo)
        val proteinInfoTextView: TextView = findViewById(R.id.proteinInfo)
        val fatInfoTextView: TextView = findViewById(R.id.fatInfo)

        mealNameTextView.text = mealInfo.mealname
        calorieInfoTextView.text = "${mealInfo.calories} kcal "
        carbsInfoTextView.text = "Carbs: ${mealInfo.carbs}g"
        proteinInfoTextView.text = "Protein: ${mealInfo.protein}g"
        fatInfoTextView.text = "Fat: ${mealInfo.fat}g"


        gridView.adapter = RecipeAdapter(this, mealInfo.recipes, pixels_item)
        setGridViewHeightBasedOnChildren(gridView, 2)

        Glide.with(this)
            .load(mealInfo.mealimgurl)
            .into(transparentImage)

        findViewById<ImageButton>(R.id.backButton).apply {
            setOnClickListener {
                finish()
            }
        }

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = scrollView.scrollY
            if (scrollY > 50f) {
                toolbar.setBackgroundResource(R.drawable.green_gradient_background)
            } else {
                toolbar.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        gridView.setOnItemClickListener { _, _, position, _ ->
            val intent = Intent(this@SecondActivity, ThirdActivity::class.java)

            val recipe = gridView.adapter.getItem(position) as Recipe
            intent.putExtra("recipe", recipe)

            startActivity(intent)
        }
    }
    fun setGridViewHeightBasedOnChildren(gridView: GridView, numberOfColumns: Int) {
        val adapter = gridView.adapter ?: return

        val scale: Float = resources.displayMetrics.density
        val pixels: Int = (210 * scale + 0.5f).toInt()

        var totalHeight = pixels
        var items = adapter.count
        var rows = 0

        val listItem = adapter.getView(0, null, gridView)
        listItem.measure(0, 0)

        val x: Float
        if (items > numberOfColumns) {
            rows = ceil(items.toFloat() / numberOfColumns.toFloat()).toInt()
            totalHeight *= rows
        }

        val params = gridView.layoutParams
        params.height = totalHeight
        gridView.layoutParams = params
    }
}