package com.example.mlworld.helper;

import android.graphics.Rect;
import android.graphics.RectF;

public class BoxWithTitle {
    public String title;
    public Rect rect;

    public BoxWithTitle(String title, Rect rect) {
        this.title = title;
        this.rect = rect;
    }

    public BoxWithTitle(String displayName, RectF boundingBox) {
        this.title = displayName;
        this.rect = new Rect();
        boundingBox.round(rect);
    }
}