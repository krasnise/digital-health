package com.example.pomodoro2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.pomodoro2.models.Recipe

class RecipeAdapter(private val context: Context, private val dataSource: List<Recipe>, private val pixels: Int) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int = dataSource.size

    override fun getItem(position: Int): Any = dataSource[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: inflater.inflate(R.layout.recipe_item, parent, false)

        val recipeImageView = view.findViewById<ImageView>(R.id.recipeImage)
        val recipeNameTextView = view.findViewById<TextView>(R.id.recipeName)

        val recipe = getItem(position) as Recipe


        Glide.with(context)
            .load(recipe.image_url)
            .transform(CenterCrop(), RoundedCorners(pixels))
            .into(recipeImageView)

        recipeNameTextView.text = recipe.name

        return view
    }
}