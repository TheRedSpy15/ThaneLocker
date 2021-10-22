package com.theredspy15.thanelocker.customviews

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.longboardlife.R

class NewsView(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    init {
        inflate(context, R.layout.news_view, this)

        val imageView: ImageView = findViewById(R.id.image)
        val textView: TextView = findViewById(R.id.caption)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.NewsView)
        imageView.setImageDrawable(attributes.getDrawable(R.styleable.NewsView_image))
        textView.text = attributes.getString(R.styleable.NewsView_text)
        attributes.recycle()

    }
}