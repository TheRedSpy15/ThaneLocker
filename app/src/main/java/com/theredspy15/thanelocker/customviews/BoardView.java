package com.theredspy15.thanelocker.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.longboardlife.R;

public class BoardView extends LinearLayout {

    public BoardView (Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.board_view, this);

        ImageView imageView = findViewById(R.id.imageViewBoard);
        TextView textViewName = findViewById(R.id.textViewName);
        TextView textViewSpeed = findViewById(R.id.textViewSpeed);
        TextView textViewDistance = findViewById(R.id.textViewDistance);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.BoardView,0,0);
        imageView.setImageDrawable(attributes.getDrawable(R.styleable.BoardView_imageboard));
        textViewName.setText(attributes.getString(R.styleable.BoardView_textname));
        textViewSpeed.setText(attributes.getString(R.styleable.BoardView_textspeed));
        textViewDistance.setText(attributes.getString(R.styleable.BoardView_textdistance));

        attributes.recycle();
    }

    public BoardView(Context context) {
        this(context, null);
    }

    public void setBitmap(Bitmap bitmap) {
        ImageView imageView = findViewById(R.id.imageViewBoard);
        imageView.setImageBitmap(bitmap);
    }

    public void setDrawable(Drawable drawable) {
        ImageView imageView = findViewById(R.id.imageViewBoard);
        imageView.setImageDrawable(drawable);
    }

    public ImageView getImageView() {
        return findViewById(R.id.imageViewBoard);
    }

    public void setTextName(String text) {
        TextView textView = findViewById(R.id.textViewName);
        textView.setText(text);
    }

    public void setTextSpeed(String text) {
        TextView textView = findViewById(R.id.textViewSpeed);
        textView.setText(text);
    }

    public void setTextDistance(String text) {
        TextView textView = findViewById(R.id.textViewDistance);
        textView.setText(text);
    }
}