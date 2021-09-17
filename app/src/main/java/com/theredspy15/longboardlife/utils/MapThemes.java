package com.theredspy15.longboardlife.utils;

import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;

public class MapThemes {

    public static ColorMatrixColorFilter darkFilter() {
        //taking cue from https://medium.com/square-corner-blog/welcome-to-the-color-matrix-64d112e3f43d
        ColorMatrix inverseMatrix = new ColorMatrix(new float[] {
                -1.0f, 0.0f, 0.0f, 0.0f, 255f,
                0.0f, -1.0f, 0.0f, 0.0f, 255f,
                0.0f, 0.0f, -1.0f, 0.0f, 255f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        });

        int destinationColor = Color.parseColor("#FF2A2A2A");
        float lr = (255.0f - Color.red(destinationColor))/255.0f;
        float lg = (255.0f - Color.green(destinationColor))/255.0f;
        float lb = (255.0f - Color.blue(destinationColor))/255.0f;
        ColorMatrix grayscaleMatrix = new ColorMatrix(new float[] {
                lr, lg, lb, 0, 0, //
                lr, lg, lb, 0, 0, //
                lr, lg, lb, 0, 0, //
                0, 0, 0, 0, 255, //
        });
        grayscaleMatrix.preConcat(inverseMatrix);
        int dr = Color.red(destinationColor);
        int dg = Color.green(destinationColor);
        int db = Color.blue(destinationColor);
        float drf = dr / 255f;
        float dgf = dg / 255f;
        float dbf = db / 255f;
        ColorMatrix tintMatrix = new ColorMatrix(new float[] {
                drf, 0, 0, 0, 0, //
                0, dgf, 0, 0, 0, //
                0, 0, dbf, 0, 0, //
                0, 0, 0, 1, 0, //
        });
        tintMatrix.preConcat(grayscaleMatrix);
        float lDestination = drf * lr + dgf * lg + dbf * lb;
        float scale = 1f - lDestination;
        float translate = 1 - scale * 0.5f;
        ColorMatrix scaleMatrix = new ColorMatrix(new float[] {
                scale, 0, 0, 0, dr * translate, //
                0, scale, 0, 0, dg * translate, //
                0, 0, scale, 0, db * translate, //
                0, 0, 0, 1, 0, //
        });
        scaleMatrix.preConcat(tintMatrix);

        return new ColorMatrixColorFilter(scaleMatrix);
    }
}
