package com.dudev.android.hackernews.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.TextView;

public class NoteTextView extends TextView {

    public NoteTextView(Context context) {
        super(context);
       setPadding(20, 20, 10, 10);
    }
    private Paint paint = new Paint();
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.parseColor("#F00000FF"));
        paint.setStrokeWidth(10f);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawLine(0, 0, 0, getHeight(), paint);
    }
}