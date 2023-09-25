package com.example.pomodoro2

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.activity.ComponentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.pomodoro2.models.MealInfo
import com.example.pomodoro2.models.Recipe

class ThirdActivity : ComponentActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.third_activity)

        val recipe = intent.getSerializableExtra("recipe") as Recipe
        val linearLayout: LinearLayout = findViewById(R.id.info)

        Glide.with(this)
            .asBitmap()
            .load(recipe.image_url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val bitmapDrawable = BitmapDrawable(resources, resource)
                    linearLayout.background = bitmapDrawable
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })

        val mealNameTextView: TextView = findViewById(R.id.mealName)
        val durationTextView: TextView = findViewById(R.id.duration)
        val kcalTextView: TextView = findViewById(R.id.kcal)
        val carbsTextView: TextView = findViewById(R.id.carbs)
        val proteinTextView: TextView = findViewById(R.id.protein)
        val fatTextView: TextView = findViewById(R.id.fat)

        val durationStr: String = if ( recipe.duration > 60) {
            "${(recipe.duration / 60).toString()}h ${(recipe.duration % 60).toString()}min"
        } else {
            "${recipe.duration.toString()}min"
        }

        mealNameTextView.text = recipe.name
        durationTextView.text = durationStr
        kcalTextView.text = recipe.calories.toString()
        carbsTextView.text = recipe.carbs.toString()
        proteinTextView.text = recipe.protein.toString()
        fatTextView.text = recipe.fat.toString()

        val ingredientsLayout: LinearLayout = findViewById(R.id.ingredients)
        val adapter = IngredientAdapter(this, recipe.ingredients)

        for (i in 0 until adapter.count) {
            val item = adapter.getItem(i)
            val view = adapter.getView(i, null, ingredientsLayout)
            ingredientsLayout.addView(view)
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val scrollView: ScrollView = findViewById(R.id.scrollView)

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = scrollView.scrollY
            if (scrollY > 50f) {
                toolbar.setBackgroundResource(R.drawable.green_gradient_background)
            } else {
                toolbar.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        findViewById<Button>(R.id.instructions).apply {
            setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(recipe.url)
                startActivity(intent)
            }
        }

        findViewById<ImageButton>(R.id.backButton).apply {
            setOnClickListener {
                finish()
            }
        }
    }
}