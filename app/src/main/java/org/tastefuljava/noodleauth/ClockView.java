package org.tastefuljava.noodleauth;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ClockView extends View {
    private Paint paint;
    private int margin;
    private int angle;

    public ClockView(Context context) {
        super(context);
        init(null, 0);
    }

    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ClockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ClockView, defStyle, 0);

        angle = a.getInt(R.styleable.ClockView_angle, angle);
        margin = a.getInt(R.styleable.ClockView_margin, margin);
        setColor(a.getColor(R.styleable.ClockView_color, 0));
        getBackground().setAlpha(0);
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = this.getWidth();
        int height = this.getHeight();
        int radius = Math.min(width, height)/2-margin;
        int x = width/2;
        int y = height/2;
        canvas.drawArc(x-radius, y-radius, x+radius, y+radius,
                270, angle, true, paint);
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
        invalidate();
    }

    public int getColor() {
        return paint.getColor();
    }

    public void setColor(int color) {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setColor(color);
        invalidate();
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
        invalidate();
    }
}
