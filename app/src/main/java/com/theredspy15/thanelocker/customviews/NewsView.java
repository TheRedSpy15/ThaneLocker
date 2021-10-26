package com.theredspy15.thanelocker.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.longboardlife.R;

public class NewsView extends LinearLayout {

    private View mValue;

    public NewsView (Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.news_view, this);

        ImageView imageView = findViewById(R.id.image);
        TextView textView = findViewById(R.id.caption);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.NewsView,0,0);
        imageView.setImageDrawable(attributes.getDrawable(R.styleable.NewsView_image));
        textView.setText(attributes.getString(R.styleable.NewsView_text));

        attributes.recycle();
    }

    public NewsView(Context context) {
        this(context, null);
    }

    public void setBitmap(Bitmap bitmap) {
        ImageView imageView = findViewById(R.id.image);
        imageView.setImageBitmap(bitmap);
    }

    public void setDrawable(Drawable drawable) {
        ImageView imageView = findViewById(R.id.image);
        imageView.setImageDrawable(drawable);
    }

    public void setText(String text) {
        TextView textView = findViewById(R.id.caption);
        textView.setText(text);
    }
}