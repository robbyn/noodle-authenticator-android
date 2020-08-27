package org.tastefuljava.noodleauth;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class ClockView extends View {
    private final Paint fill = initialFill();
    private final Paint stroke = initialStroke();
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
        canvas.drawCircle(x, y, radius, stroke);
        canvas.drawArc(x-radius, y-radius, x+radius, y+radius,
                270, angle-360, true, fill);
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
        invalidate();
    }

    public int getColor() {
        return fill.getColor();
    }

    public void setColor(int color) {
        fill.setColor(color);
        stroke.setColor(color);
        invalidate();
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
        invalidate();
    }

    private static Paint initialFill() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        return paint;
    }
    private static Paint initialStroke() {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(1);
        return paint;
    }
}
